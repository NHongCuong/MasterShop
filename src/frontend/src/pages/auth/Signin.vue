<script setup lang="ts">
import axios from 'axios';
import { reactive } from 'vue';
import Swal from 'sweetalert2';
import {useRouter} from 'vue-router';
import {MyApp} from '../../app/MyApp';

const router = useRouter();
interface User {
        email:string,
        password:string,
        password2:string
    }
    const form = reactive<User>(
    {
        email:'',
        password:'',
        password2:''
    });
    const SubmitForm = (event:Event) => {
        event.preventDefault();

        axios.post('http://localhost:8081/login',form).then(res=>{  
            Swal.fire({text: res.data.message, timer: 1500, icon: 'success', showConfirmButton: false}); 
            localStorage.setItem("token", res.data.token);
            
            // ✅ Cập nhật trạng thái authenticate ngay lập tức
            MyApp.getInstance().authenticate().then(() => {
                setTimeout(()=>{
                    router.push({path:'/'});
                }, 1000);
            });
        }).catch(err=>{
            Swal.fire({text: err.response?.data?.message || "Lỗi đăng nhập", icon: 'error'});
        })
    };
</script>
<template>
    <section class="auth-section py-5" style="background-image: linear-gradient(to right, #f6d9d9 , #e88e9d); min-height: 85vh; display: flex; align-items: center;">
        <div class="container py-3">
            <div class="row d-flex justify-content-center align-items-center w-100 mx-auto">
            <div class="col col-xl-10">
                <div class="card shadow-lg" style="border-radius: 1rem; border: none;">
                <div class="row g-0">
                    <div class="col-md-6 col-lg-5 d-none d-md-block">
                    <img style="width:100%; height: 100%; border-radius: 1rem 0 0 1rem; object-fit: cover;" src="https://images.unsplash.com/photo-1517836357463-d25dfeac3438?q=80&w=2070&auto=format&fit=crop"
                        alt="login form" class="img-fluid" />
                    </div>
                    <div class="col-md-6 col-lg-7 d-flex align-items-center bg-white" style="border-radius: 0 1rem 1rem 0;">
                    <div class="card-body p-4 p-lg-5 text-black">
                        <form @submit="SubmitForm">
                            <div class="text-center mb-4">
                                <i class="fas fa-running fa-3x mb-3" style="color: #212529;"></i>
                                <h3 class="fw-bold text-uppercase">Đăng nhập</h3>
                                <p class="text-muted">Chào mừng bạn quay trở lại MasterShop</p>
                            </div>

                            <div class="form-outline mb-3">
                                <label class="form-label fw-bold" for="email">Địa chỉ email</label>
                                <input v-model="form.email" type="email" id="email" class="form-control form-control-lg border-2" placeholder="name@example.com" required/>
                            </div>

                            <div class="form-outline mb-4">
                                <label class="form-label fw-bold" for="password">Mật khẩu</label>
                                <input v-model="form.password" type="password" id="password" class="form-control form-control-lg border-2" placeholder="••••••••" required/>
                            </div>

                            <div class="pt-1 mb-4">
                                <button class="btn btn-dark btn-lg w-100 fw-bold" type="submit">Đăng nhập</button>
                            </div>

                            <p class="text-center mb-0" style="color: #393f81;">
                                Bạn chưa có tài khoản? <router-link to="/auth/signup" class="fw-bold text-decoration-none" style="color: #e88e9d;">Đăng kí ngay</router-link>
                            </p>
                        </form>
                    </div>
                    </div>
                </div>
                </div>
            </div>
            </div>
        </div>
    </section>
</template>