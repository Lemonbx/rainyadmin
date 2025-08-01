import {fileURLToPath, URL} from 'node:url'

import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import vueDevTools from 'vite-plugin-vue-devtools'
import AutoImport from 'unplugin-auto-import/vite'
import { NaiveUiResolver } from 'unplugin-vue-components/resolvers'
import Components from 'unplugin-vue-components/vite'

// https://vite.dev/config/
export default defineConfig({
    plugins: [
        vue(),
        vueJsx(),
        vueDevTools(),
        AutoImport({
            imports: [
              'vue',
              {
                'naive-ui': [
                  'useDialog',
                  'useMessage',
                  'useNotification',
                  'useLoadingBar'
                ]
              }
            ]
          }),
          Components({
            resolvers: [NaiveUiResolver()]
          })
    ],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        },
    },
    server: {
        host: '0.0.0.0',
        port: 2333,
        proxy: {
            '/api': {
                target: 'http://localhost:12346',
                changeOrigin: true,
                secure: false
            }
        }
    }
})
