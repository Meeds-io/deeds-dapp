// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.20;

import './XMeedsToken.sol';
import './SafeMath.sol';

contract MeedsPointsRewarding is XMeedsToken {
    using SafeMath for uint256;

    // The block time when Points rewarding will starts
    uint256 public startRewardsTime;

    mapping(address => uint256) internal points;
    mapping(address => uint256) internal pointsLastUpdateTime;

    event Staked(address indexed user, uint256 amount);
    event Withdrawn(address indexed user, uint256 amount);

    /**
     * @dev a modifier to store earned points for a designated address until
     * current block after having staked some MEEDs. if the Points rewarding didn't started yet
     * the address will not receive points yet.
     */
    modifier updateReward(address account) {
        if (account != address(0)) {
          if (block.timestamp < startRewardsTime) {
            points[account] = 0;
            pointsLastUpdateTime[account] = startRewardsTime;
          } else {
            points[account] = earned(account);
            pointsLastUpdateTime[account] = block.timestamp;
          }
        }
        _;
    }

    constructor (IERC20 _meed, FundDistribution _rewardDistribution, uint256 _startRewardsTime) XMeedsToken(_meed, _rewardDistribution) {
        startRewardsTime = _startRewardsTime;
    }

    /**
     * @dev returns the earned points for the designated address after having staked some MEEDs
     * token. If the Points rewarding distribution didn't started yet, 0 will be returned instead.
     */
    function earned(address account) public view returns (uint256) {
        if (block.timestamp < startRewardsTime) {
          return 0;
        } else {
          uint256 timeDifference = block.timestamp.sub(pointsLastUpdateTime[account]);
          uint256 balance = balanceOf(account);
          uint256 decimals = 1e18;
          uint256 x = balance / decimals;
          uint256 ratePerSecond = decimals.mul(x).div(x.add(12000)).div(240);
          return points[account].add(ratePerSecond.mul(timeDifference));
        }
    }

    /**
     * @dev This method will:
     * 1/ Update Rewarding Points for address of caller (using modifier)
     * 2/ retrieve staked amount of MEEDs that should already been approved on ERC20 MEED Token
     * 3/ Send back some xMEED ERC20 Token for staker
     */
    function stake(uint256 amount) public updateReward(msg.sender) {
        require(amount > 0, "Invalid amount");

        _stake(amount);
        emit Staked(msg.sender, amount);
    }

    /**
     * @dev This method will:
     * 1/ Update Rewarding Points for address of caller (using modifier)
     * 2/ Withdraw staked amount of MEEDs that wallet has already staked in this contract
     *  plus a proportion of Rewarded MEEDs sent from TokenFactory/MasterChef
     * 3/ Burn equivalent amount of xMEED from caller account
     */
    function withdraw(uint256 amount) public updateReward(msg.sender) {
        require(amount > 0, "Cannot withdraw 0");

        _withdraw(amount);
        emit Withdrawn(msg.sender, amount);
    }

    /**
     * @dev This method will:
     * 1/ Update Rewarding Points for address of caller (using modifier)
     * 2/ Withdraw all staked MEEDs that are wallet has staked in this contract
     *  plus a proportion of Rewarded MEEDs sent from TokenFactory/MasterChef
     * 3/ Burn equivalent amount of xMEED from caller account
     */
    function exit() external {
        withdraw(balanceOf(msg.sender));
    }

    /**
     * @dev ERC-20 transfer method in addition to updating earned points
     * of spender and recipient (using modifiers)
     */
    function transfer(address recipient, uint256 amount)
        public
        virtual
        override
        updateReward(msg.sender)
        updateReward(recipient)
        returns (bool) {
        return super.transfer(recipient, amount);
    }

    /**
     * @dev ERC-20 transferFrom method in addition to updating earned points
     * of spender and recipient (using modifiers)
     */
    function transferFrom(address sender, address recipient, uint256 amount)
        public
        virtual
        override
        updateReward(sender)
        updateReward(recipient)
        returns (bool) {
        return super.transferFrom(sender, recipient, amount);
    }

}


