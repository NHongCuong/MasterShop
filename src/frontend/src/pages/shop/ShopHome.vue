<script setup lang="ts">
import axios from 'axios';
import {Category, Product} from '../../interfaces/app';
import {onMounted, ref, watch, onUnmounted} from 'vue';
import {useRoute} from 'vue-router';
import Helper from '../../helper/helper';
import '../../shop.css';

const route = useRoute();
let myIndex = 0;
let slideInterval: any = null;
const isSearching = ref(false);
const categories = ref<Category[]>([]);
const products = ref<Product[]>([]);
const filteredProducts = ref<Product[]>([]);
const categoryScrollRef = ref<HTMLElement | null>(null);

function showSlides(n: number) {
  const slides = document.getElementsByClassName("mySlides") as HTMLCollectionOf<HTMLElement>;
  if (slides.length === 0) return;
  
  if (n > slides.length) myIndex = 1;
  if (n < 1) myIndex = slides.length;
  
  for (let i = 0; i < slides.length; i++) {
    slides[i].style.display = "none";
  }
  slides[myIndex - 1].style.display = "block";
}

function plusSlides(n: number) {
  clearInterval(slideInterval);
  myIndex += n;
  showSlides(myIndex);
  startAutoSlide();
}

function startAutoSlide() {
  slideInterval = setInterval(() => {
    myIndex++;
    showSlides(myIndex);
  }, 5000);
}

function loadCategory() {
  axios.get("http://localhost:8081/category/list").then(res => {
    if (res.status == 200) {
      categories.value = res.data;
    }
  });
}

function loadProduct() {
  isSearching.value = true;
  axios.get("http://localhost:8081/product/all").then(res => {
    if (res.status == 200) {
      products.value = res.data;
      applyFilter();
    }
    isSearching.value = false;
  }).catch(() => {
    isSearching.value = false;
  });
}

function applyFilter() {
  const categoryName = route.query.category as string;
  if (categoryName) {
    filteredProducts.value = products.value.filter(p => p.category?.name === categoryName);
  } else {
    filteredProducts.value = products.value;
  }
}

function scrollCategories(direction: 'left' | 'right') {
  if (categoryScrollRef.value) {
    const scrollAmount = 300;
    categoryScrollRef.value.scrollBy({
      left: direction === 'left' ? -scrollAmount : scrollAmount,
      behavior: 'smooth'
    });
  }
}

watch(() => route.query.category, () => {
  applyFilter();
});

function recordVisit() {
  axios.post('http://localhost:8081/traffic/visit').catch(() => {});
}

onMounted(() => {
  recordVisit();
  showSlides(myIndex = 1);
  startAutoSlide();
  loadCategory();
  loadProduct();
});

onUnmounted(() => {
  clearInterval(slideInterval);
});
</script>

