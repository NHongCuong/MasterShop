<script setup lang="ts">
import { ref, onMounted } from "vue";
import axios from "axios";
import { useRoute, useRouter } from "vue-router";
import { useToast } from "primevue/usetoast";
import Toast from "primevue/toast";
import Dialog from "primevue/dialog";
import InputText from "primevue/inputtext";
import Textarea from "primevue/textarea";
import Dropdown from "primevue/dropdown";
import Button from "primevue/button";

import { Product, ShipMethod, MethodOfPayment } from "../interfaces/app";
import { MyApp } from "../app/MyApp.ts";

const route = useRoute();
const router = useRouter();
const toast = useToast();

// ==== LẤY THAM SỐ TỪ QUERY ====
const idProduct = route.query.id as string | undefined;
const selectedColorId = route.query.color ? Number(route.query.color) : null;
const selectedMaterialId = route.query.material ? Number(route.query.material) : null;
const selectedDimensionId = route.query.dimension ? Number(route.query.dimension) : null;
const selectedAmount = route.query.amount ? Number(route.query.amount) : 1;

// ==== REF ====
const myProduct = ref<Product | null>(null);
const colors = ref<any[]>([]);
const materials = ref<any[]>([]);
const dimensions = ref<any[]>([]);
const colorName = ref<string>("");
const materialName = ref<string>("");
const dimensionName = ref<string>("");

const isAuthenticated = ref(false);
const selectedUser = ref<any>(null);

// ==== DIALOG BIẾN ====
const visibleDialog = ref(false);
const shipmethodList = ref<ShipMethod[]>([]);
const methodofpaymentList = ref<MethodOfPayment[]>([]);
const selectedOrder = ref<any>({
  id: null,
  customerName: "",
  noteO: "",
  phone: "",
  addressO: "",
  shipMethod: null,
  methodofPayment: null
});

// ==== API ====
const PRODUCT_API = "http://localhost:8081/product";
const COLOR_API = "http://localhost:8081/detailcolor";
const MATERIAL_API = "http://localhost:8081/detailmaterial";
const DIMENSION_API = "http://localhost:8081/dimensions";
const SHIPMETHOD_API = "http://localhost:8081/shipmethod";
const METHODOFPAYMENT_API = "http://localhost:8081/methodofpayment";
const ORDER_URL = "http://localhost:8081/order";
const CART_API = "http://localhost:8081/cart";
const CARTDETAIL_API = "http://localhost:8081/cartdetail";
const ORDERDETAIL_API = "http://localhost:8081/orderdetail";

// ==== ĐỊNH DẠNG TIỀN ====
function formatMoney(value: number | string | undefined): string {
  if (!value) return "0 VNĐ";
  return Number(value).toLocaleString("vi-VN") + " VNĐ";
}

// ==== HÀM LẤY ID/NAME CHUNG ====
function extractId(obj: any) {
  return obj?.id ?? obj?.idColor ?? obj?.idMaterial ?? obj?.idD ?? obj?.ID ?? null;
}
function extractName(obj: any) {
  return (
      obj?.nameColor ??
      obj?.nameMaterial ??
      obj?.nameD ??
      obj?.name ??
      obj?.label ??
      ""
  );
}

// ==== LOAD API ====
onMounted(() => {
  MyApp.getIntance().authenticate(isAuthenticated, selectedUser);
  if (!idProduct) {
    toast.add({ severity: "warn", summary: "Thiếu thông tin", detail: "Không tìm thấy product id", life: 2500 });
    return;
  }

  loadProduct();
  loadColors();
  loadMaterials();
  loadDimensions();
  loadShipMethod();
  loadMethodOfPayment();
});

// ==== LOAD PRODUCT ====
async function loadProduct() {
  try {
    const res = await axios.get(`${PRODUCT_API}/${idProduct}`);
    myProduct.value = res.data;
  } catch (err) {
    toast.add({ severity: "error", summary: "Lỗi", detail: "Không tải được sản phẩm", life: 2000 });
  }
}

// ==== LOAD COLORS ====
async function loadColors() {
  try {
    const res = await axios.get(`${COLOR_API}/all/${idProduct}`);
    colors.value = res.data || [];
    const found = colors.value.find((c: any) => extractId(c) === selectedColorId);
    colorName.value = found ? extractName(found) : "Không xác định";
  } catch (err) {
    console.error("Load colors failed", err);
  }
}

