<template>
  <div class="container-fluid py-4">
    <div class="row mb-4 align-items-center">
      <div class="col-md-4">
        <h2 class="font-weight-bold">Quản lý bài viết</h2>
      </div>
      <div class="col-md-8 d-flex justify-content-md-end align-items-center flex-wrap gap-2">
        <button class="btn btn-success font-weight-bold mr-2" @click="$router.push('/admin/post/create')">
          <i class="fas fa-plus mr-1"></i> Thêm bài viết
        </button>
        <button class="btn btn-primary font-weight-bold mr-2" @click="exportToExcel">
          <i class="fas fa-file-excel mr-1"></i> Xuất Excel
        </button>
        <div class="d-flex align-items-center bg-secondary rounded px-2 py-1 mr-2" style="height: 38px;">
          <form @submit.prevent="importExcel" class="d-flex align-items-center">
            <span class="text-white mr-2"><i class="fas fa-file-import"></i> Nhập Excel</span>
            <input type="file" ref="fileInput" @change="onFileChange" class="form-control-file text-white" accept=".xlsx,.xls" style="max-width: 200px; font-size: 0.8rem;">
            <button type="submit" class="d-none"></button>
          </form>
        </div>
      </div>
    </div>

    <div class="card shadow-sm border-0 root-card">
      <div class="card-body">
        <!-- Toolbar Filters -->
        <div class="d-flex justify-content-between mb-4 flex-wrap">
          <div class="d-flex align-items-center">
            <input type="text" v-model="searchQuery" @keyup.enter="fetchPosts(0)" 
                   class="form-control mr-3" placeholder="Tìm kiếm tiêu đề hoặc danh mục..." style="width: 250px;">
            <select v-model="selectedCategory" @change="fetchPosts(0)" class="form-control custom-select" style="width: 200px;">
              <option value="Tất cả danh mục">Tất cả danh mục</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.name">{{ cat.name }}</option>
            </select>
          </div>
          
          <div class="d-flex align-items-center">
            <select v-model="sortOrder" @change="fetchPosts(0)" class="form-control custom-select mr-3" style="width: 150px;">
              <option value="newest">Mới nhất</option>
              <option value="oldest">Cũ nhất</option>
            </select>
            <select v-model="size" @change="fetchPosts(0)" class="form-control custom-select" style="width: 120px;">
              <option :value="5">Hiển thị 5</option>
              <option :value="10">Hiển thị 10</option>
              <option :value="20">Hiển thị 20</option>
            </select>
          </div>
        </div>

        <!-- Table -->
        <div class="table-responsive">
          <table class="table table-hover align-middle">
            <thead class="bg-light">
              <tr class="text-muted" style="font-size: 0.9rem;">
                <th>STT</th>
                <th>Ảnh</th>
                <th>Tiêu đề</th>
                <th style="width: 30%;">Nội dung tóm tắt</th>
                <th>Danh mục</th>
                <th>Tác giả</th>
                <th>Ngày tạo</th>
                <th>Ngày sửa</th>
                <th class="text-center">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="9" class="text-center py-4">
                  <div class="spinner-border text-primary" role="status">
                    <span class="sr-only">Loading...</span>
                  </div>
                </td>
              </tr>
              <tr v-else-if="posts.length === 0">
                <td colspan="9" class="text-center py-4 text-muted">
                  Không có dữ liệu bài viết
                </td>
              </tr>
              <tr v-for="(item, index) in posts" :key="item.id" v-else>
                <td>{{ index + 1 + page * size }}</td>
                <td>
                  <img v-if="item.image" :src="Helper.GetImageUrl(item.image)" style="width: 40px; height: 40px; object-fit: contain;">
                </td>
                <td>
                  <a href="#" class="text-primary fw-bold text-decoration-none">{{ item.title }}</a>
                </td>
                <td class="text-muted" style="font-size: 0.9rem;">{{ truncate(item.shortDescription, 60) }}</td>
                <td>{{ item.category?.name }}</td>
                <td>{{ item.authorName }}</td>
                <td>{{ formatDate(item.createdAt) }}</td>
                <td>{{ formatDate(item.updatedAt) }}</td>
                <td class="text-center">
                  <button @click="$router.push('/admin/post/edit/' + item.id)" class="btn btn-sm btn-outline-primary mr-1" title="Sửa">
                    <i class="fas fa-edit"></i>
                  </button>
                  <button @click="deletePost(item.id)" class="btn btn-sm btn-outline-danger" title="Xóa">
                    <i class="fas fa-trash-alt"></i>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div class="d-flex justify-content-center align-items-center mt-4">
          <button class="btn btn-light border mr-3" :disabled="page === 0" @click="fetchPosts(page - 1)">
            Trước
          </button>
          <span class="font-weight-bold">Trang {{ totalPages > 0 ? page + 1 : 0 }} / {{ totalPages }}</span>
          <button class="btn btn-light border ml-3" :disabled="page >= totalPages - 1 || totalPages === 0" @click="fetchPosts(page + 1)">
            Sau
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import Helper from '../../helper/helper';

