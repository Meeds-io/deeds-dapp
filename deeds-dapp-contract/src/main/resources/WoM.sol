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
        address hub; // Hub Address
    }

    /**
     * @dev The Hub properties
     */
    struct Hub {
        uint256 deedId; // Deed NFT identifier
        address owner; // Hub owner
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
    function connect(Hub memory _hub, Deed memory _deed) public onlyManager {
        address _hubAddress = _deed.hub;
        require(_hubAddress != address(0), "wom.hubAddressIsMandatory");
        require(_hub.owner != address(0), "wom.hubOwnerIsMandatory");
        require(_hub.deedId >= 0, "wom.deedIdIsMandatory");

        if (hubs[_hubAddress].owner != address(0)) {
            _disconnect(_hubAddress);
        }

        hubs[_hubAddress] = _hub;
        nfts[_hub.deedId] = _deed;

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
        _disconnect(_hubAddress);

        emit HubDisconnected(_hubAddress, _deedId);
    }

    function _disconnect(address _hubAddress) internal virtual onlyManager {
        nfts[hubs[_hubAddress].deedId].hub = address(0);
        delete hubs[_hubAddress];
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