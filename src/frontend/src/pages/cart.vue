<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import axios from 'axios';
import { state, MyApp } from '../app/MyApp';
import Helper from '../helper/helper';
import { useToast } from "primevue/usetoast";
import Toast from "primevue/toast";
import { useRouter } from 'vue-router';

const router = useRouter();
const cartItems = ref<any[]>([]);
const loading = ref(true);
const toast = useToast();

// Voucher state
const voucherCode = ref('');
const voucherLoading = ref(false);
const appliedVoucher = ref<any>(null);
const voucherError = ref('');

const loadCart = async () => {
    if (!state.isAuthenticated || !state.user) return;
    loading.value = true;
    try {
        const res = await axios.get(`http://localhost:8081/cartdetail/user/${state.user.id}`);
        cartItems.value = res.data;
    } catch (err) {
        console.error("Lỗi tải giỏ hàng:", err);
    } finally {
        loading.value = false;
    }
};

const totalPrice = computed(() => {
    return cartItems.value.reduce((sum, item) => sum + (item.productPrice * item.amountCD), 0);
});

const discountAmount = computed(() => {
    if (!appliedVoucher.value || !appliedVoucher.value.discountPercent) return 0;
    return Math.round(totalPrice.value * appliedVoucher.value.discountPercent / 100);
});

const finalPrice = computed(() => {
    return totalPrice.value - discountAmount.value;
});

const applyVoucher = async () => {
    if (!voucherCode.value.trim()) {
        voucherError.value = 'Vui lòng nhập mã giảm giá';
        return;
    }
    voucherLoading.value = true;
    voucherError.value = '';
    try {
        const res = await axios.get(`http://localhost:8081/voucher/check/${voucherCode.value.trim().toUpperCase()}`);
        appliedVoucher.value = res.data;
        toast.add({ severity: 'success', summary: 'Thành công', detail: `Áp dụng mã giảm ${res.data.discountPercent}% thành công!`, life: 3000 });
    } catch (err: any) {
        appliedVoucher.value = null;
        voucherError.value = 'Mã giảm giá không hợp lệ hoặc đã hết hạn';
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Mã giảm giá không hợp lệ', life: 2000 });
    } finally {
        voucherLoading.value = false;
    }
};

const removeVoucher = () => {
    appliedVoucher.value = null;
    voucherCode.value = '';
    voucherError.value = '';
};

async function removeItem(item: any) {
    if (!confirm("Bạn có muốn xóa sản phẩm này khỏi giỏ hàng?")) return;
    try {
        await axios.delete(`http://localhost:8081/cart/remove-item`, {
            params: { scId: item.idSC, productId: item.idProduct }
        });
        toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa sản phẩm', life: 2000 });
        loadCart();
        MyApp.getInstance().updateCartCount();
    } catch (err) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể xóa sản phẩm', life: 2000 });
    }
}

async function changeQty(item: any, delta: number) {
    const newAmount = item.amountCD + delta;
    if (newAmount < 1) { removeItem(item); return; }
    try {
        await axios.post(`http://localhost:8081/cart/update-amount`, {
            scId: item.idSC,
            productId: item.idProduct,
            amount: newAmount
        });
        item.amountCD = newAmount;
        MyApp.getInstance().updateCartCount();
    } catch (err) {
        console.error("Lỗi khi cập nhật số lượng:", err);
    }
}

const goToCheckout = () => {
    if (cartItems.value.length === 0) {
        toast.add({ severity: 'warn', summary: 'Giỏ hàng trống', detail: 'Vui lòng thêm sản phẩm trước khi thanh toán', life: 2000 });
        return;
    }
    // Lưu voucher info vào sessionStorage để Order page dùng
    sessionStorage.setItem('checkoutData', JSON.stringify({
        cartItems: cartItems.value,
        voucher: appliedVoucher.value,
        totalPrice: totalPrice.value,
        discountAmount: discountAmount.value,
        finalPrice: finalPrice.value
    }));
    router.push('/order');
};

onMounted(() => {
    MyApp.getInstance().authenticate().then(() => { loadCart(); });
});
</script>

