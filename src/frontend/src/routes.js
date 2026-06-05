import { state, MyApp } from './app/MyApp';

const adminGuard = async (to, from) => {
    await MyApp.getInstance().authenticate();
    if (state.isAuthenticated && state.user?.userType.name === 'admin') {
        return true;
    } else {
        return '/admin-login';
    }
};

export default [
    {
        path: '/',
        name: 'home',
        component: () => import('./pages/Home.vue'),
        meta: { title: 'Home' },
        children: [
            {
                path: '/products',
                name: 'products',
                component: () => import('./pages/shop/Products.vue'),
                meta: { title: 'Sản phẩm' }
            },
            {
                path: '',
                name: 'shophome',
                component: () => import('./pages/shop/ShopHome.vue'),
                meta: { title: 'Shop Home' }
            },
            {
                path: '/product/:id',
                name: 'detailproduct',
                component: () => import('./pages/shop/DetailProduct.vue'),
                meta: { title: 'Detail Product' }
            },
            {
                path: '/contact',
                name: 'contact',
                component: () => import('./pages/Contact.vue'),
                meta: { title: 'Contact' }
            },
            {
                path: '/cart',
                name: 'cart',
                component: () => import('./pages/cart.vue'),
                meta: { title: 'Cart' }
            },
            {
                path: '/order',
                name: 'order',
                component: () => import('./pages/Order.vue'),
                meta: { title: 'Đặt hàng' }
            },
            {
                path: '/profile',
                name: 'profile',
                component: () => import('./pages/shop/Profile.vue'),
                meta: { title: 'Profile' }
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
            },
            {
                path: '/guides',
                name: 'guides',
                component: () => import('./pages/shop/ShopHome.vue'),
                meta: { title: 'Hướng dẫn' }
            },
            {
                path: '/about',
                name: 'about',
                component: () => import('./pages/shop/ShopHome.vue'),
                meta: { title: 'Về chúng tôi' }
            }
        ]
    },
    {
        path: '/admin-login',
        name: 'adminlogin',
        component: () => import('./pages/admin/AdminLogin.vue'),
        meta: { title: 'Admin Login' }
    },
    {
        path: '/admin',
        name: 'admin',
        component: () => import('./pages/admin/Admin.vue'),
        beforeEnter: adminGuard,
        children: [
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
                meta: { title: 'Dashboard' }
            },
            {
                path: 'user',
                name: 'user',
                component: () => import('./pages/admin/user.vue'),
                meta: { title: 'Users' }
            },
            {
                path: 'product',
                name: 'product',
                component: () => import('./pages/admin/product.vue'),
                meta: { title: 'Products' }
            },
            {
                path: 'category',
                name: 'category',
                component: () => import('./pages/admin/category.vue'),
                meta: { title: 'Category' }
            },
            {
                path: 'product-color',
                name: 'productColor',
                component: () => import('./pages/admin/AdminProductColor.vue'),
                meta: { title: 'Product Color' }
            },
            {
                path: 'product-material',
                name: 'productMaterial',
                component: () => import('./pages/admin/AdminProductMaterial.vue'),
                meta: { title: 'Product Material' }
            },
            {
                path: 'product-dimensions',
                name: 'productDimensions',
                component: () => import('./pages/admin/AdminProductDimensions.vue'),
                meta: { title: 'Product Dimensions' }
            },
            {
                path: 'color',
                name: 'adminColor',
                component: () => import('./pages/admin/AdminColor.vue'),
                meta: { title: 'Colors' }
            },
            {
                path: 'material',
                name: 'adminMaterial',
                component: () => import('./pages/admin/AdminMaterial.vue'),
                meta: { title: 'Materials' }
            },
            {
                path: 'voucher',
                name: 'adminVoucher',
                component: () => import('./pages/admin/AdminVoucher.vue'),
                meta: { title: 'Voucher' }
            },
            {
                path: 'saleoff',
                name: 'saleoff',
                component: () => import('./pages/admin/AdminVoucher.vue'),
                meta: { title: 'Khuyến mãi' }
            },
            {
                path: 'bill/:status',
                name: 'bill',
                component: () => import('./pages/admin/AdminOrders.vue'),
                meta: { title: 'Đơn hàng' }
            }
        ]
    }
]