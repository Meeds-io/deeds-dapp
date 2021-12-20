// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.0;

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

    constructor (IERC20 _meed, uint256 _startRewardsDelay) XMeedsToken(_meed) {
        startRewardsTime = block.timestamp + _startRewardsDelay;
    }

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

    function stake(uint256 amount) public updateReward(msg.sender) {
        require(amount > 0, "Invalid amount");

        _stake(amount);
        emit Staked(msg.sender, amount);
    }

    function withdraw(uint256 amount) public updateReward(msg.sender) {
        require(amount > 0, "Cannot withdraw 0");

        _withdraw(amount);
        emit Withdrawn(msg.sender, amount);
    }

    function exit() external {
        withdraw(balanceOf(msg.sender));
    }

    function transfer(address recipient, uint256 amount)
        public
        virtual
        override
        updateReward(msg.sender)
        updateReward(recipient)
        returns (bool) {
        return super.transfer(recipient, amount);
    }

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


