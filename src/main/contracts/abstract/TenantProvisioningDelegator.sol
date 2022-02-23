// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.11;

/**
 * @title Deed Tenant provisionning Delegator Contract.
 * Allows to check if the Deed Owner had approved another Wallet
 * to use his NFT for Tenant provisionning.
 */
interface TenantProvisioningDelegator {

    /**
     * @dev Returns true when the NFT Deed Owner had approved another wallet owner
     * to manage Tenant provisioning.
     */
    function isApprovedForProvisioning(address _user, uint256 _nftId) external returns (bool);

}