// ==== LOAD MATERIALS ====
async function loadMaterials() {
  try {
    const res = await axios.get(`${MATERIAL_API}/all/${idProduct}`);
    materials.value = res.data || [];
    if (!selectedMaterialId || selectedMaterialId === 0) {
      materialName.value = "Không xác định";
      return;
    }
    const found = materials.value.find((m: any) => extractId(m) === selectedMaterialId);
    materialName.value = found ? extractName(found) : "Không xác định";
  } catch (err) {
    console.error("Load materials failed", err);
  }
}

// ==== LOAD DIMENSIONS ====
async function loadDimensions() {
  try {
    const res = await axios.get(`${DIMENSION_API}/all/${idProduct}`);
    dimensions.value = res.data || [];
    const found = dimensions.value.find((d: any) => extractId(d) === selectedDimensionId);
    dimensionName.value = found ? extractName(found) : "Không xác định";
  } catch (err) {
    console.error("Load dimensions failed", err);
  }
}

// ==== LOAD SHIP / PAYMENT ====
async function loadShipMethod() {
  try {
    const res = await axios.get(`${SHIPMETHOD_API}/all`);
    shipmethodList.value = res.data.map((item: any) => ({
      label: item.nameSM,
      value: item.id
    }));
  } catch (err) {
    console.error("Load ship method failed", err);
  }
}

async function loadMethodOfPayment() {
  try {
    const res = await axios.get(`${METHODOFPAYMENT_API}/all`);
    methodofpaymentList.value = res.data.map((item: any) => ({
      label: item.name_mop,
      value: item.id
    }));
  } catch (err) {
    console.error("Load method of payment failed", err);
  }
}

// ==== BUY NOW ====
function BuyNow() {
  selectedOrder.value = {
    id: null,
    customerName: "",
    noteO: "",
    phone: "",
    addressO: "",
    shipMethod: null,
    methodofPayment: null
  }
  visibleDialog.value = true
}

// ==== SAVE ORDER ====
async function saveOrder() {
  try {
    if (!selectedOrder.value.customerName || !selectedOrder.value.addressO) {
      toast.add({ severity: "warn", summary: "Thiếu thông tin", detail: "Vui lòng nhập đủ thông tin khách hàng", life: 2500 });
      return;
    }

    const payload = {
      customerName: selectedOrder.value.customerName,
      noteO: selectedOrder.value.noteO,
      phone: selectedOrder.value.phone,
      addressO: selectedOrder.value.addressO,
      shipMethod: selectedOrder.value.shipMethod ? {id: selectedOrder.value.shipMethod} : null,
      methodofPayment: selectedOrder.value.methodofPayment ? {id: selectedOrder.value.methodofPayment} : null
    };

		// 1) Tạo đơn hàng, lấy ID_Order
		const orderRes = await axios.post(`${ORDER_URL}/add`, payload);
		const orderId = orderRes.data?.id;
		if (!orderId) {
			throw new Error("Không nhận được ID_Order từ server");
		}

		// 2) Tạo shopcart cho user hiện tại, trạng thái = 3
		if (!selectedUser.value?.id) {
			throw new Error("Chưa xác thực người dùng");
		}
		const cartPayload = {
			userSC: { id: selectedUser.value.id },
			cartStatus: { id: 3 }
		};
		const scRes = await axios.post(`${CART_API}/add`, cartPayload);
		const scId = scRes.data?.id;
		if (!scId) {
			throw new Error("Không nhận được ID_SC từ server");
		}

		// 3) Thêm chi tiết giỏ hàng (sản phẩm + biến thể + số lượng) vào cart_detail
		await axios.post(`${CARTDETAIL_API}/addcart`, {
			idSC: scId,
			idProduct: Number(idProduct),
			amountCD: selectedAmount,
			idColor: selectedColorId ?? null,
			idMaterial: selectedMaterialId ?? null,
			idD: selectedDimensionId ?? null
		});

		// 4) Tạo bản ghi order_detail liên kết ID_Order và ID_SC
		await axios.post(`${ORDERDETAIL_API}/add`, {
			idOrder: orderId,
			idSC: scId
		});

		toast.add({ severity: "success", summary: "Thành công", detail: "Đơn hàng đã được lưu", life: 2000 });
		visibleDialog.value = false;
  } catch (err) {
    console.error("Save order failed", err);
    toast.add({ severity: "error", summary: "Lỗi", detail: "Không thể lưu đơn hàng", life: 2500 });
  }
}

