<template>
  <div class="container-fluid py-4">
    <div class="row mb-4">
      <div class="col-12">
        <h2 class="font-weight-bold">Quản lý Ưa thích</h2>
      </div>
    </div>

    <div class="card shadow-sm border-0 root-card">
      <div class="card-body">
        <!-- Toolbar -->
        <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap">
          <div class="d-flex align-items-center mb-2 mb-md-0">
            <span class="mr-2">Hiển thị:</span>
            <select v-model="size" @change="fetchWishlists(0)" class="form-control form-control-sm d-inline-block w-auto mr-3 custom-select">
              <option :value="5">5</option>
              <option :value="10">10</option>
              <option :value="20">20</option>
              <option :value="50">50</option>
            </select>
            <button @click="exportToExcel" class="btn btn-success btn-sm font-weight-bold ml-2">
              <i class="fas fa-file-excel mr-1"></i> Export Wishlists
            </button>
          </div>
          
          <div class="d-flex align-items-center">
            <div class="input-group input-group-sm mr-3" style="width: 250px;">
              <input type="text" v-model="searchQuery" @keyup.enter="fetchWishlists(0)" 
                     class="form-control" placeholder="Tìm theo người dùng, sản phẩm...">
              <div class="input-group-append">
                <button class="btn btn-outline-secondary" type="button" @click="fetchWishlists(0)">
                  <i class="fas fa-search"></i>
                </button>
              </div>
            </div>
            
            <span class="mr-2">Sắp xếp theo:</span>
            <select class="form-control form-control-sm d-inline-block w-auto custom-select">
              <option value="newest">Mới nhất</option>
              <option value="oldest">Cũ nhất</option>
            </select>
          </div>
        </div>

        <!-- Table -->
        <div class="table-responsive">
          <table class="table table-hover text-center align-middle">
            <thead class="bg-warning text-white">
              <tr>
                <th>STT</th>
                <th>Tên người dùng</th>
                <th>Tên sản phẩm</th>
                <th>Ngày tạo</th>
                <th>Ngày sửa</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="6" class="text-center py-4">
                  <div class="spinner-border text-primary" role="status">
                    <span class="sr-only">Loading...</span>
                  </div>
                </td>
              </tr>
              <tr v-else-if="wishlists.length === 0">
                <td colspan="6" class="text-center py-4 text-muted">
                  Không có dữ liệu
                </td>
              </tr>
              <tr v-for="(item, index) in wishlists" :key="item.id" v-else>
                <td>{{ index + 1 + page * size }}</td>
                <td>{{ item.user?.nameUser }}</td>
                <td>{{ item.product?.name }}</td>
                <td>{{ formatDate(item.createdAt) }}</td>
                <td>{{ formatDate(item.updatedAt) }}</td>
                <td>
                  <button @click="deleteWishlist(item.id)" class="btn btn-sm btn-outline-danger" title="Xóa">
                    <i class="fas fa-trash-alt"></i>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div class="d-flex justify-content-between align-items-center mt-3" v-if="totalPages > 0">
          <span class="text-muted small">
            Đang hiển thị {{ (page * size) + 1 }} đến {{ Math.min((page + 1) * size, totalElements) }} của {{ totalElements }} mục
          </span>
          <ul class="pagination pagination-sm mb-0">
            <li class="page-item" :class="{ disabled: page === 0 }">
              <a class="page-link" href="#" @click.prevent="fetchWishlists(page - 1)"><i class="fas fa-chevron-left"></i></a>
            </li>
            <li class="page-item" v-for="p in totalPages" :key="p" :class="{ active: page === p - 1 }">
              <a class="page-link" href="#" @click.prevent="fetchWishlists(p - 1)">{{ p }}</a>
            </li>
            <li class="page-item" :class="{ disabled: page >= totalPages - 1 }">
              <a class="page-link" href="#" @click.prevent="fetchWishlists(page + 1)"><i class="fas fa-chevron-right"></i></a>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const wishlists = ref([]);
const loading = ref(false);
const page = ref(0);
const size = ref(10);
const totalPages = ref(0);
const totalElements = ref(0);
const searchQuery = ref('');

const fetchWishlists = async (newPage = 0) => {
  if (newPage < 0 || (totalPages.value > 0 && newPage >= totalPages.value)) return;
  
  loading.value = true;
  try {
    const res = await axios.get(`http://localhost:8081/wishlist/admin/all`, {
      params: {
        page: newPage,
        size: size.value,
        search: searchQuery.value || null
      }
    });
    wishlists.value = res.data.content;
    page.value = res.data.number;
    totalPages.value = res.data.totalPages;
    totalElements.value = res.data.totalElements;
  } catch (error) {
    alert("Lỗi tải danh sách ưa thích");
  } finally {
    loading.value = false;
  }
};

const deleteWishlist = async (id) => {
  if (!confirm('Bạn có chắc chắn muốn xóa mục này?')) return;
  
  try {
    await axios.delete(`http://localhost:8081/wishlist/admin/delete/${id}`);
    alert("Xóa thành công");
    fetchWishlists(page.value);
  } catch (error) {
    alert("Lỗi xóa: " + error.message);
  }
};

const exportToExcel = () => {
    window.location.href = `http://localhost:8081/wishlist/export-excel`;
};

const formatDate = (dateString) => {
  if (!dateString) return '---';
  const date = new Date(dateString);
  return date.toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  });
};

onMounted(() => {
  fetchWishlists();
});
</script>

<style scoped>
.root-card {
  border-radius: 12px;
}
.bg-warning {
  background-color: #dcb324 !important;
}
table th {
  font-weight: 600;
  vertical-align: middle;
}
table td {
  vertical-align: middle;
}
</style>
