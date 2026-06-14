<script setup lang="ts">
import axios from 'axios';
import { reactive } from 'vue';
import Swal from 'sweetalert2';
import { useRouter } from 'vue-router';
import { MyApp, state } from '../../app/MyApp';
import Helper from '../../helper/helper';

const router = useRouter();

const form = reactive({
    email: '',
    password: ''
});

const SubmitForm = async (event: Event) => {
    event.preventDefault();

    try {
        const res = await axios.post('http://localhost:8081/login', form);
        if (res.data.token) {
            localStorage.setItem("token", res.data.token);
            
            // Xác thực và kiểm tra vai trò
            await MyApp.getInstance().authenticate();
            
            if (state.isAuthenticated && state.user?.userType.name === 'admin') {
                Swal.fire({
                    text: 'Đăng nhập thành công!',
                    timer: 1500,
                    icon: 'success',
                    showConfirmButton: false
                });
                router.push('/admin/dashboard');
            } else {
                Swal.fire({
                    text: 'Bạn không có quyền truy cập trang quản trị!',
                    icon: 'error'
                });
                MyApp.getInstance().clearState();
            }
        }
    } catch (err: any) {
        Swal.fire({
            text: err.response?.data?.message || "Lỗi đăng nhập",
            icon: 'error'
        });
    }
};
</script>

<template>
    <div class="admin-login-page d-flex align-items-center justify-content-center vh-100" :style="{ backgroundImage: `url(${(state.generalImages?.['ADMIN HCSHOP'] || state.generalImages?.['Logo']) ? Helper.GetImageUrl(state.generalImages['ADMIN HCSHOP'] || state.generalImages['Logo']) : '/images/admin-bg.jpg'})`, backgroundPosition: 'center center', backgroundSize: 'cover', backgroundRepeat: 'no-repeat' }">
        <div class="login-box shadow-lg rounded-lg overflow-hidden position-relative p-4 p-md-5" style="width: 420px; z-index: 10;">
            <div class="text-center mb-4">
                <h3 class="font-weight-bold m-0" style="color: #edab28; letter-spacing: 1px; font-size: 24px;">HCSHOP ADMIN</h3>
                <p class="small m-0 text-white mt-2" style="opacity: 0.8;">Vui lòng đăng nhập để tiếp tục</p>
            </div>
            
            <form @submit="SubmitForm">
                <div class="form-group mb-3">
                    <input v-model="form.email" type="email" class="form-control glass-input" placeholder="Email quản trị" required>
                </div>
                
                <div class="form-group mb-4">
                    <input v-model="form.password" type="password" class="form-control glass-input" placeholder="Mật khẩu" required>
                </div>
                
                <button type="submit" class="btn w-100 py-2 fw-bold shadow-sm" style="background-color: #e5b026; border: none; color: #1a1a1a; font-weight: 600;">
                    Đăng nhập ngay
                </button>
                
                <div class="text-center mt-5">
                    <span style="font-size: 11px; color: rgba(255,255,255,0.5);">© 2020 SmashShop. All rights reserved.</span>
                </div>
            </form>
        </div>
        <!-- Optional overlay to make background slightly darker for contrast if needed, but we'll stick to CSS in box -->
    </div>
</template>

<style scoped>
.admin-login-page {
    /* Fallback background color */
    background-color: #0b1528;
}

.login-box {
    background: rgba(255, 255, 255, 0.08);
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
    border: 1px solid rgba(255, 255, 255, 0.15);
    box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
    border-radius: 16px;
    transform: translateY(10%); /* Pushed down slightly to mimic the screenshot center */
}

.glass-input {
    background: rgba(255, 255, 255, 0.1) !important;
    border: 1px solid rgba(255, 255, 255, 0.2) !important;
    color: #fff !important;
    border-radius: 8px;
    padding: 12px 15px;
}

.glass-input::placeholder {
    color: rgba(255, 255, 255, 0.6);
}

.glass-input:focus {
    box-shadow: 0 0 0 0.2rem rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.4) !important;
    outline: none;
}

.btn:hover {
    filter: brightness(1.1);
}
</style>
