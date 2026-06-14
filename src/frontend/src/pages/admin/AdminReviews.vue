<template>
  <div class="container-fluid py-4">
    <div class="row mb-4 align-items-center">
      <div class="col-md-6">
        <h2 class="font-weight-bold">Quản lý đánh giá sản phẩm</h2>
      </div>
    </div>

    <div class="card shadow-sm border-0 root-card">
      <div class="card-body">
        <!-- Toolbar Filters -->
        <div class="d-flex justify-content-between mb-4 flex-wrap">
          <div class="d-flex align-items-center mb-2">
            <span class="mr-2">Hiển thị: </span>
            <select v-model="size" class="form-control custom-select mr-3" style="width: 80px;">
              <option :value="10">10</option>
              <option :value="20">20</option>
              <option :value="50">50</option>
            </select>
            <button class="btn btn-success font-weight-bold">
              <i class="fas fa-file-excel mr-1"></i> Export Reviews
            </button>
          </div>
          
          <div class="d-flex align-items-center mb-2">
            <input type="text" v-model="searchQuery" class="form-control mr-3" placeholder="Tìm theo người dùng, sản phẩn..." style="width: 250px;">
            <span class="mr-2">Sắp xếp theo: </span>
            <select v-model="sortOrder" class="form-control custom-select" style="width: 150px;">
              <option value="newest">Mới nhất</option>
              <option value="oldest">Cũ nhất</option>
            </select>
          </div>
        </div>

        <!-- Table -->
        <div class="table-responsive">
          <table class="table align-middle table-bordered text-center">
            <thead style="background-color: #f1c40f; color: #333;">
              <tr>
                <th>STT</th>
                <th>Tên người dùng</th>
                <th style="width: 25%;">Tên sản phẩm</th>
                <th>Rating</th>
                <th style="width: 25%;">Bình luận</th>
                <th>Ngày tạo</th>
                <th>Ngày sửa</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="8" class="text-center py-4">
                  <div class="spinner-border text-primary" role="status"></div>
                </td>
              </tr>
              <tr v-else-if="filteredReviews.length === 0">
                <td colspan="8" class="text-center py-4 text-muted">
                  Không có đánh giá nào
                </td>
              </tr>
              <tr v-for="(item, index) in paginatedReviews" :key="item.id" v-else>
                <td>{{ index + 1 + page * size }}</td>
                <td>{{ item.userName }}</td>
                <td>{{ item.productName }}</td>
                <td class="text-warning">
                  <i class="fas fa-star" v-for="n in item.rating" :key="'star-' + n"></i>
                  <span class="text-muted ml-1">({{ item.rating }})</span>
                </td>
                <td class="text-left">{{ item.comment }}</td>
                <td>{{ formatDate(item.createdAt) }}</td>
                <td>{{ formatDate(item.updatedAt) }}</td>
                <td>
                  <button class="btn btn-sm text-primary border-0" @click="editReview(item)">
                    <i class="fas fa-edit"></i>
                  </button>
                  <button @click="deleteReview(item.id)" class="btn btn-sm text-danger border-0">
                    <i class="fas fa-trash-alt"></i>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div class="d-flex justify-content-center align-items-center mt-4">
          <button class="btn btn-light border mr-3" :disabled="page === 0" @click="page--">
            Trước
          </button>
          <span class="font-weight-bold">Trang {{ totalPages > 0 ? page + 1 : 0 }} / {{ totalPages }}</span>
          <button class="btn btn-light border ml-3" :disabled="page >= totalPages - 1 || totalPages === 0" @click="page++">
            Sau
          </button>
        </div>
      </div>
    </div>

    <!-- Edit Modal -->
    <div v-if="showEditModal" class="modal-backdrop fade show"></div>
    <div v-if="showEditModal" class="modal fade show" style="display: block; z-index: 1050;" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered" style="max-width: 400px;">
        <div class="modal-content shadow">
          <div class="modal-header border-bottom-0 pb-0 pt-3 px-4">
            <h5 class="modal-title font-weight-bold" style="font-size: 1.1rem; color: #333;">Chỉnh sửa đánh giá</h5>
          </div>
          <div class="modal-body px-4 pt-3 pb-4">
            <div class="form-group mb-4">
              <label class="mb-2 text-muted" style="font-size: 0.9rem;">Số sao:</label>
              <select class="form-control" v-model="editRating" :style="{'border-color': '#28a745', 'cursor': 'pointer'}">
                <option :value="5">5 Sao</option>
                <option :value="4">4 Sao</option>
                <option :value="3">3 Sao</option>
                <option :value="2">2 Sao</option>
                <option :value="1">1 Sao</option>
              </select>
            </div>
            <div class="d-flex justify-content-end mt-4">
              <button type="button" class="btn text-white mr-2" style="background-color: #6c757d; font-weight: 500; padding: 6px 16px; border-radius: 4px;" @click="closeEditModal">Hủy</button>
              <button type="button" class="btn text-white" style="background-color: #28a745; font-weight: 500; padding: 6px 16px; border-radius: 4px;" @click="saveEdit">Lưu thay đổi</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';

const reviews = ref([]);
const loading = ref(false);

const page = ref(0);
const size = ref(10);
const searchQuery = ref('');
const sortOrder = ref('newest');

const showEditModal = ref(false);
const editingReviewId = ref(null);
const editRating = ref(5);

const fetchReviews = async () => {
  loading.value = true;
  try {
    const res = await axios.get('http://localhost:8081/api/reviews');
    reviews.value = res.data;
  } catch (error) {
    alert("Lỗi tải danh sách đánh giá");
  } finally {
    loading.value = false;
  }
};

const deleteReview = async (id) => {
  if (!confirm('Bạn có chắc chắn muốn xóa đánh giá này?')) return;
  try {
    await axios.delete(`http://localhost:8081/api/reviews/${id}`);
    alert("Đã xóa đánh giá");
    fetchReviews();
  } catch (error) {
    alert("Lỗi khi xóa");
  }
};

const editReview = (item) => {
    editingReviewId.value = item.id;
    editRating.value = item.rating;
    showEditModal.value = true;
};

const closeEditModal = () => {
    showEditModal.value = false;
    editingReviewId.value = null;
};

const saveEdit = async () => {
    if (!editingReviewId.value) return;
    try {
        await axios.put(`http://localhost:8081/api/reviews/${editingReviewId.value}`, {
            rating: editRating.value
        });
        showEditModal.value = false;
        fetchReviews();
        alert("Cập nhật đánh giá thành công!");
    } catch (error) {
        alert("Lỗi khi cập nhật đánh giá");
    }
};

const filteredReviews = computed(() => {
  let result = reviews.value;
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase();
    result = result.filter(r => 
      (r.userName && r.userName.toLowerCase().includes(q)) || 
      (r.productName && r.productName.toLowerCase().includes(q))
    );
  }
  if (sortOrder.value === 'newest') {
    result = result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  } else {
    result = result.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
  }
  return result;
});

const paginatedReviews = computed(() => {
    const start = page.value * size.value;
    return filteredReviews.value.slice(start, start + size.value);
});

const totalPages = computed(() => {
    return Math.ceil(filteredReviews.value.length / size.value);
});

const formatDate = (dateString) => {
  if (!dateString) return '---';
  const date = new Date(dateString);
  return date.toLocaleString('vi-VN');
};

onMounted(() => {
  fetchReviews();
});
</script>

<style scoped>
.root-card {
  border-radius: 12px;
}
table th {
  font-weight: 600;
}
</style>
