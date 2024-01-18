// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

import "./abstract/ERC1967Proxy.sol";

contract WoMProxy is ERC1967Proxy {

    constructor(address _logic, bytes memory _data) payable ERC1967Proxy(_logic, _data) {
    }

}
