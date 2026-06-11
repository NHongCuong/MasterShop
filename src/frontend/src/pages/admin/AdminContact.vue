<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import axios from 'axios';
import Toast from 'primevue/toast';
import { useToast } from 'primevue/usetoast';
import Helper from '../../helper/helper';

const API = 'http://localhost:8081/contact';
const toast = useToast();

const list = ref<any[]>([]);
const loading = ref(false);
const totalItems = ref(0);
const totalPages = ref(0);
const currentPage = ref(0);
const pageSize = ref(10);
const pageSizeOptions = [5, 10, 20, 50];
const search = ref('');
const sortOption = ref('createdAt,desc');

const loadData = async () => {
    loading.value = true;
    try {
        const res = await axios.get(`${API}/list`, {
            params: {
                page: currentPage.value,
                size: pageSize.value,
                search: search.value,
                sort: sortOption.value
            }
        });
        list.value = res.data.content;
        totalItems.value = res.data.totalElements;
        totalPages.value = res.data.totalPages;
    } catch (err) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải danh sách liên hệ', life: 3000 });
    } finally {
        loading.value = false;
    }
};

const deleteContact = async (id: number) => {
    if (!confirm('Bạn có chắc chắn muốn xóa liên hệ này?')) return;
    try {
        await axios.delete(`${API}/delete/${id}`);
        toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa liên hệ', life: 3000 });
        loadData();
    } catch (err) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể xóa liên hệ', life: 3000 });
    }
};

const updateStatus = async (id: number, status: number) => {
    try {
        await axios.put(`${API}/status/${id}`, null, { params: { status } });
        toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật trạng thái', life: 3000 });
        loadData();
    } catch (err) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Lỗi khi cập nhật trạng thái', life: 3000 });
    }
};

const exportExcel = () => {
    window.location.href = `${API}/export-excel`;
};

const pages = computed(() => {
    const arr: number[] = [];
    for (let i = 0; i < totalPages.value; i++) arr.push(i);
    return arr;
});

const goToPage = (p: number) => {
    currentPage.value = p;
};

watch([pageSize, sortOption, currentPage], () => loadData());
let timeout: any = null;
watch(search, () => {
    if (timeout) clearTimeout(timeout);
    timeout = setTimeout(() => {
        currentPage.value = 0;
        loadData();
    }, 400);
});

onMounted(() => loadData());
</script>

