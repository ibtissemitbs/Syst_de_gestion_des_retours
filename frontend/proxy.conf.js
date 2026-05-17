const rewriteOrigin = (proxyReq) => {
  proxyReq.setHeader('Origin', 'http://localhost:4200');
};

module.exports = {
  '/api': {
    target: 'http://localhost:8080',
    secure: false,
    changeOrigin: true,
    logLevel: 'debug',
    onProxyReq: rewriteOrigin,
    on: {
      proxyReq: rewriteOrigin,
    },
  },
};
