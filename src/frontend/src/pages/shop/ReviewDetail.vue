<template>
    <div class="review-detail bg-light py-5 min-vh-100">
        <div class="container">
            <!-- Breadcrumb -->
            <nav aria-label="breadcrumb" class="mb-4">
                <ol class="breadcrumb m-0 shadow-sm px-3 py-2 bg-white rounded">
                    <li class="breadcrumb-item"><router-link to="/">Trang chủ</router-link></li>
                    <li class="breadcrumb-item"><router-link to="/guides">Hướng dẫn</router-link></li>
                    <li class="breadcrumb-item active fw-bold text-success" aria-current="page">{{ post.title || 'Đang tải...' }}</li>
                </ol>
            </nav>

            <div class="row g-4 loader-wrapper" v-if="loading">
                <div class="col-12 text-center py-5">
                    <div class="spinner-border text-success" role="status"></div>
                </div>
            </div>

            <div class="row g-4" v-else-if="post && post.id">
                <div class="col-lg-8">
                    <!-- Header -->
                    <div class="mb-4 bg-white p-4 rounded shadow-sm">
                         <div class="d-inline-block text-success bg-success-subtle fw-bold rounded-pill px-3 py-1 text-uppercase small mb-3 border border-success" v-if="post.category">
                            {{ post.category.name }}
                        </div>
                        <h1 class="fw-bold mb-4">{{ post.title }}</h1>
                        
                        <div class="d-flex flex-wrap gap-4 text-muted small border-bottom pb-3">
                            <span class="d-flex align-items-center">
                                <i class="far fa-calendar-alt me-2"></i> Ngày đăng: {{ formatDate(post.createdAt) }}
                            </span>
                            <span class="d-flex align-items-center">
                                <i class="far fa-calendar-check me-2"></i> Cập nhật: {{ formatDate(post.updatedAt) }}
                            </span>
                            <span class="d-flex align-items-center">
                                <i class="fas fa-user-edit me-2"></i> Tác giả: {{ post.authorName || 'Admin' }}
                            </span>
                        </div>

                        <!-- Table of contents - only if headings exist -->
                        <div class="card mt-4 bg-light border-0" v-if="tocItems.length > 0">
                            <div class="card-header bg-light d-flex justify-content-between align-items-center border-0 py-3 cursor-pointer" @click="tocOpen = !tocOpen">
                                <h6 class="m-0 fw-bold"><i class="fas fa-list me-2"></i> Mục lục bài viết</h6>
                                <button class="btn btn-sm btn-light border"><i class="fas" :class="tocOpen ? 'fa-minus' : 'fa-plus'"></i></button>
                            </div>
                            <div class="card-body bg-white border border-top-0 pt-3" v-show="tocOpen">
                                <ol class="m-0 ps-3">
                                    <li class="mb-2" v-for="(sec, idx) in tocItems" :key="'toc'+idx">
                                        <a :href="'#heading-' + sec.origIdx" class="text-decoration-none text-dark hover-success">{{ sec.heading }}</a>
                                    </li>
                                </ol>
                            </div>
                        </div>
                    </div>

                    <!-- Content body -->
                    <div class="bg-white p-4 rounded shadow-sm content-body">
                        <!-- Post image -->
                        <div class="text-center mb-4" v-if="post.image">
                            <img :src="post.image" :alt="post.title"
                                 class="img-fluid rounded shadow-sm"
                                 style="max-height: 400px; object-fit: cover; max-width: 100%;"
                                 @error="$event.target.style.display='none'">
                        </div>

                        <!-- Short description as intro -->
                        <div class="mb-4 text-muted fst-italic border-start border-success border-4 ps-3 py-2" v-if="post.shortDescription">
                            {{ post.shortDescription }}
                        </div>

                        <!-- Structured sections (JSON) -->
                        <template v-if="hasSections">
                            <div v-for="(sec, idx) in sections" :key="'sec'+idx" class="mb-5">
                                <!-- Only render heading block if heading has text -->
                                <h3 v-if="sec.heading && sec.heading.trim()"
                                    :id="'heading-' + idx"
                                    class="section-title fw-bold mb-4 d-flex align-items-center">
                                    <span class="title-bar bg-success rounded d-inline-block me-3"></span>
                                    {{ sec.heading }}
                                </h3>

                                <!-- Only render content if it has text -->
                                <div v-if="sec.content && sec.content.trim()"
                                     class="section-desc mb-4"
                                     style="white-space: pre-wrap;">{{ sec.content }}</div>

                                <!-- Table (optional) -->
                                <div class="table-responsive" v-if="sec.showTable && sec.tableData && sec.tableData.length > 0 && sec.tableData[0].some(c => c)">
                                    <table class="table table-bordered table-striped custom-table">
                                        <thead>
                                            <tr class="bg-success text-white">
                                                <th v-for="(col, cIdx) in sec.tableData[0]" :key="'h'+cIdx" class="text-center">{{ col }}</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <tr v-for="(row, rIdx) in sec.tableData.slice(1)" :key="'tr'+rIdx">
                                                <td v-for="(cell, cIdx) in row" :key="'c'+cIdx">{{ cell }}</td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </template>

                        <!-- Fallback: plain text content -->
                        <div v-else-if="post.content && post.content.trim()" style="white-space: pre-wrap;">
                            {{ post.content }}
                        </div>

                        <!-- Nothing to show at all -->
                        <div v-else-if="!post.shortDescription" class="text-center text-muted py-4">
                            <i class="fas fa-file-alt fa-3x mb-3 d-block opacity-25"></i>
                            Bài viết chưa có nội dung chi tiết.
                        </div>

                        <!-- Share section -->
                        <div class="mt-5 pt-4 border-top">
                            <h6 class="mb-3 fw-bold text-dark">Chia sẻ bài viết:</h6>
                            <div class="d-flex gap-2 share-buttons">
                                <button class="btn btn-icon btn-messenger" @click="share('messenger')" title="Chia sẻ qua Messenger"><i class="fab fa-facebook-messenger"></i></button>
                                <button class="btn btn-icon btn-tiktok" @click="share('tiktok')" title="Chia sẻ qua TikTok"><i class="fab fa-tiktok"></i></button>
                                <button class="btn btn-icon btn-youtube" @click="share('youtube')" title="Chia sẻ qua YouTube"><i class="fab fa-youtube"></i></button>
                                <button class="btn btn-icon btn-facebook" @click="share('facebook')" title="Chia sẻ qua Facebook"><i class="fab fa-facebook-f"></i></button>
                                <button class="btn btn-icon btn-zalo" @click="share('zalo')" title="Chia sẻ qua Zalo"><span class="fw-bold" style="font-size: 0.75rem;">Zalo</span></button>
                                <button class="btn btn-icon btn-copy" @click="copyLink" title="Sao chép liên kết"><i class="fas fa-link"></i></button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Sidebar -->
                <div class="col-lg-4">
                    <div class="card border-0 shadow-sm rounded mb-4">
                        <div class="card-body p-4">
                            <h5 class="fw-bold mb-4 border-bottom border-success border-2 pb-2 d-inline-block">Bài viết mới nhất</h5>
                            <div v-for="item in recentPosts" :key="item.id" class="d-flex mb-3 align-items-center border-bottom pb-3 last-no-border">
                                <img v-if="item.image" :src="Helper.GetImageUrl(item.image)" class="rounded object-fit-cover me-3" style="width: 70px; height: 70px;">
                                <div>
                                    <h6 class="fw-bold mb-1">
                                        <router-link :to="'/guide/' + (item.slug || item.id)" class="text-decoration-none text-dark recent-title">{{ item.title }}</router-link>
                                    </h6>
                                    <small class="text-muted">{{ formatDate(item.createdAt) }}</small>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="card border-0 shadow-sm rounded text-center promotional-banner overflow-hidden" style="background-color: #92c5ef;">
                        <div class="card-body p-5">
                            <img src="/images/logotech.png" alt="Logo" class="img-fluid mb-4 rounded-circle bg-white p-2 shadow" style="width: 100px;">
                            <h6 class="fw-bold mb-4 px-3" style="line-height: 1.5; color: #1e3a8a;">HC SHOP - Hệ thống cửa hàng cầu lông hàng đầu Việt Nam</h6>
                            <router-link to="/products" class="btn btn-success fw-bold px-4 rounded-pill shadow-sm">Mua sắm ngay</router-link>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="row g-4" v-else>
                <div class="col-12 text-center py-5">
                    <h4 class="text-muted">{{ errorMsg || 'Không tìm thấy bài viết!' }}</h4>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import Helper from '../../helper/helper';

