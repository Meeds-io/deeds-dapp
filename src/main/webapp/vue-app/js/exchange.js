export function retrieveMeedsData() {
  return fetch('/deeds-dapp/json/exchangeRate-meeds.json', {
    cache: 'reload',
    method: 'GET',
  }).then(resp => resp && resp.ok && resp.json());
}

export function retrieveCurrencyExchangeRate() {
  return fetch('/deeds-dapp/json/exchangeRate-eur.json', {
    cache: 'reload',
    method: 'GET'
  }).then(resp => resp && resp.ok && resp.json());
}