// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.11;

import "./Ownable.sol";
import "./SafeMath.sol";
import "./Context.sol";
import "./Roles.sol";

/**
 * @title ManagerRole
 * @dev Owner is responsible to add/remove manager
 */
contract ManagerRole is Context, Ownable {
    using Roles for Roles.Role;

    event ManagerAdded(address indexed account);
    event ManagerRemoved(address indexed account);

    Roles.Role private _managers;

    modifier onlyManager() {
        require(isManager(_msgSender()), "ManagerRole: caller does not have the Manager role");
        _;
    }

    function isManager(address account) public view returns (bool) {
        return _managers.has(account);
    }

    function addManager(address account) public onlyOwner {
        _addManager(account);
    }

    function removeManager(address account) public onlyOwner {
        _removeManager(account);
    }

    function _addManager(address account) internal {
        _managers.add(account);
        emit ManagerAdded(account);
    }

    function _removeManager(address account) internal {
        _managers.remove(account);
        emit ManagerRemoved(account);
    }
}


