<script setup lang="ts">
import axios from 'axios';
import {onMounted, ref} from 'vue';
import {User} from '../interfaces/app';
import {MyApp} from '../app/MyApp';

const isAuthenticated = ref(false);
const userList = ref<User[]>([]);
const user = ref<User>();
axios.get("http://localhost:8081/user").then(res => {
  userList.value = res.data;
});

function logOut() {
  localStorage.removeItem("token");
  user.value = undefined;
  isAuthenticated.value = false;
}

onMounted(() => {
  MyApp.getIntance().authenticate(isAuthenticated, user);
});
</script>
<template>
  <div>
    <nav class="navbar navbar-expand-sm w3-animate-top bg-light">
      <input id="id_user_login" hidden value="{{session('id_user')}}"/>
      <div class="container-fluid">
        <div class="d-flex">
          <div>
            <router-link class="navbar-brand" to="/">
              <img src="/images/logotech.png" class="rounded-circle" width="50" height="50">
            </router-link>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mynavbar">
              <i class="fa fa-bars" style="font-size:24px;color:black"></i>
            </button>
          </div>
          <ul class="navbar-nav me-auto">
            <li class="nav-item">
              <router-link class="nav-link" to="/introduce">Giới thiệu</router-link>
            </li>
            <li class="nav-item">
              <router-link class="nav-link" to="/contact">Liên hệ</router-link>
            </li>

          </ul>
        </div>
        <div class="d-flex" id="mynavbar">

          <div class="d-flex position-relative" id="search_bar">
            <input id="search" class="form-control me-2" type="text" style="height: 50px;"
                   placeholder="Tìm kiếm thứ gì đó">
            <ul id="search-results" style="    position: absolute;z-index: 999;top:40px"></ul>

            <button id="btn_search" class="btn btn-primary" type="button">Search</button>
          </div>

        </div>
        <!-- Right Menu -->
        <div class="d-flex justify-content-end">


          <div v-if="isAuthenticated == false">
            <router-link to="/auth/signin" class="btn btn-success">Sign in</router-link>
            <router-link to="/auth/signup" class="btn btn-danger">Sign up</router-link>
          </div>
          <div v-else>
            <div class="d-flex">

              <div class="position-relative btn hover">
                                <span id='count_product_in_cart' style="width:25px;height:25px;top: -10px;left: -12px;"
                                      class="position-absolute bg-danger text-light rounded-circle text-center">
                                2</span>

                <router-link to="/cart">
                  <img width="35" height="35" src="https://img.icons8.com/ios/50/shopping-cart--v1.png"
                       alt="shopping-cart--v1"/>
                </router-link>
              </div>
              <div class="dropdown d-flex">
                <button class="btn btn-light dropdown-toggle" type="button" data-bs-toggle="dropdown"
                        aria-expanded="false">
                  <span class="badge bg-primary me-2">{{ user?.userType.name }}</span>{{ user?.email }}
                </button>
                <ul class="dropdown-menu">

                  <li v-if="user?.userType.name == 'admin' || user?.userType.name == 'seller'"><a class="dropdown-item "
                                                                                                  href="/admin/dashboard">Trang
                    quản trị</a></li>
                  <li>
                    <router-link class="dropdown-item " to="/profile/{{session('id_user')}}">Thông tin cá nhân
                    </router-link>
                  </li>
                  <li @click="logOut()">
                    <button class="btn btn-light">Đăng xuất</button>
                  </li>
                </ul>
                <img src="https://lazycodet.com/vite.svg" style="width:35px">
              </div>
            </div>
          </div>

        </div>
      </div>

    </nav>
    <router-view></router-view>
  </div>

</template>