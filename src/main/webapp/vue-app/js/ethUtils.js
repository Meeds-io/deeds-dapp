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
    minimumFractionDigits: 0,
    maximumFractionDigits: currency === 'eth' && 8 || 2,
  }).format(value);
}

export function fractionsToDisplay(value, fractions) {
  if (value && fractions) {
    const result = new BigNumber(value.toString && value.toString() || value).toString();
    return toFixed(result, fractions);
  } else {
    return value;
  }
}

export function toFixed(value, fractions) {
  if (value) {
    return Number.parseFloat(value).toFixed(fractions).replace(/(\..*[1-9])0+$/, '$1').replace(/\.0*$/, '');
  } else {
    return value;
  }
}
