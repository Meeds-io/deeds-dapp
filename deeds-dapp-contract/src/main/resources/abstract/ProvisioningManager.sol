// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.20;

import "./ManagerRole.sol";
import "./SafeMath.sol";
import "./Context.sol";
import "./Roles.sol";

/**
 * @title ProvisioningManager
 * @dev Contract Responsible to manage access on provisioning management
 */
abstract contract ProvisioningManager {

    function isProvisioningManager(address account, uint256 deedId) external virtual view returns (bool);

}