// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

/**
 * @dev A structure of data to define Rewards shares between
 *      all participating Deed Owners and Tenants
 */
struct Recipient {
    // Already claimed rewards
    uint256 claimedRewards;
    // Accumulated Rewards already computed before
    // the "index" of the list of reportIds table
    // thus this may not hold the total rewards amount
    uint256 accRewards;
    // List of report ids where the recipient
    // shares a UEM reward
    uint256[] reportIds;
    // Last index that was used to compute
    // the accumulated rewards ("accRewards")
    uint index;
}
