// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.11;

import "./abstract/SafeMath.sol";
import "./abstract/Address.sol";
import "./abstract/ManagerRole.sol";
import "./abstract/TenantProvisioningDelegator.sol";
import "./abstract/StrategyHandler.sol";

/**
 * @title Deed Tenant provisionning Contract for Deed NFT owners
 */
contract TenantProvisioningStrategy is ManagerRole {
    using SafeMath for uint256;
    using Address for address;

    event DelegateeAdded(address indexed account, uint256 nftId, address indexed manager);
    event DelegateeRemoved(address indexed account, uint256 nftId, address indexed manager);

    StrategyHandler public deed;

    // NFT ID => Delegatee on behalf of NFT owner
    mapping(uint256 => address) private delegatees;

    /**
     * @dev Throws if called by any account other than the owner of the NFT.
     */
    modifier onlyProvisioningManager(uint256 _nftId) {
        require(isProvisioningManager(msg.sender, _nftId), "TenantProvisioningStrategy#onlyProvisioningOwner: Not provisioning owner of the NFT");
        _;
    }

    /**
     * @dev checks if the NFT is used inside another blocker strategy,
     * if so, throws an error. This will throws an error too when the NFT is already
     * used by current strategy
     */
    modifier canUse(uint256 _nftId) {
        require(deed.getStrategyUseCount(msg.sender, _nftId, address(this)) == 0, "TenantProvisioningStrategy#startDeedTenant: NFT is already used in current strategy");
        _;
    }

    constructor (StrategyHandler _deed) {
        deed = _deed;
    }

    /**
     * @dev Enroll NFT into current strategy to start corresponding Tenant in its city
     */
    function startTenant(uint256 _nftId) external onlyProvisioningManager(_nftId) canUse(_nftId) {
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
    function stopTenant(uint256 _nftId) external onlyProvisioningManager(_nftId) {
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

    function setDelegatee(address _address, uint256 _nftId) external onlyManager returns(bool) {
        delegatees[_nftId] = _address;
        emit DelegateeAdded(_address, _nftId, _msgSender());
        return true;
    }

    function removeDelegatee(uint256 _nftId) external onlyManager returns(bool) {
        address delegatee = delegatees[_nftId];
        delete delegatees[_nftId];
        emit DelegateeRemoved(delegatee, _nftId, _msgSender());
        return true;
    }

    function getDelegatee(uint256 _nftId) public view returns(address) {
        return delegatees[_nftId];
    }

    /**
     * @dev returns true if the address can manage NFT Tenant provisionning (Start & Stop)
     */
    function isProvisioningManager(address _address, uint256 _nftId) public view returns(bool) {
        uint256 _balance = deed.balanceOf(_address, _nftId);
        if (_balance == 0) {
            // If this is not NFT owner, we will check if the _address is approved to manage provisioning
            return _address == delegatees[_nftId];
        } else {
            // If this is about owner, the provisionning shouldn't be delegated
            return address(0x0) == delegatees[_nftId];
        }
    }

}
