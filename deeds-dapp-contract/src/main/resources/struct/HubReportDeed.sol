// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

/**
 * @dev A structure of Hub report data
 */
struct HubReportDeed {
    // Deed Id
    uint256 deedId;
    // Deed City
    uint8 city;
    // Deed Type
    uint8 cardType;
    // Deed Minting Power
    uint8 mintingPower;
    // Deed Rewarded Max Users
    uint256 maxUsers;
}
