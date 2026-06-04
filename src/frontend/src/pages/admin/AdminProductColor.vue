<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import Toast from 'primevue/toast'
import { useToast } from 'primevue/usetoast'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import Button from 'primevue/button'

const toast = useToast()
const API_URL = 'http://localhost:8081/admin/product-color'
const BASE_COLOR_API = 'http://localhost:8081/color/all'
const BASE_PRODUCT_API = 'http://localhost:8081/product/all'

// Data
const items = ref<any[]>([])
const totalItems = ref(0)
const totalPages = ref(0)
const loading = ref(false)

// Options for dropdowns
const colorOptions = ref<any[]>([])
const productOptions = ref<any[]>([])

// Dialog state
const displayDialog = ref(false)
const isEdit = ref(false)
const oldKeys = ref({ idColor: 0, idProduct: 0 })
const currentItem = ref({
  idColor: null,
  idProduct: null
})

// Filters
const search = ref('')
const pageSize = ref(10)
const currentPage = ref(0)
const sortOption = ref('newest')
const pageSizeOptions = [10, 20, 50, 100, 200]

// Load data
const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize.value,
      sort: sortOption.value
    }
    if (search.value.trim()) {
      params.search = search.value.trim()
    }
    const res = await axios.get(`${API_URL}/all`, { params })
    items.value = res.data.content || []
    totalItems.value = res.data.totalElements || 0
    totalPages.value = res.data.totalPages || 0
  } catch (err) {
    console.error('Lỗi tải dữ liệu:', err)
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải danh sách Product Color', life: 3000 })
  } finally {
    loading.value = false
  }
}

const loadBaseOptions = async () => {
  try {
    const [cRes, pRes] = await Promise.all([
      axios.get(BASE_COLOR_API),
      axios.get(BASE_PRODUCT_API)
    ])
    colorOptions.value = cRes.data.map((c: any) => ({ label: c.nameColor, value: c.id }))
    productOptions.value = pRes.data.map((p: any) => ({ label: p.name, value: p.id }))
  } catch (err) {
    console.error('Lỗi tải options:', err)
  }
}

// Add / Edit
const openAdd = () => {
  currentItem.value = { idColor: null, idProduct: null }
  isEdit.value = false
  displayDialog.value = true
}

const openEdit = (item: any) => {
  currentItem.value = { idColor: item.idColor, idProduct: item.idProduct }
  oldKeys.value = { idColor: item.idColor, idProduct: item.idProduct }
  isEdit.value = true
  displayDialog.value = true
}

const saveItem = async () => {
  if (!currentItem.value.idColor || !currentItem.value.idProduct) {
    toast.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Vui lòng chọn đầy đủ màu và sản phẩm', life: 2000 })
    return
  }

  try {
    if (isEdit.value) {
      await axios.put(`${API_URL}/update`, currentItem.value, {
        params: {
          oldIdColor: oldKeys.value.idColor,
          oldIdProduct: oldKeys.value.idProduct
        }
      })
      toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật Product Color', life: 2000 })
    } else {
      await axios.post(`${API_URL}/add`, currentItem.value)
      toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã thêm mới Product Color', life: 2000 })
    }
    displayDialog.value = false
    loadData()
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Lưu thất bại', life: 2000 })
  }
}

// Delete
const deleteItem = async (idColor: number, idProduct: number) => {
  if (!confirm('Bạn có chắc muốn xóa mục này?')) return
  try {
    await axios.delete(`${API_URL}/delete`, { params: { idColor, idProduct } })
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa Product Color', life: 2000 })
    loadData()
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Xóa thất bại', life: 2000 })
  }
}

// Export Excel
const exportExcel = async () => {
  try {
    const res = await axios.get(`${API_URL}/export-excel`, { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'product_colors.xlsx')
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã tải file Excel', life: 2000 })
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Xuất Excel thất bại', life: 2000 })
  }
}

// Format date
const formatDate = (date: string | null) => {
  if (!date) return '—'
  return new Date(date).toLocaleDateString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

// Pagination
const pages = computed(() => {
  const arr: number[] = []
  const maxVisible = 5
  let start = Math.max(0, currentPage.value - Math.floor(maxVisible / 2))
  let end = Math.min(totalPages.value, start + maxVisible)
  if (end - start < maxVisible) start = Math.max(0, end - maxVisible)
  for (let i = start; i < end; i++) arr.push(i)
  return arr
})

const goToPage = (p: number) => {
  if (p < 0 || p >= totalPages.value) return
  currentPage.value = p
}

// Watchers
let searchTimeout: any = null
watch(search, () => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    currentPage.value = 0
    loadData()
  }, 400)
})
watch([pageSize, sortOption], () => {
  currentPage.value = 0
  loadData()
})
watch(currentPage, () => {
  loadData()
})

// Showing info
const showingFrom = computed(() => totalItems.value === 0 ? 0 : currentPage.value * pageSize.value + 1)
const showingTo = computed(() => Math.min((currentPage.value + 1) * pageSize.value, totalItems.value))