const route = useRoute();
const post = ref({});
const recentPosts = ref([]);
const sections = ref([]);
const loading = ref(true);
const tocOpen = ref(true);
const errorMsg = ref('');

const isSectionsArray = computed(() => Array.isArray(sections.value));
const hasSections = computed(() => isSectionsArray.value && sections.value.length > 0);
const tocItems = computed(() => {
    if (!isSectionsArray.value) return [];
    return sections.value
        .map((sec, idx) => ({ ...sec, origIdx: idx }))
        .filter(sec => sec.heading && sec.heading.trim() !== '');
});

const parseContent = (content) => {
    if (!content) return [];
    try {
        const trimmed = content.trim();
        if (trimmed.startsWith('[')) {
            return JSON.parse(trimmed);
        }
    } catch (e) {}
    return [];
};

const fetchPostDetail = async () => {
    loading.value = true;
    errorMsg.value = '';
    post.value = {};
    sections.value = [];

    const slugOrId = route.params.slug;
    
    try {
        let pRes;
        const isNumericId = !isNaN(slugOrId) && slugOrId !== '';
        
        if (isNumericId) {
            // Direct numeric ID lookup
            pRes = await axios.get(`http://localhost:8081/post/${slugOrId}`, { timeout: 10000 });
        } else {
            // Slug lookup, fallback to search if needed
            try {
                pRes = await axios.get(`http://localhost:8081/post/slug/${slugOrId}`, { timeout: 10000 });
            } catch (slugErr) {
                console.warn('Slug lookup failed, trying search:', slugErr.message);
                errorMsg.value = 'Không tìm thấy bài viết.';
                loading.value = false;
                return;
            }
        }

        if (!pRes || !pRes.data || !pRes.data.id) {
            errorMsg.value = 'Không tìm thấy bài viết.';
            loading.value = false;
            return;
        }

        post.value = pRes.data;
        sections.value = parseContent(pRes.data.content);
        fetchRecent();

    } catch (err) {
        console.error('Lỗi tải bài viết:', err);
        errorMsg.value = 'Lỗi tải bài viết: ' + (err.message || 'Không xác định');
    } finally {
        loading.value = false;
    }
};

