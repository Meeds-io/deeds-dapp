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
        uint8 city; // Deed city: Tanit, Reshef, Ashtarte, Melqart, Eshmun, Kushor or Hammon
        uint8 cardType; // Deed type: COMMON, UNCOMMON, RARE or LEGENDARY
        uint8 mintingPower; // Deed Minting Power: COMMON = 1.0 = 10, UNCOMMON = 1.1 = 11, RARE = 1.3 = 13 or LEGENDARY = 2.0 = 20
        uint256 maxUsers; // Deed Hub Max users = COMMON = 100, UNCOMMON = 1000, RARE = 10000 or LEGENDARY = unlimited
        address owner; // Deed NFT owner
        address tenant; // Deed NFT tenant
        uint8 ownerPercentage; // Deed NFT owner UEM rewarding share percentage
    }

    /**
     * @dev The Hub properties
     */
    struct Hub {
        uint256 deedId; // Deed NFT identifier
        address owner; // Hub owner
        bool enabled; // Hub is connected or disconnected
    }

    /**
     * @dev a mapping of deeds identified by its NFT Id
     */
    mapping(uint256 => Deed) public nfts;

    /**
     * @dev a mapping of connected Hubs identified by its address
     */
    mapping(address => Hub) public hubs;

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
     * @dev Connect a Hub to the WoM.
     *      Can be called only by delegated manager(s)
     *      chosen by the contract owner.
     */
    function connect(address _hubAddress, Hub memory _hub) public onlyManager {
        require(_hubAddress != address(0), "wom.hubAddressIsMandatory");
        require(_hub.owner != address(0), "wom.hubOwnerIsMandatory");
        require(_hub.deedId >= 0, "wom.deedIdIsMandatory");
        require(_hub.enabled, "wom.hubConnectionMustBeEnabled");

        if (hubs[_hubAddress].owner == address(0)) {
          // First time connection
          require(hubs[_hubAddress].owner == nfts[_hub.deedId].owner || hubs[_hubAddress].owner == nfts[_hub.deedId].tenant, "wom.hubOwnerNotDeedManager");
        } else {
          // Already connected once
          require(hubs[_hubAddress].owner == _hub.owner, "wom.notHubOwner");
        }

        // Connect
        hubs[_hubAddress] = _hub;

        emit HubConnected(_hubAddress, _hub.deedId, _hub.owner);
    }

    /**
     * @dev Disconnect a Hub from the WoM.
     *      Can be called only by delegated manager(s)
     *      chosen by the contract owner.
     */
    function disconnect(address _hubAddress) public onlyManager {
        require(_hubAddress != address(0), "wom.hubAddressIsMandatory");
        require(hubs[_hubAddress].owner != address(0), "wom.hubNotFound");

        uint256 _deedId = hubs[_hubAddress].deedId;

        // Disconnect
        hubs[_hubAddress].enabled = false;

        emit HubDisconnected(_hubAddress, _deedId);
    }

    /**
     * @dev Transfer Hub Ownership.
     *      Can be called only by:
     *      - the Hub Owner
     *      Or
     *      - a delegated manager(s) chosen by the contract owner.
     */
    function transferHubOwnership(address _hubAddress, address _owner) public {
        require(_hubAddress != address(0), "wom.hubAddressIsMandatory");
        require(_owner != address(0), "wom.newHubOwnerIsMandatory");
        require(hubs[_hubAddress].owner != _owner, "wom.alreadyOwner");
        require(isManager(_msgSender()) || hubs[_hubAddress].owner == _msgSender(), "wom.onlyWoMManagerOrHubOwnerAreAllowed");

        address _previousOwner = hubs[_hubAddress].owner;
        // Transfer ownership
        hubs[_hubAddress].owner = _owner;

        emit HubOwnershipTransferred(_previousOwner, _owner);
    }

    /**
     * @dev Updates Deed NFT characteristics.
     *      Can be called only by delegated manager(s)
     *      chosen by the contract owner.
     */
    function updateDeed(uint256 _nftId, Deed memory _deed) public onlyManager {
        require(_nftId >= 0, "wom.deedIdMustBePositive");
        require(_deed.city >= 0, "wom.deedCityMustBePositive");
        require(_deed.cardType >= 0, "wom.deedTypeMustBePositive");
        require(_deed.mintingPower >= 0, "wom.deedMintingPowerMustBePositive");
        require(_deed.maxUsers >= 0, "wom.deedMaxUsersMustBePositive");
        require(_deed.owner != address(0), "wom.deedOwnerIsMandatory");

        nfts[_nftId] = _deed;

        emit DeedUpdated(_nftId);
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