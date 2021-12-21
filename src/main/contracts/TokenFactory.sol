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
contract TokenFactory is Ownable, FundDistribution {

    using SafeMath for uint256;
    using SafeERC20 for IERC20;

    // Info of each user who staked LP Tokens
    struct UserInfo {
        uint256 amount; // How many LP tokens the user has staked
        uint256 rewardDebt; // How much MEED rewards the user had received
    }

    // Info of each fund
    // A fund can be either a Fund that will receive Minted MEED
    // to use its own rewarding distribution strategy or a Liquidity Pool.
    struct FundInfo {
        uint256 fixedPercentage; // How many fixed percentage of minted MEEDs will be sent to this fund contract
        uint256 allocationPoint; // How many allocation points assigned to this pool comparing to other pools
        uint256 lastRewardTime; // Last block timestamp that MEEDs distribution has occurred
        uint256 accMeedPerShare; // Accumulated MEEDs per share: price of LP Token comparing to 1 MEED (multiplied by 10^12 to make the computation more precise)
        bool isLPToken; // // The Liquidity Pool rewarding distribution will be handled by this contract
        // in contrary to a simple Fund Contract which will manage distribution by its own and thus, receive directly minted MEEDs.
    }

    // Since, the minting privilege is exclusively hold
    // by the current contract and it's not transferable,
    // this will be the absolute Maximum Supply of all MEED Token.
    uint256 public constant MAX_MEED_SUPPLY = 1e26;

    uint256 public constant MEED_REWARDING_PRECISION = 1e12;

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

    // LP Operations Events
    event Deposit(address indexed user, address indexed lpAddress, uint256 amount);
    event Withdraw(address indexed user, address indexed lpAddress, uint256 amount);
    event EmergencyWithdraw(address indexed user, address indexed lpAddress, uint256 amount);
    event Harvest(address indexed user, address indexed lpAddress, uint256 amount);

    // Fund Events
    event FundAdded(address indexed fundAddress, uint256 allocation, bool fixedPercentage, bool isLPToken);
    event FundAllocationChanged(address indexed fundAddress, uint256 allocation, bool fixedPercentage);

    constructor (
        MeedToken _meed,
        uint256 _meedPerMinute,
        uint256 _startRewardsDelay
    ) {
        meed = _meed;
        meedPerMinute = _meedPerMinute;
        startRewardsTime = block.timestamp + _startRewardsDelay;
    }

    function setMeedPerMinute(uint256 _meedPerMinute) external onlyOwner {
        require(_meedPerMinute > 0, "!meedPerMinute-0");
        meedPerMinute = _meedPerMinute;
    }

    function addFund(address _fundAddress, uint256 _value, bool _isFixedPercentage, bool _isLPToken) external onlyOwner {
        uint256 lastRewardTime = block.timestamp > startRewardsTime ? block.timestamp : startRewardsTime;

        fundAddresses.push(_fundAddress);
        fundInfos[_fundAddress] = FundInfo({
          lastRewardTime: lastRewardTime,
          isLPToken: _isLPToken,
          allocationPoint: 0,
          fixedPercentage: 0,
          accMeedPerShare: 0
        });

        if (_isFixedPercentage) {
            totalFixedPercentage = totalFixedPercentage.add(_value);
            fundInfos[_fundAddress].fixedPercentage = _value;
            require(totalFixedPercentage <= 100, "#addFund: total percentage can't be greater than 100%");
        } else {
            totalAllocationPoints = totalAllocationPoints.add(_value);
            fundInfos[_fundAddress].allocationPoint = _value;
        }
        emit FundAdded(_fundAddress, _value, _isFixedPercentage, _isLPToken);
    }

    function setFundAllocation(address _fundAddress, uint256 _value, bool _isFixedPercentage) external onlyOwner {
        updateFundReward(_fundAddress);

        FundInfo storage fund = fundInfos[_fundAddress];
        if (_isFixedPercentage) {
            require(fund.accMeedPerShare == 0, "#setFundAllocation Error: can't change fund percentage from variable to fixed");
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
        emit FundAllocationChanged(_fundAddress, _value, _isFixedPercentage);
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
        FundInfo storage fund = fundInfos[_fundAddress];

        // Minting didn't started yet
        if (block.timestamp < startRewardsTime) {
            return;
        }

        uint256 pendingRewardAmount = _pendingRewardBalanceOf(fund);
        if (fund.isLPToken) {
          fund.accMeedPerShare = _getAccMeedPerShare(_fundAddress, pendingRewardAmount);
          _mint(address(this), pendingRewardAmount);
        } else {
          _mint(_fundAddress, pendingRewardAmount);
        }
        fund.lastRewardTime = block.timestamp;
    }

    function deposit(address _lpAddress, uint256 _amount) public {
        FundInfo storage fund = fundInfos[_lpAddress];
        require(fund.isLPToken, "#deposit Error: Liquidity Pool doesn't exist");

        // Update & Mint MEED for the designated pool
        // to ensure systematically to have enough
        // MEEDs balance in current contract
        updateFundReward(_lpAddress);

        UserInfo storage user = userLpInfos[_lpAddress][msg.sender];
        if (user.amount > 0) {
            uint256 pending = user
                .amount
                .mul(fund.accMeedPerShare).div(MEED_REWARDING_PRECISION)
                .sub(user.rewardDebt);
            _safeMeedTransfer(msg.sender, pending);
        }
        IERC20(_lpAddress).safeTransferFrom(address(msg.sender), address(this), _amount);
        user.amount = user.amount.add(_amount);
        user.rewardDebt = user.amount.mul(fund.accMeedPerShare).div(MEED_REWARDING_PRECISION);
        emit Deposit(msg.sender, _lpAddress, _amount);
    }

    function withdraw(address _lpAddress, uint256 _amount) public {
        FundInfo storage fund = fundInfos[_lpAddress];
        require(fund.isLPToken, "#withdraw Error: Liquidity Pool doesn't exist");

        // Update & Mint MEED for the designated pool
        // to ensure systematically to have enough
        // MEEDs balance in current contract
        updateFundReward(_lpAddress);

        UserInfo storage user = userLpInfos[_lpAddress][msg.sender];
        // Send pending MEED Reward to user
        uint256 pendingUserReward = user.amount.mul(fund.accMeedPerShare).div(1e12).sub(
            user.rewardDebt
        );
        _safeMeedTransfer(msg.sender, pendingUserReward);
        user.amount = user.amount.sub(_amount);
        user.rewardDebt = user.amount.mul(fund.accMeedPerShare).div(1e12);

        if (_amount > 0) {
          // Send pending Reward
          IERC20(_lpAddress).safeTransfer(address(msg.sender), _amount);
          emit Withdraw(msg.sender, _lpAddress, _amount);
        } else {
          emit Harvest(msg.sender, _lpAddress, pendingUserReward);
        }
    }

    /**
     * @dev Withdraw without caring about rewards. EMERGENCY ONLY.
     */
    function emergencyWithdraw(address _lpAddress) public {
        FundInfo storage fund = fundInfos[_lpAddress];
        require(fund.isLPToken, "#emergencyWithdraw Error: Liquidity Pool doesn't exist");

        UserInfo storage user = userLpInfos[_lpAddress][msg.sender];
        IERC20(_lpAddress).safeTransfer(address(msg.sender), user.amount);
        emit EmergencyWithdraw(msg.sender, _lpAddress, user.amount);
        user.amount = 0;
        user.rewardDebt = 0;
    }

    /**
     * @dev Claim reward for current wallet from designated Liquidity Pool
     */
    function harvest(address _lpAddress) public {
        withdraw(_lpAddress, 0);
    }

    function fundsLength() public view returns (uint256) {
        return fundAddresses.length;
    }

    function pendingRewardBalanceOf(address _lpAddress, address _user) public view returns (uint256) {
        if (block.timestamp < startRewardsTime) {
            return 0;
        }
        FundInfo storage fund = fundInfos[_lpAddress];
        if (!fund.isLPToken) {
            return 0;
        }
        uint256 pendingRewardAmount = _pendingRewardBalanceOf(fund);
        uint256 accMeedPerShare = _getAccMeedPerShare(_lpAddress, pendingRewardAmount);
        UserInfo storage user = userLpInfos[_lpAddress][_user];
        return user.amount.mul(accMeedPerShare).div(MEED_REWARDING_PRECISION).sub(user.rewardDebt);
    }

    function pendingRewardBalanceOf(address _fundAddress) public view returns (uint256) {
        if (block.timestamp < startRewardsTime) {
            return 0;
        }
        return _pendingRewardBalanceOf(fundInfos[_fundAddress]);
    }

    function _getMultiplier(uint256 _fromTimestamp, uint256 _toTimestamp) internal view returns (uint256) {
        return _toTimestamp.sub(_fromTimestamp).mul(meedPerMinute).div(1 minutes);
    }

    function _pendingRewardBalanceOf(FundInfo memory _fund) internal view returns (uint256) {
        uint256 periodTotalMeedRewards = _getMultiplier(_fund.lastRewardTime, block.timestamp);
        uint256 meedRewardAmount = 0;
        if (_fund.fixedPercentage > 0) {
          meedRewardAmount = periodTotalMeedRewards
            .mul(_fund.fixedPercentage)
            .div(100);
        } else if (_fund.allocationPoint > 0) {
          meedRewardAmount = periodTotalMeedRewards
            .mul(_fund.allocationPoint)
            .div(totalAllocationPoints)
            .mul(100)
            .div(100 - totalFixedPercentage);
        }
        return meedRewardAmount;
    }

    function _getAccMeedPerShare(address _lpAddress, uint256 pendingRewardAmount) internal view returns (uint256) {
        FundInfo memory fund = fundInfos[_lpAddress];
        if (block.timestamp > fund.lastRewardTime) {
            uint256 lpSupply = IERC20(_lpAddress).balanceOf(address(this));
            if (lpSupply != 0) {
              return fund.accMeedPerShare.add(pendingRewardAmount.mul(MEED_REWARDING_PRECISION).div(lpSupply));
            }
        }
        return fund.accMeedPerShare;
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
