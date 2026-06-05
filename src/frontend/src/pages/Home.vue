<script setup lang="ts">
import axios from 'axios';
import {onMounted, ref} from 'vue';
import {Category} from '../interfaces/app';
import {MyApp, state} from '../app/MyApp';

const categories = ref<Category[]>([]);

const loadCategories = async () => {
  try {
    const res = await axios.get("http://localhost:8081/category/list");
    categories.value = res.data;
  } catch (err) {
    console.error("Lỗi tải danh mục:", err);
  }
}

function logOut() {
  MyApp.getInstance().clearState();
}

onMounted(() => {
  MyApp.getInstance().authenticate().then(() => {
    MyApp.getInstance().updateCartCount();
  });
  loadCategories();
});
</script>

<template>
  <div class="main-wrapper">
    <!-- Header Top -->
    <div class="header-top py-2" style="background-color: #9adbf6;">
      <div class="container d-flex align-items-center justify-content-between">
        <!-- Logo -->
        <router-link to="/" class="navbar-brand d-flex align-items-center">
          <img src="/images/logotech.png" alt="Logo" class="rounded" width="45" height="45">
          <div class="ms-2 lh-1 text-dark">
            <span class="fw-bold d-block" style="font-size: 1.2rem;">HC</span>
            <span class="fw-bold" style="font-size: 0.9rem;">SHOP</span>
          </div>
        </router-link>

        <!-- Search Bar -->
        <div class="d-flex flex-grow-1 mx-5" style="max-width: 500px;">
          <div class="input-group">
            <input type="text" class="form-control border-0" placeholder="Tìm kiếm thương hiệu, sản phẩm..." style="height: 38px;">
            <button class="btn border-0 px-3" type="button" style="background-color: #f1c40f; color: white;">
              <i class="fas fa-search"></i>
            </button>
          </div>
        </div>

        <!-- Right Side Icons -->
        <div class="d-flex align-items-center gap-4 text-white">
          <!-- Phone -->
          <div class="d-flex align-items-center gap-2">
            <i class="fas fa-phone-alt" style="font-size: 1.1rem;"></i>
            <span class="fw-bold" style="font-size: 0.9rem;">0776856666</span>
          </div>

          <!-- Account Dropdown -->
          <div class="dropdown">
            <div class="d-flex align-items-center gap-2 cursor-pointer dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false" style="cursor: pointer;">
              <i class="fas fa-user" style="font-size: 1.1rem;"></i>
              <span class="fw-bold" style="font-size: 0.9rem;">Tài Khoản</span>
            </div>
            <ul class="dropdown-menu dropdown-menu-end mt-2 border-0 shadow-sm p-0 overflow-hidden" style="min-width: 180px;">
              <template v-if="!state.isAuthenticated">
                <li><router-link class="dropdown-item py-2 px-3" to="/auth/signin">Đăng nhập</router-link></li>
                <li><router-link class="dropdown-item py-2 px-3" to="/auth/signup">Đăng ký</router-link></li>
              </template>
              <template v-else>
                <li><router-link class="dropdown-item py-2 px-3" to="/profile">Thông tin cá nhân</router-link></li>
                <li><button class="dropdown-item py-2 px-3" @click="logOut">Đăng xuất</button></li>
              </template>
            </ul>
          </div>

          <!-- Cart -->
          <router-link to="/cart" class="text-white text-decoration-none d-flex align-items-center gap-2">
            <div class="position-relative">
              <i class="fas fa-shopping-cart" style="font-size: 1.1rem;"></i>
              <span v-if="state.cartCount > 0" class="position-absolute top-0 start-100 translate-middle badge rounded-circle bg-danger" style="font-size: 0.6rem; padding: 0.25em 0.5em;">
                {{ state.cartCount }}
              </span>
            </div>
            <span class="fw-bold" style="font-size: 0.9rem;">Giỏ Hàng ({{ state.cartCount }})</span>
          </router-link>
        </div>
      </div>
    </div>

    <!-- Navigation Bar -->
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark py-1 shadow-sm">
      <div class="container justify-content-center">
        <ul class="navbar-nav gap-4">
          <li class="nav-item">
            <router-link class="nav-link text-white fw-bold px-3" style="font-size: 0.85rem;" to="/">TRANG CHỦ</router-link>
          </li>
          <li class="nav-item dropdown category-dropdown">
            <router-link class="nav-link dropdown-toggle text-white fw-bold px-3" style="font-size: 0.85rem;" to="/products">
              SẢN PHẨM
            </router-link>
            <ul class="dropdown-menu border-0 shadow">
              <li v-for="cat in categories" :key="cat.id">
                <router-link class="dropdown-item" :to="'/products?category=' + cat.name">{{ cat.name }}</router-link>
              </li>
            </ul>
          </li>
          <li class="nav-item">
            <router-link class="nav-link text-white fw-bold px-3" style="font-size: 0.85rem;" to="/guides">HƯỚNG DẪN / REVIEW</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link text-white fw-bold px-3" style="font-size: 0.85rem;" to="/about">VỀ CHÚNG TÔI</router-link>
          </li>
        </ul>
      </div>
    </nav>

    <!-- Main Content -->
    <div class="content-body">
      <router-view></router-view>
    </div>
  </div>
</template>

<style scoped>
.main-wrapper {
  min-height: 100vh;
}

.header-top .navbar-brand .lh-1 {
  line-height: 1.1;
}

.nav-link {
  transition: color 0.3s ease;
}

.nav-link:hover {
  color: #f1c40f !important;
}

.dropdown-item {
  font-size: 0.9rem;
  transition: all 0.2s ease;
  color: #333;
}

.dropdown-item:hover {
  background-color: #f8f9fa;
  color: #000;
}

.header-top {
  position: relative;
  z-index: 1030;
}

.navbar {
  position: relative;
  z-index: 1020;
}

/* Hover Dropdown Effect */
.category-dropdown:hover .dropdown-menu {
  display: block;
  top: 90%; /* Adjust position to align with navbar */
  margin-top: 5px;
}

.category-dropdown .dropdown-toggle::after {
    transition: transform 0.3s ease;
}

.category-dropdown:hover .dropdown-toggle::after {
    transform: rotate(180deg);
}
</style>