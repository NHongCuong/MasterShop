<script setup lang="ts">
import axios from 'axios';
import {Category, Product} from '../../interfaces/app';
import {onMounted, ref, watch, onUnmounted} from 'vue';
import {useRoute} from 'vue-router';
import { state, MyApp } from '../../app/MyApp';
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
const wishlisted = ref<Record<number, boolean>>({});

const loadWishlists = async () => {
    await MyApp.getInstance().authenticate();
    if (state.isAuthenticated && state.user?.id) {
        try {
            const res = await axios.get(`http://localhost:8081/wishlist/user/${state.user.id}`);
            res.data.forEach((item: any) => {
                if (item.product?.id) {
                    wishlisted.value[item.product.id] = true;
                }
            });
        } catch (err) {}
    }
};

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

const addToWishlist = async (p: Product) => {
    if (!state.isAuthenticated || !state.user?.id) {
        alert("Vui lòng đăng nhập để sử dụng tính năng Ưa thích!");
        return;
    }
    try {
        const payload = { userId: state.user.id, productId: p.id };
        const res = await axios.post(`http://localhost:8081/wishlist/toggle`, payload);
        if (res.data.status === 'added') {
            wishlisted.value[p.id] = true;
            alert(`Đã thêm "${p.name}" vào danh sách yêu thích!`);
        } else {
            wishlisted.value[p.id] = false;
            alert(`Đã gỡ "${p.name}" khỏi danh sách yêu thích!`);
        }
    } catch (err) {
        alert("Lỗi khi cập nhật danh sách ưa thích");
    }
};

const shareProduct = (p: Product) => {
  const url = window.location.origin + '/product/' + p.id;
  navigator.clipboard.writeText(url);
  alert('Đã sao chép liên kết sản phẩm!');
};

const getSalePrice = (p: Product) => {
  if (p.discountPercent) {
    return Math.round(p.price * (1 - p.discountPercent / 100));
  }
  return p.price;
};

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
  loadWishlists();
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
      <div v-for="pr in filteredProducts" :key="pr.id" class="col-6 col-md-4 col-lg-3">
        <div class="product-card-wrap">
          <router-link :to="'/product/' + pr.id" class="text-decoration-none">
            <div class="product-card h-100 bg-white shadow-sm border-0 rounded-4 overflow-hidden position-relative">
              <div v-if="pr.discountPercent" class="discount-badge">
                -{{ pr.discountPercent }}%
              </div>

              <div class="product-img-box">
                <img :src="Helper.GetImageUrl(pr.avatar)" :alt="pr.name" class="img-fluid">
                <div class="product-side-actions">
                  <button class="side-btn" @click.stop.prevent="addToWishlist(pr)" title="Yêu thích">
                    <i :class="wishlisted[pr.id] ? 'fas fa-heart text-danger' : 'far fa-heart'"></i>
                  </button>
                  <button class="side-btn" @click.stop.prevent="shareProduct(pr)" title="Chia sẻ">
                    <i class="fas fa-share-alt"></i>
                  </button>
                </div>
              </div>

              <div class="p-3 text-center">
                <h6 class="product-name text-dark mb-2">{{ pr.name }}</h6>
                <div class="d-flex flex-column align-items-center">
                  <div v-if="pr.discountPercent" class="old-price small text-muted text-decoration-line-through">
                    {{ Helper.ToMoney(pr.price) }}
                  </div>
                  <div class="price fw-bold text-danger fs-5">
                    {{ Helper.ToMoney(getSalePrice(pr)) }}
                  </div>
                </div>
              </div>
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
.product-card-wrap {
  transition: all 0.3s ease;
}

.product-card-wrap:hover {
  transform: translateY(-8px);
}

.product-card {
  transition: box-shadow 0.3s ease;
}

.product-card-wrap:hover .product-card {
  box-shadow: 0 10px 25px rgba(0,0,0,0.1) !important;
}

.product-img-box {
  position: relative;
  padding: 15px;
  background: #fff;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.product-img-box img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  transition: transform 0.5s ease;
}

.product-card-wrap:hover .product-img-box img {
  transform: scale(1.1);
}

.product-name {
  font-size: 0.95rem;
  font-weight: 600;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 2.6em;
}

.discount-badge {
  position: absolute;
  top: 15px;
  left: 15px;
  background: #ef4444;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 700;
  z-index: 5;
  box-shadow: 0 4px 6px -1px rgba(239, 68, 68, 0.3);
}

.product-side-actions {
  position: absolute;
  right: 15px;
  top: 15px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  transition: all 0.3s ease;
  z-index: 10;
}

.product-card:hover .product-side-actions {
  right: 15px;
}

.side-btn {
  width: 35px;
  height: 35px;
  border-radius: 4px;
  background: white;
  border: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  transition: all 0.2s;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.side-btn:hover {
  background: #f8fafc;
  color: #ef4444;
  border-color: #ef4444;
}
</style>