// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

import "./abstract/Initializable.sol";
import "./abstract/UUPSUpgradeable.sol";
import "./abstract/SafeMath.sol";
import "./abstract/Context.sol";
import "./abstract/Ownable.sol";
import "./abstract/IERC1155.sol";
import "./abstract/IERC20.sol";
import "./abstract/ProvisioningManager.sol";
import "./DeedTenantProvisioning.sol";

/**
 * @title Deed Renting Contract
 */
contract DeedRenting is UUPSUpgradeable, Initializable, ProvisioningManager, Context, Ownable {
    using SafeMath for uint256;
    using Address for address;

    uint256 public constant MONTH_IN_SECONDS = 2629800;

    uint256 public constant DAY_IN_SECONDS = 1 days;

    struct DeedLease {
        uint256 id; // Unique identifier for a Lease And the same used for Offer
        uint256 deedId;
        uint16 paidMonths; // Number of Paid months
        uint256 paidRentsDate; // Paid Rents End Date
        uint256 noticePeriodDate; // End Date of notice Period by considering paid rents
        uint256 leaseStartDate; // Lease Effective Start/Acquired Date
        uint256 leaseEndDate; // Lease Effective End Date
        address tenant; // Tenant Address who acquired the offer
    }

    struct DeedOffer {
        uint256 id; // Unique identifier for a Lease And Offer
        uint256 deedId;
        address creator; // Original Renter/Offer Creator
        uint16 months; // Number of months to rent
        uint8 noticePeriod; // Number of months of Notice Period
        uint256 price; // Monthly Rent Amount
        uint256 allDurationPrice; // All Rental Duration Price For Discount If proposed
        uint256 offerStartDate; // Offer Start Date
        uint256 offerExpirationDate; // Offer Expiration Date
        uint8 offerExpirationDays; // Offer Expiration Date
        address authorizedTenant; // Tenant Address who is authorized to acquire the offer
        uint8 ownerMintingPercentage; // Owner Minting Percentage
    }

    event OfferCreated(
        uint256 indexed id,
        uint256 indexed deedId,
        address owner
    );

    event OfferUpdated(
        uint256 indexed id,
        uint256 indexed deedId,
        address owner
    );

    event OfferDeleted(
        uint256 indexed id,
        uint256 indexed deedId,
        address owner
    );

    event RentPaid(
        uint256 indexed id,
        uint256 indexed deedId,
        address tenant,
        address owner,
        uint16 paidMonths,
        bool firstRent
    );

    event TenantEvicted(
        uint256 indexed id,
        uint256 indexed deedId,
        address tenant,
        address owner,
        uint16 leaseRemainingMonths
    );

    event LeaseEnded(
        uint256 indexed id,
        uint256 indexed deedId,
        address tenant,
        uint16 leaseRemainingMonths
    );

    IERC20 public meed;

    IERC1155 public deed;

    DeedTenantProvisioning public tenantProvisioning;

    uint256 public offersCount;

    // Lease/Offer ID => DeedLease
    mapping(uint256 => DeedOffer) public deedOffers;

    // Lease/Offer ID => DeedLease
    mapping(uint256 => DeedLease) public deedLeases;

    // Deed ID => List of lease IDs
    mapping(uint256 => uint256[]) public leases;

    /**
     * @dev Throws if called by any account other than the owner of the NFT.
     */
    modifier onlyDeedOwner(uint256 _deedId) {
        require(deed.balanceOf(_msgSender(), _deedId) > 0, "DeedRenting#NotOwnerOfDeed");
        _;
    }

    /**
     * @dev Throws if called by any account other than the owner of the NFT
     * identified using Lease ID.
     */
    modifier onlyDeedOwnerByLeaseId(uint256 _offerId) {
        require(deed.balanceOf(_msgSender(), deedOffers[_offerId].deedId) > 0, "DeedRenting#NotOwnerOfDeed");
        _;
    }

    /**
     * @dev Throws if called by any account other than the owner of the NFT.
     */
    modifier isReceiverDeedOwnerByLeaseId(uint256 _offerId, address _deedOwner) {
        require(deed.balanceOf(_deedOwner, deedOffers[_offerId].deedId) > 0, "DeedRenting#ReceiverNotOwnerOfDeed");
        _;
    }

    /**
     * @dev Throws if called by any account other than the owner of the NFT.
     */
    modifier notDeedOwnerByLeaseId(uint256 _offerId) {
        require(deed.balanceOf(_msgSender(), deedOffers[_offerId].deedId) == 0, "DeedRenting#DeedOwnerCantAcquireHisOwnOffer");
        _;
    }

    /**
     * @dev Throws when offer id isn't currently assigned to the NFT Id
     */
    modifier isDeedOffer(uint256 _offerId, uint256 _deedId) {
        require(_deedId == deedOffers[_offerId].deedId, "DeedRenting#NotAdequateDeedForOffer");
        _;
    }

    /**
     * @dev Throws if offer creator isn't the NFT owner anymore.
     */
    modifier isOfferCreatorDeedOwner(uint256 _offerId) {
        require(deed.balanceOf(deedOffers[_offerId].creator, deedOffers[_offerId].deedId) > 0, "DeedRenting#DeedOwnerChangedThusInvalidOffer");
        _;
    }

    /**
     * @dev Throws if called by any account other than the owner of the NFT
     * identified using Lease ID.
     */
    modifier onlyOfferCreator(uint256 _offerId) {
        require(deedOffers[_offerId].creator == _msgSender(), "DeedRenting#NotOfferCreator");
        _;
    }

    /**
     * @dev Throws if lease offer has been already acquired.
     */
    modifier notAcquiredOffer(uint256 _offerId) {
        require(deedLeases[_offerId].leaseStartDate == 0, "DeedRenting#OfferAlreadyAcquiredByTenant");
        _;
    }

    /**
     * @dev Throws if offer has a start date that is less than last acquired offer end date.
     */
    modifier hasOfferValidStartDate(uint256 _deedId, uint256 _offerStartDate) {
        if (_offerStartDate == 0) {
            _offerStartDate = block.timestamp;
        }
        for (uint i = 0; i < leases[_deedId].length; i++) {
            require(_offerStartDate >= deedLeases[leases[_deedId][i]].leaseEndDate, "DeedRenting#InvalidOfferStartDate");
        }
        _;
    }

    /**
     * @dev Throws if Rent months is 0 or Notice months is more than Rent months
     */
    modifier isValidOfferPeiod(uint16 _months, uint8 _noticePeriod) {
        require(_months > 0, "DeedRenting#RentalDurationMustBePositive");
        require(_noticePeriod < _months, "DeedRenting#NoticePeriodMustBeLessThanRentalDuration");
        _;
    }

    /**
     * @dev Throws if offer is meant to a different Tenant Address.
     */
    modifier isAuthorizedTenant(uint256 _offerId) {
        if (deedOffers[_offerId].authorizedTenant != address(0)) {
          require(deedOffers[_offerId].authorizedTenant == _msgSender(), "DeedRenting#OfferNotAuthorizedForAddress");
        }
        _;
    }

    /**
     * @dev Throws if offer had expired or has a start date that is less than last acquired offer end date
     */
    modifier isOfferNotExpired(uint256 _offerId) {
        DeedOffer storage offer = deedOffers[_offerId];
        if (offer.offerExpirationDate > 0) {
          require(offer.offerExpirationDate > block.timestamp, "DeedRenting#OfferExpired");
        }
        for (uint i = 0; i < leases[offer.deedId].length; i++) {
          uint256 leaseId = leases[offer.deedId][i];
          require(offer.offerStartDate >= deedLeases[leaseId].leaseEndDate, "DeedRenting#OfferExpiredInvalidOfferStartDate");
        }
        _;
    }

    /**
     * @dev Throws if Percentage is more than 100
     */
    modifier isPercentageValid(uint8 _percentage) {
        require(_percentage <= 100, "DeedRenting#InvalidPercentageValue");
        _;
    }

    /**
     * @dev Throws if neither "price" nor "all duration price" are strictly positive number or both are set
     */
    modifier isValidPrice(uint256 _price, uint256 _allDurationPrice) {
        require(_price > 0 || _allDurationPrice > 0, "DeedRenting#InvalidOfferPrice");
        require(_price == 0 || _allDurationPrice == 0, "DeedRenting#EitherMonthlyRentOrAllDurationPrice");
        _;
    }

    /**
     * @dev Throws if current address isn't the current Deed Tenant/Provisioning Manager
     */
    modifier onlyDeedTenant(uint256 _leaseId) {
        DeedLease storage lease = deedLeases[_leaseId];
        require(lease.tenant == _msgSender(), "DeedRenting#NotDeedManager");
        _;
    }

    /**
     * @dev Throws if Lease Contract is already ended
     */
    modifier isOngoingLease(uint256 _leaseId) {
        DeedLease storage lease = deedLeases[_leaseId];
        require(lease.leaseEndDate > block.timestamp && lease.leaseEndDate > lease.noticePeriodDate, "DeedRenting#LeaseAlreadyEnded");
        _;
    }

    /**
     * @dev Throws if Deed tenant isn't the current address
     */
    modifier isRentNotPaid(uint256 _leaseId) {
        DeedLease storage lease = deedLeases[_leaseId];
        require(lease.paidRentsDate < block.timestamp, "DeedRenting#TenantHasAlreadyPaidDueRents");
        _;
    }

    /**
     * This method replaces the constructor since this is about an Upgradable Contract
     */
    function initialize(
        IERC20 _meed,
        IERC1155 _deed,
        DeedTenantProvisioning _tenantProvisioning
    )
        virtual
        public
        initializer {

        meed = _meed;
        deed = _deed;
        tenantProvisioning = _tenantProvisioning;
        _transferOwnership(_msgSender());
    }

    /**
     * @dev This method allows to a Deed NFT owner to create a renting offer
     */
    function createOffer(
        DeedOffer memory _offer
    )
        public
        onlyDeedOwner(_offer.deedId)
        hasOfferValidStartDate(_offer.deedId, _offer.offerStartDate)
        isValidOfferPeiod(_offer.months, _offer.noticePeriod)
        isPercentageValid(_offer.ownerMintingPercentage)
        isValidPrice(_offer.price, _offer.allDurationPrice) {

        offersCount = offersCount.add(1);
        _offer.id = offersCount;

        _setOffer(_offer);
        _setTenantProvisioningDelegatee(_offer.deedId);

        emit OfferCreated(
          _offer.id,
          _offer.deedId,
          _msgSender()
        );
    }

    /**
     * @dev This method allows to a Deed NFT owner to update a renting offer
     */
    function updateOffer(DeedOffer memory _offer)
        public
        isDeedOffer(_offer.id, _offer.deedId)
        onlyDeedOwner(_offer.deedId)
        onlyOfferCreator(_offer.id)
        notAcquiredOffer(_offer.id)
        hasOfferValidStartDate(_offer.deedId, _offer.offerStartDate)
        isValidOfferPeiod(_offer.months, _offer.noticePeriod)
        isPercentageValid(_offer.ownerMintingPercentage) {

        _setOffer(_offer);

        emit OfferUpdated(
          _offer.id,
          _offer.deedId,
          _msgSender()
        );
    }

    /**
     * @dev This method allows to a Deed NFT owner to delete a renting offer
     */
    function deleteOffer(uint256 _id)
        public
        onlyDeedOwnerByLeaseId(_id)
        notAcquiredOffer(_id) {

        uint256 deedId = deedOffers[_id].deedId;
        delete deedOffers[_id];

        emit OfferDeleted(
          _id,
          deedId,
          _msgSender()
        );
    }

    /**
     * @dev This method allows to acquire an offer by a Tenant
     */
    function acquireRent(uint256 _id, uint8 _monthsToPay)
        public
        notAcquiredOffer(_id)
        isAuthorizedTenant(_id)
        isOfferNotExpired(_id)
        notDeedOwnerByLeaseId(_id)
        isOfferCreatorDeedOwner(_id) {

        DeedOffer storage offer = deedOffers[_id];

        uint8 overallMonthsToPay = offer.noticePeriod + _monthsToPay;
        require(_monthsToPay > 0, "DeedRenting#AtLeastOneMonthPayment");
        require(overallMonthsToPay <= offer.months, "DeedRenting#ExceedsRemainingMonthsToPay");

        uint256 amount;
        if (offer.allDurationPrice > 0 && overallMonthsToPay == offer.months) {
          amount = offer.allDurationPrice;
        } else {
          amount = offer.price.mul(overallMonthsToPay);
        }
        require(meed.transferFrom(_msgSender(), offer.creator, amount), "DeedRenting#DeedRentingPaymentFailed");

        uint256 leaseStartDate;
        if (offer.offerStartDate > block.timestamp) {
            leaseStartDate = offer.offerStartDate;
        } else {
            leaseStartDate = block.timestamp;
        }
        DeedLease memory lease = DeedLease({
            id: offer.id,
            deedId: offer.deedId,
            leaseStartDate: leaseStartDate,
            leaseEndDate: leaseStartDate.add(MONTH_IN_SECONDS.mul(offer.months)),
            paidRentsDate: leaseStartDate.add(MONTH_IN_SECONDS.mul(_monthsToPay)),
            noticePeriodDate: leaseStartDate.add(MONTH_IN_SECONDS.mul(overallMonthsToPay)),
            paidMonths: overallMonthsToPay,
            tenant: _msgSender()
        });
        deedLeases[lease.id] = lease;
        leases[lease.deedId].push(_id);

        emit RentPaid(_id, lease.deedId, _msgSender(), offer.creator, lease.paidMonths, true);
    }

    /**
     * @dev This method allows to pay a Rent by Tenant
     */
    function payRent(uint256 _id, address _deedOwner, uint8 _monthsToPay)
        public
        isReceiverDeedOwnerByLeaseId(_id, _deedOwner)
        onlyDeedTenant(_id)
        isOngoingLease(_id) {

        DeedOffer storage offer = deedOffers[_id];
        DeedLease storage lease = deedLeases[_id];

        require(_monthsToPay > 0, "DeedRenting#AtLeastOneMonthPayment");
        require((offer.months - lease.paidMonths) >= _monthsToPay, "DeedRenting#ExceedsRemainingMonthsToPay");

        uint256 amount = offer.price.mul(_monthsToPay);
        require(meed.transferFrom(_msgSender(), _deedOwner, amount), "DeedRenting#DeedRentingPaymentFailed");

        lease.paidMonths += _monthsToPay;
        lease.paidRentsDate = lease.paidRentsDate.add(MONTH_IN_SECONDS.mul(_monthsToPay));
        lease.noticePeriodDate = lease.noticePeriodDate.add(MONTH_IN_SECONDS.mul(_monthsToPay));

        emit RentPaid(_id, lease.deedId, _msgSender(), _deedOwner, _monthsToPay, false);
    }

    /**
     * @dev This method allows to end Lease by Tenant before the End of the Rent Date
     */
    function endLease(uint256 _id)
        public
        onlyDeedTenant(_id)
        isOngoingLease(_id) {

        DeedOffer storage offer = deedOffers[_id];
        DeedLease storage lease = deedLeases[_id];
        if (lease.noticePeriodDate > block.timestamp) {
          lease.leaseEndDate = lease.noticePeriodDate;
        } else {
          lease.leaseEndDate = block.timestamp;
        }

        emit LeaseEnded(_id, lease.deedId, _msgSender(), (offer.months - lease.paidMonths));
    }

    /**
     * @dev This method allows to evict a Tenant who hasn't paid rents at time.
     * Once evicted, the Tenant can still until the end of the Notice Period which
     * was already paid when acquiring offer.
     */
    function evictTenant(uint256 _id)
        public
        onlyDeedOwnerByLeaseId(_id)
        isOngoingLease(_id)
        isRentNotPaid(_id) {

        DeedOffer storage offer = deedOffers[_id];
        DeedLease storage lease = deedLeases[_id];
        if (lease.noticePeriodDate > block.timestamp) {
          lease.leaseEndDate = lease.noticePeriodDate;
        } else {
          lease.leaseEndDate = block.timestamp;
        }

        emit TenantEvicted(_id, lease.deedId, lease.tenant, _msgSender(), (offer.months - lease.paidMonths));
    }

    /**
     * @dev returns true if the address can manage Deed Provisioning
     */
    function isProvisioningManager(
        address _address,
        uint256 _deedId
    )
        public
        view
        override
        returns (bool) {

        for (uint i = 0; i < leases[_deedId].length;i++) {
            uint256 leaseId = leases[_deedId][i];
            DeedLease storage lease = deedLeases[leaseId];
            if (lease.leaseStartDate <= block.timestamp && lease.leaseEndDate > block.timestamp) {
                return lease.tenant == _address;
            }
        }
        return deed.balanceOf(_address, _deedId) > 0;
    }

    function _setTenantProvisioningDelegatee(uint256 _deedId) internal {
        if (tenantProvisioning.getDelegatee(_deedId) != address(this)) {
          require(tenantProvisioning.setDelegatee(address(this), _deedId), "DeedRenting#RentingContractIsntProvisioningManager");
        }
    }

    function _setOffer(DeedOffer memory _offer) internal {
        _offer.creator = _msgSender();
        if (_offer.offerStartDate == 0) {
            _offer.offerStartDate = block.timestamp;
        }
        if (_offer.offerExpirationDays == 0) {
            _offer.offerExpirationDate = 0;
        } else {
            _offer.offerExpirationDate = _offer.offerStartDate.add(DAY_IN_SECONDS.mul(_offer.offerExpirationDays));
        }
        deedOffers[_offer.id] = _offer;
    }

    function _authorizeUpgrade(address newImplementation) internal view virtual override onlyOwner {}

}
