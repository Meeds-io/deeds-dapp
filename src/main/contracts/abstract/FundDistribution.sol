// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.0;

/**
 * @title Fund Distribution interface that could be used by other contracts to reference
 * TokenFactory/MasterChef in order to enable minting/rewarding to a designated fund address.
 */
interface FundDistribution {
    /**
     * @dev an operation that triggers reward distribution by minting to the designated address
     * from TokenFactory. The fund address must be already configured in TokenFactory to receive
     * funds, else no funds will be retrieved.
     */
    function updateFundReward(address _fundAddress) external;
}
