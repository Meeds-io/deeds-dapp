const cardTypes = {};

export function getNftsOfWallet(nftContract, xMeedContract, address) {
  if (nftContract && xMeedContract && address) {
    return nftContract.nftsOf(address).then(nftIds => {
      const nftsPromises = [];
      if (nftIds && nftIds.length) {
        nftIds.forEach(nftId => {
          nftsPromises.push(
            getNftInfo(nftContract, xMeedContract, nftId)
          );
        });
      }
      return Promise.all(nftsPromises);
    });
  }
  Promise.resolve([]);
}

export function getNftInfo(nftContract, xMeedContract, nftId) {
  if (localStorage.getItem(`nft-info-${nftContract.address}-${nftId}`)) {
    return Promise.resolve(JSON.parse(localStorage.getItem(`nft-info-${nftContract.address}-${nftId}`)));
  } else {
    return nftContract.cardType(nftId)
      .then(cardType => {
        return nftContract.cityIndex(nftId)
          .then(cityIndex => {
            const cardTypeIndex = cityIndex.toNumber() * 4 + cardType.toNumber();
            return getCityCardType(xMeedContract, cardTypeIndex);
          })
          .then(cardTypeInfo => {
            if (cardTypeInfo) {
              const card = {
                id: nftId && nftId.toNumber(),
                name: cardTypeInfo.name,
                uri: cardTypeInfo.uri,
                cardType: Number(cardTypeInfo.cardType),
                cityIndex: Number(cardTypeInfo.cityIndex),
              };
              localStorage.setItem(`nft-info-${nftContract.address}-${nftId}`, JSON.stringify(card));
              return card;
            }
          });
      });
  }
}

export function getPointsBalance(xMeedContract, address) {
  return xMeedContract.earned(address);
}

export function getCurrentCity(xMeedContract) {
  return xMeedContract.currentCityIndex()
    .then(cityIndex => {
      return xMeedContract.cityInfo(cityIndex)
        .then(cityInfo => {
          const city = {};
          Object.keys(cityInfo).forEach(key => {
            if (Number.isNaN(Number(key))) {
              city[key] = cityInfo[key];
            }
          });
          city.id = cityIndex;
          return city;
        });
    });
}

export function isCurrentCityMintable(xMeedContract) {
  return xMeedContract.isCurrentCityMintable();
}

export function getLastCityMintingCompleteDate(xMeedContract) {
  return xMeedContract.lastCityMintingCompleteDate();
}

export function getCityCardTypes(xMeedContract, cityIndex) {
  const promises = [];
  for (let i =0; i < 4; i++) {
    const cardType = cityIndex * 4 + i;
    promises.push(
      getCityCardType(xMeedContract, cardType, true)
    );
  }
  return Promise.all(promises);
}

export function getCityCardType(xMeedContract, cardType, forceReload) {
  if (!forceReload && cardTypes[cardType]) {
    return Promise.resolve(cardTypes[cardType]);
  } else {
    return xMeedContract.cardTypeInfo(cardType)
      .then(cardInfo => {
        const card = {};
        Object.keys(cardInfo).forEach(key => {
          if (Number.isNaN(Number(key))) {
            card[key] = cardInfo[key];
          }
        });
        card.cardType = cardType && cardType.toString && cardType.toString() || cardType;
        return cardTypes[cardType] = card;
      });
  }
}