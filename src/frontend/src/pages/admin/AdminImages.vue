<template>
  <div class="container-fluid py-4">
    <div class="row mb-4 align-items-center">
      <div class="col-md-6">
        <h2 class="font-weight-bold">Quản lý Ảnh Chung</h2>
      </div>
      <div class="col-md-6 d-flex justify-content-md-end align-items-center gap-2">
        <button class="btn btn-primary font-weight-bold mr-2">
          <i class="fas fa-file-excel mr-1"></i> Xuất Excel
        </button>
        <button class="btn btn-warning text-white font-weight-bold mr-2" style="position: relative; overflow: hidden;">
          <i class="fas fa-file-import mr-1"></i> Nhập Excel
          <input type="file" style="position: absolute; top: 0; right: 0; min-width: 100%; min-height: 100%; font-size: 100px; text-align: right; filter: alpha(opacity=0); opacity: 0; outline: none; cursor: pointer; display: block;" />
        </button>
        <button class="btn btn-success font-weight-bold" @click="openAddModal">
          <i class="fas fa-plus mr-1"></i> Thêm ảnh
        </button>
      </div>
    </div>

    <div class="card shadow-sm border-0 root-card">
      <div class="card-body">
        <!-- Toolbar Filters -->
        <div class="d-flex justify-content-between mb-4 flex-wrap">
          <div class="d-flex align-items-center">
            <input type="text" v-model="searchQuery" class="form-control mr-3" placeholder="Tìm kiếm theo tên..." style="width: 250px;">
          </div>
          
          <div class="d-flex align-items-center">
            <span class="mr-2">Hiển thị: </span>
            <select v-model="size" class="form-control custom-select mr-3" style="width: 80px;">
              <option :value="10">10</option>
              <option :value="20">20</option>
              <option :value="50">50</option>
            </select>
            <select v-model="sortOrder" class="form-control custom-select" style="width: 150px;">
              <option value="newest">Mới nhất</option>
              <option value="oldest">Cũ nhất</option>
            </select>
          </div>
        </div>

        <!-- Table -->
        <div class="table-responsive">
          <table class="table align-middle table-hover text-center">
            <thead class="bg-light text-muted">
              <tr>
                <th>STT</th>
                <th>Ảnh</th>
                <th>Đường dẫn ảnh (Url_image)</th>
                <th>Tên ảnh</th>
                <th>Ngày tạo</th>
                <th>Ngày sửa</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="7" class="text-center py-4">
                  <div class="spinner-border text-primary" role="status"></div>
                </td>
              </tr>
              <tr v-else-if="filteredImages.length === 0">
                <td colspan="7" class="text-center py-4 text-muted">
                  Chưa có ảnh nào
                </td>
              </tr>
              <tr v-for="(item, index) in paginatedImages" :key="item.id" v-else>
                <td>{{ index + 1 + page * size }}</td>
                <td>
                  <img v-if="getFirstImage(item.imageUrl)" :src="getFirstImage(item.imageUrl)" alt="img" style="width: 40px; height: 40px; object-fit: contain; border-radius: 4px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);" loading="lazy">
                </td>
                <td class="text-left">
                  <div style="max-width: 250px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                     <a :href="getFirstImage(item.imageUrl)" target="_blank" class="text-primary" style="font-size: 0.9rem;" :title="item.imageUrl">{{ item.imageUrl }}</a>
                  </div>
                </td>
                <td>{{ item.imageName }}</td>
                <td>{{ formatDate(item.createdAt) }}</td>
                <td>{{ formatDate(item.updatedAt) }}</td>
                <td>
                  <button class="btn btn-sm text-primary border-0" @click="editImage(item)">
                    <i class="fas fa-edit"></i>
                  </button>
                  <button class="btn btn-sm text-danger border-0" @click="deleteImage(item.id)">
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

    <!-- Add/Edit Modal -->
    <div v-if="showModal" class="modal-backdrop fade show"></div>
    <div v-if="showModal" class="modal fade show" style="display: block; z-index: 1050; overflow-y: auto;" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content shadow border-0" style="border-radius: 12px;">
          <div class="modal-header border-bottom-0 pb-0 pt-4 px-4">
            <h4 class="modal-title font-weight-bold" style="color: #1a365d;">{{ isEdit ? 'Chỉnh sửa' : 'Thêm mới' }} Ảnh Chung</h4>
            <button type="button" class="close" @click="closeModal" aria-label="Close" style="font-size: 1.5rem;">
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body px-4 pt-4 pb-4">
            <div class="form-group mb-4">
              <label class="mb-2 text-secondary font-weight-bold">Tên ảnh</label>
              <input type="text" class="form-control font-weight-bold border-secondary" v-model="form.imageName" style="border-radius: 6px; padding: 10px 15px;">
            </div>
            
            <div class="form-group mb-4">
               <label class="mb-2 text-secondary font-weight-bold">Tải lên hình ảnh</label>
               <div class="upload-container border border-secondary p-4 rounded bg-white text-dark w-100" style="border-style: dashed !important; border-width: 2px !important;">
                 <label class="font-weight-bold d-inline-flex align-items-center m-0 w-100" style="cursor: pointer; justify-content: flex-start;">
                   <div>
                       <i class="fas fa-cloud-upload-alt fa-2x mr-2 text-secondary" style="vertical-align: middle;"></i> 
                       <span style="font-size: 1.1rem; vertical-align: middle;">Chọn nhiều ảnh</span>
                       <input type="file" ref="fileInput" multiple @change="onFileChange" class="d-block mt-2 w-100 border p-1 rounded" accept="image/*">
                   </div>
                 </label>
               </div>
               <div v-if="uploading" class="mt-2 text-primary font-weight-bold"><i class="fas fa-spinner fa-spin mr-2"></i> Đang tải lên...</div>
            </div>

            <div v-if="currentImages.length > 0" class="image-preview-grid d-flex flex-wrap border rounded p-3 bg-light" style="gap: 20px;">
               <div v-for="(imgUrl, idx) in currentImages" :key="idx" class="position-relative preview-item shadow-sm rounded overflow-hidden" style="background: #fff; width: fit-content; padding: 4px; border: 1px solid #dee2e6;">
                 <span v-if="isOldImage(imgUrl)" class="badge badge-secondary position-absolute" style="top: 8px; left: 8px; z-index: 2; border: 1px border-white;">Cũ</span>
                 <img :src="Helper.GetImageUrl(imgUrl)" style="width: 140px; height: 140px; object-fit: cover; border-radius: 4px;" loading="lazy" />
                 <button class="btn btn-danger btn-sm position-absolute" style="top: 8px; right: 8px; z-index: 2; border-radius: 4px;" @click="removeImage(idx)">
                   <i class="fas fa-trash-alt"></i>
                 </button>
               </div>
            </div>

            <div class="d-flex mt-5">
              <button type="button" class="btn text-white font-weight-bold px-4 py-2 rounded" style="background-color: #2b9348; flex: 1; margin-right: 10px; font-size: 1.1rem;" @click="saveImage" :disabled="uploading">Lưu thay đổi</button>
              <button type="button" class="btn text-white font-weight-bold px-4 py-2 rounded" style="background-color: #6c757d; flex: 1; margin-left: 10px; font-size: 1.1rem;" @click="closeModal">Quay lại</button>
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
import Helper from '../../helper/helper';

