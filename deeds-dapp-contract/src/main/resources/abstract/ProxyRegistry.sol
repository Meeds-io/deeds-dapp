// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

import './OwnableDelegateProxy.sol';

// Part: ProxyRegistry

contract ProxyRegistry {
    mapping(address => OwnableDelegateProxy) public proxies;
}
