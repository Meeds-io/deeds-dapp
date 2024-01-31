// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

import "../../main/resources/WoM.sol";

contract TestWoM is WoM {

    function setJoinDate(address _hubAddress, uint256 _joinDate) public {
        hubs[_hubAddress].joinDate = _joinDate;
    }

    /**
     * dev Test Embedded EVM uses Milliseconds, thus to make it compatible
     *     add a generic check to produce the value of block.timestamp
     *     to be always in seconds
     */
    function _blocktimeInSeconds()
        internal
        view
        override
        returns(uint256) {
        return block.timestamp < 170676093000 ? block.timestamp : (block.timestamp / 1000);
    }

}


