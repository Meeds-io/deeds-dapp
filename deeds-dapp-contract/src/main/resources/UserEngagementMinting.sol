// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.20;

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
     * @dev An event that will be triggered when a Hub is connected to the WoM
     */
    event ReportSent(
        address indexed hub,
        uint256 indexed reportId
    );

    event ReportFraud(
        uint256 indexed reportId
    );

    /**
     * @dev An event that will be triggered when a Hub is connected to the WoM
     */
    event Claimed(
        address indexed recipient, // Rewards Recipient, could be a Deed Tenant or Owner
        address indexed receiver, // Amount Receiver
        uint256 indexed amount // Claimed amount
    );

    /**
     * @dev An event that will be triggered when a Hub is connected to the WoM
     */
    event RewardAmountChanged(
        uint256 indexed amount
    );

    // Since, the minting privilege is exclusively hold
    // by the current contract and it's not transferable,
    // this will be the absolute Maximum Supply of all MEED Token.
    uint256 public constant REWARD_PERIOD_IN_SECONDS = 7 days;

    IERC20 public meed;

    WoM public wom;

    uint256 public startRewardsTime = 0;

    uint256 public periodicRewardAmount = 0;

    uint256 public lastReportId = 0;

    // Associated Hub report Ids by period Id: Hub Addr => Period Id => Report Id
    // (will ensure unicity of reports for a given hub by period)
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

    // Associated Hub report Ids by period Id: Hub Addr => Period Id => Report Id
    // (will ensure unicity of reports for a given hub by period)
    // reportsByPeriodByHub[rewardPeriodId][hubAddress] = reportId
    mapping(uint256 => mapping(address => uint256)) public reportsByPeriodByHub;

    // Associated Deed report Ids by period Id: Deed Id => Period Id => Report Id
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
     * @dev Used when a Hub sends its internal Users Rewarding Report to the WoM.
     *      Can be called by the Hub only.
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
     * @dev Used when a Hub sends its internal Users Rewarding Report to the WoM.
     *      Can be called by the Hub only.
     */
    function addReport(HubReport memory _report, address _hubAddress, uint256 _deedId)
        external
        virtual
    {
        require(_hubAddress == _msgSender(), "uem.onlyHubCanSendUEMReport");

        // Check Hub is connected
        require(_deedId > 0, "wom.deedIdIsMandatory");
        require(wom.getConnectedDeed(_hubAddress) == _deedId, "wom.hubIsNotConnectedToWoMUsingDeed");

        // Check Hub Report is eligible
        require(_report.usersCount > 0, "wom.hubUsersIsMandatory");
        require(_report.recipientsCount > 0, "wom.hubRecipientsCountIsMandatory");
        require(_report.participantsCount > 0, "wom.hubParticipantsCountIsMandatory");
        require(_report.achievementsCount > 0, "wom.hubAchievementsCountIsMandatory");
        require(_report.amount > 0, "wom.hubUsedRewardAmountAmountIsMandatory");

        // In deterministic way, compute Reward Identifier
        uint256 _rewardPeriodId = block.timestamp.sub(startRewardsTime).div(REWARD_PERIOD_IN_SECONDS);

        // Check Hub hasn't already sent a report for the current Reward Period
        require(reportsByPeriodByHub[_rewardPeriodId][_hubAddress] == 0, "wom.hubAlreadySentReportInCurrentPeriod");
        // Check Deed hasn't have an associated report for the current Reward Period
        require(reportsByPeriodByDeed[_rewardPeriodId][_deedId] == 0, "wom.deedAlreadySentReportInCurrentPeriod");

        // Increment report Id before assignig it to current report
        lastReportId = lastReportId.add(1);

        {// Compute Hub Report Reward Structure
            hubReports[lastReportId] = HubReport(
                _hubAddress,
                _report.usersCount,
                _report.recipientsCount,
                _report.participantsCount,
                _report.achievementsCount,
                _report.amount,
                _report.tokenAddress,
                _report.tockenChainId
            );
        }

        Deed memory deed = wom.getDeed(_deedId);
        {// Embed Deed Characteristics
            hubDeeds[lastReportId] = HubReportDeed(
                _deedId,
                deed.city,
                deed.cardType,
                deed.mintingPower,
                deed.maxUsers
            );
        }

        {// Compute Hub Report Fixed Reward indices
          HubReportReward storage reportReward = hubRewards[lastReportId];
          reportReward.rewardPeriodId = _rewardPeriodId;
          reportReward.sentDate = block.timestamp;
          reportReward.owner = deed.owner;
          reportReward.tenant = deed.tenant;

          reportReward.fixedRewardIndex = _getReportFixedIndice(lastReportId);
          reportReward.ownerFixedIndex = reportReward.fixedRewardIndex.mul(deed.ownerPercentage);

          if (reportReward.ownerFixedIndex > 0) {
            recipients[reportReward.owner].reportIds.push(lastReportId);
          }
          reportReward.tenantFixedIndex = reportReward.fixedRewardIndex.mul(deed.tenantPercentage);
          if (reportReward.tenantFixedIndex > 0) {
            recipients[reportReward.tenant].reportIds.push(lastReportId);
          }
        }

        {
          Reward storage reward = rewards[_rewardPeriodId];
          if (reward.fromReport == 0) { // First time the reward of current period is initialized
              reward.fromReport = lastReportId;
              // Set to a fixed Reward amount for the current period
              // once the first report is received on UEM contract
              reward.amount = periodicRewardAmount;
              // Start date (informatif)
              reward.fromDate = _rewardPeriodId.mul(REWARD_PERIOD_IN_SECONDS).add(startRewardsTime);
              // End date (informatif) = Start date + 7 days
              reward.toDate = reward.fromDate.add(REWARD_PERIOD_IN_SECONDS);
          }
          reward.toReport = lastReportId;
          reward.reportsCount = reward.reportsCount.add(1);
          reward.sumEd = reward.sumEd.add(hubReports[lastReportId].achievementsCount.div(hubReports[lastReportId].participantsCount));
        }

        hubReportIds[_hubAddress].push(lastReportId);
        reportsByPeriodByHub[_rewardPeriodId][_hubAddress] = lastReportId;
        reportsByPeriodByDeed[_rewardPeriodId][_deedId] = lastReportId;

        emit ReportSent(_hubAddress, lastReportId);
    }

    /**
     * @dev Used claim rewards by a recipient as Deed Owner or Deed Tenant
     *      having previously sent reward reports in a previous UEM Reward period.
     */
    function claim(address _receiver, uint256 _amount)
        external
        virtual
    {
        Recipient storage recipient = recipients[_msgSender()];
        require(recipient.reportIds.length > 0, "uem.noUemReports");
        for (uint i = recipient.index; i < recipient.reportIds.length; i++) {
          HubReportReward memory reportReward = hubRewards[recipient.reportIds[i]];
          if (!reportReward.fraud) {
            Reward memory reward = rewards[reportReward.rewardPeriodId];
            if (block.timestamp > reward.toDate && reward.sumEd > 0) { // Only past rewards
              if (reportReward.owner == msg.sender) {
                recipient.accRewards += reportReward.ownerFixedIndex.mul(reward.amount).div(reward.sumEd);
              } else if (reportReward.tenant == msg.sender) {
                recipient.accRewards += reportReward.tenantFixedIndex.mul(reward.amount).div(reward.sumEd);
              } else {
                revert("uem.notDeedManager"); // Should never happen
              }
            }
          }
        }
        recipient.index = recipient.reportIds.length;
        if (_amount == 0) { // Claim all
            _amount = recipient.accRewards.sub(recipient.claimedRewards);
        }
        recipient.claimedRewards = recipient.claimedRewards.add(_amount);
        if (recipient.claimedRewards > recipient.accRewards) {
          revert("uem.exceedsRewardsAmount");
        }
        if (_receiver == address(0)) {
            _receiver = _msgSender();
        }
        meed.transfer(_receiver, _amount);

        emit Claimed(_msgSender(), _receiver, _amount);
    }

    /**
     * @dev Used to estimate claimable rewards for a given address.
     */
    function pendingRewardBalanceOf(address _address)
        external
        view
        returns (uint256)
    {
        Recipient storage recipient = recipients[_address];
        uint256 accRewards = 0;
        for (uint i = recipient.index; i < recipient.reportIds.length; i++) {
          HubReportReward memory reportReward = hubRewards[recipient.reportIds[i]];
          if (!reportReward.fraud) {
            Reward memory reward = rewards[reportReward.rewardPeriodId];
            if (block.timestamp > reward.toDate && reward.sumEd > 0) { // Only past rewards
              if (reportReward.owner == msg.sender) {
                accRewards = recipient.accRewards + reportReward.ownerFixedIndex.mul(reward.amount).div(reward.sumEd);
              } else if (reportReward.tenant == msg.sender) {
                accRewards = recipient.accRewards + reportReward.tenantFixedIndex.mul(reward.amount).div(reward.sumEd);
              }
            }
          }
        }
        return recipient.accRewards.sub(recipient.claimedRewards);
    }

    function _getReportFixedIndice(uint256 _reportId)
      internal
      view
      returns (uint256)
    {
      uint256 lastRewardedAmount = _getLastRewardedAmount(_reportId);
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
      }
      {
        HubReport memory hubReport = hubReports[_reportId];
        usersCount = hubReport.usersCount;
        participantsCount = hubReport.participantsCount;
        recipientsCount = hubReport.recipientsCount;
        achievementsCount = hubReport.achievementsCount;
        recipientsCount = maxUsers > 0 && hubReport.recipientsCount > maxUsers
          ? maxUsers
          : hubReport.recipientsCount;
        if (lastRewardedAmount == 0) {
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
      return achievementsCount
        .mul(amount)
        .mul(recipientsCount)
        .mul(mintingPower) // Percentage ..
        .div(100) // .. to divide by 100
        .div(participantsCount)
        .div(lastRewardedAmount)
        .div(usersCount);
    }

    function _getLastRewardedAmount(uint256 _reportId)
      internal
      view
      returns (uint256)
    {
      uint256[] memory ids = hubReportIds[hubReports[_reportId].hub];
      if (ids.length > 0) {
        uint256 lastRewardedReportId = ids[ids.length - 1];
        Reward memory lastReward = rewards[hubRewards[lastRewardedReportId].rewardPeriodId];
        if (lastReward.sumEd > 0) {
          // reportReward.fraud will not be considered here
          // even if fraud = true, the previous report
          // is considered as it was rewarded (kind of penality)
          return hubRewards[lastRewardedReportId].fixedRewardIndex.mul(lastReward.amount).div(lastReward.sumEd);
        }
      }
      return 0;
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


