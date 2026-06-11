<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import axios from 'axios';
import Toast from 'primevue/toast';
import { useToast } from 'primevue/usetoast';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';
import Helper from '../../helper/helper';

const API = 'http://localhost:8081/supplier';
const toast = useToast();

const list = ref<any[]>([]);
const loading = ref(false);
const totalItems = ref(0);
const totalPages = ref(0);
const currentPage = ref(0);
const pageSize = ref(10);
const pageSizeOptions = [5, 10, 20, 50];
const search = ref('');
const sortOption = ref('id,desc');

const visibleDialog = ref(false);
const editMode = ref(false);
const saving = ref(false);
const supplierForm = ref({
    id: null,
    name: '',
    address: '',
    phone: '',
    email: '',
    website: ''
});

// Import state
const fileInput = ref<HTMLInputElement | null>(null);
const importPreviewList = ref<any[]>([]);
const visibleImportDialog = ref(false);
const isConfirmingImport = ref(false);

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
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải danh sách nhà cung cấp', life: 3000 });
    } finally {
        loading.value = false;
    }
};

const openAdd = () => {
    editMode.value = false;
    supplierForm.value = { id: null, name: '', address: '', phone: '', email: '', website: '' };
    visibleDialog.value = true;
};

const openEdit = (item: any) => {
    editMode.value = true;
    supplierForm.value = { ...item };
    visibleDialog.value = true;
};

const saveSupplier = async () => {
    if (!supplierForm.value.name?.trim()) {
        toast.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Vui lòng nhập tên nhà cung cấp', life: 2000 });
        return;
    }
    saving.value = true;
    try {
        const payload = {
            name: supplierForm.value.name.trim(),
            phone: supplierForm.value.phone?.trim() || '',
            email: supplierForm.value.email?.trim() || '',
            address: supplierForm.value.address?.trim() || '',
            website: supplierForm.value.website?.trim() || ''
        };
        if (editMode.value) {
            await axios.put(`${API}/update/${supplierForm.value.id}`, payload);
            toast.add({ severity: 'success', summary: 'Thành công', detail: 'Cập nhật nhà cung cấp thành công', life: 2000 });
        } else {
            await axios.post(`${API}/add`, payload);
            toast.add({ severity: 'success', summary: 'Thành công', detail: 'Thêm nhà cung cấp mới thành công', life: 2000 });
        }
        visibleDialog.value = false;
        loadData();
    } catch (err: any) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: err.response?.data || 'Không thể lưu nhà cung cấp', life: 3000 });
    } finally {
        saving.value = false;
    }
};

const deleteSupplier = async (id: number) => {
    if (!confirm('Bạn có chắc muốn xóa nhà cung cấp này?')) return;
    try {
        await axios.delete(`${API}/delete/${id}`);
        toast.add({ severity: 'success', summary: 'Thành công', detail: 'Xóa nhà cung cấp thành công', life: 2000 });
        loadData();
    } catch (err: any) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: err.response?.data || 'Không thể xóa', life: 3000 });
    }
};

const exportExcel = () => {
    window.location.href = `${API}/export-excel`;
};

// Import Excel with Review logic
const importExcel = () => fileInput.value?.click();
const handleImportFileSelect = async (event: any) => {
    const file = event.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);
    loading.value = true;

    try {
        const res = await axios.post(`${API}/preview-import`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
        importPreviewList.value = res.data;
        visibleImportDialog.value = true;
    } catch (err: any) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: err.response?.data || 'Không thể đọc file Excel', life: 3000 });
    } finally {
        loading.value = false;
        event.target.value = '';
    }
};

const confirmImport = async () => {
    const validItems = importPreviewList.value.filter(x => x.isValid && !x.exists);
    if (validItems.length === 0) {
        toast.add({ severity: 'warn', summary: 'Thông báo', detail: 'Không có dữ liệu mới hợp lệ để nhập', life: 3000 });
        return;
    }
    isConfirmingImport.value = true;
    try {
        const res = await axios.post(`${API}/confirm-import`, validItems);
        toast.add({ severity: 'success', summary: 'Thành công', detail: res.data, life: 2500 });
        visibleImportDialog.value = false;
        loadData();
    } catch (err: any) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: err.response?.data || 'Import thất bại', life: 3000 });
    } finally {
        isConfirmingImport.value = false;
    }
};

