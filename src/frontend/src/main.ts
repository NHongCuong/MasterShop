import { createApp } from 'vue'
import './style.css'
import { createRouter, createWebHistory } from 'vue-router';
import Routes from './routes.js';
import App from './App.vue';
import '../node_modules/bootstrap/js/index.esm.js';
import 'primevue/resources/themes/lara-light-blue/theme.css'
import 'primevue/resources/primevue.min.css'
import 'primeicons/primeicons.css'

import PrimeVue from 'primevue/config';
import ToastService from 'primevue/toastservice';

const app = createApp(App);
const router = createRouter({
    routes: Routes,
    history: createWebHistory(),
});
router.afterEach((to:any) => {
    // Sử dụng tiêu đề từ meta nếu nó tồn tại, nếu không thì sử dụng một tiêu đề mặc định
    document.title = to.meta.title || 'Default Title';
});
app.use(router);
app.use(PrimeVue);
app.use(ToastService);
app.mount('#app');
