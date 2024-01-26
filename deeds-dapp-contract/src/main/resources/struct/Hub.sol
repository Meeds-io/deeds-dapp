// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

/**
 * @dev A structure of Hub associated to a Deed in the WoM
 */
struct Hub {
    // Connected or last previously connected Deed NFT identifier
    uint256 deedId;
    // Hub owner
    address owner;
    // Hub is connected or disconnected
    bool enabled;
    // Date of joining the WoM
    uint256 joinDate;
}