const posts = ref([]);
const categories = ref([]);
const loading = ref(false);

const page = ref(0);
const size = ref(10);
const totalPages = ref(0);
const totalElements = ref(0);
const searchQuery = ref('');
const selectedCategory = ref('Tất cả danh mục');
const sortOrder = ref('newest');

const fileInput = ref(null);
const selectedFile = ref(null);

const loadCategories = async () => {
  try {
    const res = await axios.get('http://localhost:8081/category/list');
    categories.value = res.data;
  } catch (err) {}
};

const fetchPosts = async (newPage = 0) => {
  if (newPage < 0 || (totalPages.value > 0 && newPage >= totalPages.value)) return;
  
  loading.value = true;
  try {
    const res = await axios.get(`http://localhost:8081/post/admin/all`, {
      params: {
        page: newPage,
        size: size.value,
        search: searchQuery.value || null,
        categoryName: selectedCategory.value,
        sort: sortOrder.value
      }
    });
    posts.value = res.data.content;
    page.value = res.data.number;
    totalPages.value = res.data.totalPages;
    totalElements.value = res.data.totalElements;
  } catch (error) {
    alert("Lỗi tải danh sách bài viết");
  } finally {
    loading.value = false;
  }
};



const deletePost = async (id) => {
  if (!confirm('Bạn có chắc chắn muốn xóa bài viết này?')) return;
  try {
    await axios.delete(`http://localhost:8081/post/admin/delete/${id}`);
    alert("Đã xóa bài viết");
    fetchPosts(page.value);
  } catch (error) {
    alert("Lỗi khi xóa");
  }
};

const exportToExcel = () => {
    window.location.href = `http://localhost:8081/post/export-excel`;
};

const onFileChange = (e) => {
  selectedFile.value = e.target.files[0];
  importExcel();
};

const importExcel = async () => {
  if (!selectedFile.value) return;
  
  const formData = new FormData();
  formData.append('file', selectedFile.value);
  
  try {
    loading.value = true;
    await axios.post('http://localhost:8081/post/admin/import-excel', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    alert("Nhập Excel thành công!");
    fetchPosts(0);
  } catch (err) {
    alert("Lỗi nhập file: " + err.message);
  } finally {
    loading.value = false;
    fileInput.value.value = null;
    selectedFile.value = null;
  }
};

const formatDate = (dateString) => {
  if (!dateString) return '---';
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN');
};

const truncate = (text, len) => {
    if (!text) return '';
    return text.length > len ? text.substring(0, len) + '...' : text;
};

onMounted(() => {
  loadCategories();
  fetchPosts();
});
</script>

<style scoped>
.root-card {
  border-radius: 12px;
}
table th {
  font-weight: 600;
  vertical-align: middle;
}
table td {
  vertical-align: middle;
}
.custom-select {
    cursor: pointer;
}
</style>
