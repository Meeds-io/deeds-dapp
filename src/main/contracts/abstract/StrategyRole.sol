// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.0;

import "./Ownable.sol";
import "./SafeMath.sol";
import "./Context.sol";
import "./Roles.sol";

/**
 * @title StrategyRole
 * @dev Owner is responsible to add/remove strategy
 */
contract StrategyRole is Context, Ownable {
    using Roles for Roles.Role;

    event StrategyAdded(address indexed account);
    event StrategyRemoved(address indexed account);

    Roles.Role private _strategies;

    modifier onlyStrategy() {
        require(isStrategy(_msgSender()), "StrategyRole: caller does not have the Strategy role");
        _;
    }

    function isStrategy(address account) public view returns (bool) {
        return _strategies.has(account);
    }

    function addStrategy(address account) public onlyOwner {
        _addStrategy(account);
    }

    function removeStrategy(address account) public onlyOwner {
        _removeStrategy(account);
    }

    function _addStrategy(address account) internal {
        _strategies.add(account);
        emit StrategyAdded(account);
    }

    function _removeStrategy(address account) internal {
        _strategies.remove(account);
        emit StrategyRemoved(account);
    }
}


