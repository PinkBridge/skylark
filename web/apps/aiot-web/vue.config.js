const { defineConfig } = require('@vue/cli-service')
const path = require('path')

const gatewayTarget = process.env.VUE_APP_GATEWAY_TARGET || 'http://localhost'

module.exports = defineConfig({
  transpileDependencies: [
    '@skylark/admin-shell',
    '@skylark/oauth-client',
    '@skylark/tenant-client',
    '@skylark/authz-vue'
  ],
  pages: {
    index: {
      entry: 'src/main.js',
      title: process.env.VUE_APP_DISPLAY_NAME || 'Skylark Demo Web'
    }
  },
  devServer: {
    port: 9532,
    proxy: {
      '/login': {
        target: gatewayTarget,
        changeOrigin: false,
        secure: false,
        timeout: 30000,
        proxyTimeout: 30000
      },
      '/oauth': {
        target: gatewayTarget,
        changeOrigin: false,
        secure: false,
        timeout: 30000,
        proxyTimeout: 30000
      },
      '/api': {
        target: gatewayTarget,
        changeOrigin: false,
        pathRewrite: { '^/api': '/api' },
        secure: false,
        timeout: 30000,
        proxyTimeout: 30000
      }
    }
  },
  configureWebpack: {
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    }
  }
})