onMounted(() => {
  loadData()
  loadBaseOptions()
})
</script>

<template>
  <div class="pc-page">
    <Toast />

    <!-- Header -->
    <div class="pc-header">
      <div class="pc-header-left">
        <i class="fas fa-palette pc-icon"></i>
        <h2>Quản lý Product Color</h2>
      </div>
      <div class="pc-header-actions">
        <button class="pc-btn pc-btn-add" @click="openAdd">
          <i class="fas fa-plus"></i> Thêm mới
        </button>
        <button class="pc-btn pc-btn-export" @click="exportExcel">
          <i class="fas fa-file-excel"></i> Export Excel
        </button>
      </div>
    </div>

    <!-- Toolbar: search, sort, pageSize -->
    <div class="pc-toolbar">
      <div class="pc-toolbar-left">
        <div class="pc-select-group">
          <label>Hiển thị:</label>
          <select v-model="pageSize">
            <option v-for="opt in pageSizeOptions" :key="opt" :value="opt">{{ opt }}</option>
          </select>
        </div>
        <div class="pc-select-group">
          <label>Sắp xếp:</label>
          <select v-model="sortOption">
            <option value="newest">Mới nhất</option>
            <option value="oldest">Cũ nhất</option>
            <option value="az">A - Z</option>
            <option value="za">Z - A</option>
          </select>
        </div>
      </div>
      <div class="pc-toolbar-right">
        <div class="pc-search-box">
          <i class="fas fa-search"></i>
          <input type="text" v-model="search" placeholder="Tìm theo tên màu, tên sản phẩm...">
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="pc-table-wrapper">
      <table class="pc-table">
        <thead>
          <tr>
            <th style="width:60px">STT</th>
            <th>Tên màu</th>
            <th>Tên sản phẩm</th>
            <th style="width:160px">Ngày tạo</th>
            <th style="width:160px">Ngày sửa</th>
            <th style="width:110px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="6" class="pc-empty">
              <i class="fas fa-spinner fa-spin"></i> Đang tải...
            </td>
          </tr>
          <tr v-else-if="items.length === 0">
            <td colspan="6" class="pc-empty">
              <i class="fas fa-inbox"></i> Không có dữ liệu
            </td>
          </tr>
          <tr v-for="(item, index) in items" :key="item.idColor + '-' + item.idProduct" class="pc-row">
            <td class="text-center">{{ currentPage * pageSize + index + 1 }}</td>
            <td>
              <span class="pc-color-badge">
                {{ item.nameColor || '—' }}
              </span>
            </td>
            <td>{{ item.productName || '—' }}</td>
            <td class="text-center">{{ formatDate(item.createdAt) }}</td>
            <td class="text-center">{{ formatDate(item.updatedAt) }}</td>
            <td class="text-center">
              <div class="pc-actions">
                <button class="pc-action-btn pc-action-edit" title="Sửa" @click="openEdit(item)">
                  <i class="fas fa-edit"></i>
                </button>
                <button class="pc-action-btn pc-action-delete" title="Xóa"
                        @click="deleteItem(item.idColor, item.idProduct)">
                  <i class="fas fa-trash-alt"></i>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div class="pc-pagination-bar" v-if="totalPages > 0">
      <div class="pc-pagination-info">
        Hiển thị {{ showingFrom }} - {{ showingTo }} / {{ totalItems }} mục
      </div>
      <div class="pc-pagination">
        <button class="pc-page-btn" :disabled="currentPage === 0" @click="goToPage(0)">
          <i class="fas fa-angle-double-left"></i>
        </button>
        <button class="pc-page-btn" :disabled="currentPage === 0" @click="goToPage(currentPage - 1)">
          <i class="fas fa-angle-left"></i>
        </button>
        <button v-for="p in pages" :key="p"
                class="pc-page-btn" :class="{ 'pc-page-active': p === currentPage }"
                @click="goToPage(p)">
          {{ p + 1 }}
        </button>
        <button class="pc-page-btn" :disabled="currentPage >= totalPages - 1" @click="goToPage(currentPage + 1)">
          <i class="fas fa-angle-right"></i>
        </button>
        <button class="pc-page-btn" :disabled="currentPage >= totalPages - 1" @click="goToPage(totalPages - 1)">
          <i class="fas fa-angle-double-right"></i>
        </button>
      </div>
    </div>

    <!-- Dialog -->
    <Dialog v-model:visible="displayDialog" :header="isEdit ? '✏️ Chỉnh sửa Product Color' : '➕ Thêm mới Product Color'" 
            :modal="true" class="pc-dialog" :style="{ width: '500px' }">
      <div class="pc-form">
        <div class="pc-field">
          <label class="pc-label">Sản phẩm</label>
          <Dropdown v-model="currentItem.idProduct" :options="productOptions" optionLabel="label" optionValue="value" 
                    placeholder="Chọn sản phẩm" filter class="pc-dropdown" />
        </div>
        <div class="pc-field">
          <label class="pc-label">Màu sắc</label>
          <Dropdown v-model="currentItem.idColor" :options="colorOptions" optionLabel="label" optionValue="value" 
                    placeholder="Chọn màu sắc" filter class="pc-dropdown" />
        </div>
      </div>
      <template #footer>
        <div class="pc-dialog-footer">
          <Button label="Hủy" icon="pi pi-times" class="p-button-text p-button-secondary" @click="displayDialog = false"/>
          <Button :label="isEdit ? 'Cập nhật' : 'Thêm mới'" :icon="isEdit ? 'pi pi-check' : 'pi pi-plus'" 
                  class="p-button-primary pc-save-btn" @click="saveItem" :loading="loading" />
        </div>
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
.pc-page {
  padding: 24px 28px;
  font-family: 'Inter', 'Segoe UI', sans-serif;
  color: #1e293b;
}

