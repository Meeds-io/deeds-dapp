// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

/**
 * @dev A structure of date for a Reward period
 */
struct Reward {
    // Total configured Reward amount at this time
    uint256 amount;
    // Eligible reports count
    uint256 reportsCount;
    // First report index included in this Reward period
    uint256 fromReport;
    // Last report index included in this Reward period
    uint256 toReport;
    // Sum of all Ed of all reports
    // (doesn't correspond to Ew because it's not divided by reports count yet)
    uint256 sumEd;
    // Start date of reward period
    uint256 fromDate;
    // End date of reward period
    uint256 toDate;
}
