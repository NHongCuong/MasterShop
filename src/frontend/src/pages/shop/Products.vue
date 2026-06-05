<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import axios from 'axios';
import { useRoute } from 'vue-router';
import { Product } from '../../interfaces/app';
import Helper from '../../helper/helper';

const route = useRoute();
const products = ref<Product[]>([]);
const loading = ref(false);

// Filter master data
const colors = ref<any[]>([]);
const materials = ref<any[]>([]);
const sizes = ref<any[]>([]);

// Selected Filters
const selectedCategory = ref<string | null>(null);
const minPrice = ref<number | null>(null);
const maxPrice = ref<number | null>(null);
const selectedColors = ref<number[]>([]);
const selectedMaterials = ref<number[]>([]);
const selectedSizes = ref<string[]>([]);
const sortOrder = ref('newest');

const loadData = async () => {
    loading.value = true;
    try {
        const [pRes, cRes, mRes, sRes] = await Promise.all([
            axios.get('http://localhost:8081/product/all'),
            axios.get('http://localhost:8081/color/list'),
            axios.get('http://localhost:8081/material/list'),
            axios.get('http://localhost:8081/admin/product-dimensions/all?size=100')
        ]);
        products.value = pRes.data;
        colors.value = cRes.data;
        materials.value = mRes.data;
        
        if (sRes.data && sRes.data.content) {
            const sizeNames = sRes.data.content.map((s: any) => s.nameD);
            sizes.value = [...new Set(sizeNames)];
        }
    } catch (err) {
        console.error("Lỗi tải dữ liệu:", err);
    } finally {
        loading.value = false;
    }
};

const filteredProducts = computed(() => {
    let result = [...products.value];

    if (selectedCategory.value) {
        result = result.filter(p => p.category?.name === selectedCategory.value);
    }

    if (minPrice.value !== null) {
        result = result.filter(p => p.price >= (minPrice.value || 0));
    }
    if (maxPrice.value !== null) {
        result = result.filter(p => p.price <= (maxPrice.value || Infinity));
    }

    if (selectedColors.value.length > 0) {
        result = result.filter(p => 
            p.detailproductcolor?.some((c: any) => selectedColors.value.includes(c.detailColor?.id))
        );
    }

    if (selectedMaterials.value.length > 0) {
        result = result.filter(p => 
            p.detailproductmaterial?.some((m: any) => selectedMaterials.value.includes(m.detailMaterial?.id))
        );
    }

    if (selectedSizes.value.length > 0) {
        result = result.filter(p => 
            p.productDemensions?.some((d: any) => selectedSizes.value.includes(d.nameD))
        );
    }

    if (sortOrder.value === 'price-asc') {
        result.sort((a, b) => a.price - b.price);
    } else if (sortOrder.value === 'price-desc') {
        result.sort((a, b) => b.price - a.price);
    } else if (sortOrder.value === 'newest') {
        result.sort((a, b) => (b.id || 0) - (a.id || 0));
    }

    return result;
});

const clearFilters = () => {
    minPrice.value = null;
    maxPrice.value = null;
    selectedColors.value = [];
    selectedMaterials.value = [];
    selectedSizes.value = [];
    selectedCategory.value = null;
};

watch(() => route.query.category, (newCat) => {
    selectedCategory.value = newCat as string || null;
}, { immediate: true });

onMounted(() => {
    loadData();
});
</script>

