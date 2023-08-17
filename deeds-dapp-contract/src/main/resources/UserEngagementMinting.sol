// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

import "./abstract/Initializable.sol";
import "./abstract/SafeMath.sol";
import "./abstract/MeedsToken.sol";
import "./abstract/ManagerRole.sol";
import "./abstract/UUPSUpgradeable.sol";

/**
 * @title User Engagement Minting Contract to rewards Meeds DAO Deed Hub communities switch their engagement score
 */
contract UserEngagementMinting is UUPSUpgradeable, Initializable, ManagerRole {

    using SafeMath for uint256;

    using Address for address;

    /**
     * @dev An event that will be triggered when a Deed Tenant/Owner claims their Hub rewards
     */
    event RewardClaimed(uint256 indexed nftId, address indexed earner, address to, uint256 value);

    /**
     * @dev An event that will be triggered once a partial reward report is sent.
     */
    event UEMPartialReward(uint256 indexed _fromDate, uint256 _toDate, bytes32 indexed _blockHash);

    /**
     * @dev An event that will be triggered once a reward report is fully sent for a given period.
     */
    event UEMRewardCompleted(uint256 indexed _fromDate, uint256 _toDate, bytes32 indexed _blockHash);

    /**
     * @dev The Meeds DAO Token
     */
    MeedsToken public meed;

    /**
     * @dev Total cumulative rewards
     */
    uint256 public totalRewards;

    /**
     * @dev Total cumulative claimed rewards
     */
    uint256 public claimedRewards;

    /**
     * 
     * @dev Earner address => amount of claimable Meeds accumulated using UEM rewards
     * 
     * Earner can be:
     * - Deed Owner: who operates a Hub using his/her own Deed
     *   or who rent the Deed and perceive a percentage of Hub community earnings
     *   that is shared with Hub Tenant.
     * - Deed Tenant: the address who acquired a lease
     *   (Using DeedRenting contract) to operate a Hub
     * - Earner Address
     */
    mapping(address => mapping(uint256 => uint256)) public balances;

    /**
     * @dev Readonly for information: Accumulated rewards for a given Hub identified by its address
     */
    mapping(address => uint256) public hubEarnings;

    /**
     * @dev Readonly for information: Accumulated rewards for a given NFT/Deed
     */
    mapping(uint256 => uint256) public nftEarnings;

    /**
     * @dev Readonly for information: Accumulated rewards for a given NFT/Deed manager
     */
    mapping(address => uint256) public managerEarnings;

    /**
     * This method replaces the constructor since this is about an Upgradable Contract
     */
    function initialize(MeedsToken _meed)
        public
        initializer {
        meed = _meed;
        _transferOwnership(_msgSender());
        _addManager(_msgSender());
    }

    /**
     * @notice Send UEM claimable reward amounts
     * 
     * @param _fromDate        Reward period start date
     * @param _toDate          Reward period end date
     * @param _blockHash       Reward block hash as stored in Meeds DAO
     * @param _hubAddress      Hub addresses being rewarded
     * @param _nftId           Deed NFT identifiers corresponding to Hubs being rewarded
     * @param _managerAddress  Deed NFT Tenant/Owner being rewarded
     * @param _rewardAmount    Reward Amount by Deed manager
     */
    function sendRewards(
        uint256 _fromDate,
        uint256 _toDate,
        bytes32 _blockHash,
        bool partialReward,
        address[] memory _hubAddress,
        uint256[] memory _nftId,
        address[] memory _managerAddress,
        uint256[] memory _rewardAmount
    ) public onlyManager {
        for (uint256 i = 0; i < _managerAddress.length; i++) {
            uint256 amount = _rewardAmount[i];
            totalRewards = totalRewards.add(amount);
            hubEarnings[_hubAddress[i]] = hubEarnings[_hubAddress[i]].add(amount);
            nftEarnings[_nftId[i]] = nftEarnings[_nftId[i]].add(amount);
            managerEarnings[_managerAddress[i]] = managerEarnings[_managerAddress[i]].add(amount);
            balances[_managerAddress[i]][_nftId[i]] = balances[_managerAddress[i]][_nftId[i]].add(amount);
        }

        if (partialReward) {
            emit UEMPartialReward(_fromDate, _toDate, _blockHash);
        } else {
            emit UEMRewardCompleted(_fromDate, _toDate, _blockHash);
        }
    }

    /**
     * @dev Total rewards not claimed yet by Deed managers
     */
    function claimableRewards() public view returns (uint256) {
        return totalRewards - claimedRewards;
    }

    /**
     * @dev Get the not-claimed Meeds earning amount, per Deed NFT Manager Address
     */
    function balanceOf(address account, uint256 nftId) public view returns (uint256) {
        return balances[account][nftId];
    }

    /**
     * @dev Claim all rewards for a given Deed NFT to submit to current sender of the transaction
     */
    function claim(uint256 nftId) public returns (bool) {
        return _claim(nftId, _msgSender(), _msgSender(), balances[_msgSender()][nftId]);
    }

    /**
     * @dev Claim a part of the rewards for a given Deed NFT to submit to current sender of the transaction
     */
    function claim(uint256 nftId, uint256 amount) public returns (bool) {
        return _claim(nftId, _msgSender(), _msgSender(), amount);
    }

    /**
     * @dev Claim a part of the rewards for a given Deed NFT to submit to a specific recipient
     */
    function claim(uint256 nftId, address recipient, uint256 amount) public returns (bool) {
        return _claim(nftId, _msgSender(), recipient, amount);
    }

    function _claim(uint256 nftId, address earner, address recipient, uint256 amount) internal returns (bool) {
        require(earner != address(0), "uem.earnerZeroAddress");
        require(recipient != address(0), "uem.recipientZeroAddress");
        balances[earner][nftId] = balances[earner][nftId].sub(amount, "uem.amountExceedsBalance");
        claimedRewards = claimedRewards.add(amount);

        bool result = meed.transferFrom(address(this), recipient, amount);
        if (result) {
          emit RewardClaimed(nftId, earner, recipient, amount);
        }
        return result;
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


