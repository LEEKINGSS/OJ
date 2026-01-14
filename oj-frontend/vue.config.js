const { defineConfig } = require("@vue/cli-service");
const MonacoWebpackPlugin = require("monaco-editor-webpack-plugin");
module.exports = {
  chainWebpack(config) {
    config.plugin("monaco").use(new MonacoWebpackPlugin());
  },
  devServer: {
    port: 8081,
    proxy: {
      "/api": {
        target: "http://172.22.232.42:8121",
        changeOrigin: true,
      },
    },
  },
};
