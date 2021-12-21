// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.0;

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
contract ERC1155Tradable is ERC1155MintBurn, Ownable {
    using SafeMath for uint256;
    using Address for address;

    address proxyRegistryAddress;
    uint256 internal _currentTokenID = 0;
    mapping(uint256 => string) public tokenUri;
    mapping(uint256 => address) public creators;
    mapping(uint256 => uint256) public tokenSupply;
    mapping(uint256 => uint256) public tokenMaxSupply;
    mapping(uint256 => uint8) public tokenCityIndex;
    mapping(uint256 => uint8) public tokenType;

    // Contract name
    string public name;

    // Contract symbol
    string public symbol;

    constructor (string memory _name, string memory _symbol, address _proxyRegistryAddress) {
        name = _name;
        symbol = _symbol;
        proxyRegistryAddress = _proxyRegistryAddress;
    }

    function uri(uint256 _id) override public view returns (string memory) {
        require(_exists(_id), "ERC721Tradable#uri: NONEXISTENT_TOKEN");
        // We have to convert string to bytes to check for existence
        bytes memory customUriBytes = bytes(tokenUri[_id]);
        if (customUriBytes.length > 0) {
            return tokenUri[_id];
        } else {
            return "";
        }
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

    function cityIndex(uint256 _id) public view returns (uint256) {
        return tokenCityIndex[_id];
    }

    function cardType(uint256 _id) public view returns (uint256) {
        return tokenType[_id];
    }

    /**
     * @dev Creates a new token type and assigns _initialSupply to an address
     * @param _maxSupply max supply allowed
     * @param _initialSupply Optional amount to supply the first owner
     * @param _uri Optional URI for this token type
     * @param _data Optional data to pass if receiver is contract
     * @return The newly created token ID
     */
    function create(
        address _initialOwner,
        uint256 _initialSupply,
        uint256 _maxSupply,
        string memory _uri,
        uint8 _cityIndex,
        uint8 _type,
        bytes memory _data
    ) public onlyOwner returns (uint256) {
        require(_initialSupply <= _maxSupply, "_initialSupply > _maxSupply");
        uint256 _id = _getNextTokenID();
        _incrementTokenTypeId();
        creators[_id] = _initialOwner;

        if (bytes(_uri).length > 0) {
            tokenUri[_id] = _uri;
            emit URI(_uri, _id);
        }

        if (_initialSupply != 0) _mint(_initialOwner, _id, _initialSupply, _data);
        tokenSupply[_id] = _initialSupply;
        tokenMaxSupply[_id] = _maxSupply;
        tokenCityIndex[_id] = _cityIndex;
        tokenType[_id] = _type;
        return _id;
    }

    /**
     * Override isApprovedForAll to whitelist user's OpenSea proxy accounts to enable gas-free listings.
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
}