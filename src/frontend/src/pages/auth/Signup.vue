<script setup lang="ts">
import axios from 'axios';
import { reactive } from 'vue';
import Swal from 'sweetalert2';
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
        if(form.password != form.password2)
        {
            Swal.fire({text: 'Mật khẩu nhập lại không khớp', icon: 'error'});
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
    <section class="auth-section py-5" style="background-image: linear-gradient(to right, #f6d9d9 , #e88e9d); min-height: 85vh; display: flex; align-items: center;">
        <div class="container py-3">
            <div class="row d-flex justify-content-center align-items-center w-100 mx-auto">
            <div class="col col-xl-10">
                <div class="card shadow-lg" style="border-radius: 1rem; border: none;">
                <div class="row g-0">
                    <div class="col-md-6 col-lg-5 d-none d-md-block">
                    <img style="width:100%; height: 100%; border-radius: 1rem 0 0 1rem; object-fit: cover;" src="https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?q=80&w=1920&auto=format&fit=crop"
                        alt="signup form" class="img-fluid" />
                    </div>
                    <div class="col-md-6 col-lg-7 d-flex align-items-center bg-white" style="border-radius: 0 1rem 1rem 0;">
                    <div class="card-body p-4 p-lg-5 text-black">
                        <form @submit="SubmitForm">
                            <div class="text-center mb-4">
                                <i class="fas fa-user-plus fa-3x mb-3" style="color: #212529;"></i>
                                <h3 class="fw-bold text-uppercase">Đăng ký</h3>
                                <p class="text-muted">Tham gia cùng cộng đồng MasterShop</p>
                            </div>

                            <div class="form-outline mb-3">
                                <label class="form-label fw-bold" for="email">Địa chỉ Email</label>
                                <input v-model="form.email" type="email" id="email" class="form-control form-control-lg border-2" placeholder="name@example.com" required/>
                            </div>

                            <div class="form-outline mb-3">
                                <label class="form-label fw-bold" for="password">Mật khẩu</label>
                                <input v-model="form.password" type="password" id="password" class="form-control form-control-lg border-2" placeholder="••••••••" required />
                            </div>

                            <div class="form-outline mb-4">
                                <label class="form-label fw-bold" for="password2">Nhập lại mật khẩu</label>
                                <input v-model="form.password2" type="password" id="password2" class="form-control form-control-lg border-2" placeholder="••••••••" required/>
                            </div>

                            <div class="pt-1 mb-4">
                                <button class="btn btn-dark btn-lg w-100 fw-bold" type="submit">Đăng ký</button>
                            </div>

                            <p class="text-center mb-0" style="color: #393f81;">
                                Bạn đã có tài khoản? <router-link to="/auth/signin" class="fw-bold text-decoration-none" style="color: #e88e9d;">Đăng nhập tại đây</router-link>
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