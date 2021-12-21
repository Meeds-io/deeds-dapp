// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.0;

import "./abstract/Ownable.sol";
import "./abstract/SafeMath.sol";
import "./abstract/ERC1155Tradable.sol";

/**
 * @title NFT Contract for Meeds DAO
 */
contract MeedsNFT is ERC1155Tradable {

    constructor (address _proxyRegistryAddress) ERC1155Tradable("Meeds DAO NFT", "DEED", _proxyRegistryAddress) {
    }

    function nftsOf(address _account) public view returns (uint256[] memory) {
        uint256 len = 0;
        for (uint256 i = 1; i <= _currentTokenID; i++) {
            if (balances[_account][i] > 0) {
              len++;
            }
        }
        uint256 index = 0;
        uint256[] memory nfts = new uint256[](len);
        for (uint256 i = 1; i <= _currentTokenID; i++) {
            if (balances[_account][i] > 0) {
              nfts[index++] = i;
            }
        }
        return nfts;
    }

    function totalSupply() public view returns (uint256) {
        return _currentTokenID;
    }

}
