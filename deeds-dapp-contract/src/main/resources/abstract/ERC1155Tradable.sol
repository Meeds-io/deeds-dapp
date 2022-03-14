// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

import "./ERC1155.sol";
import "./Ownable.sol";
import "./SafeMath.sol";
import "./Address.sol";
import "./ProxyRegistry.sol";
import "./ERC1155MintBurn.sol";

/**
 * @title ERC1155Tradable
 * ERC1155Tradable - ERC1155 contract that whitelists an operator address, 
 * has create and mint functionality, and supports useful standards from OpenZeppelin,
 *   like _exists(), name(), symbol(), and totalSupply()
 */
abstract contract ERC1155Tradable is ERC1155MintBurn, Ownable {
    using SafeMath for uint256;
    using Address for address;

    // OpenSea proxy registry to ease selling NFTs on OpenSea
    address public proxyRegistryAddress;

    mapping(uint256 => address) public creators;
    mapping(uint256 => uint256) public tokenSupply;
    mapping(uint256 => uint256) public tokenMaxSupply;
    mapping(uint256 => uint8) public tokenCityIndex;
    mapping(uint256 => uint8) public tokenType;

    // Contract name
    string public name;

    // Contract symbol
    string public symbol;

    // URI's default URI prefix
    string internal baseMetadataURI;

    uint256 internal _currentTokenID = 0;

    constructor (string memory _name, string memory _symbol, address _proxyRegistryAddress, string memory _baseMetadataURI) {
        name = _name;
        symbol = _symbol;
        proxyRegistryAddress = _proxyRegistryAddress;
        baseMetadataURI = _baseMetadataURI;
    }

    /**
     * @dev Returns URIs are defined in RFC 3986.
     *      URIs are assumed to be deterministically generated based on token ID
     *      Token IDs are assumed to be represented in their hex format in URIs
     * @return URI string
     */
    function uri(uint256 _id) override public view returns (string memory) {
        return string(abi.encodePacked(baseMetadataURI, _uint2str(_id)));
    }

    /**
     * @dev Returns the total quantity for a token ID
     * @param _id uint256 ID of the token to query
     * @return amount of token in existence
     */
    function totalSupply(uint256 _id) public view returns (uint256) {
        return tokenSupply[_id];
    }

    /**
     * @dev Returns the max quantity for a token ID
     * @param _id uint256 ID of the token to query
     * @return amount of token in existence
     */
    function maxSupply(uint256 _id) public view returns (uint256) {
        return tokenMaxSupply[_id];
    }

    /**
     * @dev return city index of designated NFT with its identifier
     */
    function cityIndex(uint256 _id) public view returns (uint256) {
        return tokenCityIndex[_id];
    }

    /**
     * @dev return card type of designated NFT with its identifier
     */
    function cardType(uint256 _id) public view returns (uint256) {
        return tokenType[_id];
    }

    /**
     * @dev Creates a new token type and assigns _initialSupply to an address
     * @param _initialOwner the first owner of the Token
     * @param _initialSupply Optional amount to supply the first owner (1 for NFT)
     * @param _maxSupply max supply allowed (1 for NFT)
     * @param _cityIndex city index of NFT
     *    (0 = Tanit, 1 = Reshef, 2 = Ashtarte, 3 = Melqart, 4 = Eshmun, 5 = Kushor, 6 = Hammon)
     * @param _type card type of NFT
     *    (0 = Common, 1 = Uncommon, 2 = Rare, 3 = Legendary)
     * @param _data Optional data to pass if receiver is contract
     * @return The newly created token ID
     */
    function create(
        address _initialOwner,
        uint256 _initialSupply,
        uint256 _maxSupply,
        uint8 _cityIndex,
        uint8 _type,
        bytes memory _data
    ) public onlyOwner returns (uint256) {
        require(_initialSupply <= _maxSupply, "_initialSupply > _maxSupply");
        uint256 _id = _getNextTokenID();
        _incrementTokenTypeId();
        creators[_id] = _initialOwner;

        if (_initialSupply != 0) {
            _mint(_initialOwner, _id, _initialSupply, _data);
        }
        tokenSupply[_id] = _initialSupply;
        tokenMaxSupply[_id] = _maxSupply;
        tokenCityIndex[_id] = _cityIndex;
        tokenType[_id] = _type;
        return _id;
    }

    /**
     * @dev Override isApprovedForAll to whitelist user's OpenSea proxy accounts to enable gas-free listings.
     * @param _owner      The owner of the Tokens
     * @param _operator   Address of authorized operator
     * @return isOperator true if the operator is approved, false if not
     */
    function isApprovedForAll(address _owner, address _operator) override public view returns (bool isOperator) {
        // Whitelist OpenSea proxy contract for easy trading.
        ProxyRegistry proxyRegistry = ProxyRegistry(proxyRegistryAddress);
        if (address(proxyRegistry.proxies(_owner)) == _operator) {
            return true;
        }

        return _isApprovedForAll(_owner, _operator);
    }

    /**
     * @dev Returns whether the specified token exists by checking to see if it has a creator
     * @param _id uint256 ID of the token to query the existence of
     * @return bool whether the token exists
     */
    function _exists(uint256 _id) internal view returns (bool) {
        return creators[_id] != address(0);
    }

    /**
     * @dev calculates the next token ID based on value of _currentTokenID
     * @return uint256 for the next token ID
     */
    function _getNextTokenID() private view returns (uint256) {
        return _currentTokenID.add(1);
    }

    /**
     * @dev increments the value of _currentTokenID
     */
    function _incrementTokenTypeId() private {
        _currentTokenID++;
    }

    /**
     * @dev Convert uint256 to string
     * @param _i Unsigned integer to convert to string
     */
    function _uint2str(uint256 _i) internal pure returns (string memory _uintAsString) {
        if (_i == 0) {
            return "0";
        }
        uint256 j = _i;
        uint256 ii = _i;
        uint256 len;
        // Get number of bytes
        while (j != 0) {
            len++;
            j /= 10;
        }
        bytes memory bstr = new bytes(len);
        uint256 k = len - 1;
        // Get each individual ASCII
        while (ii != 0) {
            bstr[k--] = bytes1(uint8(48 + (ii % 10)));
            ii /= 10;
        }
        // Convert to string
        return string(bstr);
    }

}