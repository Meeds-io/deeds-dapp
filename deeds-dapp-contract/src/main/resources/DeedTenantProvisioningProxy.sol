// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.20;

import "./abstract/ERC1967Proxy.sol";

contract DeedTenantProvisioningProxy is ERC1967Proxy {

    constructor(address _logic, bytes memory _data) payable ERC1967Proxy(_logic, _data) {
    }

}
