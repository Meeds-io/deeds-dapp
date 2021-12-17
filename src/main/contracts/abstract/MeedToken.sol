// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.0;

import "./Ownable.sol";
import "./ERC20.sol";

contract MeedToken is ERC20("Stake Dao Token", "SDT"), Ownable {
    /// @notice Creates `_amount` token to `_to`. Must only be called by the owner (TokenFactory).
    function mint(address _to, uint256 _amount) public onlyOwner {
        _mint(_to, _amount);
    }
}