<template>
  <!-- Full Width Slider with Manual Controls -->
  <div class="slider-wrapper position-relative w3-animate-opacity">
    <div class="slides-container">
      <img class="mySlides w3-animate-right" src="/images/1.png" style="width:100%; object-fit: cover; height: 500px;">
      <img class="mySlides w3-animate-right" src="/images/2.png" style="width:100%; display:none; object-fit: cover; height: 500px;">
      <img class="mySlides w3-animate-right" src="/images/3.png" style="width:100%; display:none; object-fit: cover; height: 500px;">
      <img class="mySlides w3-animate-right" src="/images/4.png" style="width:100%; display:none; object-fit: cover; height: 500px;">
    </div>
    
    <!-- Slider Buttons -->
    <button class="slider-btn prev" @click="plusSlides(-1)">
      <i class="fas fa-chevron-left"></i>
    </button>
    <button class="slider-btn next" @click="plusSlides(1)">
      <i class="fas fa-chevron-right"></i>
    </button>
  </div>

  <!-- Category Section -->
  <div class="category-section py-5" style="background-color: #9adbf6;">
    <div class="container text-center">
      <h2 class="category-title mb-4 fw-bold text-uppercase" style="letter-spacing: 2px;">Danh Mục</h2>
      <div class="category-underline mb-5 mx-auto"></div>
      
      <div class="category-slider-container position-relative px-5">
        <!-- Category Scroll Buttons -->
        <button class="cat-nav-btn left" @click="scrollCategories('left')">
          <i class="fas fa-chevron-left"></i>
        </button>
        
        <div ref="categoryScrollRef" class="d-flex flex-nowrap overflow-auto gap-4 pb-3 no-scrollbar">
          <div v-for="ca in categories" :key="ca.id" class="category-card shadow-sm hover-up">
            <router-link :to="'/?category=' + ca.name" class="text-reset text-decoration-none d-block">
              <div class="card-image-box p-3 bg-white d-flex align-items-center justify-content-center" style="height: 140px; width: 180px;">
                <img :src="Helper.GetImageUrl(ca.icon)" style="max-width: 100%; max-height: 100%; object-fit: contain;">
              </div>
              <div class="card-footer bg-white border-0 py-3 rounded-bottom">
                <p class="text-center fw-bold m-0 small">{{ ca.name }}</p>
              </div>
            </router-link>
          </div>
        </div>

        <button class="cat-nav-btn right" @click="scrollCategories('right')">
          <i class="fas fa-chevron-right"></i>
        </button>
      </div>
    </div>
  </div>

  <!-- Products Section -->
  <div class="container py-5 mt-3 mb-5 text-center">
    <!-- Title -->
    <div class="d-inline-block border-bottom border-4 border-primary pb-2 mb-5">
        <h2 class="h2 fw-bold text-uppercase m-0" style="letter-spacing: 1.5px;">
            <span v-if="route.query.category">Sản phẩm: <span class="text-primary">{{ route.query.category }}</span></span>
            <span v-else>SẢN PHẨM PHONG PHÚ</span>
        </h2>
    </div>

    <!-- Product Grid -->
    <div v-if="filteredProducts?.length == 0 && !isSearching" class="py-5 text-center w-100">
      <img src="https://cdni.iconscout.com/illustration/premium/thumb/no-product-found-8266457-6651100.png" width="180">
      <h4 class="mt-4 text-muted">Không tìm thấy sản phẩm nào!</h4>
    </div>
    
    <div v-else class="row g-4 px-2 justify-content-center">
      <div v-for="pr in filteredProducts" :key="pr.id" class="col-6 col-md-4 col-lg-3 col-xl-2">
        <div class="product-card h-100 bg-white shadow-sm border rounded overflow-hidden">
          <router-link :to="'/product/' + pr.id" class="text-decoration-none">
            <div class="position-relative overflow-hidden" style="height: 200px;">
              <img :src="Helper.GetImageUrl(pr.avatar)" class="w-100 h-100 object-fit-cover transition-scale">
              <div class="overlay d-flex align-items-center justify-content-center">
                <span class="btn btn-sm btn-primary rounded-pill px-3">Chi tiết</span>
              </div>
            </div>
            <div class="p-3 text-center">
              <h6 class="text-dark fw-bold mb-2 text-truncate">{{ pr.name }}</h6>
              <div class="text-danger fw-bold fs-6">{{ Helper.ToMoney(pr.price) }}</div>
            </div>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.slider-wrapper {
  overflow: hidden;
}

.slider-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(255, 255, 255, 0.7);
  border: none;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #333;
}

.slider-btn:hover {
  background: #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}

.slider-btn.prev { left: 30px; }
.slider-btn.next { right: 30px; }

/* Category Slider Styling */
.category-underline {
  width: 60px;
  height: 4px;
  background-color: #28a745;
  border-radius: 2px;
}

.category-card {
  min-width: 180px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.category-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 10px 25px rgba(0,0,0,0.1) !important;
}

.cat-nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(255, 255, 255, 0.9);
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  z-index: 5;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  color: #f39c12;
  transition: all 0.2s ease;
}

.cat-nav-btn:hover {
  background: #fff;
  transform: translateY(-50%) scale(1.1);
}

.cat-nav-btn.left { left: 0; }
.cat-nav-btn.right { right: 0; }

.no-scrollbar::-webkit-scrollbar { display: none; }
.no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }

/* Product card styling */
.product-card { transition: all 0.3s ease; }
.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 20px rgba(0,0,0,0.1) !important;
}

.transition-scale { transition: transform 0.5s ease; }
.product-card:hover .transition-scale { transform: scale(1.08); }

.overlay {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.1);
  opacity: 0;
  transition: opacity 0.3s ease;
}
.product-card:hover .overlay { opacity: 1; }
</style>