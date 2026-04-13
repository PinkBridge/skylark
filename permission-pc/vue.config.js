const { defineConfig } = require('@vue/cli-service')
const gatewayTarget = process.env.VUE_APP_GATEWAY_TARGET || 'http://localhost'

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 9528,
    proxy: {
      '/login': {
        target: gatewayTarget,
        changeOrigin: false,
        secure: false,
        timeout: 30000,
        proxyTimeout: 30000,
        logLevel: 'debug'
      },
      '/oauth': {
        target: gatewayTarget,
        changeOrigin: false,
        secure: false,
        timeout: 30000,
        proxyTimeout: 30000,
        logLevel: 'debug'
      },
      '/api': {
        target: gatewayTarget,
        changeOrigin: false,
        pathRewrite: {
          '^/api': '/api'
        },
        secure: false,
        timeout: 30000,
        proxyTimeout: 30000
      }
    }
  }
})