<template>
    <div class="products-page py-4">
        <div class="container">
            <!-- Breadcrumb -->
            <nav aria-label="breadcrumb" class="mb-4">
                <ol class="breadcrumb m-0 shadow-sm px-3 py-2 bg-white rounded">
                    <li class="breadcrumb-item"><router-link to="/">Trang chủ</router-link></li>
                    <li class="breadcrumb-item active" aria-current="page">Sản phẩm</li>
                </ol>
            </nav>

            <div class="row g-4">
                <!-- Sidebar Filters -->
                <div class="col-lg-3">
                    <div class="filter-sidebar shadow-sm bg-white border-end h-100 rounded-3 overflow-hidden">
                        <div class="filter-header p-3 fw-bold text-uppercase" style="background: #eab308; color: white; font-size: 1.1rem;">
                            SẢN PHẨM
                        </div>
                        
                        <div class="p-3">
                            <!-- All Products Toggle -->
                            <div class="mb-4">
                                <a href="#" class="cat-link shadow-sm border" :class="{ active: !selectedCategory }" @click.prevent="clearFilters">
                                    <i class="fas fa-th-large me-2"></i>Tất cả sản phẩm
                                </a>
                            </div>

                            <!-- Price Filter -->
                            <h6 class="fw-bold mb-3">Chọn mức giá</h6>
                            <div class="price-inputs mb-4">
                                <div class="mb-2">
                                    <label class="small mb-1">Từ:</label>
                                    <input type="number" class="form-control" v-model="minPrice" placeholder="Nhập giá">
                                </div>
                                <div class="mb-0">
                                    <label class="small mb-1">Đến:</label>
                                    <input type="number" class="form-control" v-model="maxPrice" placeholder="Nhập giá">
                                </div>
                            </div>

                            <!-- Color Filter -->
                            <h6 class="fw-bold mb-3 d-flex align-items-center">Màu sắc</h6>
                            <div class="filter-grid mb-4">
                                <label v-for="c in colors" :key="c.id" class="checkbox-item">
                                    <input type="checkbox" :value="c.id" v-model="selectedColors"> {{ c.nameColor }}
                                </label>
                            </div>

                            <!-- Size Filter -->
                            <h6 class="fw-bold mb-3">Kích cỡ</h6>
                            <div class="filter-grid mb-4">
                                <label v-for="s in sizes" :key="s" class="checkbox-item">
                                    <input type="checkbox" :value="s" v-model="selectedSizes"> {{ s }}
                                </label>
                            </div>

                            <!-- Material Filter -->
                            <h6 class="fw-bold mb-3">Chất liệu</h6>
                            <div class="filter-grid mb-4">
                                <label v-for="m in materials" :key="m.id" class="checkbox-item">
                                    <input type="checkbox" :value="m.id" v-model="selectedMaterials"> {{ m.nameMaterial }}
                                </label>
                            </div>

                            <button class="btn btn-warning w-100 fw-bold mt-2" @click="clearFilters">
                                <i class="fas fa-sync-alt me-2"></i>Xóa bộ lọc
                            </button>
                        </div>
                    </div>
                </div>

                <!-- Main Content -->
                <div class="col-lg-9">
                    <!-- Sorting Header -->
                    <div class="d-flex align-items-center justify-content-between mb-4 bg-white p-3 rounded shadow-sm border">
                        <div class="fw-bold">
                            <span v-if="selectedCategory">Danh mục: <span class="text-primary">{{ selectedCategory }}</span></span>
                            <span v-else>Tất cả sản phẩm</span>
                            <small class="text-muted ms-2">({{ filteredProducts.length }} sản phẩm)</small>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            <span class="small text-muted d-none d-md-inline">Sắp xếp theo:</span>
                            <select class="form-select form-select-sm border-0 bg-light fw-bold" style="width: 160px; cursor: pointer;" v-model="sortOrder">
                                <option value="newest">Mới nhất</option>
                                <option value="price-asc">Giá tăng dần</option>
                                <option value="price-desc">Giá giảm dần</option>
                            </select>
                        </div>
                    </div>

                    <!-- Product Grid -->
                    <div v-if="loading" class="row g-4">
                        <div v-for="i in 8" :key="i" class="col-6 col-md-4 col-lg-3">
                            <div class="product-skeleton shadow-sm rounded"></div>
                        </div>
                    </div>

                    <div v-else-if="filteredProducts.length === 0" class="no-results text-center py-5 bg-white rounded shadow-sm border">
                        <img src="https://cdni.iconscout.com/illustration/premium/thumb/no-product-found-8266457-6651100.png" width="200">
                        <h5 class="text-muted mt-4">Không tìm thấy sản phẩm phù hợp!</h5>
                    </div>

                    <div v-else class="row g-4">
                        <div v-for="p in filteredProducts" :key="p.id" class="col-6 col-md-4 col-lg-3">
                            <div class="product-card-wrap">
                                <router-link :to="'/product/' + p.id" class="text-decoration-none">
                                    <div class="product-card h-100 bg-white shadow-sm border-0 rounded-4 overflow-hidden position-relative">
                                        <!-- Voucher Badge -->
                                        <div v-if="p.voucher" class="voucher-badge animate__animated animate__fadeInDown">
                                            <i class="fas fa-minus me-1"></i>{{ p.voucher.discountPercent }}%
                                        </div>
                                        
                                        <div class="product-img-box">
                                            <img :src="Helper.GetImageUrl(p.avatar)" :alt="p.name" class="img-fluid">
                                            <div class="product-actions">
                                                <button class="btn-action"><i class="far fa-heart"></i></button>
                                                <button class="btn-action"><i class="fas fa-share-alt"></i></button>
                                            </div>
                                        </div>
                                        
                                        <div class="p-3 text-center">
                                            <h6 class="product-name text-dark mb-2">{{ p.name }}</h6>
                                            <div class="d-flex flex-column align-items-center">
                                                <div v-if="p.voucher" class="old-price small text-muted text-decoration-line-through">
                                                    {{ Helper.ToMoney(p.price) }}
                                                </div>
                                                <div class="price fw-bold text-danger fs-5">
                                                    {{ Helper.ToMoney(p.voucher ? Math.round(p.price * (1 - p.voucher.discountPercent / 100)) : p.price) }}
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </router-link>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Chat Bubble -->
        <div class="chat-wrapper position-fixed" style="bottom: 30px; right: 30px; z-index: 1000;">
            <div class="chat-tooltip animate__animated animate__fadeInUp mb-3 bg-white p-3 rounded shadow-lg border-0 position-relative">
                <i class="fas fa-times position-absolute top-0 end-0 p-2 cursor-pointer text-muted" style="font-size: 0.7rem;"></i>
                <div class="d-flex align-items-center gap-3">
                    <img src="/images/logotech.png" width="35" class="rounded-circle shadow-sm">
                    <div style="font-size: 0.8rem; line-height: 1.3;">
                        <strong class="d-block mb-1">HC Shop</strong>
                        HC shop hân hạnh phục vụ! Anh/chị có cần tư vấn sản phẩm hay hỗ trợ gì không ạ?
                    </div>
                </div>
            </div>
            <div class="chat-icons d-flex flex-column gap-2 align-items-end">
                <div class="icon-circle bg-success shadow-sm animate__animated animate__bounceIn">
                    <i class="fab fa-whatsapp text-white"></i>
                </div>
                <div class="icon-circle bg-warning shadow-sm animate__animated animate__bounceIn" style="animation-delay: 0.1s;">
                    <i class="fas fa-comment shadow-sm text-white"></i>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.products-page {
    background: #f8fafc;
    min-height: 80vh;
}

