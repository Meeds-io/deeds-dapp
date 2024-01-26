// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

/**
 * @dev A structure of Hub report data
 */
struct HubReportReward {
    // Reward Period Id
    uint256 rewardPeriodId;
    // Deed Owner address
    address owner;
    // Deed Tenant address
    address tenant;
    // Fixed Reward indice = ( 𝐸𝑑 ∗ 𝐷𝑟 ∗ 𝐷𝑠 ∗ 𝑀) : without 𝐸𝑤
    uint256 fixedRewardIndex;
    // Fixed Reward indice * ownerSharePercentage
    uint256 ownerFixedIndex;
    // Fixed Reward indice * tenantSharePercentage
    uint256 tenantFixedIndex;
    // Sent date of the report
    uint256 sentDate;
    // Whether the Hub Report is fraud or not
    bool fraud;
}
