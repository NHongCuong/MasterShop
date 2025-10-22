

export default [
    {
        path: '/',
        name: 'home',
        component: () => import('./pages/Home.vue'),
        meta: { title: 'Home' },
        children:[
            {
                path: '',
                name: 'shophome',
                component: () => import('./pages/shop/shophome.vue'),
                meta: { title: 'Shop Home' }
            },
            {
                path: '/product/:id',
                name: 'detailproduct',
                component: () => import('./pages/shop/DetailProduct.vue'),
                meta: { title: 'Detail Product' }
            }
            // {
            //     path: '/category/:id',
            //     name: 'detailcategory',
            //     component: () => import('./pages/shop/DetailCategory.vue'),
            //     meta: { title: 'Detail Category' }
            // }

        ]
    },  
    {
        path: '/admin',
        name: 'admin',
        component: () => import('./pages/admin/Admin.vue'),
        children:[
            {
                path: '',
                name: 'dashboardroot',
                component: () => import('./pages/admin/Dashboard.vue'),
                meta: { title: 'Dashboard' }
            },
            {
                path: 'dashboard',
                name: 'dashboard',
                component: () => import('./pages/admin/Dashboard.vue'),
                meta: {title: 'Dashboard'}
            },
            {
                path: 'user',
                name: 'user',
                component: () => import('./pages/admin/user.vue'),
                meta: {title: 'user'}
            },
            {
                path: 'product',
                name: 'product',
                component: () => import('./pages/admin/product.vue'),
                meta: {title: 'product'}
            },
            {
                path: 'category',
                name: 'category',
                component: () => import('./pages/admin/category.vue'),
                meta: { title: 'category' }
            }


        ]
    },  
    
    {
        path: '/contact',
        name: 'contact',
        component: () => import('./pages/Contact.vue'),
        meta: { title: 'Contact' }
    },  
    {
        path: '/auth/signin',
        name: 'signin',
        component: () => import('./pages/auth/Signin.vue'),
        meta: { title: 'Signin' }
    },
    {
        path: '/auth/signup',
        name: 'signup',
        component: () => import('./pages/auth/Signup.vue'),
        meta: { title: 'Signup' }
    }
]