const fetchRecent = async () => {
    try {
        const res = await axios.get(`http://localhost:8081/post/all`, { params: { size: 5 }, timeout: 8000 });
        recentPosts.value = (res.data.content || []).filter(x => x.id !== post.value.id).slice(0, 4);
    } catch (e) {
        console.warn('Không tải được bài viết mới nhất');
    }
};

const formatDate = (dateString) => {
    if (!dateString) return '---';
    return new Date(dateString).toLocaleDateString('vi-VN');
};

const share = (platform) => {
    const url = encodeURIComponent(window.location.href);
    
    let shareUrl = '';
    switch (platform) {
        case 'facebook':
            shareUrl = `https://www.facebook.com/sharer/sharer.php?u=${url}`;
            break;
        case 'messenger':
            shareUrl = `https://www.facebook.com/dialog/send?link=${url}&app_id=291494419107518&redirect_uri=${url}`; // generic app_id just for UI or use intent
            break;
        case 'zalo':
            shareUrl = `https://zalo.me/share?url=${url}`;
            break;
        case 'tiktok':
        case 'youtube':
            alert(`Chia sẻ lên ${platform} yêu cầu ứng dụng tương ứng. Vui lòng copy link!`);
            return;
        default:
            break;
    }
    if (shareUrl) {
        window.open(shareUrl, '_blank', 'width=600,height=400');
    }
};

const copyLink = () => {
    navigator.clipboard.writeText(window.location.href).then(() => {
        alert('Đã copy liên kết bài viết!');
    }).catch(() => {
        alert('Không thể copy liên kết, vui lòng thực hiện thủ công.');
    });
};

watch(() => route.params.slug, (newVal) => {
    if (newVal) fetchPostDetail();
});

onMounted(() => {
    fetchPostDetail();
});
</script>

<style scoped>
.bg-success-subtle {
    background-color: #e8f5e9 !important;
}
.cursor-pointer {
    cursor: pointer;
}
.section-title {
    font-size: 1.5rem;
    color: #333;
}
.title-bar {
    width: 6px;
    height: 30px;
}
.hover-success:hover {
    color: #198754 !important;
}
.content-body {
    font-size: 1.05rem;
    line-height: 1.7;
    color: #4a5568;
}
.custom-table th {
    background-color: #198754;
    color: #fff;
    border-color: #157347;
}
.custom-table td {
    vertical-align: middle;
}
.recent-title {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    transition: color 0.2s;
}
.recent-title:hover {
    color: #198754 !important;
}
.last-no-border:last-child {
    border-bottom: none !important;
    padding-bottom: 0 !important;
    margin-bottom: 0 !important;
}
.promotional-banner {
    transition: transform 0.3s ease;
}
.promotional-banner:hover {
    transform: translateY(-5px);
}

/* Share Buttons */
.btn-icon {
    width: 42px;
    height: 42px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    border: none;
    transition: transform 0.2s;
    font-size: 1.1rem;
    box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}
.btn-icon:hover {
    transform: scale(1.1);
    color: white;
}
.btn-messenger { background: linear-gradient(45deg, #FF6968, #A334FA, #0695FF); }
.btn-tiktok { background: #000000; }
.btn-youtube { background: #FF0000; }
.btn-facebook { background: #1877F2; }
.btn-zalo { background: #0068FF; }
.btn-copy { background: #4B4B4B; }
</style>