const imagesList = ref([]);
const loading = ref(false);

const page = ref(0);
const size = ref(10);
const searchQuery = ref('');
const sortOrder = ref('newest');

const showModal = ref(false);
const isEdit = ref(false);
const form = ref({ id: null, imageName: '' });
const currentImages = ref([]);
const originalImages = ref([]);
const fileInput = ref(null);
const uploading = ref(false);

const fetchImages = async () => {
  loading.value = true;
  try {
    const res = await axios.get('http://localhost:8081/api/general-images');
    imagesList.value = res.data;
  } catch (error) {
    alert("Lỗi tải danh sách ảnh");
  } finally {
    loading.value = false;
  }
};

const deleteImage = async (id) => {
  if (!confirm('Bạn có chắc chắn muốn xóa ảnh này?')) return;
  try {
    await axios.delete(`http://localhost:8081/api/general-images/${id}`);
    alert("Đã xóa ảnh");
    fetchImages();
  } catch (error) {
    alert("Lỗi khi xóa");
  }
};

const openAddModal = () => {
    isEdit.value = false;
    form.value = { id: null, imageName: '' };
    currentImages.value = [];
    originalImages.value = [];
    showModal.value = true;
    if (fileInput.value) fileInput.value.value = null;
};

const editImage = (item) => {
    isEdit.value = true;
    form.value = { id: item.id, imageName: item.imageName };
    const urls = item.imageUrl ? item.imageUrl.split(',').map(s => s.trim()).filter(Boolean) : [];
    currentImages.value = [...urls];
    originalImages.value = [...urls];
    showModal.value = true;
    if (fileInput.value) fileInput.value.value = null;
};

const isOldImage = (url) => {
    return originalImages.value.includes(url);
};

const removeImage = (index) => {
    currentImages.value.splice(index, 1);
};

const closeModal = () => {
    showModal.value = false;
};

const onFileChange = async (e) => {
    const files = Array.from(e.target.files);
    if (!files.length) return;
    
    uploading.value = true;
    
    try {
        const uploadPromises = files.map(async (file) => {
            const formData = new FormData();
            formData.append('file', file);
            formData.append('folder', 'general');
            const res = await axios.post('http://localhost:8081/storage/upload', formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            return res.data.url;
        });

        const urls = await Promise.all(uploadPromises);
        currentImages.value.push(...urls);
    } catch(err) {
        alert("Lỗi tải một số ảnh lên");
    } finally {
        uploading.value = false;
        if (fileInput.value) fileInput.value.value = null;
    }
};

const saveImage = async () => {
    if (!form.value.imageName || currentImages.value.length === 0) {
        alert("Vui lòng nhập tên ảnh và tải lên ít nhất một ảnh");
        return;
    }
    
    const finalImageUrl = currentImages.value.join(',');
    
    try {
        const payload = {
            imageName: form.value.imageName,
            imageUrl: finalImageUrl
        };
        if (form.value.id) {
            payload.id = form.value.id;
        }

        await axios.post(`http://localhost:8081/api/general-images`, payload);
        showModal.value = false;
        fetchImages();
        alert("Lưu ảnh thành công!");
    } catch (error) {
        alert("Lỗi lưu ảnh");
    }
};

const filteredImages = computed(() => {
  let result = imagesList.value;
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase();
    result = result.filter(r => 
      (r.imageName && r.imageName.toLowerCase().includes(q))
    );
  }
  if (sortOrder.value === 'newest') {
    result = result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  } else {
    result = result.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
  }
  return result;
});

const paginatedImages = computed(() => {
    const start = page.value * size.value;
    return filteredImages.value.slice(start, start + size.value);
});

const totalPages = computed(() => {
    return Math.ceil(filteredImages.value.length / size.value);
});

const formatDate = (dateString) => {
  if (!dateString) return '---';
  const date = new Date(dateString);
  return date.toLocaleString('vi-VN');
};

const getFirstImage = (urls) => {
  if (!urls) return '';
  return Helper.GetImageUrl(urls.split(',')[0].trim(), { w: 100, h: 100, c: 'fit' });
};

onMounted(() => {
  fetchImages();
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