<template>
    <div class="admin-contact-page p-4">
        <Toast />
        
        <!-- Header -->
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2 class="fw-bold m-0" style="color: #333; font-size: 1.75rem;">Danh sách liên hệ</h2>
            <button class="btn btn-success d-flex align-items-center gap-2 px-3 py-2 fw-bold shadow-sm" @click="exportExcel" style="background-color: #27ae60; border: none;">
                <i class="fas fa-file-excel"></i> Xuất Excel
            </button>
        </div>

        <!-- Filter Card -->
        <div class="card border-0 shadow-sm mb-4">
            <div class="card-body px-4 py-3">
                <div class="row align-items-center">
                    <div class="col-md-3">
                        <div class="d-flex align-items-center gap-2">
                            <span class="text-muted small fw-bold">Hiển thị:</span>
                            <select v-model="pageSize" class="form-select form-select-sm border-secondary-subtle" style="width: 75px;">
                                <option v-for="o in pageSizeOptions" :key="o" :value="o">{{ o }}</option>
                            </select>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="input-group">
                            <input type="text" v-model="search" class="form-control border-end-0 border-secondary-subtle" placeholder="Tìm kiếm liên hệ...">
                            <span class="input-group-text bg-white border-start-0 border-secondary-subtle">
                                <i class="fas fa-search text-muted"></i>
                            </span>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="d-flex align-items-center justify-content-end gap-2">
                            <span class="text-muted small fw-bold">Sắp xếp:</span>
                            <select v-model="sortOption" class="form-select form-select-sm border-secondary-subtle" style="width: 120px;">
                                <option value="createdAt,desc">Mới nhất</option>
                                <option value="createdAt,asc">Cũ nhất</option>
                            </select>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Table Card -->
        <div class="card border-0 shadow-sm rounded-3 overflow-hidden">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead style="background-color: #f1c40f;">
                        <tr>
                            <th class="py-3 ps-4 text-center" style="width: 60px;">STT</th>
                            <th class="py-3">Họ và tên</th>
                            <th class="py-3">Email</th>
                            <th class="py-3">Số điện thoại</th>
                            <th class="py-3">Chủ đề</th>
                            <th class="py-3">Nội dung</th>
                            <th class="py-3">Ngày tạo</th>
                            <th class="py-3 text-center" style="width: 130px;">Trạng thái</th>
                            <th class="py-3 text-center" style="width: 100px;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(item, index) in list" :key="item.id">
                            <td class="ps-4 text-center text-muted small">{{ currentPage * pageSize + index + 1 }}</td>
                            <td>
                                <div class="fw-bold text-dark">{{ item.fullName }}</div>
                            </td>
                            <td class="text-muted small">{{ item.email }}</td>
                            <td class="text-dark">{{ item.phoneNumber }}</td>
                            <td class="text-dark">{{ item.subject }}</td>
                            <td class="text-truncate text-muted small" style="max-width: 150px;" :title="item.message">
                                {{ item.message }}
                            </td>
                            <td>
                                <div class="small text-muted">{{ Helper.DateFormat(item.createdAt) }}</div>
                            </td>
                            <td class="text-center">
                                <span v-if="item.status === 1" 
                                      class="badge-status success"
                                      @click="updateStatus(item.id, 0)">
                                    Đã xử lý
                                </span>
                                <span v-else 
                                      class="badge-status waiting"
                                      @click="updateStatus(item.id, 1)">
                                    Chưa xử lý
                                </span>
                            </td>
                            <td class="text-center">
                                <button class="btn btn-sm text-danger-emphasis btn-light border-0" @click="deleteContact(item.id)">
                                    <i class="fas fa-trash-alt"></i>
                                </button>
                            </td>
                        </tr>
                        <tr v-if="list.length === 0 && !loading">
                            <td colspan="9" class="text-center py-5 text-muted fst-italic">
                                <i class="fas fa-inbox d-block mb-2 fs-3 opacity-50"></i>
                                Không tìm thấy tin nhắn liên hệ nào
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Pagination -->
        <div class="d-flex justify-content-between align-items-center mt-4">
            <div class="text-muted small">
                Hiển thị <strong>{{ list.length }}</strong> trên tổng số <strong>{{ totalItems }}</strong> liên hệ
            </div>
            <nav v-if="totalPages > 1">
                <ul class="pagination pagination-sm m-0">
                    <li class="page-item" :class="{ disabled: currentPage === 0 }">
                        <button class="page-link shadow-none" @click="goToPage(currentPage - 1)">
                            <i class="fas fa-chevron-left small"></i>
                        </button>
                    </li>
                    <li v-for="p in pages" :key="p" class="page-item" :class="{ active: p === currentPage }">
                        <button class="page-link shadow-none" @click="goToPage(p)">{{ p + 1 }}</button>
                    </li>
                    <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
                        <button class="page-link shadow-none" @click="goToPage(currentPage + 1)">
                            <i class="fas fa-chevron-right small"></i>
                        </button>
                    </li>
                </ul>
            </nav>
        </div>
    </div>
</template>

<style scoped>
.admin-contact-page {
    background-color: #fcfcfc;
    min-height: 100vh;
    font-family: 'Inter', system-ui, -apple-system, sans-serif;
}

.table thead th {
    font-size: 0.825rem;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    color: #333;
    border: none;
}

.table tbody td {
    padding: 1rem 0.5rem;
    border-bottom: 1px solid #f1f5f9;
}

.table tbody tr:last-child td {
    border-bottom: none;
}

.form-select-sm, .form-control {
    border-radius: 8px;
    transition: all 0.2s;
}

.form-select-sm:focus, .form-control:focus {
    box-shadow: 0 0 0 3px rgba(241, 196, 15, 0.15);
    border-color: #f1c40f;
}

.badge-status {
    padding: 6px 14px;
    border-radius: 8px;
    font-size: 0.75rem;
    font-weight: 700;
    cursor: pointer;
    display: inline-block;
    transition: all 0.2s;
}

.badge-status.success {
    background-color: #e8f5e9;
    color: #2e7d32;
    border: 1px solid #c8e6c9;
}

.badge-status.waiting {
    background-color: #fff9c4;
    color: #f9a825;
    border: 1px solid #fff59d;
}

.badge-status:hover {
    filter: brightness(0.95);
    transform: translateY(-1px);
}

.pagination .page-link {
    border: 1px solid #e2e8f0;
    color: #64748b;
    margin: 0 2px;
    border-radius: 6px;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.pagination .page-item.active .page-link {
    background-color: #f1c40f;
    border-color: #f1c40f;
    color: white;
}

.btn-light:hover {
    background-color: #fee2e2;
    color: #dc2626 !important;
}
</style>
