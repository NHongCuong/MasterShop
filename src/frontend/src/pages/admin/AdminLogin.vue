<script setup lang="ts">
import axios from 'axios';
import { reactive } from 'vue';
import Swal from 'sweetalert2';
import { useRouter } from 'vue-router';
import { MyApp, state } from '../../app/MyApp';

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
                MyApp.getIntance().clearState();
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
    <div class="admin-login-page d-flex align-items-center justify-content-center vh-100 bg-dark">
        <div class="login-box shadow-lg bg-white rounded-lg overflow-hidden" style="width: 400px;">
            <div class="login-header p-4 text-center text-white" style="background-color: #343a40;">
                <img src="/images/logotech.png" alt="Logo" width="60" class="mb-2 rounded-circle border border-white">
                <h4 class="m-0 fw-bold">HC TECH ADMIN</h4>
                <p class="small m-0 text-gray-400">Đăng nhập để quản trị hệ thống</p>
            </div>
            
            <div class="login-body p-4 p-md-5">
                <form @submit="SubmitForm">
                    <div class="form-group mb-3">
                        <label class="form-label fw-bold small text-muted text-uppercase">Email Quản Trị</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light border-end-0"><i class="fas fa-envelope text-muted"></i></span>
                            <input v-model="form.email" type="email" class="form-control border-start-0 bg-light" placeholder="admin@mastershop.com" required>
                        </div>
                    </div>
                    
                    <div class="form-group mb-4">
                        <label class="form-label fw-bold small text-muted text-uppercase">Mật Khẩu</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light border-end-0"><i class="fas fa-lock text-muted"></i></span>
                            <input v-model="form.password" type="password" class="form-control border-start-0 bg-light" placeholder="••••••••" required>
                        </div>
                    </div>
                    
                    <button type="submit" class="btn btn-primary w-100 py-2 fw-bold text-uppercase shadow-sm">
                        Đăng Nhập Ngay
                    </button>
                    
                    <div class="text-center mt-4">
                        <router-link to="/" class="text-decoration-none small text-muted hover-dark">
                            <i class="fas fa-arrow-left me-1"></i> Quay lại cửa hàng
                        </router-link>
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>

<style scoped>
.admin-login-page {
    background: linear-gradient(135deg, #1a1a1a 0%, #343a40 100%);
}

.login-box {
    transition: transform 0.3s ease;
}

.login-box:hover {
    transform: translateY(-5px);
}

.form-control:focus {
    box-shadow: none;
    border-color: #007bff;
}

.btn-primary {
    background-color: #007bff;
    border: none;
    transition: all 0.2s ease;
}

.btn-primary:hover {
    background-color: #0056b3;
    transform: scale(1.02);
}

.hover-dark:hover {
    color: #333 !important;
}
</style>
