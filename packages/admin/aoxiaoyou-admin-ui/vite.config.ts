import { defineConfig, loadEnv, searchForWorkspaceRoot } from 'vite';
import react from '@vitejs/plugin-react-swc';
import path from 'path';

const adminRoot = path.resolve(__dirname);
const sharedClientSrc = path.resolve(__dirname, '../../client/src');

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, __dirname, '');
  const proxyTarget = env.VITE_DEV_PROXY_TARGET || env.VITE_API_BASE_URL || 'http://127.0.0.1:8081';
  const isHttpsTarget = proxyTarget.startsWith('https://');

  return {
    plugins: [react()],
    base: '/admin/',
    server: {
      port: 5173,
      host: '0.0.0.0',
      fs: {
        allow: [adminRoot, searchForWorkspaceRoot(__dirname), sharedClientSrc],
      },
      proxy: {
        '/api': {
          target: proxyTarget,
          changeOrigin: true,
          secure: isHttpsTarget,
        },
      },
    },
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
        '@shared-client-assets': path.resolve(sharedClientSrc, 'assets'),
      },
    },
  };
});
