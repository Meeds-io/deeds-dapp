/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
  return ethers.utils.formatUnits(new BigNumber(value.toString && value.toString() || value).toFixed(0), decimals).replace(/(\..*[1-9])0+$/, '$1').replace(/\.0*$/, '');
}

export function formatDate(date, lang) {
  lang = lang && lang.replace(/[-_][a-z]+$/i, '');
  return new window.Intl.DateTimeFormat(lang || 'en', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  }).format(date);
}

export function computeTokenBalanceNoDecimals(tokenBalance, fractions, language) {
  const tokenBalanceNoDecimals = fromDecimals(tokenBalance || 0, 18);
  const tokenBalanceWithFractions = fractionsToDisplay(tokenBalanceNoDecimals, fractions);
  return toFixedDisplay(tokenBalanceWithFractions, fractions, language);
}

export function computeFiatBalance(tokenBalance, meedsPrice, ethPrice, exchangeRate, selectedFiatCurrency, language) {
  tokenBalance = tokenBalance || 0;
  meedsPrice = meedsPrice || 0;
  const tokenBalanceNoDecimals = fromDecimals(tokenBalance, 18);
  if (selectedFiatCurrency === 'eur') {
    const tokenBalanceEur = fractionsToDisplay(new BigNumber(tokenBalanceNoDecimals).multipliedBy(meedsPrice).multipliedBy(ethPrice).multipliedBy(new BigNumber(exchangeRate)), 3);
    return toCurrencyDisplay(tokenBalanceEur, selectedFiatCurrency, language);
  } else if (selectedFiatCurrency === 'usd' && ethPrice) {
    const tokenBalanceUsd = fractionsToDisplay(new BigNumber(tokenBalanceNoDecimals).multipliedBy(meedsPrice).multipliedBy(ethPrice), 3);
    return toCurrencyDisplay(tokenBalanceUsd, selectedFiatCurrency, language);
  } else if (selectedFiatCurrency === 'eth') {
    const tokenBalanceEth = fractionsToDisplay(new BigNumber(tokenBalanceNoDecimals).multipliedBy(meedsPrice), 8);
    return toCurrencyDisplay(tokenBalanceEth, selectedFiatCurrency, language);
  }
  return 0;
}

export function toCurrencyDisplay(value, currency, lang) {
  return new Intl.NumberFormat(lang || 'en', {
    style: 'currency',
    currency,
    minimumFractionDigits: 0,
    maximumFractionDigits: currency === 'eth' && 8 || 2,
  }).format(value || 0).replace(/(\..*[1-9])0+$/, '$1').replace(/\.0*$/, '');
}

export function fractionsToDisplay(value, fractions) {
  if (value && (value.length || value > 0)) {
    const result = new BigNumber(value.toString && value.toString() || value).toString();
    return toFixed(result, fractions);
  } else {
    return value;
  }
}

export function toFixed(value, fractions) {
  if (value && (value.length || value > 0)) {
    return Number.parseFloat(value).toFixed(fractions).replace(/(\..*[1-9])0+$/, '$1').replace(/\.0*$/, '');
  } else {
    return value;
  }
}

export function toFixedDisplay(value, fractions, lang) {
  if (value && (value.length || value > 0)) {
    return new Intl.NumberFormat(lang || 'en', {
      style: 'decimal',
      minimumFractionDigits: 0,
      maximumFractionDigits: fractions,
    }).format(value).replace(/(\..*[1-9])0+$/, '$1').replace(/\.0*$/, '');
  } else {
    return value;
  }
}
