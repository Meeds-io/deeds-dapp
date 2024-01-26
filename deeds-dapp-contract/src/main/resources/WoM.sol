// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

import "./abstract/Initializable.sol";
import "./abstract/ManagerRole.sol";
import "./abstract/UUPSUpgradeable.sol";
import "./struct/Deed.sol";
import "./struct/Hub.sol";

/**
 * @title User Engagement Minting Contract to rewards Meeds DAO Deed Hub communities switch their engagement score
 */
contract WoM is UUPSUpgradeable, Initializable, ManagerRole {

    /**
     * @dev An event that will be triggered when a Hub is connected to the WoM
     */
    event HubConnected(address indexed hub, uint256 indexed nftId, address hubOwner);

    /**
     * @dev An event that will be triggered when a Hub is disconnected from the WoM
     */
    event HubDisconnected(address indexed hub, uint256 indexed nftId);

    /**
     * @dev An event that will be triggered when the Deed characteristics are updated
     */
    event DeedUpdated(uint256 indexed nftId);

    /**
     * @dev An event that will be triggered when the Hub ownership is transferred
     */
    event HubOwnershipTransferred(address indexed previousOwner, address indexed newOwner);

    /**
     * @dev a mapping of deeds identified by its NFT Id
     */
    mapping(uint256 => Deed) public nfts;

    /**
     * @dev a mapping of connected Hubs identified by its address
     */
    mapping(address => Hub) public hubs;

    modifier onlyHubOwner(address hubAddress) {
        require(_msgSender() == hubs[hubAddress].owner, "wom.onlyHubOwnerIsAllowed");
        _;
    }

    modifier onlyHubOwnerOrNewHubDeedManager(address hubAddress, uint256 nftId) {
        require(
            _msgSender() == hubs[hubAddress].owner
            || (hubs[hubAddress].owner == address(0) // First time connection, thus must be the Deed tenant which may be the Deed owner
                && _msgSender() == nfts[nftId].tenant
            ), "wom.onlyHubOwnerOrNewHubDeedManagerIsAllowed"
        );
        _;
    }

    /**
     * This method replaces the constructor since this is about an Upgradable Contract
     */
    function initialize()
        public
        initializer {
        _transferOwnership(_msgSender());
        _addManager(_msgSender());
    }

    /**
     * @dev returns a Hub switch its address
     */
    function getHub(address _hubAddress)
        external
        virtual
        view
        returns (Hub memory) {
        return hubs[_hubAddress];
    }

    /**
     * @dev returns a Deed switch its id
     */
    function getDeed(uint256 _deedId)
        external
        virtual
        view
        returns (Deed memory) {
        return nfts[_deedId];
    }

    /**
     * @dev returns the connected Hub address to the given Deed
     */
    function getConnectedHub(uint256 _deedId)
        external
        virtual
        view
        returns (address) {
        return isDeedConnected(_deedId) ? nfts[_deedId].hub : address(0);
    }

    /**
     * @dev returns the connected Deed NFT to the given Hub address
     */
    function getConnectedDeed(address _hubAddress)
        external
        virtual
        view
        returns (uint256) {
        return isHubConnected(_hubAddress) ? hubs[_hubAddress].deedId : 0;
    }

    /**
     * @dev returns true if the hub is connected to the WoM
     */
    function isHubConnected(address _hubAddress)
        public
        virtual
        view
        returns (bool) {
        Hub memory hub = hubs[_hubAddress];
        Deed memory deed = nfts[hub.deedId];
        return hub.enabled
          && deed.hub == _hubAddress // Check whether the Deed is always assigned to the Hub
          && (hub.owner == deed.owner
              || hub.owner == deed.tenant); // If the Hub was firstly created by Tenant,
                                            // disconnect automatically when changed
    }

    /**
     * @dev returns true if the Deed is connected to the WoM
     */
    function isDeedConnected(uint256 _deedId)
        public
        virtual
        view
        returns (bool) {
        Deed memory deed = nfts[_deedId];
        Hub memory hub = hubs[deed.hub];
        return hub.enabled
          && hub.deedId == _deedId // Check whether the Deed is always assigned to the Hub
          && (hub.owner == deed.owner
              || hub.owner == deed.tenant); // If the Hub was firstly created by Tenant,
                                            // disconnect automatically when changed
    }

    /**
     * @dev returns the first time the hub joined the WoM
     */
    function getHubJoinDate(address _hubAddress)
        external
        virtual
        view
        returns (uint256) {
        return hubs[_hubAddress].joinDate;
    }

    /**
     * @dev Connect a Hub to the WoM.
     *      Can be called only by Hub owner.
     */
    function connect(address _hubAddress, uint256 _deedId)
        external
        virtual
        onlyHubOwnerOrNewHubDeedManager(_hubAddress, _deedId) // Only Hub Owner or Deed Tenant/Owner making th connection the first time
    {
        require(_hubAddress != address(0), "wom.hubAddressIsMandatory");
        require(_deedId > 0, "wom.deedIdIsMandatory");

        _connect(_hubAddress, _deedId);
        emit HubConnected(_hubAddress, _deedId, _msgSender());
    }

    /**
     * @dev Disconnect a Hub from the WoM.
     *      Can be called only by delegated manager(s)
     *      chosen by the contract owner.
     */
    function disconnect(address _hubAddress)
      external
      virtual
      onlyHubOwner(_hubAddress) // Only Hub Owner is allowed
    {
        Hub memory hub = hubs[_hubAddress];
        Deed memory deed = nfts[hub.deedId];
        require(_hubAddress != address(0), "wom.hubAddressIsMandatory");
        require(hub.enabled, "wom.hubAlreadyDisconnected");
        require(hub.deedId > 0, "wom.deedIdDisconnected");
        require(deed.hub == _hubAddress, "wom.deedNotConnectedToHub");
        require(deed.tenant != address(0), "wom.deedNotFound");

        // Disconnect
        _disconnect(_hubAddress, hub.deedId);

        emit HubDisconnected(_hubAddress, hub.deedId);
    }

    /**
     * @dev Transfer Hub Ownership.
     *      Can be called only by the Hub Owner
     */
    function transferHubOwnership(address _hubAddress, address _owner)
      external
      virtual
      onlyHubOwner(_hubAddress) // Only Hub Owner is allowed
    {
        require(_hubAddress != address(0), "wom.hubAddressIsMandatory");
        require(_owner != address(0), "wom.newHubOwnerIsMandatory");
        require(hubs[_hubAddress].owner != _owner, "wom.alreadyOwner");

        address _previousOwner = hubs[_hubAddress].owner;
        // Transfer ownership
        Hub storage hub = hubs[_hubAddress];
        hub.owner = _owner;

        emit HubOwnershipTransferred(_previousOwner, _owner);
    }

    /**
     * @dev Updates Deed NFT characteristics bridged from Etheurem Blockchain
     *      Can be called only by delegated manager(s).
     */
    function updateDeed(uint256 _deedId, Deed memory _deed)
      external
      virtual
      onlyManager // Only WoM Manager can bridge Deed from Ethereum
    {
        require(_deedId > 0, "wom.deedIdMustBePositive");
        require(_deed.owner != address(0), "wom.deedOwnerIsMandatory");
        require(_deed.tenant != address(0), "wom.deedTenantIsMandatory");

        Deed storage deed = nfts[_deedId];
        deed.city = _deed.city;
        deed.cardType = _deed.cardType;
        deed.mintingPower = _deed.mintingPower;
        deed.maxUsers = _deed.maxUsers;
        deed.owner = _deed.owner;
        deed.tenant = _deed.tenant;
        deed.ownerPercentage = _deed.ownerPercentage;
        deed.tenantPercentage = 100 - _deed.ownerPercentage;

        // In case WoM Manager hosts some Hubs, it should automatically connect to the WoM
        // once Deed Manager/Owner movedIn (requested to Start a Hub) on **Ethereum Blockchain**
        // using Deed Provisioning Management Contract
        if (_deed.hub != address(0)
            // && Hub Owner is compatible with Deed Owner or Tenant
            // Else Hub Ownership has to be explicitely transferred from original Hub Owner
            && (hubs[_deed.hub].owner == address(0) // First connection
              || hubs[_deed.hub].owner == _deed.owner // Hub Owner == Deed Owner
              || hubs[_deed.hub].owner == _deed.tenant) // Hub Owner == Deed Tenant
        ) {
            _connect(_deed.hub, _deedId);
            emit HubConnected(_deed.hub, _deedId, _deed.tenant);
        }

        emit DeedUpdated(_deedId);
    }

    function _connect(address _hubAddress, uint256 _deedId)
        internal
    {
        Deed storage deed = nfts[_deedId];
        require(deed.tenant != address(0), "wom.deedNotFound");

        address previousHubAddress = deed.hub;
        Hub storage hub = hubs[_hubAddress];
        Hub memory previousHub = hubs[previousHubAddress];
        if (previousHub.enabled // Previous Hub always connected
            && previousHubAddress != _hubAddress // And not about current Hub
            && previousHub.deedId == _deedId // And the previous Hub was connected to the current Deed
        ) {
            // Auto disconnect previously connected Hub to the Deed
            _disconnect(previousHubAddress, _deedId);
            emit HubDisconnected(previousHubAddress, _deedId);
        }

        uint256 previousDeedId = hub.deedId;
        Deed memory previousDeed = nfts[previousDeedId];
        if (hub.enabled // Current Hub already connected
            && previousDeedId != _deedId // And a new Deed NFT will be associated
            && previousDeed.hub == _hubAddress // And the previous Deed was connected to the current Hub
        ) {
            // Auto disconnect previously connected Deed to the Hub
            _disconnect(_hubAddress, previousDeedId);
            emit HubDisconnected(_hubAddress, previousDeedId);
        }

        // Connect
        if (hub.owner == address(0)) {
            // First time the Hub is connected to the WoM
            // Hub owner id the current Deed Provisioning
            // Manager Address
            hub.owner = deed.tenant;
            hub.joinDate = block.timestamp;
        }
        deed.hub = _hubAddress;
        hub.deedId = _deedId;
        hub.enabled = true;
    }

    function _disconnect(address _hubAddress, uint256 _deedId) internal {
        Deed storage deed = nfts[_deedId];
        deed.hub = address(0);
        Hub storage hub = hubs[_hubAddress];
        hub.enabled = false;
    }

    function _authorizeUpgrade(
        address newImplementation
    )
        internal
        view
        virtual
        override
        onlyOwner {}

}