<template>
  <div class="container my-5 cart-page">
    <Toast />
    
    <!-- Breadcrumb -->
    <nav aria-label="breadcrumb" class="mb-4 bg-warning p-2 rounded shadow-sm">
      <ol class="breadcrumb m-0 fw-bold text-white">
        <li class="breadcrumb-item"><router-link to="/" class="text-white text-decoration-none">TRANG CHỦ</router-link></li>
        <li class="breadcrumb-item active text-white" aria-current="page">GIỎ HÀNG</li>
      </ol>
    </nav>

    <div class="row g-4">
      <!-- Cart Table -->
      <div class="col-lg-8">
        <div class="card border-0 shadow-sm rounded-4 overflow-hidden">
          <div class="card-body p-0">
            <table class="table table-hover align-middle mb-0">
              <thead class="bg-light">
                <tr>
                  <th class="ps-4 py-3">Sản phẩm</th>
                  <th class="text-center py-3">Đơn giá</th>
                  <th class="text-center py-3" style="width: 150px;">Số lượng</th>
                  <th class="text-center py-3">Thành tiền</th>
                  <th class="pe-4 py-3"></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in cartItems" :key="item.idSC + '-' + item.idProduct">
                  <td class="ps-4 py-4">
                    <div class="d-flex align-items-center gap-3">
                      <img :src="Helper.GetImageUrl(item.productAvatar)" 
                           class="rounded-3 object-fit-cover shadow-sm" width="70" height="70">
                      <div>
                        <h6 class="fw-bold mb-1">{{ item.productName }}</h6>
                        <div class="small text-muted d-flex flex-column gap-1">
                          <span v-if="item.idColor"><i class="fas fa-palette me-1"></i>Màu sắc: 
                            <span class="badge bg-light text-dark border">{{ item.idColor }}</span>
                          </span>
                          <span v-if="item.materialName"><i class="fas fa-layer-group me-1"></i>Chất liệu: {{ item.materialName }}</span>
                          <span v-if="item.dimensionName"><i class="fas fa-expand me-1"></i>Kích cỡ: {{ item.dimensionName }}</span>
                        </div>
                      </div>
                    </div>
                  </td>
                  <td class="text-center fw-bold">{{ Helper.ToMoney(item.productPrice) }}</td>
                  <td class="text-center">
                    <div class="input-group input-group-sm">
                        <button class="btn btn-outline-secondary" type="button" @click="changeQty(item, -1)">-</button>
                        <input type="text" class="form-control text-center" :value="item.amountCD" readonly>
                        <button class="btn btn-outline-secondary" type="button" @click="changeQty(item, 1)">+</button>
                    </div>
                  </td>
                  <td class="text-center text-danger fw-bold">
                    {{ Helper.ToMoney(item.productPrice * item.amountCD) }}
                  </td>
                  <td class="pe-4 text-end">
                    <button class="btn btn-sm btn-outline-danger border-0" @click="removeItem(item)">
                      <i class="fas fa-trash-alt"></i>
                    </button>
                  </td>
                </tr>
                <tr v-if="!loading && cartItems.length === 0">
                    <td colspan="5" class="text-center py-5">
                        <div class="text-muted">
                            <i class="fas fa-shopping-cart fa-3x mb-3 d-block"></i>
                            Giỏ hàng của bạn đang trống
                            <router-link to="/" class="d-block mt-2 text-decoration-none">Tiếp tục mua sắm</router-link>
                        </div>
                    </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Coupon Code Section -->
        <div class="voucher-card mt-4">
          <h6 class="fw-bold mb-3"><i class="fas fa-tag me-2 text-warning"></i>Mã giảm giá</h6>
          
          <!-- Đã áp dụng voucher -->
          <div v-if="appliedVoucher" class="applied-voucher">
            <div class="applied-voucher-content">
              <i class="fas fa-check-circle text-success me-2"></i>
              <div>
                <span class="fw-bold text-success">{{ appliedVoucher.maVoucher }}</span>
                <span class="text-muted ms-2">— Giảm {{ appliedVoucher.discountPercent }}%</span>
              </div>
            </div>
            <button class="btn btn-sm btn-outline-danger" @click="removeVoucher">
              <i class="fas fa-times"></i> Xóa
            </button>
          </div>
          
          <!-- Form nhập mã -->
          <div v-else>
            <div class="input-group">
              <input
                type="text"
                class="form-control"
                placeholder="Nhập mã giảm giá"
                v-model="voucherCode"
                @keyup.enter="applyVoucher"
                :class="{ 'is-invalid': voucherError }"
              >
              <button
                class="btn btn-warning fw-bold px-4"
                @click="applyVoucher"
                :disabled="voucherLoading"
              >
                <i v-if="voucherLoading" class="fas fa-spinner fa-spin me-1"></i>
                ÁP DỤNG
              </button>
            </div>
            <div v-if="voucherError" class="invalid-msg mt-1">
              <i class="fas fa-exclamation-circle me-1"></i>{{ voucherError }}
            </div>
          </div>
        </div>
      </div>

      <!-- Summary -->
      <div class="col-lg-4">
        <div class="card border-0 shadow-sm rounded-4 p-4 sticky-top" style="top: 100px;">
          <h5 class="fw-bold mb-4">Tổng quan đơn hàng</h5>
          <div class="d-flex justify-content-between mb-3">
            <span class="text-muted">Số lượng:</span>
            <span class="fw-bold">{{ cartItems.reduce((acc, i) => acc + i.amountCD, 0) }}</span>
          </div>
          <div class="d-flex justify-content-between mb-3">
            <span class="text-muted">Thành tiền:</span>
            <span class="fw-bold">{{ Helper.ToMoney(totalPrice) }}</span>
          </div>
          
          <!-- Giảm giá row -->
          <div v-if="appliedVoucher && discountAmount > 0" class="d-flex justify-content-between mb-3">
            <span class="text-success">
              <i class="fas fa-tag me-1"></i>Khuyến mãi ({{ appliedVoucher.discountPercent }}%):
            </span>
            <span class="fw-bold text-success">-{{ Helper.ToMoney(discountAmount) }}</span>
          </div>

          <hr class="my-4">
          <div class="d-flex justify-content-between mb-4 align-items-center">
            <span class="h6 mb-0 fw-bold">Tổng cộng:</span>
            <span class="h4 mb-0 text-danger fw-bold">{{ Helper.ToMoney(finalPrice) }}</span>
          </div>
          
          <button
            class="btn btn-warning w-100 py-3 fw-bold rounded-3 mb-3 shadow-sm"
            @click="goToCheckout"
            :disabled="cartItems.length === 0"
          >
            THANH TOÁN <i class="fas fa-arrow-right ms-2"></i>
          </button>
          
          <div class="text-center">
            <div class="d-flex align-items-center justify-content-center gap-4 text-muted small">
              <span class="cursor-pointer hover-text-primary"><i class="fas fa-phone-alt"></i></span>
              <span class="cursor-pointer hover-text-primary"><i class="fas fa-comment"></i></span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cart-page {
    min-height: 80vh;
}
.breadcrumb-item + .breadcrumb-item::before {
    color: white;
}
.table thead th {
    font-size: 0.85rem;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    border: none;
}
.table tbody td {
    border-color: #f8f9fa;
}
.hover-text-primary:hover {
    color: #0d6efd !important;
}
.btn-warning {
    background-color: #f1c40f;
    border: none;
    color: #000;
}
.btn-warning:hover {
    background-color: #d4ac0d;
}

/* Voucher card */
.voucher-card {
    background: white;
    border-radius: 16px;
    padding: 20px 24px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.08);
    max-width: 440px;
}

.applied-voucher {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: #f0fdf4;
    border: 1px solid #86efac;
    border-radius: 10px;
    padding: 12px 16px;
}
.applied-voucher-content {
    display: flex;
    align-items: center;
}
.invalid-msg {
    color: #ef4444;
    font-size: 13px;
}
.form-control.is-invalid {
    border-color: #ef4444;
}
</style>
