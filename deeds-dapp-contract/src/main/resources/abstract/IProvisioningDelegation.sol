// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

/**
 * @dev Interface of DeedProvisioning contract Delegation methods.
 */
interface IProvisioningDelegation {

  function setDelegatee(address _address, uint256 _nftId) external returns(bool);

  function getDelegatee(uint256 _nftId) view external returns(address);

}


