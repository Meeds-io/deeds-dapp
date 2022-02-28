// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.9;

import './abstract/MeedsPointsRewarding.sol';
import './abstract/ERC1155Tradable.sol';
import './abstract/SafeMath.sol';

contract XMeedsNFTRewarding is MeedsPointsRewarding {
    using SafeMath for uint256;

    // Info of each Card Type
    struct CardTypeDetail {
        string name;
        string uri;
        uint8 cityIndex;
        uint8 cardType;
        uint32 supply;
        uint32 maxSupply;
        uint256 amount;
    }

    // Info of each City
    struct CityDetail {
        string name;
        uint32 population;
        uint32 maxPopulation;
        uint256 availability;
    }

    ERC1155Tradable public nft;

    CardTypeDetail[] public cardTypeInfo;
    CityDetail[] public cityInfo;
    uint8 public currentCityIndex = 0;
    uint256 public lastCityMintingCompleteDate = 0;

    event Redeemed(address indexed user, string city, string cardType, uint256 id);
    event NFTSet(ERC1155Tradable indexed newNFT);

    constructor (
        IERC20 _meed,
        FundDistribution _rewardDistribution,
        ERC1155Tradable _nftAddress,
        uint256 _startRewardsDelay,
        string[] memory _cityNames,
        string[] memory _cardNames,
        uint256[] memory _cardPrices,
        uint32[] memory _cardSupply,
        string[] memory _uris
    ) MeedsPointsRewarding(_meed, _rewardDistribution, _startRewardsDelay) {
        nft = _nftAddress;
        lastCityMintingCompleteDate = block.timestamp;

        uint256 citiesLength = _cityNames.length;
        uint256 cardsLength = _cardNames.length;
        uint32 citiesCardsLength = uint32(citiesLength * cardsLength);
        require(uint32(_uris.length) == citiesCardsLength, "Provided URIs length must equal to Card Type length");
        require(uint32(_cardSupply.length) == citiesCardsLength, "Provided Supply list per card per city must equal to Card Type length");

        uint256 _month = 30 days;
        uint256 _index = 0;
        for (uint8 i = 0; i < citiesLength; i++) {
            uint32 _maxPopulation = 0;
            for (uint8 j = 0; j < cardsLength; j++) {
                string memory _cardName = _cardNames[j];
                uint256 _cardPrice = _cardPrices[j];
                string memory _uri = _uris[_index];
                uint32 _maxSupply = _cardSupply[_index];
                cardTypeInfo.push(CardTypeDetail({
                    name: _cardName,
                    cityIndex: i,
                    cardType: j,
                    amount: _cardPrice,
                    supply: 0,
                    maxSupply: _maxSupply,
                    uri: _uri
                }));
                _maxPopulation += _maxSupply;
                _index++;
            }

            uint256 _availability = i > 0 ? ((2 ** (i + 1)) * _month) : 0;
            string memory _cityName = _cityNames[i];
            cityInfo.push(CityDetail({
                name: _cityName,
                population: 0,
                maxPopulation: _maxPopulation,
                availability: _availability
            }));
        }
    }

    /**
     * @dev Set MEED NFT address
     */
    function setNFT(ERC1155Tradable _nftAddress) public onlyOwner {
        nft = _nftAddress;
        emit NFTSet(_nftAddress);
    }

    /**
     * @dev Checks if current city is mintable
     */
    function isCurrentCityMintable() public view returns (bool) {
        return block.timestamp > cityMintingStartDate();
    }

    /**
     * @dev returns current city minting start date
     */
    function cityMintingStartDate() public view returns (uint256) {
        CityDetail memory city = cityInfo[currentCityIndex];
        return city.availability.add(lastCityMintingCompleteDate);
    }

    /**
     * @dev Redeem an NFT by minting it and substracting the amount af Points (Card Type price)
     * from caller balance of points.
     */
    function redeem(uint8 cardTypeId) public updateReward(msg.sender) returns (uint256 tokenId) {
        require(cardTypeId < cardTypeInfo.length, "Card Type doesn't exist");

        CardTypeDetail storage cardType = cardTypeInfo[cardTypeId];
        require(cardType.maxSupply > 0, "Card Type not available for minting");
        require(points[msg.sender] >= cardType.amount, "Not enough points to redeem for card");
        require(cardType.supply < cardType.maxSupply, "Max cards supply reached");
        require(cardType.cityIndex == currentCityIndex, "Designated city isn't available for minting yet");

        CityDetail storage city = cityInfo[cardType.cityIndex];
        require(block.timestamp > city.availability.add(lastCityMintingCompleteDate), "Designated city isn't available for minting yet");

        city.population = city.population + 1;
        cardType.supply = cardType.supply + 1;
        if (city.population >= city.maxPopulation) {
            currentCityIndex++;
            lastCityMintingCompleteDate = block.timestamp;
        }

        points[msg.sender] = points[msg.sender].sub(cardType.amount);
        uint256 _tokenId = nft.create(msg.sender, 1, 1, cardType.uri, cardType.cityIndex, cardType.cardType, "");
        emit Redeemed(msg.sender, city.name, cardType.name, _tokenId);
        return _tokenId;
    }

}