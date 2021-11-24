export function getSelectedAddress() {
  return window.ethereum && window.ethereum.request({ method: 'eth_accounts' });
}

export function getSelectedChainId() {
  return window.ethereum && window.ethereum.request({
    method: 'eth_chainId'
  });
}

export function isMetamaskInstalled() {
  return window.ethereum && window.ethereum.isMetaMask;
}

export function isMetamaskConnected() {
  return isMetamaskInstalled() && window.ethereum.isConnected();
}

export function connectToMetamask() {
  return window.ethereum.request({
    method: 'eth_requestAccounts'
  });
}

export function switchMetamaskNetwork() {
  return window.ethereum.request({
    method: 'wallet_switchEthereumChain',
    params: [{ chainId: '0x1' }],
  });
}

export function toDecimals(value, decimals) {
  return ethers.utils.parseUnits(new BigNumber(value.toString && value.toString() || value).toFixed(decimals), decimals);
}

export function fromDecimals(value, decimals) {
  return ethers.utils.formatUnits(new BigNumber(value.toString && value.toString() || value).toFixed(0), decimals);
}

export function formatDate(date, lang) {
  lang = lang.replace(/[-_][a-z]+$/i, '');
  return new window.Intl.DateTimeFormat(lang, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  }).format(date);
}

export function toCurrencyDisplay(value, currency, lang) {
  return new Intl.NumberFormat(lang, {
    style: 'currency',
    currency,
  }).format(value);
}

export function fractionsToDisplay(value, fractions) {
  if (value && fractions) {
    return new BigNumber(value.toString && value.toString() || value).toFixed(fractions);
  } else {
    return value;
  }
}