<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import axios from 'axios';
import { useRoute, useRouter } from 'vue-router';
import { Product } from '../../interfaces/app';
import Helper from '../../helper/helper';
import { state, MyApp } from '../../app/MyApp';

const route = useRoute();
const router = useRouter();
const products = ref<Product[]>([]);
const loading = ref(false);

// Filter master data
const colors = ref<any[]>([]);
const materials = ref<any[]>([]);
const sizes = ref<any[]>([]);

// Selected Filters
const selectedCategory = ref<string | null>(null);
const searchKeyword = ref('');
const minPrice = ref<number | null>(null);
const maxPrice = ref<number | null>(null);
const selectedColors = ref<number[]>([]);
const selectedMaterials = ref<number[]>([]);
const selectedSizes = ref<string[]>([]);
const sortOrder = ref('newest');
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

const filteredProducts = computed((): Product[] => {
    let result = [...products.value] as Product[];

    if (searchKeyword.value) {
        const kw = searchKeyword.value.toLowerCase();
        result = result.filter(p =>
            (p.name?.toLowerCase().includes(kw)) ||
            (p.category?.name?.toLowerCase().includes(kw))
        );
    }

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
    searchKeyword.value = '';
    router.push('/products');
};

watch(() => route.query.category, (newCat) => {
    selectedCategory.value = newCat as string || null;
}, { immediate: true });

watch(() => route.query.search, (newSearch) => {
    searchKeyword.value = typeof newSearch === 'string' ? newSearch.trim() : '';
}, { immediate: true });

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

onMounted(() => {
    loadData();
    loadWishlists();
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
                            <span v-if="searchKeyword">Kết quả tìm kiếm: <span class="text-primary">"{{ searchKeyword }}"</span></span>
                            <span v-else-if="selectedCategory">Danh mục: <span class="text-primary">{{ selectedCategory }}</span></span>
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
                                        <!-- Product Discount Badge (Lấy từ % giảm giá của sản phẩm) -->
                                        <div v-if="p.discountPercent" class="discount-badge animate__animated animate__fadeInDown">
                                            -{{ p.discountPercent }}%
                                        </div>
                                        
                                        <div class="product-img-box">
                                            <img :src="Helper.GetImageUrl(p.avatar)" :alt="p.name" class="img-fluid">
                                            <!-- Cụm nút Wishlist & Share -->
                                            <div class="product-side-actions">
                                                <button class="side-btn" @click.stop.prevent="addToWishlist(p)" title="Yêu thích">
                                                    <i :class="wishlisted[p.id] ? 'fas fa-heart text-danger' : 'far fa-heart'"></i>
                                                </button>
                                                <button class="side-btn" @click.stop.prevent="shareProduct(p)" title="Chia sẻ">
                                                    <i class="fas fa-share-alt"></i>
                                                </button>
                                            </div>
                                        </div>
                                        
                                        <div class="p-3 text-center">
                                            <h6 class="product-name text-dark mb-2">{{ p.name }}</h6>
                                            <div class="d-flex flex-column align-items-center">
                                                <!-- Giá cũ nếu có giảm giá -->
                                                <div v-if="p.discountPercent" class="old-price small text-muted text-decoration-line-through">
                                                    {{ Helper.ToMoney(p.price) }}
                                                </div>
                                                <!-- Giá thực tế sau giảm -->
                                                <div class="price fw-bold text-danger fs-5">
                                                    {{ Helper.ToMoney(p.discountPercent ? Math.round(p.price * (1 - p.discountPercent / 100)) : p.price) }}
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

.breadcrumb-item a {
    color: #475569;
    text-decoration: none;
}
.breadcrumb-item a:hover {
    color: #f1c40f;
}
</style>
