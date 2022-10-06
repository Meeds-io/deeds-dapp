const path = require('path');
const { merge } = require('webpack-merge');
const webpackCommonConfig = require('./webpack.prod.js');

// the display name of the war
const app = 'deeds-dapp';

const serverPath = "/deeds-server";

let config = merge(webpackCommonConfig, {
  output: {
    path: path.resolve(`${serverPath}/webapps/${app}/`),
  },
  mode: 'development',
  devtool: 'inline-source-map'
});

module.exports = config;
