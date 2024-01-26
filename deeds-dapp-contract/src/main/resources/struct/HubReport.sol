// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

/**
 * @dev A structure of Hub report data
 */
struct HubReport {
    // Hub address
    address hub;
    // Users Count
    uint256 usersCount;
    // Recipients Count
    uint256 recipientsCount;
    // Participants Count
    uint256 participantsCount;
    // Achievements Count
    uint256 achievementsCount;
    // Sent rewards by Hub Manager to its users
    uint256 amount;
    // Used Token address used by Hub to send its rewards
    address tokenAddress;
    // Used Token chain Id used by Hub to send its rewards
    uint256 tokenChainId;
    // Sent rewards by Hub Manager to its users
    uint256 fromDate;
    // Sent rewards by Hub Manager to its users
    uint256 toDate;
}
