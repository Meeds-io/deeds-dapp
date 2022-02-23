// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.11;

import "./abstract/SafeMath.sol";
import "./abstract/Address.sol";
import "./abstract/Ownable.sol";
import "./abstract/TenantProvisioningDelegator.sol";
import "./Deed.sol";

/**
 * @title Deed Tenant provisionning Contract for Deed NFT owners
 */
contract TenantProvisioningStrategy is Ownable {
    using SafeMath for uint256;
    using Address for address;

    Deed public deed;

    address[] public blockerStrategies;

    TenantProvisioningDelegator[] public delegators;

    /**
     * @dev Throws if called by any account other than the owner of the NFT.
     */
    modifier onlyNFTOwner(uint256 _nftId) {
        uint256 _balance = deed.balanceOf(msg.sender, _nftId);
        if (_balance == 0) {
            uint i = 0;
            while ( i < delegators.length && _balance == 0) {
                if (delegators[i++].isApprovedForProvisioning(msg.sender, _nftId)) {
                    _balance++;
                }
            }
            if (_balance == 0) {
              revert("TenantProvisioningStrategy#onlyNFTOwner: Not owner or approved to use the NFT");
            }
        }
        _;
    }

    /**
     * @dev checks if the NFT is used inside another blocker strategy,
     * if so, throws an error. This will throws an error too when the NFT is already
     * used by current strategy
     */
    modifier canUse(uint256 _nftId) {
        require(deed.getStratUseCount(msg.sender, _nftId, address(this)) == 0, "TenantProvisioningStrategy#startDeedTenant: NFT is already used in current strategy");
        for (uint i = 0; i < blockerStrategies.length;i++) {
            require(deed.getStratUseCount(msg.sender, _nftId, blockerStrategies[i]) == 0, "TenantProvisioningStrategy#startDeedTenant: NFT is used in another blocker strategy");
        }
        _;
    }

    constructor (Deed _deed) {
        deed = _deed;
    }

    /**
     * @dev Enroll NFT into current strategy to start corresponding Tenant in its city
     */
    function startUsingDeed(uint256 _nftId) external onlyNFTOwner(_nftId) canUse(_nftId) {
        try deed.startUsingNFT(msg.sender, _nftId) returns (bool response) {
            if (response != true) {
                revert("TenantProvisioningStrategy#startUsingDeed: Error Starting Strategy");
            }
        } catch Error(string memory reason) {
            revert(reason);
        } catch {
            revert("TenantProvisioningStrategy#startUsingDeed: Error Starting Strategy");
        }
    }

    /**
     * @dev ends using NFT. This will throws an error when NFT is not in use
     */
    function endUsingDeed(uint256 _nftId) external onlyNFTOwner(_nftId) {
        try deed.endUsingNFT(msg.sender, _nftId) returns (bool response) {
            if (response != true) {
                revert("TenantProvisioningStrategy#endUsingDeed: Error Stopping Strategy");
            }
        } catch Error(string memory reason) {
            revert(reason);
        } catch {
            revert("TenantProvisioningStrategy#endUsingDeed: Error Stopping Strategy");
        }
    }

    /**
     * @dev Add a blocker strategy
     */
    function addBlockerStrategy(address _strategy) external onlyOwner {
        blockerStrategies.push(_strategy);
    }

    /**
     * @dev Removes a blocker strategy identified by its index in the array
     */
    function removeBlockerStrategy(uint index) external onlyOwner {
        delete blockerStrategies[index];
    }

    /**
     * @dev Add a delegator
     */
    function addDelegator(TenantProvisioningDelegator _delegator) external onlyOwner {
        require(address(_delegator).isContract(), "TenantProvisioningStrategy#addDelegator: Address must be a TenantProvisioningDelegator contract");
        delegators.push(_delegator);
    }

    /**
     * @dev Removes a _delegator identified by its index in the array
     */
    function removeDelegator(uint index) external onlyOwner {
        delete delegators[index];
    }

}
