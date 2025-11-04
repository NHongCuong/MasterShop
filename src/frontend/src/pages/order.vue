<!--<script setup lang="ts">-->
<!--import {ref, onMounted} from 'vue'-->
<!--import axios from 'axios'-->
<!--import {ShipMethod, MethodOfPayment} from '../interfaces/app.ts'-->

<!--import DataTable from 'primevue/datatable'-->
<!--import Column from 'primevue/column'-->
<!--import Button from 'primevue/button'-->
<!--import Dialog from 'primevue/dialog'-->
<!--import InputText from 'primevue/inputtext'-->
<!--import InputNumber from 'primevue/inputnumber'-->
<!--import Textarea from 'primevue/textarea'-->
<!--import Dropdown from 'primevue/dropdown'-->
<!--import Toast from 'primevue/toast'-->
<!--import {useToast} from 'primevue/usetoast'-->
<!--import {useRoute, useRouter} from "vue-router";-->

<!--const route = useRoute();-->
<!--const router = useRouter();-->

<!--const productList = ref<any[]>([])-->
<!--const shipmethodList = ref<ShipMethod[]>([])-->
<!--const methodofpaymentList = ref<MethodOfPayment[]>([])-->
<!--const selectedProduct = ref<any>(null)-->
<!--const visibleDialog = ref(false)-->
<!--const editMode = ref(false)-->
<!--const toast = useToast()-->
<!--const API_URL = 'http://localhost:8081/product'-->
<!--const SHIPMETHOD_API = 'http://localhost:8081/shipmethod'-->
<!--const METHODOFPAYMENT_API = 'http://localhost:8081/methodofpayment'-->
<!--const ORDER_URL = 'http://localhost:8081/order'-->
<!--const selectedOrder = ref<any>(null)-->

<!--// ========== LOAD DATA ==========-->
<!--const loadProducts = async () => {-->
<!--  try {-->
<!--    const res = await axios.get(`${API_URL}/all`)-->
<!--    productList.value = res.data-->
<!--  } catch (err) {-->
<!--    toast.add({severity: 'error', summary: 'Lỗi', detail: 'Không thể tải sản phẩm', life: 2000})-->
<!--  }-->
<!--}-->

<!--const loadShipMethod = async () => {-->
<!--  const res = await axios.get(`${SHIPMETHOD_API}/all`)-->
<!--  shipmethodList.value = res.data-->
<!--}-->

<!--const loadMethodOfPayment = async () => {-->
<!--  const res = await axios.get(`${METHODOFPAYMENT_API}/all`)-->
<!--  methodofpaymentList.value = res.data-->
<!--}-->

<!--// ========== ADD ==========-->
<!--const openAddDialog = () => {-->
<!--  selectedOrder.value = {-->
<!--    id: null,-->
<!--    CustomerName: '',-->
<!--    Note_O: '',-->
<!--    Phone_O: 0,-->
<!--    Address_O: '',-->
<!--    shipmethod: null,-->
<!--    methodofpayment: null-->
<!--  }-->
<!--  visibleDialog.value = true-->
<!--}-->


<!--// ========== SAVE (THÊM / SỬA) ==========-->
<!--const saveProduct = async () => {-->
<!--  try {-->
<!--    const payload = {-->
<!--      CustomerName: selectedOrder.value.name,-->
<!--      Note_O: selectedOrder.value.description,-->
<!--      Phone_O: selectedOrder.value.price,-->
<!--      Address_O: selectedOrder.value.avatar,-->
<!--      shipmethod: selectedOrder.value.shipmethod ? {id: selectedOrder.value.category.id} : null,-->
<!--      methodofpayment: selectedOrder.value.methodofpayment ? {id: selectedOrder.value.supplier.id} : null-->
<!--    }-->


<!--      await axios.post(`${ORDER_URL}/add`, payload)-->
<!--      toast.add({severity: 'success', summary: 'Thêm mới', detail: 'Thêm đơn hàng thành công', life: 1500})-->


<!--    visibleDialog.value = false-->
<!--  } catch (err) {-->
<!--    console.error('❌ Save error:', err)-->
<!--    toast.add({severity: 'error', summary: 'Lỗi', detail: 'Không thể lưu sản phẩm', life: 2000})-->
<!--  }-->
<!--}-->

<!--onMounted(() => {-->

<!--  loadShipMethod()-->
<!--  loadMethodOfPayment()-->
<!--})-->
<!--</script>-->

<!--<template>-->
<!--  <div class="p-6">-->
<!--    <h2 class="text-2xl font-bold mb-4">📦 Quản lý sản phẩm</h2>-->

<!--    <Button label="➕ Thêm sản phẩm" icon="pi pi-plus" class="mb-3" @click="openAddDialog"/>-->

<!--    &lt;!&ndash; Dialog &ndash;&gt;-->
<!--    <Dialog-->
<!--        v-model:visible="visibleDialog"-->
<!--        class="w-[30rem]" style="width:555px"-->
<!--    >-->
<!--      <div class="flex flex-col gap-3">-->
<!--        <span class="p-float-label">-->
<!--          <InputText style="width:500px" v-model="selectedProduct!.name"/>-->
<!--          <label style="font-size:15px">Tên khách hàng</label>-->
<!--        </span>-->
<!--        <br>-->
<!--        <span class="p-float-label">-->
<!--          <Dropdown-->
<!--              v-model="selectedOrder!.shipmethod"-->
<!--              :options="shipmethodList"-->
<!--              optionLabel="nameSM"-->
<!--              placeholder="Chọn phương thức giao hàng"-->
<!--              style="width:500px"-->
<!--          />-->
<!--          <label style="font-size:15px">Phương thức giao hàng</label>-->
<!--        </span>-->
<!--        <br>-->
<!--        <span class="p-float-label">-->
<!--          <Dropdown-->
<!--              v-model="selectedOrder!.methodofpayment"-->
<!--              :options="methodofpaymentList"-->
<!--              optionLabel="nameMOP"-->
<!--              placeholder="Chọn phương thức thanh toán"-->
<!--              style="width:500px"-->
<!--          />-->
<!--          <label style="font-size:15px">Phương thức thanh toán</label>-->
<!--        </span>-->
<!--        <br>-->
<!--        <span class="p-float-label">-->
<!--          <InputText style="width:500px" v-model="selectedOrder!.Note_O"/>-->
<!--          <label style="font-size:15px">Ghi Chú</label>-->
<!--        </span>-->
<!--        <br>-->

<!--        <span class="p-float-label">-->
<!--          <InputNumber style="width:400px" v-model="selectedOrder!.Phone_O"/>-->
<!--          <label style="font-size:15px">Số điện thoại</label>-->
<!--        </span>-->
<!--        <br>-->
<!--        <span class="p-float-label">-->
<!--          <Textarea style="width:500px" v-model="selectedOrder!.Address_O" rows="4"/>-->
<!--          <label style="font-size:15px">Địa chỉ</label>-->
<!--        </span>-->
<!--      </div>-->

<!--      <template #footer>-->
<!--        <Button label="Hủy" icon="pi pi-times" class="p-button-text" @click="visibleDialog = false"/>-->
<!--        <Button label="Lưu" icon="pi pi-check" @click="saveProduct"/>-->
<!--      </template>-->
<!--    </Dialog>-->

<!--    <Toast/>-->

<!--  </div>-->

<!--</template>-->
<!--<style scoped>-->
<!--.p-datatable {-->
<!--  font-size: 14px;-->
<!--}-->
<!--</style>-->