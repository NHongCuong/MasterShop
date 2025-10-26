<script setup lang="ts">
import {ref, onMounted} from 'vue'
import axios from 'axios'
import {Category, Supplier} from '../../interfaces/app.ts'

import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Textarea from 'primevue/textarea'
import Dropdown from 'primevue/dropdown'
import Toast from 'primevue/toast'
import {useToast} from 'primevue/usetoast'

const productList = ref<any[]>([])
const categoryList = ref<Category[]>([])
const supplierList = ref<Supplier[]>([])
const selectedProduct = ref<any>(null)
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
    toast.add({severity: 'error', summary: 'Lỗi', detail: 'Không thể tải sản phẩm', life: 2000})
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
  selectedProduct.value = {
    id: null,
    name: '',
    description: '',
    price: 0,
    avatar: '',
    amount: 0,
    category: null,
    supplier: null
  }
  editMode.value = false
  visibleDialog.value = true
}

// ========== EDIT ==========
const openEditDialog = (product: any) => {
  selectedProduct.value = {...product}
  // Gắn đúng object category & supplier từ danh sách
  if (product.category && product.category.id) {
    selectedProduct.value.category = categoryList.value.find(c => c.id === product.category.id) || {id: product.category.id}
  }
  if (product.supplier && product.supplier.id) {
    selectedProduct.value.supplier = supplierList.value.find(s => s.id === product.supplier.id) || {id: product.supplier.id}
  }
  editMode.value = true
  visibleDialog.value = true
}

// ========== DELETE ==========
const deleteProduct = async (id: number) => {
  if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) return
  try {
    await axios.delete(`${API_URL}/delete/${id}`)
    toast.add({severity: 'success', summary: 'Thành công', detail: 'Đã xóa sản phẩm', life: 1500})
    loadProducts()
  } catch {
    toast.add({severity: 'error', summary: 'Lỗi', detail: 'Xóa thất bại', life: 2000})
  }
}

// ========== SAVE (THÊM / SỬA) ==========
const saveProduct = async () => {
  try {
    const payload = {
      name: selectedProduct.value.name,
      description: selectedProduct.value.description,
      price: selectedProduct.value.price,
      avatar: selectedProduct.value.avatar,
      amount: selectedProduct.value.amount,
      category: selectedProduct.value.category ? {id: selectedProduct.value.category.id} : null,
      supplier: selectedProduct.value.supplier ? {id: selectedProduct.value.supplier.id} : null
    }

    if (editMode.value) {
      // 🔥 Sửa URL theo format backend
      await axios.put(`${API_URL}/update/${selectedProduct.value.id}`, payload)
      toast.add({severity: 'success', summary: 'Cập nhật', detail: 'Sửa sản phẩm thành công', life: 1500})
    } else {
      await axios.post(`${API_URL}/add`, payload)
      toast.add({severity: 'success', summary: 'Thêm mới', detail: 'Thêm sản phẩm thành công', life: 1500})
    }

    visibleDialog.value = false
    loadProducts()
  } catch (err) {
    console.error('❌ Save error:', err)
    toast.add({severity: 'error', summary: 'Lỗi', detail: 'Không thể lưu sản phẩm', life: 2000})
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
    <h2 class="text-2xl font-bold mb-4">📦 Quản lý sản phẩm</h2>

    <Button label="➕ Thêm sản phẩm" icon="pi pi-plus" class="mb-3" @click="openAddDialog"/>

    <DataTable :value="productList" paginator :rows="20" stripedRows tableStyle="min-width: 60rem">
      <Column field="id" header="ID" sortable/>
      <Column field="name" header="Tên sản phẩm" sortable/>
      <Column field="category.name" header="Danh mục" sortable/>
      <Column field="supplier.name" header="Nhà cung cấp" sortable/>
      <Column field="price" header="Giá" sortable/>
      <Column field="amount" header="Số lượng"/>
      <Column field="description" header="Mô tả"/>
      <Column header="Ảnh">
        <template #body="{ data }">
          <img :src="data.avatar" alt="Ảnh" width="60" height="60" class="rounded" style="object-fit:cover;"/>
        </template>
      </Column>
      <Column header="Thao tác" style="width: 150px">
        <template #body="{ data }">
          <Button icon="pi pi-pencil" class="p-button-warning mr-2" @click="openEditDialog(data)"/>
          <Button icon="pi pi-trash" class="p-button-danger" @click="deleteProduct(data.id)"/>
        </template>
      </Column>
    </DataTable>

    <!-- Dialog -->
    <Dialog
        v-model:visible="visibleDialog"
        :header="editMode ? '✏️ Sửa sản phẩm' : '🆕 Thêm sản phẩm'"
        modal
        class="w-[30rem]" style="width:555px"
    >
      <div class="flex flex-col gap-3">
        <span class="p-float-label">
          <InputText style="width:500px" v-model="selectedProduct!.name"/>
          <label style="font-size:15px">Tên sản phẩm</label>
        </span>
        <br>
        <span class="p-float-label">
          <Dropdown
              v-model="selectedProduct!.category"
              :options="categoryList"
              optionLabel="name"
              placeholder="Chọn danh mục"
              style="width:500px"
          />
          <label style="font-size:15px">Danh mục</label>
        </span>
        <br>
        <span class="p-float-label">
          <Dropdown
              v-model="selectedProduct!.supplier"
              :options="supplierList"
              optionLabel="name"
              placeholder="Chọn nhà cung cấp"
              style="width:500px"
          />
          <label style="font-size:15px">Nhà cung cấp</label>
        </span>
        <br>
        <span class="p-float-label">
          <InputText style="width:500px" v-model="selectedProduct!.avatar"/>
          <label style="font-size:15px">Link ảnh</label>
        </span>
        <br>
        <span class="p-float-label">
          <InputNumber style="width:400px" v-model="selectedProduct!.price" mode="currency" currency="VND"
                       locale="vi-VN"/>
          <label style="font-size:15px">Giá</label>
        </span>
        <br>
        <span class="p-float-label">
          <InputNumber style="width:400px" v-model="selectedProduct!.amount"/>
          <label style="font-size:15px">Số lượng</label>
        </span>
        <br>
        <span class="p-float-label">
          <Textarea style="width:500px" v-model="selectedProduct!.description" rows="4"/>
          <label style="font-size:15px">Mô tả</label>
        </span>
      </div>

      <template #footer>
        <Button label="Hủy" icon="pi pi-times" class="p-button-text" @click="visibleDialog = false"/>
        <Button label="Lưu" icon="pi pi-check" @click="saveProduct"/>
      </template>
    </Dialog>

    <Toast/>

  </div>

</template>
<style scoped>
.p-datatable {
  font-size: 14px;
}
</style>