const pages = computed(() => {
    const arr: number[] = [];
    const max = 5;
    let start = Math.max(0, currentPage.value - 2);
    let end = Math.min(totalPages.value, start + max);
    if (end - start < max) start = Math.max(0, end - max);
    for (let i = start; i < end; i++) arr.push(i);
    return arr;
});

const goToPage = (p: number) => {
    if (p >= 0 && p < totalPages.value) currentPage.value = p;
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
    <div class="vc-page">
        <Toast />

        <div class="vc-header">
            <div class="vc-header-left">
                <i class="fas fa-truck vc-icon"></i>
                <h2>Quản lý Nhà cung cấp</h2>
            </div>
            <div class="vc-header-actions">
                <button class="vc-btn-add" @click="openAdd">
                    <i class="fas fa-plus me-2"></i>Thêm nhà cung cấp
                </button>
                <input type="file" ref="fileInput" style="display: none" accept=".xlsx, .xls" @change="handleImportFileSelect">
                <button class="vc-btn-import" @click="importExcel">
                    <i class="fas fa-file-import me-2"></i>Nhập Excel
                </button>
                <button class="vc-btn-export" @click="exportExcel">
                    <i class="fas fa-file-excel me-2"></i>Xuất Excel
                </button>
            </div>
        </div>

        <div class="vc-toolbar">
            <div class="vc-toolbar-left">
                <div class="vc-select-group">
                    <label>Hiển thị:</label>
                    <select v-model="pageSize">
                        <option v-for="o in pageSizeOptions" :key="o" :value="o">{{ o }}</option>
                    </select>
                </div>
                <div class="vc-select-group">
                    <label>Sắp xếp:</label>
                    <select v-model="sortOption">
                        <option value="id,desc">Mới nhất</option>
                        <option value="name,asc">Tên A-Z</option>
                        <option value="name,desc">Tên Z-A</option>
                        <option value="id,asc">ID cũ nhất</option>
                    </select>
                </div>
            </div>
            <div class="vc-toolbar-right">
                <div class="vc-search-box">
                    <i class="fas fa-search"></i>
                    <input type="text" v-model="search" placeholder="Tìm tên, email, sđt...">
                </div>
            </div>
        </div>

        <div class="vc-table-container">
            <table class="vc-table">
                <thead>
                    <tr>
                        <th style="width:80px">STT</th>
                        <th>Tên nhà cung cấp</th>
                        <th>Số điện thoại</th>
                        <th>Email</th>
                        <th>Địa chỉ</th>
                        <th>Website</th>
                        <th>Ngày tạo</th>
                        <th>Ngày sửa</th>
                        <th style="width:120px">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(item, idx) in list" :key="item.id">
                        <td class="text-center font-bold text-slate-500">{{ currentPage * pageSize + idx + 1 }}</td>
                        <td>
                            <div class="font-bold text-slate-700">{{ item.name }}</div>
                        </td>
                        <td>{{ item.phone || '—' }}</td>
                        <td>{{ item.email || '—' }}</td>
                        <td class="text-slate-500 italic">{{ item.address || '—' }}</td>
                        <td><a v-if="item.website" :href="item.website" target="_blank">{{ item.website }}</a><span v-else>—</span></td>
                        <td>{{ item.createdAt ? Helper.DateFormat(item.createdAt) : '—' }}</td>
                        <td>{{ item.updatedAt ? Helper.DateFormat(item.updatedAt) : '—' }}</td>
                        <td class="text-center">
                            <div class="vc-actions">
                                <button class="vc-action-btn vc-edit" @click="openEdit(item)" title="Sửa">
                                    <i class="fas fa-edit"></i>
                                </button>
                                <button class="vc-action-btn vc-delete" @click="deleteSupplier(item.id)" title="Xóa">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </div>
                        </td>
                    </tr>
                    <tr v-if="list.length === 0 && !loading">
                        <td colspan="9" class="text-center py-5 text-slate-400 italic">Không tìm thấy nhà cung cấp nào</td>
                    </tr>
                    <tr v-if="loading">
                        <td colspan="9" class="text-center py-5"><i class="fas fa-spinner fa-spin"></i> Đang tải...</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="vc-pagination-bar" v-if="totalPages > 0">
            <div class="vc-page-info">
                Hiển thị {{ currentPage * pageSize + 1 }} - {{ Math.min((currentPage + 1) * pageSize, totalItems) }} / {{ totalItems }} nhà cung cấp
            </div>
            <div class="vc-pagination">
                <button class="vc-page-btn" :disabled="currentPage === 0" @click="goToPage(currentPage - 1)">
                    <i class="fas fa-chevron-left"></i>
                </button>
                <button v-for="pg in pages" :key="pg" class="vc-page-btn" :class="{ 'vc-page-active': pg === currentPage }" @click="goToPage(pg)">
                    {{ pg + 1 }}
                </button>
                <button class="vc-page-btn" :disabled="currentPage >= totalPages - 1" @click="goToPage(currentPage + 1)">
                    <i class="fas fa-chevron-right"></i>
                </button>
            </div>
        </div>

        <!-- Dialog Thêm/Sửa -->
        <Dialog v-model:visible="visibleDialog" modal :header="editMode ? 'Chỉnh sửa nhà cung cấp' : 'Thêm nhà cung cấp mới'" :style="{ width: '500px' }">
            <div class="p-fluid">
                <div class="field mb-3">
                    <label class="font-bold block mb-1">Tên nhà cung cấp</label>
                    <InputText v-model="supplierForm.name" placeholder="Nhập tên nhà cung cấp..." />
                </div>
                <div class="field mb-3">
                    <label class="font-bold block mb-1">Số điện thoại</label>
                    <InputText v-model="supplierForm.phone" placeholder="Nhập số điện thoại..." />
                </div>
                <div class="field mb-3">
                    <label class="font-bold block mb-1">Email</label>
                    <InputText v-model="supplierForm.email" placeholder="Nhập email..." />
                </div>
                <div class="field mb-3">
                    <label class="font-bold block mb-1">Địa chỉ</label>
                    <Textarea v-model="supplierForm.address" rows="3" placeholder="Nhập địa chỉ..." />
                </div>
                <div class="field mb-3">
                    <label class="font-bold block mb-1">Website</label>
                    <InputText v-model="supplierForm.website" placeholder="Nhập link website..." />
                </div>
            </div>
            <template #footer>
                <div class="flex justify-content-end gap-2">
                    <Button label="Hủy" icon="pi pi-times" class="p-button-text p-button-secondary" :disabled="saving" @click="visibleDialog = false" />
                    <Button label="Lưu lại" icon="pi pi-check" class="p-button-primary" :loading="saving" @click="saveSupplier" />
                </div>
            </template>
        </Dialog>

        <!-- Dialog Review Import -->
        <Dialog v-model:visible="visibleImportDialog" modal header="📥 Xác nhận nhập Nhà cung cấp từ Excel" :style="{ width: '850px' }">
            <div class="import-review-container">
                <div class="info-banner mb-3 p-3 border-round bg-blue-50 text-blue-800 flex align-items-center gap-2">
                    <i class="fas fa-info-circle text-xl"></i>
                    <span>Hệ thống đã bỏ qua cột ID để tự động tạo ID mới. Vui lòng kiểm tra dữ liệu trước khi xác nhận.</span>
                </div>

                <div class="table-wrapper border-1 border-slate-200 border-round overflow-hidden">
                    <table class="w-full" style="border-collapse: collapse;">
                        <thead class="bg-slate-50 border-bottom-1 border-slate-200">
                            <tr>
                                <th class="p-3 text-left text-xs font-bold text-slate-500 uppercase">STT</th>
                                <th class="p-3 text-left text-xs font-bold text-slate-500 uppercase">Tên</th>
                                <th class="p-3 text-left text-xs font-bold text-slate-500 uppercase">SĐT</th>
                                <th class="p-3 text-left text-xs font-bold text-slate-500 uppercase">Email</th>
                                <th class="p-3 text-left text-xs font-bold text-slate-500 uppercase">Trạng thái</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(item, idx) in importPreviewList" :key="idx" class="border-bottom-1 border-slate-100 hover:bg-slate-50">
                                <td class="p-3 font-bold text-slate-400">{{ idx + 1 }}</td>
                                <td class="p-3 font-semibold text-slate-700">{{ item.name }}</td>
                                <td class="p-3 text-slate-600">{{ item.phone || '—' }}</td>
                                <td class="p-3 text-slate-600">{{ item.email || '—' }}</td>
                                <td class="p-3">
                                    <span v-if="item.exists" class="p-tag-warn">Đã tồn tại</span>
                                    <span v-else class="p-tag-new">Mới</span>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <template #footer>
                <div class="flex justify-content-end gap-2 py-2">
                    <Button label="Hủy bỏ" icon="pi pi-times" class="p-button-text p-button-secondary" :disabled="isConfirmingImport" @click="visibleImportDialog = false" />
                    <Button label="Xác nhận nhập" icon="pi pi-check" class="p-button-success" :loading="isConfirmingImport" @click="confirmImport" />
                </div>
            </template>
        </Dialog>
    </div>
</template>

<style scoped>
.vc-page { padding: 24px; font-family: 'Inter', sans-serif; }
.vc-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; border-bottom: 2px solid #f1f5f9; padding-bottom: 16px; }
.vc-header-left { display: flex; align-items: center; gap: 12px; }
.vc-header-actions { display: flex; gap: 12px; }
.vc-icon { font-size: 28px; color: #3b82f6; }
.vc-header-left h2 { margin: 0; font-size: 22px; font-weight: 700; color: #1e293b; }

.vc-btn-add, .vc-btn-import, .vc-btn-export { border: none; padding: 10px 20px; border-radius: 8px; font-weight: 600; cursor: pointer; transition: 0.2s; color: white; display: flex; align-items: center; }
.vc-btn-add { background: #3b82f6; }
.vc-btn-import { background: #f59e0b; }
.vc-btn-export { background: #16a34a; }
.vc-btn-add:hover, .vc-btn-import:hover, .vc-btn-export:hover { transform: translateY(-1px); filter: brightness(0.9); }

.vc-toolbar { display: flex; justify-content: space-between; align-items: center; background: white; padding: 16px; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-bottom: 20px; }
.vc-toolbar-left, .vc-toolbar-right { display: flex; gap: 20px; align-items: center; }
.vc-select-group { display: flex; align-items: center; gap: 8px; font-size: 14px; color: #64748b; }
.vc-select-group select { padding: 6px 12px; border: 1px solid #e2e8f0; border-radius: 6px; outline: none; }

.vc-search-box { position: relative; width: 300px; }
.vc-search-box i { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #94a3b8; }
.vc-search-box input { width: 100%; padding: 8px 12px 8px 36px; border: 1px solid #e2e8f0; border-radius: 8px; outline: none; }

.vc-table-container { background: white; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); overflow: hidden; }
.vc-table { width: 100%; border-collapse: collapse; }
.vc-table th { background: #f8fafc; padding: 14px; text-align: left; font-size: 13px; font-weight: 600; color: #475569; border-bottom: 1px solid #e2e8f0; text-transform: uppercase; }
.vc-table td { padding: 14px; border-bottom: 1px solid #f1f5f9; font-size: 14px; color: #334155; }
.vc-table tr:hover { background: #f8fafc; }

.vc-actions { display: flex; gap: 8px; justify-content: center; }
.vc-action-btn { width: 32px; height: 32px; border-radius: 6px; border: none; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: 0.2s; }
.vc-edit { background: #fef3c7; color: #d97706; }
.vc-delete { background: #fee2e2; color: #dc2626; }

.vc-pagination-bar { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; }
.vc-page-info { font-size: 14px; color: #64748b; }
.vc-pagination { display: flex; gap: 6px; }
.vc-page-btn { padding: 8px 14px; border: 1px solid #e2e8f0; border-radius: 8px; background: white; cursor: pointer; }
.vc-page-active { background: #3b82f6; color: white; border-color: #3b82f6; }

.text-center { text-align: center; }
.font-bold { font-weight: 700; }
.font-semibold { font-weight: 600; }
.italic { font-style: italic; }

.p-tag-new { background: #f0fdf4; color: #166534; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 700; text-transform: uppercase; }
.p-tag-warn { background: #fffbeb; color: #92400e; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 700; text-transform: uppercase; }

.table-wrapper { max-height: 400px; overflow-y: auto; }
</style>
