<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import axios from 'axios';
import { state, MyApp } from '../app/MyApp';
import Helper from '../helper/helper';
import { useToast } from 'primevue/usetoast';
import Toast from 'primevue/toast';
import { useRouter } from 'vue-router';

const router = useRouter();
const toast = useToast();

// Checkout data from cart
const checkoutData = ref<any>(null);
const cartItems = computed(() => checkoutData.value?.cartItems || []);
const voucher = computed(() => checkoutData.value?.voucher || null);
const totalPrice = computed(() => checkoutData.value?.totalPrice || 0);
const discountAmount = computed(() => checkoutData.value?.discountAmount || 0);
const finalPrice = computed(() => checkoutData.value?.finalPrice || 0);

// Form fields
const form = ref({
    fullName: '',
    gender: 'male',
    phone: '',
    email: '',
    deliveryType: 'home', // 'home' | 'store'
    province: null as any,
    district: null as any,
    ward: null as any,
    address: '',
    note: '',
    otherReceiver: false,
    receiverName: '',
    receiverPhone: '',
    paymentMethod: null as any,
    shipMethod: null as any,
    agreePolicy: false
});

const submitting = ref(false);
const provinces = ref<any[]>([]);
const districts = ref<any[]>([]);
const wards = ref<any[]>([]);
const mops = ref<any[]>([]);
const shipMethods = ref<any[]>([]);

// Fetch VN Provinces
const fetchProvinces = async () => {
    try {
        const res = await axios.get('https://provinces.open-api.vn/api/p/');
        provinces.value = res.data;
    } catch (err) {
        console.error("Lỗi lấy tỉnh thành:", err);
    }
};

const fetchDistricts = async (provinceCode: number) => {
    try {
        const res = await axios.get(`https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`);
        districts.value = res.data.districts;
        form.value.district = null;
        form.value.ward = null;
        wards.value = [];
    } catch (err) {
        console.error("Lỗi lấy quận huyện:", err);
    }
};

const fetchWards = async (districtCode: number) => {
    try {
        const res = await axios.get(`https://provinces.open-api.vn/api/d/${districtCode}?depth=2`);
        wards.value = res.data.wards;
        form.value.ward = null;
    } catch (err) {
        console.error("Lỗi lấy phường xã:", err);
    }
};

const fetchMethods = async () => {
    try {
        const [mopRes, smRes] = await Promise.all([
            axios.get('http://localhost:8081/order/mop'),
            axios.get('http://localhost:8081/order/ship-method')
        ]);
        mops.value = mopRes.data;
        shipMethods.value = smRes.data;
        
        // Default selection
        if (mops.value.length > 0) form.value.paymentMethod = mops.value[0].id;
        if (shipMethods.value.length > 0) form.value.shipMethod = shipMethods.value[0].id;
    } catch (err) {
        console.error("Lỗi lấy phương thức:", err);
    }
};

// Watches for location
watch(() => form.value.province, (newVal) => {
    if (newVal) fetchDistricts(newVal.code);
});

watch(() => form.value.district, (newVal) => {
    if (newVal) fetchWards(newVal.code);
});

const placeOrder = async () => {
    if (!form.value.fullName.trim()) {
        toast.add({ severity: 'warn', summary: 'Thiếu thông tin', detail: 'Vui lòng nhập họ và tên', life: 2000 });
        return;
    }
    if (!form.value.phone.trim()) {
        toast.add({ severity: 'warn', summary: 'Thiếu thông tin', detail: 'Vui lòng nhập số điện thoại', life: 2000 });
        return;
    }
    if (form.value.deliveryType === 'home') {
        if (!form.value.province || !form.value.district || !form.value.ward || !form.value.address.trim()) {
            toast.add({ severity: 'warn', summary: 'Thiếu thông tin', detail: 'Vui lòng nhập đầy đủ địa chỉ giao hàng', life: 2000 });
            return;
        }
    }
    if (!form.value.agreePolicy) {
        toast.add({ severity: 'warn', summary: 'Điều khoản', detail: 'Vui lòng đồng ý với chính sách bảo mật', life: 2000 });
        return;
    }

    submitting.value = true;
    try {
        const fullAddress = form.value.deliveryType === 'home'
            ? `${form.value.address}, ${form.value.ward.name}, ${form.value.district.name}, ${form.value.province.name}`
            : 'Nhận tại cửa hàng';

        const orderPayload = {
            customerName: form.value.fullName.trim(),
            email: form.value.email.trim(),
            gender: form.value.gender,
            phone: form.value.phone.trim(),
            addressO: fullAddress,
            noteO: form.value.note,
            receiverName: form.value.otherReceiver ? form.value.receiverName : null,
            receiverPhone: form.value.otherReceiver ? form.value.receiverPhone : null,
            methodofPayment: { id: form.value.paymentMethod },
            shipMethod: { id: form.value.shipMethod },
            userId: state.user?.id,
            totalMoney: totalPrice.value,
            totalMoneyaftersaleoff: finalPrice.value,
            voucherCode: voucher.value?.maVoucher || null
        };

        await axios.post('http://localhost:8081/order/create', orderPayload);

        // Clear local storage/session
        sessionStorage.removeItem('checkoutData');
        
        // Refresh app state (to clear cart count on Header)
        await MyApp.getInstance().authenticate();
        await MyApp.getInstance().updateCartCount();
        
        toast.add({ severity: 'success', summary: 'Đặt hàng thành công!', detail: 'Cảm ơn bạn đã mua hàng!', life: 4000 });
        
        setTimeout(() => { router.push('/'); }, 2000);
    } catch (err: any) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: err.response?.data || 'Có lỗi xảy ra', life: 3000 });
    } finally {
        submitting.value = false;
    }
};

