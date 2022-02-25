// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

import "./Ownable.sol";
import "./ERC20.sol";

contract MeedsToken is ERC20("Meeds Token", "MEED"), Ownable {
    /// @notice Creates `_amount` token to `_to`. Must only be called by the owner (TokenFactory).
    function mint(address _to, uint256 _amount) public onlyOwner {
        _mint(_to, _amount);
    }
}
