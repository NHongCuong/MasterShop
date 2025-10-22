<script setup lang="ts">
import {ref, onMounted} from 'vue'
import axios from 'axios'
import {Category, Supplier} from '../../interfaces/app.ts'

import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
//import InputNumber from 'primevue/inputnumber'
//import Textarea from 'primevue/textarea'
//import Dropdown from 'primevue/dropdown'
import Toast from 'primevue/toast'
import { useToast } from 'primevue/usetoast'

const productList = ref<any[]>([])
const categoryList = ref<Category[]>([])
const supplierList = ref<Supplier[]>([])
//const selectedProduct = ref<any>(null)
const selectedCategory = ref<any>(null)
const visibleDialog = ref(false)
const editMode = ref(false)
const toast = useToast()
const API_URL = 'http://localhost:8081/product'
const CATEGORY_API = 'http://localhost:8081/category'
const SUPPLIER_API = 'http://localhost:8081/supplier'

// ========== LOAD DATA ==========
const loadProducts = async () => {
  try {
    const res = await axios.get(`${API_URL}/all`)
    productList.value = res.data
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải sản phẩm', life: 2000 })
  }
}

const loadCategories = async () => {
  const res = await axios.get(`${CATEGORY_API}/all`)
  categoryList.value = res.data
}

const loadSuppliers = async () => {
  const res = await axios.get(`${SUPPLIER_API}/all`)
  supplierList.value = res.data
}

// ========== ADD ==========
const openAddDialog = () => {
  selectedCategory.value = {
    id: null,
    name: '',
    icon: ''
  }
  editMode.value = false
  visibleDialog.value = true
}

// ========== EDIT ==========
const openEditDialog = (category: any) => {
  selectedCategory.value = { ...category }
  // Gắn đúng object category & supplier từ danh sách
  if (category.category && category.category.id) {
    selectedCategory.value.category = categoryList.value.find(c => c.id === category.category.id) || { id: category.category.id }
  }
  editMode.value = true
  visibleDialog.value = true
}

// ========== DELETE ==========
const deleteCategory = async (id: number) => {
  if (!confirm('Bạn có chắc chắn muốn xóa danh mục này?')) return
  try {
    await axios.delete(`${CATEGORY_API}/delete/${id}`)
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa danh mục', life: 1500 })
    loadCategories()
  } catch {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Xóa thất bại', life: 2000 })
  }
}

// ========== SAVE (THÊM / SỬA) ==========
const saveCategory = async () => {
  try {
    const payload = {
      id: selectedCategory.value.id,
      name: selectedCategory.value.name,
      icon: selectedCategory.value.icon
    }

    if (editMode.value) {
      // 🔥 Sửa URL theo format backend
      await axios.put(`${CATEGORY_API}/update/${selectedCategory.value.id}`, payload)
      toast.add({ severity: 'success', summary: 'Cập nhật', detail: 'Sửa danh mục thành công', life: 1500 })
    } else {
      await axios.post(`${CATEGORY_API}/add`, payload)
      toast.add({ severity: 'success', summary: 'Thêm mới', detail: 'Thêm danh mục thành công', life: 1500 })
    }

    visibleDialog.value = false
    loadCategories()
  } catch (err) {
    console.error('❌ Save error:', err)
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể lưu danh mục', life: 2000 })
  }
}

onMounted(() => {
  loadProducts()
  loadCategories()
  loadSuppliers()
})
</script>

<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold mb-4">📦 Quản lý danh mục</h2>

    <Button label="➕ Thêm danh mục" icon="pi pi-plus" class="mb-3" @click="openAddDialog" />

    <DataTable :value="categoryList" paginator :rows="10" stripedRows tableStyle="min-width: 60rem">
      <Column field="id" header="ID" sortable />
      <Column field="name" header="Tên danh mục" sortable />
      <Column header="Ảnh">
        <template #body="{ data }">
          <img :src="data.icon" alt="Ảnh" width="60" height="60" class="rounded" style="object-fit:cover;" />
        </template>
      </Column>
      <Column header="Thao tác" style="width: 150px">
        <template #body="{ data }">
          <Button icon="pi pi-pencil" class="p-button-warning mr-2" @click="openEditDialog(data)" />
          <Button icon="pi pi-trash" class="p-button-danger" @click="deleteCategory(data.id)" />
        </template>
      </Column>
    </DataTable>

    <!-- Dialog -->
    <Dialog
        v-model:visible="visibleDialog"
        :header="editMode ? '✏️ Sửa danh mục' : '🆕 Thêm danh mục'"
        modal
        class="w-[30rem]" style="width:555px"
    >
      <div class="flex flex-col gap-3">
        <span class="p-float-label">
          <InputText style="width:500px" v-model="selectedCategory!.id" />
          <label style="font-size:15px">ID</label>
        </span>
        <br>
        <span class="p-float-label">
          <InputText style="width:500px" v-model="selectedCategory!.name" />
          <label style="font-size:15px">Tên danh mục</label>
        </span>
        <br>
        <span class="p-float-label">
          <InputText style="width:500px" v-model="selectedCategory!.icon" />
          <label style="font-size:15px">Icon</label>
        </span>
        <br>
      </div>

      <template #footer>
        <Button label="Hủy" icon="pi pi-times" class="p-button-text" @click="visibleDialog = false" />
        <Button label="Lưu" icon="pi pi-check" @click="saveCategory" />
      </template>
    </Dialog>

    <Toast />

  </div>

</template>
<style scoped>
.p-datatable {
  font-size: 14px;
}
</style>