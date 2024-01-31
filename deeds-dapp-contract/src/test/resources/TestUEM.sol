// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

import "../../main/resources/UserEngagementMinting.sol";

contract TestUEM is UserEngagementMinting {

    using SafeMath for uint256;

    uint256 testPreviousPeriodInWeeks = 0;

    function setTestOnPreviousPeriod(uint256 _testPreviousPeriodInWeeks) public {
        testPreviousPeriodInWeeks = _testPreviousPeriodInWeeks;
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
        uint256 currentTimestamp = block.timestamp < 170676093000 ? block.timestamp : (block.timestamp / 1000);
        return testPreviousPeriodInWeeks > 0 ?
            currentTimestamp.sub(REWARD_PERIOD_IN_SECONDS.mul(testPreviousPeriodInWeeks))
          : currentTimestamp;
    }

}


