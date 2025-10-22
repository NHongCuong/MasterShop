
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
            Swal.fire({text: 'The password is not matched', icon: 'error'});
        }
        else
        {
            axios.put('http://localhost:8081/signup',form).then(res=>{
                Swal.fire({text: res.data.message, timer: 2000});
            }).catch(err => {
                Swal.fire({text: err.response.data.message, icon: 'error'});
            });
        }
        
    }
</script>
<template>
    <section class="vh-100" style="background-image: linear-gradient(to right, #f6d9d9 , #e88e9d);">
        <div class="container justify-content-center py-5 h-100">
            <div class="row d-flex justify-content-center align-items-center h-100 w-100">
            <div class="col col-xl-10">
                <div class="card" style="border-radius: 1rem;">
                <div class="row g-0">
                    <div class="col-md-6 col-lg-5 d-none d-md-block">
                    <img style="width:100%; height: 100%;border-radius: 1rem 0 0 1rem;" src="https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTCMacosvC6Y8agy0E2klaVql50Xx2rnSipwQ&usqp=CAU"
                        alt="login form" class="img-fluid"  />
                    </div>
                    <div class="col-md-6 col-lg-7 d-flex align-items-center">
                    <div class="card-body p-4 p-lg-5 text-black">

                        <form id="form" @submit="SubmitForm">

                            <div class="d-flex align-items-center mb-3 pb-1">
                                <i class="fas fa-cubes fa-2x me-3" style="color: #ff6219;"></i>
                                <!-- <span class="h1 fw-bold mb-0">Logo</span> -->
                            </div>

                            <h5 class="fw-normal mb-3 pb-3" style="letter-spacing: 1px;">Đăng kí tài khoản</h5>

                            <div class="form-outline mb-4">
                                <input v-model="form.email" type="email" id="email" class="form-control form-control-lg" required/>
                                <label class="form-label" for="email">Địa chỉ Email</label>
                                <div id="notify_email"></div>
                            </div>
                            <div class="form-outline mb-4">
                                <input v-model="form.password" type="password" id="password" class="form-control form-control-lg" required />
                                <label class="form-label" for="password">Mật khẩu</label>
                                <div id="notify_password"></div>
                            </div>

                            <div class="form-outline mb-4">
                                <input v-model="form.password2" type="password" id="password2" class="form-control form-control-lg" required/>
                                <label class="form-label" for="password2">Nhập lại mật khẩu</label>
                            </div>

                            <div class="pt-1 mb-4">
                                <button class="btn btn-dark btn-lg btn-block" type="submit">Đăng kí</button>
                            </div>

                            <p class="mb-5 pb-lg-2" style="color: #393f81;">Bạn đã có tài khoản? <router-link to="/auth/signin"
                                style="color: #393f81;">Đăng nhập tại đây</router-link></p>
                            
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