// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

import "./abstract/Initializable.sol";
import "./abstract/SafeMath.sol";
import "./abstract/IERC20.sol";
import "./abstract/ManagerRole.sol";
import "./abstract/UUPSUpgradeable.sol";
import "./struct/Deed.sol";
import "./struct/Recipient.sol";
import "./struct/Reward.sol";
import "./struct/HubReport.sol";
import "./struct/HubReportReward.sol";
import "./struct/HubReportDeed.sol";
import "./WoM.sol";

/**
 * @title User Engagement Minting Contract to rewards Meeds DAO Deed Hub communities switch their engagement score
 */
contract UserEngagementMinting is UUPSUpgradeable, Initializable, ManagerRole {

    using SafeMath for uint256;

    using Address for address;

    /**
     * @dev An event that will be triggered when a Report is sent to the UEM
     */
    event ReportSent(
        address indexed hub,
        uint256 indexed reportId
    );

    /**
     * @dev An event that will be triggered when a Report is considered as fraud
     */
    event ReportFraud(
        uint256 indexed reportId
    );

    /**
     * @dev An event that will be triggered when recipient claimed rewards
     */
    event Claimed(
        address indexed recipient, // Rewards Recipient, could be a Deed Tenant or Owner
        address indexed receiver, // Amount Receiver
        uint256 indexed amount // Claimed amount
    );

    /**
     * @dev An event that will be triggered when the UEM periodic rewards amount changes
     */
    event RewardAmountChanged(
        uint256 indexed amount
    );

    // Fixed Index computing multiplier,
    // knowning that no floating numbers are supported
    uint256 public constant MULTIPLIER = 1000000000000000000;

    // The UEM period duration
    uint256 public constant REWARD_PERIOD_IN_SECONDS = 7 days;

    // Meeds.io Token
    IERC20 public meed;

    // WoM Contract
    WoM public wom;

    // The UEM start date
    uint256 public startRewardsTime = 0;

    // The weekly UEM reward amount
    uint256 public periodicRewardAmount = 0;

    // Last received report identifier
    uint256 public lastReportId = 0;

    // Associated reports Ids to a Hub Addr. This will be used to compute
    // the previous report received rewards
    mapping(address => uint256[]) public hubReportIds;

    // Report Id (Auto Incremented) => Hub Report
    mapping(uint256 => HubReport) public hubReports;

    // Report Id (Auto Incremented) => Hub Report Deed
    mapping(uint256 => HubReportDeed) public hubDeeds;

    // Report Id (Auto Incremented) => Hub Report Reward
    mapping(uint256 => HubReportReward) public hubRewards;

    // Reward Id (deterministic algorithm = Period index) => Period Reward
    mapping(uint256 => Reward) public rewards;

    // Deed Owner/Tenant address => Recipient structure holding the rewards information data
    mapping(address => Recipient) public recipients;

    // Associated Hub report Ids by period Id: Period Id => Hub Addr => Report Id
    // (will ensure unicity of reports for a given hub by period)
    // reportsByPeriodByHub[rewardPeriodId][hubAddress] = reportId
    mapping(uint256 => mapping(address => uint256)) public reportsByPeriodByHub;

    // Associated Deed report Ids by period Id: Period Id => Deed Id => Report Id
    // (will ensure unicity of reports for a given Deed by period)
    // reportsByPeriodByDeed[rewardPeriodId][deedId] = reportId
    mapping(uint256 => mapping(uint256 => uint256)) public reportsByPeriodByDeed;

    /**
     * This method replaces the constructor since this is about an Upgradable Contract
     */
    function initialize(
        IERC20 _meed,
        WoM _wom,
        uint256 _startRewardsTime
    )
        public
        initializer {
        _transferOwnership(_msgSender());
        _addManager(_msgSender());
        meed = _meed;
        wom = _wom;
        startRewardsTime = _startRewardsTime;
    }

    /**
     * @dev Used to change the UEM weekly rewards amount
     */
    function setPeriodicRewardAmount(uint256 _periodicRewardAmount)
        external
        virtual
        onlyManager
    {
        periodicRewardAmount = _periodicRewardAmount;
        emit RewardAmountChanged(_periodicRewardAmount);
    }

    /**
     * @dev Marks a report as fraud. Can be called by a manager only.
     *      This change will be called by a contract that will have its own business logic
     *      to gather other Hubs votes about a given report that is fraudulent.
     */
    function markAsFraud(uint256 _reportId)
        external
        virtual
        onlyManager
    {
        hubRewards[_reportId].fraud = true;
        emit ReportFraud(_reportId);
    }

    /**
     * @dev Used after a Hub sends its internal Users Rewarding Report.
     *      This will add the Hub Rewarding Report in this contract
     *      to be elligible to weekly rewards.
     *      Can be called by the Hub itself only.
     */
    function addReport(HubReport memory _report)
        external
        virtual
        returns (uint256)
    {
        address hubAddress = _report.hub;
        require(hubAddress == _msgSender(), "uem.onlyHubCanSendUEMReport");

        // Check Hub is connected
        uint256 deedId = wom.getConnectedDeed(hubAddress);
        require(deedId > 0, "wom.hubIsNotConnectedToWoMUsingDeed");

        // Avoid very old reports comparing to the whole first connection date to the WoM (even after disconnection in the meanwhile)
        require(_report.toDate.add(REWARD_PERIOD_IN_SECONDS) >= wom.getHubJoinDate(hubAddress), "wom.hubReportHasNotEligibleToDate");

        uint256 rewardPeriodId = _computeCurrentRewardId();

        // Check Hub hasn't already sent a report for the current Reward Period
        require(reportsByPeriodByHub[rewardPeriodId][hubAddress] == 0, "wom.hubAlreadySentReportInCurrentPeriod");
        // Check Deed hasn't have an associated report for the current Reward Period
        require(reportsByPeriodByDeed[rewardPeriodId][deedId] == 0, "wom.deedAlreadySentReportInCurrentPeriod");

        // Increment report Id before assignig it to current report
        lastReportId = lastReportId.add(1);

        {// Compute Hub Report Reward Structure
            hubReports[lastReportId] = HubReport(
                _report.hub,
                _report.usersCount,
                _report.recipientsCount,
                _report.participantsCount,
                _report.achievementsCount,
                _report.amount,
                _report.tokenAddress,
                _report.tokenChainId,
                _report.fromDate,
                _report.toDate
            );
        }

        Deed memory deed = wom.getDeed(deedId);
        {// Embed Deed Characteristics
            hubDeeds[lastReportId] = HubReportDeed(
                deedId,
                deed.city,
                deed.cardType,
                deed.mintingPower,
                deed.maxUsers
            );
        }

        {// Compute Hub Report Fixed Reward indices
          HubReportReward storage reportReward = hubRewards[lastReportId];
          reportReward.rewardPeriodId = rewardPeriodId;
          reportReward.sentDate = _blocktimeInSeconds();
          reportReward.owner = deed.owner;
          reportReward.tenant = deed.tenant;

          reportReward.fixedRewardIndex = _computeReportFixedIndice(lastReportId);
          reportReward.ownerFixedIndex = reportReward.fixedRewardIndex.mul(deed.ownerPercentage).div(100);
          if (reportReward.ownerFixedIndex > 0) {
            recipients[reportReward.owner].reportIds.push(lastReportId);
          }
          reportReward.tenantFixedIndex = reportReward.fixedRewardIndex.mul(deed.tenantPercentage).div(100);
          if (reportReward.tenantFixedIndex > 0) {
            recipients[reportReward.tenant].reportIds.push(lastReportId);
          }
        }

        {// Compute Period Reward Fixed Reward indices
          Reward storage reward = rewards[rewardPeriodId];
          if (reward.fromReport == 0) { // First time the reward of current period is initialized
              reward.fromReport = lastReportId;
              // Set to a fixed Reward amount for the current period
              // once the first report is received on UEM contract
              reward.amount = periodicRewardAmount;
              // Start date (informatif)
              reward.fromDate = rewardPeriodId.mul(REWARD_PERIOD_IN_SECONDS).add(startRewardsTime);
              // End date (informatif) = Start date + 7 days
              reward.toDate = reward.fromDate.add(REWARD_PERIOD_IN_SECONDS);
          }
          reward.toReport = lastReportId;
          reward.reportsCount = reward.reportsCount.add(1);
          reward.fixedGlobalIndex = reward.fixedGlobalIndex.add(hubRewards[lastReportId].fixedRewardIndex);
        }

        hubReportIds[hubAddress].push(lastReportId);
        reportsByPeriodByHub[rewardPeriodId][hubAddress] = lastReportId;
        reportsByPeriodByDeed[rewardPeriodId][deedId] = lastReportId;

        emit ReportSent(hubAddress, lastReportId);
        return lastReportId;
    }

    /**
     * @dev Used claim rewards by a recipient as Deed Owner or Deed Tenant
     *      having previously managed a Hub which have sent a Reward Report.
     */
    function claim(address _receiver, uint256 _amount)
        external
        virtual
    {
        uint256 pendingBalance = pendingRewardBalanceOf(_msgSender());

        Recipient storage recipient = recipients[_msgSender()];
        recipient.accRewards = pendingBalance.add(recipient.claimedRewards);
        for (uint i = recipient.index; i < recipient.reportIds.length; i++) {
          if (_blocktimeInSeconds() > rewards[hubRewards[recipient.reportIds[i]].rewardPeriodId].toDate) { // Only past rewards
            recipient.index = (i + 1);
          }
        }

        uint256 amount = _amount;
        if (amount == 0) { // Claim all remaining
            amount = pendingBalance;
        }
        address receiver = _receiver;
        if (_receiver == address(0)) { // Claim to current address
            receiver = _msgSender();
        }

        recipient.claimedRewards = recipient.claimedRewards.add(amount);
        require(recipient.claimedRewards <= recipient.accRewards, "uem.claimedAmountExceedAllowed");
        require(meed.transfer(receiver, amount), "uem.claimMeedsTransferFailed");
        emit Claimed(_msgSender(), receiver, amount);
    }

    /**
     * @dev Used to estimate claimable UEM rewards for a given address.
     */
    function pendingRewardBalanceOf(address _address)
        public
        view
        virtual
        returns (uint256)
    {
        Recipient memory recipient = recipients[_address];
        uint256 accRewards = recipient.accRewards;
        for (uint i = recipient.index; i < recipient.reportIds.length; i++) {
          HubReportReward memory reportReward = hubRewards[recipient.reportIds[i]];
          if (!reportReward.fraud) {
            Reward memory reward = rewards[reportReward.rewardPeriodId];
            if (_blocktimeInSeconds() > reward.toDate && reward.fixedGlobalIndex > 0) { // Only past rewards
              if (reportReward.owner == _address) {
                accRewards = accRewards.add(reward.amount.mul(reportReward.ownerFixedIndex).div(reward.fixedGlobalIndex));
              }
              if (reportReward.tenant == _address) {
                accRewards = accRewards.add(reward.amount.mul(reportReward.tenantFixedIndex).div(reward.fixedGlobalIndex));
              }
            }
          }
        }
        return accRewards.sub(recipient.claimedRewards);
    }

    /**
     * @dev Returns the list of Report Ids where the recipient
     *      is either Deed Owner or Deed Tenant
     */
    function reportsByRecipient(address _address)
        external
        view
        virtual
        returns (uint256[] memory)
    {
        return recipients[_address].reportIds;
    }

    /**
     * @dev Returns the fixed indice of a report computed switch the described formula
     *      in the whitepaper. This will return only this fixed index:
     *      ( 𝐸𝑑 ∗ 𝐷𝑟 ∗ 𝐷𝑠 ∗ 𝑀) : without 𝐸𝑤 = Fixed Indice
     *      In additon, we will use a MULTIPLIER (10^18) knowing
     *      that EVM doesn't manage floats
     */
    function _computeReportFixedIndice(uint256 _reportId)
      internal
      virtual
      returns (uint256)
    {
      uint256 lastRewardedAmount = _computeLastRewardedAmount(_reportId);
      uint256 maxUsers;
      uint256 mintingPower;
      uint256 usersCount;
      uint256 participantsCount;
      uint256 recipientsCount;
      uint256 achievementsCount;
      uint256 amount;
      {
        HubReportDeed memory hubReportDeed = hubDeeds[_reportId];
        maxUsers = hubReportDeed.maxUsers;
        mintingPower = hubReportDeed.mintingPower;
        require(mintingPower > 0, "wom.wrongDeedMintingPower");
      }
      {
        HubReport memory hubReport = hubReports[_reportId];

        usersCount = hubReport.usersCount;
        require(usersCount > 0, "wom.hubUsersIsMandatory");

        participantsCount = hubReport.participantsCount;
        require(participantsCount > 0, "wom.hubParticipantsCountIsMandatory");

        achievementsCount = hubReport.achievementsCount;
        require(achievementsCount > 0, "wom.hubAchievementsCountIsMandatory");

        recipientsCount = maxUsers > 0 && hubReport.recipientsCount > maxUsers
          ? maxUsers
          : hubReport.recipientsCount;
        require(recipientsCount > 0, "wom.hubRecipientsCountIsMandatory");

        require(hubReport.amount > 0, "wom.hubUsedRewardAmountIsMandatory");
        if (lastRewardedAmount == 0) {
          // (amount / lastRewardedAmount) = 1
          //  when the whole first time a report is sent by the Hub
          amount = 1;
          lastRewardedAmount = 1;  
        } else {
          amount = hubReport.amount;
        }
      }

      // ( 𝐸𝑑 ∗ 𝐷𝑟 ∗ 𝐷𝑠 ∗ 𝑀) : without 𝐸𝑤 = Fixed Indice
      // uint256 ed = report.achievementsCount / report.participantsCount
      // uint256 dr = report.amount / lastRewardedAmount
      // uint256 ds = report.recipientsCount / report.usersCount
      // uint256 m =  report.mintingPower / 100 (No fractions)
      uint256 result;
      {
        result = achievementsCount
          .mul(amount)
          .mul(MULTIPLIER); // No floats, thus use multiplier
      }
      {
        result = result
          .mul(recipientsCount)
          .mul(mintingPower)
          .div(100); //  100 for Minting power percentage
      }
      {
        result = result
          .div(participantsCount)
          .div(lastRewardedAmount)
          .div(usersCount);
      }
      return result;
    }

    /**
     * @dev Returns the previous report rewards which will be used in UEM formula
     *      to compute Hub Report Rewards.
     *      This will at the same time persist the computed information for tracability.
     */
    function _computeLastRewardedAmount(uint256 _reportId)
      internal
      virtual
      returns (uint256)
    {
      uint256[] memory ids = hubReportIds[hubReports[_reportId].hub];
      if (ids.length > 0) {
        uint256 lastRewardedReportId = ids[ids.length - 1];

        // Make a late requirement instead at first addReport method call for gas optimization
        require(hubReports[_reportId].fromDate > hubReports[lastRewardedReportId].fromDate, "uem.lastReportFromDateMustBeLessThanCurrentReportFromDate");

        Reward memory lastReward = rewards[hubRewards[lastRewardedReportId].rewardPeriodId];
        if (lastReward.fixedGlobalIndex > 0) {
          // reportReward.fraud will not be considered here
          // even if fraud = true, the previous report
          // is considered as it was rewarded (kind of penalty)
          hubRewards[lastReportId].lastRewardedAmount = hubRewards[lastRewardedReportId].fixedRewardIndex.mul(lastReward.amount).div(lastReward.fixedGlobalIndex);
          return hubRewards[lastReportId].lastRewardedAmount;
        }
      }
      return 0;
    }

    /**
     * @dev In deterministic way, compute Reward Identifier for current Week
     *      which have to be a constant and auto increment on each week
     */
    function _computeCurrentRewardId()
        internal
        view
        virtual
        returns(uint256) {
        return _blocktimeInSeconds().sub(startRewardsTime).div(REWARD_PERIOD_IN_SECONDS);
    }

    function _blocktimeInSeconds()
        internal
        view
        virtual
        returns(uint256) {
        return block.timestamp;
    }

    function _authorizeUpgrade(
        address newImplementation
    )
        internal
        view
        virtual
        override
        onlyOwner {}

}