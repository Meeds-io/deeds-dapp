// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

import "./abstract/Initializable.sol";
import "./abstract/ManagerRole.sol";
import "./abstract/UUPSUpgradeable.sol";

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
     * @dev The Deed NFT Structure as defined in 0x0143b71443650aa8efa76bd82f35c22ebd558090 on ethereum
     */
    struct Deed {
        // Deed city: Tanit, Reshef, Ashtarte, Melqart, Eshmun, Kushor or Hammon
        uint8 city;
        // Deed type: COMMON, UNCOMMON, RARE or LEGENDARY
        uint8 cardType;
        // Deed Minting Power: COMMON = 1.0 = 10, UNCOMMON = 1.1 = 11, RARE = 1.3 = 13 or LEGENDARY = 2.0 = 20
        uint8 mintingPower;
        // Deed Hub Max users = COMMON = 100, UNCOMMON = 1000, RARE = 10000 or LEGENDARY = unlimited
        uint256 maxUsers;
        // Deed NFT owner
        address owner;
        // Deed NFT tenant, can be the same as the Deed NFT owner when no lease
        address tenant;
        // Hub using the Deed
        address hub;
        // UEM rewarding share percentage for the Deed Owner comparing to the Deed Tenant
        uint8 ownerPercentage;
    }

    /**
     * @dev The Hub properties
     */
    struct Hub {
        // Connected or last previously connected Deed NFT identifier
        uint256 deedId;
        // Hub owner
        address owner;
        // Hub is connected or disconnected
        bool enabled;
    }

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
        require(_hubAddress != address(0), "wom.hubAddressIsMandatory");
        require(hubs[_hubAddress].enabled, "wom.alreadyDisconnected");

        Hub memory hub = hubs[_hubAddress];
        require(hub.enabled, "wom.hubAlreadyDisconnected");

        uint256 deedId = hub.deedId;
        require(deedId > 0, "wom.deedIdDisconnected");

        Deed memory deed = nfts[deedId];
        require(deed.tenant != address(0), "wom.deedNotFound");

        // Disconnect
        _disconnect(_hubAddress, deedId);

        emit HubDisconnected(_hubAddress, deedId);
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

        // In case WoM Manager hosts some Hubs, it should automatically connect to the WoM
        // once Deed Manager/Owner moved in on **Ethereum Blockchain** Deed Provisioning Management Contract
        if (deed.hub != address(0)) {
            _connect(deed.hub, _deedId);
            emit HubConnected(deed.hub, _deedId, deed.tenant);
        }

        emit DeedUpdated(_deedId);
    }

    function _connect(address _hubAddress, uint256 _deedId)
        internal
    {
        Deed storage deed = nfts[_deedId];
        require(deed.tenant != address(0), "wom.deedNotFound");

        Hub storage hub = hubs[_hubAddress];
        Hub memory previousHub = hubs[deed.hub];
        if (previousHub.enabled // Previous Hub always connected
            && deed.hub != _hubAddress // And not about current Hub
            && previousHub.deedId == _deedId // And the previous Hub was connected to the current Deed
        ) {
            // Auto disconnect previously connected Hub to the Deed
            _disconnect(deed.hub, _deedId);
            emit HubDisconnected(deed.hub, _deedId);
        }

        Deed memory previousDeed = nfts[hub.deedId];
        if (hub.enabled // Current Hub already connected
            && hub.deedId != _deedId // And a new Deed NFT will be associated
            && previousDeed.hub == _hubAddress // And the previous Deed was connected to the current Hub
        ) {
            // Auto disconnect previously connected Deed to the Hub
            _disconnect(_hubAddress, hub.deedId);
            emit HubDisconnected(_hubAddress, hub.deedId);
        }

        // Connect
        if (hub.owner == address(0)) {
            // First time the Hub is connected to the WoM
            hub.owner = deed.tenant;
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