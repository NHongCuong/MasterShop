import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  optimizeDeps: {
    include: [
      'primevue/config',
      'primevue/toastservice',
      'primevue/toast',
      'primevue/usetoast',
      'primevue/galleria',
      'primevue/image',
      'primevue/chart',
      'primevue/skeleton',
      'primevue/dialog',
      'primevue/dropdown',
      'primevue/button',
      'primevue/inputtext',
      'primevue/inputnumber',
      'primevue/textarea',
      'chart.js',
    ],
  },
})
