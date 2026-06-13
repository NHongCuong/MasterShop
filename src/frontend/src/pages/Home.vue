<script setup lang="ts">
import axios from 'axios';
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {Category, Product} from '../interfaces/app';
import {MyApp, state} from '../app/MyApp';
import ContactWidget from '../components/ContactWidget.vue';
import BackToTop from '../components/BackToTop.vue';
import Helper from '../helper/helper';

const route = useRoute();
const router = useRouter();
const categories = ref<Category[]>([]);
const products = ref<Product[]>([]);
const searchKeyword = ref('');
const showSearchSuggestions = ref(false);
let searchBlurTimeout: ReturnType<typeof setTimeout> | null = null;

const searchSuggestions = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase();
  if (!keyword) return [];

  return products.value
    .filter((product) =>
      product.name?.toLowerCase().includes(keyword) ||
      product.category?.name?.toLowerCase().includes(keyword)
    )
    .slice(0, 5);
});

const hasSearchSuggestions = computed(() => showSearchSuggestions.value && searchSuggestions.value.length > 0);

watch(
  () => route.query.search,
  (val) => {
    searchKeyword.value = typeof val === 'string' ? val : '';
  },
  { immediate: true }
);

const onSearch = () => {
  const keyword = searchKeyword.value.trim();
  showSearchSuggestions.value = false;
  if (!keyword) {
    router.push('/products');
    return;
  }
  router.push({ path: '/products', query: { search: keyword } });
};

const onSearchFocus = () => {
  if (searchBlurTimeout) {
    clearTimeout(searchBlurTimeout);
  }
  showSearchSuggestions.value = true;
};

const onSearchBlur = () => {
  searchBlurTimeout = setTimeout(() => {
    showSearchSuggestions.value = false;
  }, 150);
};

const goToProduct = (product: Product) => {
  searchKeyword.value = product.name;
  showSearchSuggestions.value = false;
  router.push('/product/' + product.id);
};

const loadCategories = async () => {
  try {
    const res = await axios.get("http://localhost:8081/category/list");
    categories.value = res.data;
  } catch (err) {
    console.error("Lỗi tải danh mục:", err);
  }
}

const loadProducts = async () => {
  try {
    const res = await axios.get("http://localhost:8081/product/all");
    products.value = res.data;
  } catch (err) {
    console.error("Error loading products:", err);
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
  loadProducts();
});
</script>

<template>
  <div class="main-wrapper">
    <!-- Sticky Header Wrapper -->
    <div style="position: sticky; top: 0; z-index: 1040;">
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
        <div class="search-area d-flex flex-grow-1 mx-5">
          <div class="input-group">
            <input
              v-model="searchKeyword"
              type="text"
              class="form-control border-0"
              placeholder="Tìm kiếm thương hiệu, sản phẩm..."
              style="height: 38px;"
              autocomplete="off"
              @focus="onSearchFocus"
              @input="showSearchSuggestions = true"
              @blur="onSearchBlur"
              @keyup.enter="onSearch"
            >
            <button class="btn border-0 px-3" type="button" style="background-color: #f1c40f; color: white;" @click="onSearch">
              <i class="fas fa-search"></i>
            </button>
          </div>
          <div v-if="hasSearchSuggestions" class="search-suggestions shadow-sm">
            <button
              v-for="product in searchSuggestions"
              :key="product.id"
              type="button"
              class="search-suggestion-item"
              @mousedown.prevent="goToProduct(product)"
            >
              <span class="suggestion-image">
                <img v-if="product.avatar" :src="Helper.GetImageUrl(product.avatar)" :alt="product.name">
                <i v-else class="fas fa-search"></i>
              </span>
              <span class="suggestion-name">{{ product.name }}</span>
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
            <router-link class="nav-link text-white fw-bold px-3" style="font-size: 0.85rem;" to="/contact">VỀ CHÚNG TÔI</router-link>
          </li>
        </ul>
      </div>
    </nav>
    </div> <!-- // Sticky Header Wrapper -->

    <!-- Main Content -->
    <div class="content-body">
      <router-view></router-view>
    </div>

    <BackToTop :bottom="96" />
    <ContactWidget />
  </div>
</template>

<style scoped>
.main-wrapper {
  min-height: 100vh;
}

.header-top .navbar-brand .lh-1 {
  line-height: 1.1;
}

.search-area {
  max-width: 500px;
  position: relative;
  z-index: 1040;
}

.search-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  overflow: hidden;
  background: #fff;
  border: 1px solid #e6e6e6;
  border-top: 0;
  max-height: 325px;
}

.search-suggestion-item {
  width: 100%;
  min-height: 65px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 0;
  border-bottom: 1px solid #eeeeee;
  background: #fff;
  padding: 10px 18px 10px 28px;
  text-align: left;
  color: #111;
  transition: background-color 0.2s ease;
}

.search-suggestion-item:last-child {
  border-bottom: 0;
}

.search-suggestion-item:hover {
  background-color: #f8f9fa;
}

.suggestion-image {
  width: 36px;
  height: 36px;
  flex: 0 0 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c7c7c7;
  font-size: 1rem;
}

.suggestion-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.suggestion-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  color: #111;
  font-size: 0.95rem;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
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

@media (max-width: 991.98px) {
  .search-area {
    margin-left: 1rem !important;
    margin-right: 1rem !important;
  }
}
</style>