onMounted(async () => {
    await MyApp.getInstance().authenticate();
    if (!state.isAuthenticated) {
        router.push('/auth/signin');
        return;
    }
    
    if (state.user) {
        form.value.fullName = state.user.nameUser || '';
        form.value.email = state.user.email || '';
        form.value.phone = state.user.phone || '';
    }

    const raw = sessionStorage.getItem('checkoutData');
    if (!raw) {
        router.push('/cart');
        return;
    }
    checkoutData.value = JSON.parse(raw);

    fetchProvinces();
    fetchMethods();
});
</script>

<template>
  <div class="order-page">
    <Toast />

    <nav class="order-breadcrumb">
      <div class="container">
        <ol class="breadcrumb m-0">
          <li class="breadcrumb-item"><router-link to="/">Trang chủ</router-link></li>
          <li class="breadcrumb-item"><router-link to="/cart">Giỏ hàng</router-link></li>
          <li class="breadcrumb-item active">Đặt hàng</li>
        </ol>
      </div>
    </nav>

    <div class="container order-container">
      <div class="row g-4">
        <!-- LEFT: Info Form -->
        <div class="col-lg-8">
          <div class="order-card">
            <div class="order-section-title">
              <span class="order-section-bar"></span>
              <h5>THÔNG TIN KHÁCH HÀNG</h5>
            </div>

            <div class="row g-3">
              <div class="col-md-8">
                <label class="order-label">Họ và Tên <span class="required">*</span></label>
                <input type="text" class="order-input" v-model="form.fullName" placeholder="Nhập họ và tên">
              </div>
              <div class="col-md-4">
                <label class="order-label">Giới tính</label>
                <div class="gender-group">
                  <label class="gender-radio">
                    <input type="radio" v-model="form.gender" value="male"> Nam
                  </label>
                  <label class="gender-radio">
                    <input type="radio" v-model="form.gender" value="female"> Nữ
                  </label>
                </div>
              </div>
              <div class="col-md-6">
                <label class="order-label">Số điện thoại <span class="required">*</span></label>
                <input type="tel" class="order-input" v-model="form.phone" placeholder="VD: 0901234567">
              </div>
              <div class="col-md-6">
                <label class="order-label">Email</label>
                <input type="email" class="order-input" v-model="form.email" placeholder="VD: user@example.com">
              </div>
            </div>
          </div>

          <div class="order-card mt-3">
            <div class="order-section-title">
              <span class="order-section-bar"></span>
              <h5>THÔNG TIN GIAO HÀNG</h5>
            </div>

            <div class="delivery-type-group mb-3">
              <label class="delivery-radio" :class="{ active: form.deliveryType === 'home' }">
                <input type="radio" v-model="form.deliveryType" value="home">
                <i class="fas fa-home me-1"></i> Giao tận nơi
              </label>
              <label class="delivery-radio" :class="{ active: form.deliveryType === 'store' }">
                <input type="radio" v-model="form.deliveryType" value="store">
                <i class="fas fa-store me-1"></i> Nhận tại cửa hàng
              </label>
            </div>

            <div v-if="form.deliveryType === 'home'" class="row g-3">
              <div class="col-md-4">
                <label class="order-label">Tỉnh / Thành <span class="required">*</span></label>
                <select class="order-input" v-model="form.province">
                  <option :value="null">Chọn Tỉnh/Thành</option>
                  <option v-for="p in provinces" :key="p.code" :value="p">{{ p.name }}</option>
                </select>
              </div>
              <div class="col-md-4">
                <label class="order-label">Quận / Huyện <span class="required">*</span></label>
                <select class="order-input" v-model="form.district" :disabled="!form.province">
                  <option :value="null">Chọn Quận/Huyện</option>
                  <option v-for="d in districts" :key="d.code" :value="d">{{ d.name }}</option>
                </select>
              </div>
              <div class="col-md-4">
                <label class="order-label">Phường / Xã <span class="required">*</span></label>
                <select class="order-input" v-model="form.ward" :disabled="!form.district">
                  <option :value="null">Chọn Phường/Xã</option>
                  <option v-for="w in wards" :key="w.code" :value="w">{{ w.name }}</option>
                </select>
              </div>
              <div class="col-12">
                <label class="order-label">Số nhà, tên đường <span class="required">*</span></label>
                <input type="text" class="order-input" v-model="form.address" placeholder="VD: 123 Lê Lợi">
              </div>
            </div>

            <div v-else class="store-notice mb-3">
              <i class="fas fa-map-marker-alt me-2"></i>
              <strong>HC TECH Store</strong> — 123 Nguyễn Văn A, Quận 1, TP. HCM
            </div>

            <div>
              <label class="order-label">Ghi chú đơn hàng</label>
              <textarea class="order-input" v-model="form.note" rows="2" placeholder="VD: Giao giờ hành chính..."></textarea>
            </div>

            <div class="mt-3">
                <label class="checkbox-label">
                    <input type="checkbox" v-model="form.otherReceiver"> Gọi cho người khác nhận hàng (nếu có)
                </label>
            </div>
            <div v-if="form.otherReceiver" class="row g-3 mt-1">
                <div class="col-md-6">
                    <input type="text" class="order-input" v-model="form.receiverName" placeholder="Tên người nhận">
                </div>
                <div class="col-md-6">
                    <input type="tel" class="order-input" v-model="form.receiverPhone" placeholder="SĐT người nhận">
                </div>
            </div>
          </div>

          <div class="order-card mt-3">
            <div class="order-section-title">
              <span class="order-section-bar"></span>
              <h5>PHƯƠNG THỨC GIAO HÀNG & THANH TOÁN</h5>
            </div>
            
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="order-label">Đơn vị vận chuyển</label>
                    <div class="payment-group">
                        <label v-for="sm in shipMethods" :key="sm.id" class="payment-option" :class="{ active: form.shipMethod === sm.id }">
                            <input type="radio" v-model="form.shipMethod" :value="sm.id">
                            <i class="fas fa-truck me-2"></i> {{ sm.nameSM || 'Giao hàng nhanh' }}
                        </label>
                    </div>
                </div>
                <div class="col-md-6">
                    <label class="order-label">Thanh toán</label>
                    <div class="payment-group">
                        <label v-for="mop in mops" :key="mop.id" class="payment-option" :class="{ active: form.paymentMethod === mop.id }">
                            <input type="radio" v-model="form.paymentMethod" :value="mop.id">
                            <i class="fas fa-wallet me-2"></i> {{ mop.name_mop }}
                        </label>
                    </div>
                </div>
            </div>

            <div class="mt-4">
              <label class="checkbox-label">
                <input type="checkbox" v-model="form.agreePolicy">
                Tôi đồng ý với chính sách mua hàng và bảo mật
              </label>
            </div>
          </div>
        </div>

        <!-- RIGHT: Order Summary -->
        <div class="col-lg-4">
          <div class="order-summary sticky-top" style="top: 20px;">
            <div v-for="item in cartItems" :key="item.idSC + '-' + item.idProduct" class="summary-item">
              <div class="summary-item-img-wrap">
                <img :src="Helper.GetImageUrl(item.productAvatar)" alt="">
                <span class="summary-qty-badge">{{ item.amountCD }}</span>
              </div>
              <div class="summary-item-info">
                <p class="summary-item-name">{{ item.productName }}</p>
                <small class="text-muted" v-if="item.idColor">Màu: {{ item.idColor }}</small>
              </div>
              <div class="summary-item-price">{{ Helper.ToMoney(item.productPrice * item.amountCD) }}</div>
            </div>

            <hr>
            <div class="summary-row">
              <span>Tạm tính</span>
              <span>{{ Helper.ToMoney(totalPrice) }}</span>
            </div>
            <div v-if="discountAmount > 0" class="summary-row text-success">
              <span>Khuyến mãi ({{ voucher.maVoucher }})</span>
              <span>-{{ Helper.ToMoney(discountAmount) }}</span>
            </div>
            <div class="summary-row">
              <span>Vận chuyển</span>
              <span class="text-muted">Tính khi giao</span>
            </div>

            <hr>
            <div class="summary-total">
              <span>TỔNG CỘNG:</span>
              <span class="total-amount">{{ Helper.ToMoney(finalPrice) }}</span>
            </div>

            <button class="btn-place-order" @click="placeOrder" :disabled="submitting">
              <i v-if="submitting" class="fas fa-spinner fa-spin me-2"></i>
              {{ submitting ? 'ĐANG XỬ LÝ...' : 'XÁC NHẬN ĐẶT HÀNG' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.order-page { font-family: 'Inter', sans-serif; background: #f8fafc; min-height: 100vh; }
.order-breadcrumb { background: white; padding: 12px 0; border-bottom: 1px solid #e2e8f0; font-size: 14px; }
.order-breadcrumb a { color: #f59e0b; text-decoration: none; }
.order-container { padding: 24px 12px 48px; }
.order-card { background: white; border-radius: 12px; padding: 24px; box-shadow: 0 1px 4px rgba(0,0,0,0.07); }
.order-section-title { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; }
.order-section-bar { width: 4px; height: 20px; background: #f59e0b; border-radius: 2px; }
.order-section-title h5 { margin: 0; font-size: 15px; font-weight: 700; color: #1e293b; }
.order-label { display: block; font-size: 13px; font-weight: 600; color: #475569; margin-bottom: 6px; }
.required { color: #ef4444; }
.order-input { width: 100%; padding: 10px 14px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 14px; outline: none; }
.order-input:focus { border-color: #f59e0b; }
.gender-group { display: flex; gap: 16px; margin-top: 5px; }
.gender-radio { display: flex; align-items: center; gap: 6px; cursor: pointer; font-size: 14px; }
.delivery-type-group { display: flex; gap: 12px; }
.delivery-radio { flex: 1; padding: 10px; border: 2px solid #e2e8f0; border-radius: 8px; text-align: center; cursor: pointer; transition: 0.2s; font-size: 14px; }
.delivery-radio.active { border-color: #f59e0b; background: #fffbeb; color: #92400e; }
.delivery-radio input { display: none; }
.store-notice { background: #f8fafc; border: 1px solid #e2e8f0; padding: 12px; border-radius: 8px; font-size: 14px; }
.checkbox-label { display: flex; align-items: center; gap: 8px; font-size: 13px; cursor: pointer; }
.payment-group { display: flex; flex-direction: column; gap: 8px; }
.payment-option { padding: 12px; border: 2px solid #e2e8f0; border-radius: 8px; cursor: pointer; font-size: 14px; transition: 0.2s; }
.payment-option.active { border-color: #f59e0b; background: #fffbeb; }
.payment-option input { display: none; }

.order-summary { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.07); }
.summary-item { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.summary-item-img-wrap { position: relative; }
.summary-item-img-wrap img { width: 50px; height: 50px; border-radius: 6px; object-fit: cover; border: 1px solid #eee; }
.summary-qty-badge { position: absolute; top: -5px; right: -5px; background: #64748b; color: white; width: 18px; height: 18px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 10px; }
.summary-item-info { flex: 1; }
.summary-item-name { font-size: 13px; font-weight: 600; margin: 0; line-height: 1.2; }
.summary-item-price { font-size: 14px; font-weight: 700; color: #1e293b; }
.summary-row { display: flex; justify-content: space-between; font-size: 14px; margin-bottom: 8px; }
.summary-total { display: flex; justify-content: space-between; align-items: center; margin: 15px 0; }
.total-amount { font-size: 20px; font-weight: 800; color: #ef4444; }
.btn-place-order { width: 100%; padding: 14px; background: #f59e0b; color: white; border: none; border-radius: 8px; font-size: 15px; font-weight: 700; cursor: pointer; transition: 0.2s; }
.btn-place-order:hover:not(:disabled) { background: #d97706; }
.btn-place-order:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
