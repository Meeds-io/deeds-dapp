// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

import "./abstract/Initializable.sol";
import "./abstract/UUPSUpgradeable.sol";
import "./abstract/SafeMath.sol";
import "./abstract/ManagerRole.sol";
import "./abstract/IERC1155.sol";

/**
 * @title Deed Tenant provisioning Contract for Deed NFT owners
 */
contract DeedTenantProvisioning is UUPSUpgradeable, Initializable, ManagerRole {
    using SafeMath for uint256;
    using Address for address;

    event DelegateeAdded(address indexed owner, uint256 nftId, address indexed manager, bool deployed);
    event DelegateeRemoved(address indexed owner, uint256 nftId, address indexed manager, bool deployed);

    event TenantStarted(address indexed manager, uint256 indexed nftId);
    event TenantStopped(address indexed manager, uint256 indexed nftId);

    IERC1155 public deed;

    // NFT ID => Status (Started = true)
    mapping(uint256 => bool) public tenantStatus;

    // NFT ID => Delegatee on behalf of NFT owner
    mapping(uint256 => address) public delegatees;

    /**
     * @dev Throws if called by any account other than the owner of the NFT.
     */
    modifier onlyProvisioningManager(uint256 _nftId) {
        require(isProvisioningManager(_msgSender(), _nftId), "DeedTenantProvisioning#onlyProvisioningOwner: Not provisioning owner of the NFT");
        _;
    }

    /**
     * @dev checks if the Deed Tenant is marked as stopped, else throw an error
     */
    modifier canStart(uint256 _nftId) {
        require(tenantStatus[_nftId] == false, "DeedTenantProvisioning#canStart: NFT is already started");
        _;
    }

    /**
     * @dev checks if the Deed Tenant is marked as started, else throw an error
     */
    modifier canStop(uint256 _nftId) {
        require(tenantStatus[_nftId] == true, "DeedTenantProvisioning#canStop: NFT is already stopped");
        _;
    }

    function initialize(IERC1155 _deed) initializer virtual public {
        deed = _deed;
        _transferOwnership(_msgSender());
    }

    /**
     * @dev Mark Deed Tenant as deployed
     */
    function startTenant(uint256 _nftId) external onlyProvisioningManager(_nftId) canStart(_nftId) {
        tenantStatus[_nftId] = true;
        emit TenantStarted(_msgSender(), _nftId);
    }

    /**
     * @dev Mark Deed Tenant as undeployed
     */
    function stopTenant(uint256 _nftId) external onlyProvisioningManager(_nftId) canStop(_nftId) {
        delete tenantStatus[_nftId];
        emit TenantStopped(_msgSender(), _nftId);
    }

    /**
     * @dev The Deed Owner can delegate Deed Tenant provisioning management to other wallet (renter, by example).
     * The call of this method can be done only through a contract designated as Manager of current contract
     * such as Renting Contract.
     */
    function setDelegatee(address _address, uint256 _nftId) external onlyManager returns(bool) {
        if (delegatees[_nftId] != address(0)) {
          _removeDelegatee(_nftId);
        }
        _setDelegatee(_address, _nftId);
        return true;
    }

    /**
     * @dev The Deed Owner can delete a previously approved delegatee of his Tenant.
     * This call can be done only through another contract such as Renting contract.
     */
    function removeDelegatee(uint256 _nftId) external onlyManager returns(bool) {
        _removeDelegatee(_nftId);
        return true;
    }

    /**
     * @dev returns true if the address can manage NFT Tenant provisioning (Start & Stop)
     */
    function isProvisioningManager(address _address, uint256 _nftId) public view returns(bool) {
        uint256 _balance = deed.balanceOf(_address, _nftId);
        if (_balance == 0) {
            // If this is not NFT owner, we will check if the _address is approved to manage provisioning
            return _address == delegatees[_nftId];
        } else {
            // If this is about owner, the provisioning shouldn't be delegated
            return address(0) == delegatees[_nftId];
        }
    }

    function _removeDelegatee(uint256 _nftId) internal virtual {
        address delegatee = delegatees[_nftId];
        delete delegatees[_nftId];
        emit DelegateeRemoved(_msgSender(), _nftId, delegatee, tenantStatus[_nftId]);
    }

    function _setDelegatee(address _address, uint256 _nftId) internal virtual {
        delegatees[_nftId] = _address;
        emit DelegateeAdded(_msgSender(), _nftId, _address, tenantStatus[_nftId]);
    }

    function _authorizeUpgrade(address newImplementation) internal view virtual override onlyOwner {}

}
