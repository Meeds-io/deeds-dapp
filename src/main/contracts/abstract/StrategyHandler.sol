// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

import "./SafeMath.sol";
import "./StrategyRole.sol";
import './IERC1155.sol';

/**
 * @title NFT Contract for Meeds DAO
 */
interface StrategyHandler is IERC1155 {

		/**
     * @dev return the total use count of an NFT by a wallet
     */
    function getTotalUseCount(address _account, uint256 _id) external view returns (uint256);

    /**
     * @dev return the use count of an NFT by wallet inside a strategy
     */
    function getStrategyUseCount(address _account, uint256 _id, address _strategy) external view returns (uint256);

    /**
     * @notice Mark NFT as being used. Only callable by registered strategies
     * @param _account  User account address
     * @param _id       ID of the token type
     */
    function startUsingNFT(address _account, uint256 _id) external returns (bool);

    /**
     * @notice Unmark NFT as being used. Only callable by registered strategies
     * @param _account  User account address
     * @param _id       ID of the token type
     */
    function endUsingNFT(address _account, uint256 _id) external returns (bool);

}