// ==== CLOSE BUTTON ====
function ClosePage() {
  if (idProduct) {
    router.push({ name: "detailproduct", params: { id: idProduct } });
  } else {
    router.push({ name: "shophome" });
  }
}
</script>

<template>
  <div class="p-5 mt-5 bg-light" style="border-radius: 25px;">
    <h1 class="mb-4 text-center">Xác nhận đơn hàng</h1>

    <div v-if="myProduct" class="card p-4">
      <h3>{{ myProduct?.name }}</h3>
      <p class="text-danger h4">
        <strong>{{ formatMoney(myProduct?.price) }}</strong>
      </p>
      <hr />

      <div class="row mb-3">
        <div class="col-md-6"><strong>Màu sắc:</strong></div>
        <div class="col-md-6">{{ colorName }}</div>
      </div>

      <div class="row mb-3">
        <div class="col-md-6"><strong>Chất liệu:</strong></div>
        <div class="col-md-6">{{ materialName }}</div>
      </div>

      <div class="row mb-3">
        <div class="col-md-6"><strong>Kích thước:</strong></div>
        <div class="col-md-6">{{ dimensionName }}</div>
      </div>

      <div class="row mb-3">
        <div class="col-md-6"><strong>Số lượng:</strong></div>
        <div class="col-md-6">{{ selectedAmount }}</div>
      </div>

      <hr />
      <div class="text-center mt-4 d-flex justify-content-center gap-3">
        <button class="btn btn-success btn-lg px-5" @click="BuyNow">
          <i class="pi pi-check mr-2"></i> Buy
        </button>
        <button class="btn btn-secondary btn-lg px-5" @click="ClosePage">
          <i class="pi pi-times mr-2"></i> Close
        </button>
      </div>
    </div>

    <div v-else>
      <p>Đang tải sản phẩm...</p>
    </div>

    <!-- DIALOG NHẬP THÔNG TIN -->
    <Dialog v-model:visible="visibleDialog" header="Thông tin khách hàng" class="w-[30rem]" style="width: 520px">
      <div class="flex flex-col gap-3">
        <span class="p-float-label">
          <InputText style="width:100%" v-model="selectedOrder!.customerName" />
          <label>Tên khách hàng</label>
        </span>

        <span class="p-float-label">
          <InputText style="width:100%" v-model="selectedOrder!.phone" />
          <label>Số điện thoại</label>
        </span>

        <span class="p-float-label">
          <Textarea style="width:100%" rows="3" v-model="selectedOrder!.addressO" />
          <label>Địa chỉ</label>
        </span>

        <span class="p-float-label">
          <Dropdown
              v-model="selectedOrder!.shipMethod"
              :options="shipmethodList"
              optionLabel="label"
              optionValue="value"
              placeholder="Chọn phương thức giao hàng"
              style="width:100%"
          />
          <label>Phương thức giao hàng</label>
        </span>

        <span class="p-float-label">
          <Dropdown
              v-model="selectedOrder!.methodofPayment"
              :options="methodofpaymentList"
              optionLabel="label"
              optionValue="value"
              placeholder="Chọn phương thức thanh toán"
              style="width:100%"
          />
          <label>Phương thức thanh toán</label>
        </span>

        <span class="p-float-label">
          <InputText style="width:100%" v-model="selectedOrder!.noteO" />
          <label>Ghi chú</label>
        </span>
      </div>

      <template #footer>
        <Button label="Hủy" icon="pi pi-times" class="p-button-text" @click="visibleDialog = false" />
        <Button label="Lưu" icon="pi pi-check" @click="saveOrder" />
      </template>
    </Dialog>

    <Toast />
  </div>
</template>

<style scoped>
.card {
  background: white;
  border-radius: 15px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}
.text-danger {
  font-weight: bold;
}
</style>
