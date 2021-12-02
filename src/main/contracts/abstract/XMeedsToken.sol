// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.0;

import './IERC20.sol';
import './ERC20.sol';
import './Context.sol';
import './SafeMath.sol';
import './Address.sol';
import './Ownable.sol';

abstract contract XMeedsToken is ERC20("Staked MEED", "xMEED"), Ownable {
    using SafeMath for uint256;
    IERC20 public meed;
    address public rewardDistribution;

    event RewardDistributorSet(address indexed newRewardDistributor);
    event MeedFeeReceived(address indexed from, uint256 meedAmount);

    modifier onlyRewardDistribution() {
        require(
            _msgSender() == rewardDistribution,
            "Caller is not reward distribution"
        );
        _;
    }

    constructor(IERC20 _Meed) {
        meed = _Meed;
    }

    function setRewardDistribution(address _rewardDistribution)
        external
        onlyOwner {
        rewardDistribution = _rewardDistribution;
        emit RewardDistributorSet(_rewardDistribution);
    }

    function notifyRewardAmount(uint256 _balance) external onlyRewardDistribution {
        meed.transferFrom(_msgSender(), address(this), _balance);
        emit MeedFeeReceived(_msgSender(), _balance);
    }

    function _stake(uint256 _amount) internal {
        uint256 totalMeed = meed.balanceOf(address(this));
        uint256 totalShares = totalSupply();
        if (totalShares == 0 || totalMeed == 0) {
            _mint(_msgSender(), _amount);
        } else {
            uint256 what = _amount.mul(totalShares).div(totalMeed);
            _mint(_msgSender(), what);
        }
        meed.transferFrom(_msgSender(), address(this), _amount);
    }

    function _withdraw(uint256 _share) internal {
        uint256 totalMeed = meed.balanceOf(address(this));
        uint256 totalShares = totalSupply();
        uint256 what = _share.mul(totalMeed).div(totalShares);
        _burn(_msgSender(), _share);
        meed.transfer(_msgSender(), what);
    }
}