// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.0;

import "./abstract/Ownable.sol";
import "./abstract/SafeMath.sol";
import "./abstract/SafeERC20.sol";
import "./abstract/MeedToken.sol";
import "./abstract/FundDistribution.sol";

/**
 * @dev This contract will send MEED rewards to multiple funds by minting on MEED contract.
 * Since it is the only owner of the MEED Token, all minting operations will be exclusively
 * made here.
 * This contract will mint for the 3 type of Rewarding mechanisms as described in MEED white paper:
 * - Liquidity providers through renting and buying liquidity pools
 * - User Engagment within the software
 * - Work / services  provided by association members to build the DOM
 * 
 * In other words, MEEDs are created based on the involvment of three different categories
 * of stake holders:
 * - the capital owners
 * - the users
 * - the builders
 * 
 * Consequently, there will be two kind of Fund Contracts that will be managed by this one:
 * - ERC20 LP Token contracts: this contract will reward LP Token stakers
 * with a proportion of minted MEED per minute
 * - Fund contract : which will receive a proportion of minted MEED (unlike LP Token contract)
 *  to make the distribution switch its internal algorithm.
 */
contract MasterChef is Ownable, FundDistribution {

    using SafeMath for uint256;
    using SafeERC20 for IERC20;

    // Info of each user who staked LP Tokens
    struct UserInfo {
        uint256 amount; // How many LP tokens the user has staked
        uint256 rewardDebt; // How much MEED rewards the user had received
    }

    // Info of each fund
    struct FundInfo {
        uint256 allocationPoint; // How many allocation points assigned to this pool comparing to other pools
        uint256 fixedPercentage; // How many fixed percentage of minted MEEDs will be sent to this fund contract
        uint256 lastRewardTime; // Last block timestamp that MEEDs distribution has occurred
        uint256 lpTokenPrice; // Accumulated MEEDs per share: price of LP Token comparing to 1 MEED (multiplied by 10^12 to make the computation more precise)
        bool isLPToken;
    }

    // Since, the minting privilege is exclusively hold
    // by the current contract and it's not transferable,
    // this will be the absolute Maximum Supply of all MEED Token.
    uint256 public constant MAX_MEED_SUPPLY = 1e26;

    // The MEED TOKEN!
    MeedToken public meed;

    // MEEDs minted per minute
    uint256 public meedPerMinute;

    // List of fund addresses
    address[] public fundAddresses;

    // Info of each pool
    mapping(address => FundInfo) public fundInfos;

    // Info of each user that stakes LP tokens
    mapping(address => mapping(address => UserInfo)) public userLpInfos;

    // Total allocation points. Must be the sum of all allocation points in all pools.
    uint256 public totalAllocationPoints = 0;
    // Total fixed percentage. Must be the sum of all allocation points in all pools.
    uint256 public totalFixedPercentage = 0;

    // The block time when MEED mining starts
    uint256 public startRewardsTime;

    // Events
    event Recovered(address token, uint256 amount);
    event Deposit(address indexed user, address indexed fundAddress, uint256 amount);
    event Withdraw(address indexed user, address indexed fundAddress, uint256 amount);
    event ClaimReward(address indexed user, address indexed fundAddress, uint256 amount);
    event EmergencyWithdraw(address indexed user, address indexed fundAddress, uint256 amount);

    constructor (
        MeedToken _meed,
        uint256 _meedPerMinute,
        uint256 _startRewardsTime
    ) {
        meed = _meed;
        meedPerMinute = _meedPerMinute;
        startRewardsTime = _startRewardsTime;
    }

    function setMeedPerMinute(uint256 _meedPerMinute) external onlyOwner {
        require(_meedPerMinute > 0, "!meedPerMinute-0");
        meedPerMinute = _meedPerMinute;
    }

    function addFund(address _fundAddress, uint256 _value, bool _fixedPercentage, bool _isLPToken) external onlyOwner {
        uint256 lastRewardTime = block.timestamp > startRewardsTime ? block.timestamp : startRewardsTime;

        fundAddresses.push(_fundAddress);
        fundInfos[_fundAddress] = FundInfo({
          lastRewardTime: lastRewardTime,
          isLPToken: _isLPToken,
          allocationPoint: 0,
          fixedPercentage: 0,
          lpTokenPrice: 0
        });

        if (_fixedPercentage) {
            totalFixedPercentage = totalFixedPercentage.add(_value);
            fundInfos[_fundAddress].fixedPercentage = _value;
            require(totalFixedPercentage <= 100, "#addFund: total percentage can't be greater than 100%");
        } else {
            totalAllocationPoints = totalAllocationPoints.add(_value);
            fundInfos[_fundAddress].allocationPoint = _value;
        }
    }

    function setFundAllocation(address _fundAddress, uint256 _value, bool _fixedPercentage) external onlyOwner {
        updateFundReward(_fundAddress);

        FundInfo storage fund = fundInfos[_fundAddress];
        if (_fixedPercentage) {
            require(fund.lpTokenPrice == 0, "#setFundAllocation Error: can't change fund percentage from variable to fixed");
            totalFixedPercentage = totalFixedPercentage.sub(fund.fixedPercentage).add(_value);
            require(totalFixedPercentage <= 100, "#setFundAllocation: total percentage can't be greater than 100%");
            fund.fixedPercentage = _value;
            totalAllocationPoints = totalAllocationPoints.sub(fund.allocationPoint);
            fund.allocationPoint = 0;
        } else {
            require(!fund.isLPToken || fund.fixedPercentage == 0, "#setFundAllocation Error: can't change Liquidity Pool percentage from fixed to variable");
            totalAllocationPoints = totalAllocationPoints.sub(fund.allocationPoint).add(_value);
            fund.allocationPoint = _value;
            totalFixedPercentage = totalFixedPercentage.sub(fund.fixedPercentage);
            fund.fixedPercentage = 0;
        }
    }

    function updateAllFundRewards() external {
        uint256 length = fundAddresses.length;
        for (uint256 index = 0; index < length; index++) {
            updateFundReward(fundAddresses[index]);
        }
    }

    function batchUpdateFundRewards(address[] memory _fundAddresses) external {
        uint256 length = _fundAddresses.length;
        for (uint256 index = 0; index < length; index++) {
            updateFundReward(fundAddresses[index]);
        }
    }

    function updateFundReward(address _fundAddress) public override {
        // Minting didn't started yet
        if (block.timestamp <= startRewardsTime) {
            return;
        }

        FundInfo storage fund = fundInfos[_fundAddress];
        uint256 periodTotalMeedRewards = getPeriodMeedRewards(fund.lastRewardTime, block.timestamp);
        uint256 meedRewardAmount = 0;
        if (fund.fixedPercentage > 0) {
          meedRewardAmount = periodTotalMeedRewards
            .mul(fund.fixedPercentage)
            .div(100);
        } else if (fund.allocationPoint > 0) {
          meedRewardAmount = periodTotalMeedRewards
            .mul(fund.allocationPoint)
            .div(totalAllocationPoints)
            .mul(100)
            .div(100 - totalFixedPercentage);
        }

        if (fund.isLPToken) {
          uint256 lpSupply = IERC20(_fundAddress).balanceOf(address(this));
          if (lpSupply > 0) {
            fund.lpTokenPrice = fund.lpTokenPrice.add(meedRewardAmount.mul(1e12).div(lpSupply));
            _mint(address(this), meedRewardAmount);
          }
        } else {
          _mint(_fundAddress, meedRewardAmount);
        }
        fund.lastRewardTime = block.timestamp;
    }

    function deposit(address _fundAddress, uint256 _amount) public {
        FundInfo storage fund = fundInfos[_fundAddress];
        require(fund.isLPToken, "#deposit Error: Liquidity Pool doesn't exist");

        UserInfo storage user = userLpInfos[_fundAddress][msg.sender];
        updateFundReward(_fundAddress);
        if (user.amount > 0) {
            uint256 pending = user
                .amount
                .mul(fund.lpTokenPrice).div(1e12)
                .sub(user.rewardDebt);
            _safeMeedTransfer(msg.sender, pending);
        }
        IERC20(_fundAddress).safeTransferFrom(address(msg.sender), address(this), _amount);
        user.amount = user.amount.add(_amount);
        user.rewardDebt = user.amount.mul(fund.lpTokenPrice).div(1e12);
        emit Deposit(msg.sender, _fundAddress, _amount);
    }

    function withdraw(address _fundAddress, uint256 _amount) public {
        FundInfo storage fund = fundInfos[_fundAddress];
        require(fund.isLPToken, "#withdraw Error: Liquidity Pool doesn't exist");

        UserInfo storage user = userLpInfos[_fundAddress][msg.sender];
        require(user.amount >= _amount, "#withdraw: not enough funds");

        // Update & Mint MEED for the designated pool
        // to ensure systematically to have enough
        // MEEDs balance in current contract
        updateFundReward(_fundAddress);

        // Send pending MEED Reward to user
        uint256 oldRewardDebt = user.rewardDebt;
        user.rewardDebt = user.amount.mul(fund.lpTokenPrice).div(1e12);
        uint256 pending = user.rewardDebt.sub(oldRewardDebt);
        _safeMeedTransfer(msg.sender, pending);

        if (_amount > 0) {
          // Send pending Reward
          user.amount = user.amount.sub(_amount);
          IERC20(_fundAddress).safeTransfer(address(msg.sender), _amount);
        }
        emit Withdraw(msg.sender, _fundAddress, _amount);
    }

    // Withdraw without caring about rewards. EMERGENCY ONLY.
    function emergencyWithdraw(address _fundAddress) public {
        FundInfo storage fund = fundInfos[_fundAddress];
        require(fund.isLPToken, "#emergencyWithdraw Error: Liquidity Pool doesn't exist");

        UserInfo storage user = userLpInfos[_fundAddress][msg.sender];
        IERC20(_fundAddress).safeTransfer(address(msg.sender), user.amount);
        emit EmergencyWithdraw(msg.sender, _fundAddress, user.amount);
        user.amount = 0;
        user.rewardDebt = 0;
    }

    function claimReward(address _fundAddress, uint256 _amount) public {
        FundInfo storage fund = fundInfos[_fundAddress];
        if (fund.isLPToken) {
          UserInfo storage user = userLpInfos[_fundAddress][msg.sender];
          require(user.amount >= _amount, "#withdraw: not enough funds");
  
          // Update & Mint MEED for the designated pool
          // to ensure systematically to have enough
          // MEEDs balance in current contract
          updateFundReward(_fundAddress);
  
          // Send pending MEED Reward to user
          uint256 oldRewardDebt = user.rewardDebt;
          user.rewardDebt = user.amount.mul(fund.lpTokenPrice).div(1e12);
          uint256 pending = user.rewardDebt.sub(oldRewardDebt);
          _safeMeedTransfer(msg.sender, pending);
        } else {
          // Mint Rewards for the designated Fund Address
          updateFundReward(_fundAddress);
        }
        emit ClaimReward(msg.sender, _fundAddress, _amount);
    }

    function fundsLength() public view returns (uint256) {
        return fundAddresses.length;
    }

    function pendingRewardBalanceOf(address _fundAddress, address _user) public view returns (uint256) {
        if (block.timestamp < startRewardsTime) {
            return 0;
        }
        uint256 lpTokenPrice = getLpTokenPrice(_fundAddress);
        UserInfo storage user = userLpInfos[_fundAddress][_user];
        return user.amount.mul(lpTokenPrice).div(1e12).sub(user.rewardDebt);
    }

    function pendingRewardBalanceOf(address _fundAddress) public view returns (uint256) {
        if (block.timestamp < startRewardsTime) {
            return 0;
        }
        FundInfo storage fund = fundInfos[_fundAddress];
        uint256 periodTotalMeedRewards = getPeriodMeedRewards(fund.lastRewardTime, block.timestamp);
        uint256 meedRewardAmount = 0;
        if (fund.fixedPercentage > 0) {
          meedRewardAmount = periodTotalMeedRewards
            .mul(fund.fixedPercentage)
            .div(100);
        } else if (fund.allocationPoint > 0) {
          meedRewardAmount = periodTotalMeedRewards
            .mul(fund.allocationPoint)
            .div(totalAllocationPoints)
            .mul(100)
            .div(100 - totalFixedPercentage);
        }
        return meedRewardAmount;
    }

    function getPeriodMeedRewards(uint256 _fromTimestamp, uint256 _toTimestamp) public view returns (uint256) {
        return _toTimestamp.sub(_fromTimestamp).mul(meedPerMinute).div(1 minutes);
    }

    function getLpTokenPrice(address _fundAddress) public view returns (uint256) {
        FundInfo storage fund = fundInfos[_fundAddress];
        if (block.timestamp > fund.lastRewardTime) {
            uint256 lpSupply = IERC20(_fundAddress).balanceOf(address(this));
            if (lpSupply != 0) {
              uint256 periodTotalMeedRewards = getPeriodMeedRewards(fund.lastRewardTime, block.timestamp);
              uint256 lpMeedReward = periodTotalMeedRewards.mul(fund.allocationPoint).div(totalAllocationPoints);
              return fund.lpTokenPrice.add(lpMeedReward.mul(1e12).div(lpSupply));
            }
        }
        return fund.lpTokenPrice;
    }

    function _safeMeedTransfer(address _to, uint256 _amount) internal {
        uint256 meedBal = meed.balanceOf(address(this));
        if (_amount > meedBal) {
            meed.transfer(_to, meedBal);
        } else {
            meed.transfer(_to, _amount);
        }
    }

    function _mint(address _to, uint256 _amount) internal {
        uint256 totalSupply = meed.totalSupply();
        if (totalSupply.add(_amount) > MAX_MEED_SUPPLY) {
            uint256 remainingAmount = totalSupply.sub(_amount);
            require(remainingAmount > 0, "#_mint: max MEED Supply reached!");
            meed.mint(_to, remainingAmount);
        } else {
            meed.mint(_to, _amount);
        }
    }

}
