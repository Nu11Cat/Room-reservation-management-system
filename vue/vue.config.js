const { defineConfig } = require('@vue/cli-service')
const webpack = require('webpack')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8080
  },
  configureWebpack: {
    resolve: {
      fallback: {
        "http": require.resolve("stream-http"),
        "https": require.resolve("https-browserify"),
        "util": require.resolve("util/"),
        "zlib": require.resolve("browserify-zlib"),
        "stream": require.resolve("stream-browserify"),
        "crypto": require.resolve("crypto-browserify"),
        "assert": require.resolve("assert/"),
        "url": require.resolve("url/"),
        "buffer": require.resolve("buffer/"),
        "process": require.resolve("process/browser")
      }
    },
    plugins: [
      new webpack.ProvidePlugin({
        process: 'process/browser',
        Buffer: ['buffer', 'Buffer']
      })
    ]
  }
})