/* Header */
.pc-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e2e8f0;
}
.pc-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.pc-header-left h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
}
.pc-icon {
  font-size: 28px;
  color: #6366f1;
}

/* Export Button */
.pc-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.pc-btn-export {
  background: linear-gradient(135deg, #059669, #10b981);
  color: white;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
}
.pc-btn-export:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
}
.pc-btn-add {
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  color: white;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}
.pc-btn-add:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
}
.pc-header-actions {
  display: flex;
  gap: 12px;
}

/* Toolbar */
.pc-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}
.pc-toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.pc-select-group {
  display: flex;
  align-items: center;
  gap: 6px;
}
.pc-select-group label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
  white-space: nowrap;
}
.pc-select-group select {
  padding: 7px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 13px;
  background: white;
  color: #334155;
  cursor: pointer;
  outline: none;
  transition: border-color 0.2s;
}
.pc-select-group select:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}
.pc-search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 14px;
  background: white;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  min-width: 280px;
  transition: border-color 0.2s;
}
.pc-search-box:focus-within {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}
.pc-search-box i {
  color: #94a3b8;
  font-size: 14px;
}
.pc-search-box input {
  border: none;
  outline: none;
  font-size: 13px;
  color: #334155;
  flex: 1;
  background: transparent;
}

/* Table */
.pc-table-wrapper {
  overflow-x: auto;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}
.pc-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
.pc-table thead {
  background: linear-gradient(135deg, #4f46e5, #6366f1);
}
.pc-table thead th {
  color: white;
  padding: 14px 16px;
  text-align: left;
  font-weight: 600;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
}
.pc-table tbody td {
  padding: 12px 16px;
  border-bottom: 1px solid #f1f5f9;
  color: #334155;
}
.pc-row {
  transition: background 0.15s;
}
.pc-row:hover {
  background: #f1f5f9;
}
.pc-row:nth-child(even) {
  background: #fafbfc;
}
.pc-row:nth-child(even):hover {
  background: #f1f5f9;
}
.text-center {
  text-align: center;
}
.pc-empty {
  text-align: center;
  padding: 48px 16px !important;
  color: #94a3b8;
  font-size: 15px;
}
.pc-empty i {
  margin-right: 8px;
  font-size: 18px;
}

/* Color Badge */
.pc-color-badge {
  display: inline-block;
  padding: 4px 12px;
  background: linear-gradient(135deg, #ede9fe, #ddd6fe);
  color: #5b21b6;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

/* Actions */
.pc-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.pc-action-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}
.pc-action-edit {
  background: #fef3c7;
  color: #b45309;
}
.pc-action-edit:hover {
  background: #fde68a;
  transform: scale(1.1);
}
.pc-action-delete {
  background: #fee2e2;
  color: #dc2626;
}
.pc-action-delete:hover {
  background: #fecaca;
  transform: scale(1.1);
}

/* Pagination */
.pc-pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  padding: 12px 0;
}
.pc-pagination-info {
  color: #64748b;
  font-size: 13px;
}
.pc-pagination {
  display: flex;
  align-items: center;
  gap: 4px;
}
.pc-page-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  color: #475569;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}
.pc-page-btn:hover:not(:disabled):not(.pc-page-active) {
  background: #f1f5f9;
  border-color: #6366f1;
  color: #6366f1;
}
.pc-page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.pc-page-active {
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  color: white;
  border-color: #4f46e5;
  box-shadow: 0 2px 6px rgba(99, 102, 241, 0.35);
}

/* Dialog & Form Styles */
:deep(.pc-dialog) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}
:deep(.p-dialog-header) {
  background: #f8fafc;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
}
:deep(.p-dialog-content) {
  padding: 24px !important;
}
:deep(.p-dialog-footer) {
  padding: 16px 24px;
  border-top: 1px solid #e2e8f0;
  background: #f8fafc;
}
.pc-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.pc-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}
.pc-label {
  font-size: 14px;
  font-weight: 600;
  color: #475569;
}
.pc-dropdown {
  width: 100%;
  border-radius: 8px;
  border-color: #cbd5e1;
}
.pc-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
.pc-save-btn {
  padding: 8px 24px;
  border-radius: 8px;
}
</style>
