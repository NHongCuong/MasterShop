<script setup lang="ts">
import { ref, onMounted } from "vue";
import axios from "axios";
import { useRoute, useRouter } from "vue-router";
import { useToast } from "primevue/usetoast";
import Toast from "primevue/toast";
import { Product } from "../interfaces/app";
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

const myProduct = ref<Product | null>(null);
const colors = ref<any[]>([]);
const materials = ref<any[]>([]);
const dimensions = ref<any[]>([]);

const colorName = ref<string>("");
const materialName = ref<string>("");
const dimensionName = ref<string>("");

const isAuthenticated = ref(false);
const selectedUser = ref<any>(null);

const ORDER_API = "http://localhost:8081/detailcart";
const PRODUCT_API = "http://localhost:8081/product";
const COLOR_API = "http://localhost:8081/detailcolor";
const MATERIAL_API = "http://localhost:8081/detailmaterial";
const DIMENSION_API = "http://localhost:8081/dimensions";

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

// ==== ON MOUNTED ====
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

// ==== BUY NOW ====
async function BuyNow() {
  try {
    if (!selectedUser.value || !selectedUser.value.id) {
      toast.add({ severity: "warn", summary: "Chưa đăng nhập", detail: "Vui lòng đăng nhập để đặt hàng", life: 2500 });
      router.push({ name: "signin" });
      return;
    }

    const orderData = {
     // user: { id: selectedUser.value.id },
      product: { id: Number(idProduct) },
      color: { id: selectedColorId },
      material: { id: selectedMaterialId },
      dimension: { id: selectedDimensionId },
      amount: selectedAmount,
      //price: myProduct.value?.price,
      //status: "PENDING"
    };

    const res = await axios.post(`${ORDER_API}/addcart`, orderData);
    if (res.status === 200 || res.status === 201) {
      toast.add({
        severity: "success",
        summary: "Thành công",
        detail: "Đặt hàng thành công!",
        life: 2000
      });
      router.push("/order-success");
    } else {
      toast.add({
        severity: "warn",
        summary: "Cảnh báo",
        detail: "Phản hồi không mong đợi từ server",
        life: 2500
      });
    }
  } catch (err: any) {
    toast.add({
      severity: "error",
      summary: "Lỗi",
      detail: err.response?.data?.message || "Không thể đặt hàng",
      life: 3000
    });
  }
}

// ==== CLOSE BUTTON ====
function ClosePage() {
  // Nếu có idProduct thì quay về chi tiết sản phẩm
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
