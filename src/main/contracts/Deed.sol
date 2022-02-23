// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.11;

import "./abstract/SafeMath.sol";
import "./abstract/Ownable.sol";
import "./abstract/StrategyRole.sol";
import "./abstract/ERC1155Tradable.sol";

/**
 * @title NFT Contract for Meeds DAO
 */
contract Deed is ERC1155Tradable, StrategyRole {

    using SafeMath for uint256;

    event StartedUsingNFT(address indexed account,
        uint256 indexed id,
        address indexed strategy);
    event EndedUsingNFT(address indexed account,
        uint256 indexed id,
        address indexed strategy);

    // mapping account => nftId => useCount
    // this is used to restrict transfers if nft is being used in any strategy
    mapping(address => mapping(uint256 => uint256)) internal totalUseCount;

    // mapping account => nftId => strategyAddress => useCount
    // this is used to make sure a strategy can only end using nft that it started using before
    mapping(address => mapping(uint256 => mapping(address => uint256)))
        internal stratUseCount;

    constructor (address _proxyRegistryAddress) ERC1155Tradable("Meeds Deed Token", "DEED", _proxyRegistryAddress) {
    }

    /**
     * @dev return the list of NFTs owned by an address
     */
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

    /**
     * @dev return the total supply of NFTs (Token Types)
     */
    function totalSupply() public view returns (uint256) {
        return _currentTokenID;
    }

		/**
     * @dev return the total use count of an NFT by owner
     */
    function getTotalUseCount(address _account, uint256 _id) public view returns (uint256) {
        return totalUseCount[_account][_id];
    }

    function getStratUseCount(address _account, uint256 _id, address _strategy) public view returns (uint256) {
        return stratUseCount[_account][_id][_strategy];
    }

    /**
     * @notice Mark NFT as being used. Only callable by registered strategies
     * @param _account  User account address
     * @param _id       ID of the token type
     */
    function startUsingNFT(address _account, uint256 _id) external onlyStrategy returns (bool) {
        require(balances[_account][_id] > 0, "Deed#startUsingNFT: user account doesn't own the NFT");
        stratUseCount[_account][_id][msg.sender] = stratUseCount[_account][_id][msg.sender].add(1);
        totalUseCount[_account][_id] = totalUseCount[_account][_id].add(1);
        emit StartedUsingNFT(_account, _id, msg.sender);
        return true;
    }

    /**
     * @notice Unmark NFT as being used. Only callable by registered strategies
     * @param _account  User account address
     * @param _id       ID of the token type
     */
    function endUsingNFT(address _account, uint256 _id) external onlyStrategy returns (bool) {
        require(stratUseCount[_account][_id][msg.sender] > 0, "Deed#endUsingNFT: NFT is not currently in use by strategy");
        stratUseCount[_account][_id][msg.sender] = stratUseCount[_account][_id][msg.sender].sub(1);
        totalUseCount[_account][_id] = totalUseCount[_account][_id].sub(1);
        emit EndedUsingNFT(_account, _id, msg.sender);
        return true;
    }

    /**
     * @dev Overrides safeTransferFrom function of ERC1155 to introduce totalUseCount check
     */
    function safeTransferFrom(address _from, address _to, uint256 _id, uint256 _amount, bytes memory _data) public override {
        require(totalUseCount[_from][_id] == 0, "Deed#safeTransferFrom: NFT being used in strategy");
        ERC1155.safeTransferFrom(_from, _to, _id, _amount, _data);
    }

    /**
     * @dev Overrides safeBatchTransferFrom function of ERC1155 to introduce totalUseCount check
     */
    function safeBatchTransferFrom(address _from, address _to, uint256[] memory _ids, uint256[] memory _amounts, bytes memory _data) public  override {
        // Number of transfer to execute
        uint256 nTransfer = _ids.length;

        // check if any nft is being used
        for (uint256 i = 0; i < nTransfer; i++) {
            require(totalUseCount[_from][_ids[i]] == 0, "Deed#safeBatchTransferFrom: NFT being used in strategy");
        }

        ERC1155.safeBatchTransferFrom(_from, _to, _ids, _amounts, _data);
    }

}
