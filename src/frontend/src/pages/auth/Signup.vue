<script setup lang="ts">
import axios from 'axios';
import { reactive } from 'vue';
import Swal from 'sweetalert2';
import { state } from '../../app/MyApp';
import Helper from '../../helper/helper';
interface User {
        nameUser: string,
        phone: string,
        email: string,
        password: string
    }
    const form = reactive<User>({
        nameUser: '',
        phone: '',
        email: '',
        password: ''
    });
    const SubmitForm = (event:Event) => {
        event.preventDefault();
        if(form.password.length < 6)
        {
            Swal.fire({text: 'Mật khẩu phải từ 6 ký tự trở lên', icon: 'error'});
        }
        else
        {
            axios.put('http://localhost:8081/signup',form).then(res=>{
                Swal.fire({text: res.data.message, timer: 2000, icon: 'success'});
            }).catch(err => {
                Swal.fire({text: err.response.data.message, icon: 'error'});
            });
        }
        
    }
</script>
<template>
    <div class="signup-page d-flex align-items-center justify-content-center vh-100" :style="{ backgroundImage: `url(${state.generalImages?.['Register'] ? Helper.GetImageUrl(state.generalImages['Register']) : 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?q=80&w=1920&auto=format&fit=crop'})`, backgroundPosition: 'center center', backgroundSize: 'cover', backgroundRepeat: 'no-repeat' }">
        <div class="signup-box shadow-lg rounded p-4 p-md-5 position-relative" style="width: 400px; z-index: 10;">
            <div class="text-center mb-4">
                <h3 class="fw-bold m-0" style="color: #edab28; font-size: 24px;">Đăng Ký</h3>
            </div>
            
            <form @submit="SubmitForm">
                <div class="form-group mb-3">
                    <input v-model="form.nameUser" type="text" class="form-control bg-white" placeholder="Họ và Tên" required style="border: 1px solid #ced4da; padding: 12px 15px;">
                </div>
                
                <div class="form-group mb-3">
                    <input v-model="form.phone" type="text" class="form-control bg-white" placeholder="Số điện thoại" required style="border: 1px solid #ced4da; padding: 12px 15px;">
                </div>
                
                <div class="form-group mb-3">
                    <input v-model="form.email" type="email" class="form-control bg-white" placeholder="Email" required style="border: 1px solid #ced4da; padding: 12px 15px;">
                </div>
                
                <div class="form-group mb-4">
                    <input v-model="form.password" type="password" class="form-control bg-white" placeholder="Mật khẩu" required style="border: 1px solid #ced4da; padding: 12px 15px;">
                </div>
                
                <button type="submit" class="btn w-100 py-2 fw-bold text-white shadow-sm mb-4" style="background-color: #e5b026; border: none; font-size: 1rem;">
                    Đăng Ký
                </button>
                
                <div class="text-left mt-3">
                    <span style="font-size: 13px; color: #333;">Đã có tài khoản? </span>
                    <router-link to="/auth/signin" class="text-decoration-none" style="color: #007bff; font-size: 13px;">Đăng nhập</router-link>
                </div>
            </form>
        </div>
    </div>
</template>

<style scoped>
.signup-page {
    background-color: #f0f2f5;
}

.signup-box {
    background: rgba(245, 245, 245, 0.95);
    backdrop-filter: blur(5px);
    -webkit-backdrop-filter: blur(5px);
    border: 1px solid rgba(255, 255, 255, 0.5);
}

.form-control:focus {
    box-shadow: 0 0 0 0.2rem rgba(229, 176, 38, 0.25);
    border-color: #e5b026;
    outline: none;
}
</style>