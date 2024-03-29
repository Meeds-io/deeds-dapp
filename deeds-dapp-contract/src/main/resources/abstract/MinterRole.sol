// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

import "./Context.sol";
import "./Ownable.sol";
import "./Roles.sol";

/**
 * @title MinterRole
 * @dev Owner is responsible to add/remove minter
 */
contract MinterRole is Context, Ownable {

    using Roles for Roles.Role;

    event MinterAdded(address indexed account);

    event MinterRemoved(address indexed account);

    Roles.Role private _minters;

    modifier onlyMinter() {
        require(
            isMinter(_msgSender()),
            "MinterRole: caller does not have the Minter role"
        );
        _;
    }

    function isMinter(address account) public view returns (bool) {
        return _minters.has(account);
    }

    function addMinter(address account) public onlyOwner {
        _addMinter(account);
    }

    function renounceMinter() public {
        _removeMinter(_msgSender());
    }

    function _addMinter(address account) internal {
        _minters.add(account);
        emit MinterAdded(account);
    }

    function _removeMinter(address account) internal {
        _minters.remove(account);
        emit MinterRemoved(account);
    }
}
