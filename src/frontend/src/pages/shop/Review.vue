<template>
    <div class="review-page py-4 bg-light min-vh-100">
        <div class="container">
            <!-- Breadcrumb -->
            <nav aria-label="breadcrumb" class="mb-4">
                <ol class="breadcrumb m-0 shadow-sm px-3 py-2 bg-white rounded">
                    <li class="breadcrumb-item"><router-link to="/">Trang chủ</router-link></li>
                    <li class="breadcrumb-item fw-bold text-success" aria-current="page">Hướng dẫn / Review</li>
                </ol>
            </nav>

            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2 class="fw-bold m-0 border-start border-success border-5 ps-3 py-1">Hướng Dẫn / Review</h2>
                
                <div class="search-box position-relative" style="width: 300px;">
                    <input type="text" class="form-control rounded-pill pe-5" placeholder="Tìm kiếm bài viết..." v-model="searchKeyword" @keyup.enter="refreshData">
                    <button class="btn position-absolute top-50 end-0 translate-middle-y rounded-pill" @click="refreshData">
                        <i class="fas fa-search text-muted"></i>
                    </button>
                </div>
            </div>

            <div class="row g-4">
                <!-- Main Content -->
                <div class="col-lg-9">
                    <div v-if="loading" class="text-center py-5">
                        <div class="spinner-border text-success" role="status">
                            <span class="visually-hidden">Loading...</span>
                        </div>
                    </div>
                    <div v-else-if="posts.length === 0" class="text-center py-5 bg-white rounded shadow-sm">
                        <h5 class="text-muted">Không có bài viết nào!</h5>
                    </div>
                    <div v-else class="row g-4">
                        <div class="col-md-6 col-lg-6" v-for="post in posts" :key="post.id">
                            <div class="card h-100 border-0 shadow-sm rounded-4 overflow-hidden post-card">
                                <div class="img-container">
                                    <img :src="Helper.GetImageUrl(post.image)" class="card-img-top w-100 h-100 object-fit-cover">
                                </div>
                                <div class="card-body p-4 d-flex flex-column">
                                    <div class="text-success small fw-bold text-uppercase mb-2" v-if="post.category">
                                        {{ post.category.name }}
                                    </div>
                                    <h5 class="card-title fw-bold text-dark mb-3 post-title">{{ post.title }}</h5>
                                    <p class="card-text text-muted small post-desc flex-grow-1">{{ post.shortDescription }}</p>
                                    <hr class="text-muted opacity-25 my-3">
                                    <div class="d-flex justify-content-between align-items-center mt-auto">
                                        <div class="text-muted small">
                                            <i class="far fa-calendar-alt me-1"></i> {{ formatDate(post.createdAt) }}
                                        </div>
                                        <router-link :to="'/guide/' + (post.slug || post.id)" class="text-success fw-bold text-decoration-none small">
                                            Xem chi tiết <i class="fas fa-chevron-right ms-1"></i>
                                        </router-link>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Pagination (if many pages) -->
                    <div class="mt-4 d-flex justify-content-center" v-if="totalPages > 1">
                        <button class="btn btn-outline-success mx-1" :disabled="page === 0" @click="page--; loadPosts()">Trước</button>
                        <span class="mx-3 align-self-center">Trang {{ page + 1 }} / {{ totalPages }}</span>
                        <button class="btn btn-outline-success mx-1" :disabled="page >= totalPages - 1" @click="page++; loadPosts()">Sau</button>
                    </div>
                </div>

                <!-- Sidebar -->
                <div class="col-lg-3">
                    <div class="card border-0 shadow-sm rounded-4">
                        <div class="card-body p-4">
                            <h5 class="fw-bold mb-4 border-bottom border-success border-2 pb-2 d-inline-block">Danh mục tin tức</h5>
                            
                            <ul class="list-unstyled m-0 cat-list">
                                <li class="mb-3">
                                    <a href="#" class="text-decoration-none fw-bold" 
                                       :class="!selectedCategory ? 'text-success' : 'text-dark'" 
                                       @click.prevent="selectCategory(null)">
                                        <i class="fas fa-chevron-right me-2 small text-muted"></i> Tất cả bài viết
                                    </a>
                                </li>
                                <li class="mb-3" v-for="cat in categories" :key="cat.id">
                                    <a href="#" class="text-decoration-none fw-bold"
                                       :class="selectedCategory === cat.name ? 'text-success' : 'text-dark'"
                                       @click.prevent="selectCategory(cat.name)">
                                        <i class="fas fa-chevron-right me-2 small text-muted"></i> {{ cat.name }}
                                    </a>
                                </li>
                            </ul>
                        </div>
                    </div>
                    
                    <!-- Decor images/badges if any can go here -->
                    <div class="mt-4 text-center d-none d-lg-block">
                        <img :src="state.generalImages?.['Logo'] ? Helper.GetImageUrl(state.generalImages['Logo']) : '/images/logotech.png'" class="img-fluid rounded-circle shadow-sm" style="width: 120px; opacity: 0.8">
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import Helper from '../../helper/helper';
import { state } from '../../app/MyApp';

const posts = ref([]);
const categories = ref([]);
const loading = ref(false);
const searchKeyword = ref('');
const selectedCategory = ref(null);
const page = ref(0);
const size = ref(10);
const totalPages = ref(0);

const loadCategories = async () => {
    try {
        const res = await axios.get('http://localhost:8081/category/list');
        categories.value = res.data;
    } catch (err) {}
};

const loadPosts = async () => {
    loading.value = true;
    try {
        const res = await axios.get('http://localhost:8081/post/all', {
            params: {
                page: page.value,
                size: size.value,
                search: searchKeyword.value || null,
                categoryName: selectedCategory.value || null
            }
        });
        posts.value = res.data.content;
        totalPages.value = res.data.totalPages;
    } catch (err) {
        console.error("Lỗi tải bài viết");
    } finally {
        loading.value = false;
    }
};

const refreshData = () => {
    page.value = 0;
    loadPosts();
};

const selectCategory = (catName) => {
    selectedCategory.value = catName;
    refreshData();
};

const formatDate = (dateString) => {
    if (!dateString) return '---';
    const d = new Date(dateString);
    return d.toLocaleDateString('vi-VN');
};

onMounted(() => {
    loadCategories();
    loadPosts();
});
</script>

<style scoped>
.img-container {
    height: 220px;
    background: #f8f9fa;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
}

.post-card {
    transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.post-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(0,0,0,0.1) !important;
    cursor: pointer;
}

.post-title {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    line-height: 1.4;
}

.post-desc {
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.cat-list a {
    transition: color 0.2s ease;
}
.cat-list a:hover {
    color: #198754 !important;
}

.border-success {
    border-color: #198754 !important;
}
.text-success {
    color: #198754 !important;
}
.bg-success {
    background-color: #198754 !important;
}
</style>
