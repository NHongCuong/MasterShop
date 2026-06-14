<script setup lang="ts">
import axios from 'axios';
import { reactive } from 'vue';
import Swal from 'sweetalert2';
import {useRouter} from 'vue-router';
import {MyApp, state} from '../../app/MyApp';
import Helper from '../../helper/helper';

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
  <section class="auth-section d-flex align-items-center justify-content-center" :style="{ backgroundImage: `url(${state.generalImages?.['Login'] ? Helper.GetImageUrl(state.generalImages['Login']) : 'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?q=80&w=2070&auto=format&fit=crop'})`, backgroundPosition: 'center center', backgroundSize: 'cover', backgroundRepeat: 'no-repeat', minHeight: '100vh', position: 'relative' }">
    <div class="login-card p-4 p-md-5 rounded shadow">
      <div class="text-center mb-4">
        <h3 class="fw-bold mb-3" style="color: #edab28; font-size: 26px;">Đăng Nhập</h3>
      </div>
      <form @submit="SubmitForm">
        <div class="form-group mb-3">
          <input v-model="form.email" type="email" class="form-control rounded" placeholder="Nhập email của bạn" required style="border: 1px solid #ced4da; padding: 12px 15px;" />
        </div>
        <div class="form-group mb-3">
          <input v-model="form.password" type="password" class="form-control rounded" placeholder="Nhập mật khẩu" required style="border: 1px solid #ced4da; padding: 12px 15px;" />
        </div>
        <div class="d-flex align-items-center mb-4 pl-1">
          <input type="checkbox" id="remember" class="mr-2" />
          <label for="remember" class="m-0 cursor-pointer" style="font-size: 14px; font-weight: normal; color: #333; cursor: pointer;">Ghi nhớ mật khẩu</label>
        </div>
        <button type="submit" class="btn w-100 fw-bold mb-3 rounded text-white" style="background-color: #e5b026; border: none; padding: 12px; font-size: 1rem;">Đăng Nhập</button>
        
        <div class="d-flex align-items-center my-3">
          <hr class="flex-grow-1" style="border-color: #ccc;">
          <span class="mx-2 text-muted" style="font-size: 13px;">Hoặc</span>
          <hr class="flex-grow-1" style="border-color: #ccc;">
        </div>

        <button type="button" class="btn w-100 bg-white border border-secondary text-dark fw-bold mb-3 rounded d-flex justify-content-center align-items-center" style="padding: 10px;">
          <img src="https://upload.wikimedia.org/wikipedia/commons/5/53/Google_%22G%22_Logo.svg" alt="G" style="width: 18px; margin-right: 10px;">
          <span style="font-size: 0.95rem;">Đăng nhập với Google</span>
        </button>

        <div class="text-center mt-4">
          <a href="#" class="d-block mb-2 text-decoration-none" style="color: #007bff; font-weight: 500; font-size: 14px;">Quên mật khẩu?</a>
          <div style="font-size: 14px; color: #333; margin-top: 5px;">
            Bạn chưa có tài khoản? 
            <router-link to="/auth/signup" class="text-decoration-none fw-bold" style="color: #007bff;">Đăng ký</router-link>
          </div>
        </div>
      </form>
    </div>
  </section>
</template>

<style scoped>
.login-card {
  width: 100%;
  max-width: 420px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  transform: translateY(-5%);
}
.cursor-pointer {
  cursor: pointer;
}
.form-control:focus {
  border-color: #e5b026;
  box-shadow: 0 0 0 0.2rem rgba(229, 176, 38, 0.25);
}
</style>