.filter-sidebar {
    position: sticky;
    top: 20px;
}

.cat-link {
    display: block;
    padding: 8px 12px;
    border-radius: 6px;
    text-decoration: none;
    color: #475569;
    font-size: 0.9rem;
    transition: all 0.2s ease;
}

.cat-link:hover {
    background: #f1f5f9;
    color: #1e293b;
    padding-left: 15px;
}

.cat-link.active {
    background: #eff6ff;
    color: #2563eb;
    font-weight: 700;
}

.filter-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 10px;
}

.checkbox-item {
    font-size: 0.9rem;
    color: #475569;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 8px;
}

.checkbox-item input {
    width: 16px;
    height: 16px;
    cursor: pointer;
}

/* Product Card Styling */
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

.voucher-badge {
    position: absolute;
    top: 10px;
    left: 10px;
    background: #ef4444;
    color: white;
    padding: 4px 10px;
    border-radius: 20px;
    font-size: 0.75rem;
    font-weight: 700;
    z-index: 5;
    box-shadow: 0 2px 5px rgba(239, 68, 68, 0.4);
}

/* Skeleton Loader */
.product-skeleton {
    height: 300px;
    background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
    background-size: 200% 100%;
    animation: loading 1.5s infinite;
}

@keyframes loading {
    0% { background-position: 200% 0; }
    100% { background-position: -200% 0; }
}

/* Chat UI Improvements */
.icon-circle {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.5rem;
    cursor: pointer;
    transition: transform 0.2s;
}

.icon-circle:hover {
    transform: scale(1.1);
}

.chat-tooltip {
    width: 250px;
}

.chat-tooltip::after {
    content: '';
    position: absolute;
    bottom: -10px;
    right: 20px;
    border-left: 10px solid transparent;
    border-right: 10px solid transparent;
    border-top: 10px solid white;
}

.breadcrumb-item a {
    color: #475569;
    text-decoration: none;
}
.breadcrumb-item a:hover {
    color: #f1c40f;
}
</style>
