// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.20;

import './IERC20.sol';
import './ERC20.sol';
import './Context.sol';
import './SafeMath.sol';
import './Address.sol';
import './Ownable.sol';
import './FundDistribution.sol';

abstract contract XMeedsToken is ERC20("Staked MEED", "xMEED"), Ownable {
    using SafeMath for uint256;
    IERC20 public meed;
    FundDistribution public rewardDistribution;

    constructor(IERC20 _meed, FundDistribution _rewardDistribution) {
        meed = _meed;
        rewardDistribution = _rewardDistribution;
    }

    /**
     * @dev This method will:
     * 1/ retrieve staked amount of MEEDs that should already been approved on ERC20 MEED Token
     * 2/ Send back some xMEED ERC20 Token for staker
     */
    function _stake(uint256 _amount) internal {
        // Retrieve MEEDs from Reserve Fund (TokenFactory)
        require(rewardDistribution.sendReward(address(this)) == true, "Error retrieving funds from reserve");

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

    /**
     * @dev This method will:
     * 1/ Withdraw staked amount of MEEDs that wallet has already staked in this contract
     *  plus a proportion of Rewarded MEEDs sent from TokenFactory/MasterChef
     * 2/ Burn equivalent amount of xMEED from caller account
     */
    function _withdraw(uint256 _amount) internal {
        // Retrieve MEEDs from Reserve Fund (TokenFactory)
        require(rewardDistribution.sendReward(address(this)) == true, "Error retrieving funds from reserve");

        uint256 totalMeed = meed.balanceOf(address(this));
        uint256 totalShares = totalSupply();
        uint256 what = _amount.mul(totalMeed).div(totalShares);
        _burn(_msgSender(), _amount);
        meed.transfer(_msgSender(), what);
    }
}