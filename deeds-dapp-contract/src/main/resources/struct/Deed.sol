// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

/**
 * @dev The Deed NFT Structure as defined in 0x0143b71443650aa8efa76bd82f35c22ebd558090 on ethereum
 */
struct Deed {
    // Deed city: Tanit, Reshef, Ashtarte, Melqart, Eshmun, Kushor or Hammon
    uint8 city;
    // Deed type: COMMON, UNCOMMON, RARE or LEGENDARY
    uint8 cardType;
    // Deed Minting Power: COMMON = 1.0 = 100%, UNCOMMON = 1.1 = 110%, RARE = 1.3 = 130% or LEGENDARY = 2.0 = 200%
    uint8 mintingPower;
    // Deed Hub Max users = COMMON = 100, UNCOMMON = 1000, RARE = 10000 or LEGENDARY = unlimited
    uint256 maxUsers;
    // Deed NFT owner
    address owner;
    // Deed NFT tenant, can be the same as the Deed NFT owner when no lease
    address tenant;
    // Hub using the Deed
    address hub;
    // UEM rewarding share percentage for the Deed Owner comparing to the Deed Tenant
    uint8 ownerPercentage;
    // UEM rewarding share percentage for the Deed Owner comparing to the Deed Tenant
    uint8 tenantPercentage;
}
