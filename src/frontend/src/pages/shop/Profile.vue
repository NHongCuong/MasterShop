<script setup lang="ts">
import { state } from '../../app/MyApp';
import { useRouter } from 'vue-router';
import { onMounted } from 'vue';

const router = useRouter();

onMounted(() => {
  if (!state.isAuthenticated) {
    router.push('/auth/signin');
  }
});
</script>

<template>
  <div class="container py-5">
    <div class="row justify-content-center">
      <div class="col-md-8">
        <div class="card shadow-sm border-0" style="border-radius: 15px;">
          <div class="card-header bg-dark text-white py-3" style="border-radius: 15px 15px 0 0;">
            <h4 class="mb-0 text-center text-uppercase fw-bold">Thông tin cá nhân</h4>
          </div>
          <div class="card-body p-4 p-md-5">
            <div class="text-center mb-5">
              <div class="position-relative d-inline-block">
                <img src="https://ui-avatars.com/api/?name=User&background=9adbf6&color=fff&size=128" 
                     class="rounded-circle shadow-sm" alt="Avatar" width="120" height="120">
                <div class="position-absolute bottom-0 end-0 bg-success border border-white border-3 rounded-circle" 
                     style="width: 25px; height: 25px;"></div>
              </div>
              <h3 class="mt-3 fw-bold">{{ state.user?.nameUser || 'Khách hàng' }}</h3>
              <span class="badge bg-primary text-uppercase px-3 py-2">{{ state.user?.userType.name }}</span>
            </div>

            <div class="row g-4">
              <!-- Email -->
              <div class="col-md-6">
                <div class="p-3 bg-light rounded shadow-sm h-100">
                  <div class="d-flex align-items-center mb-2">
                    <i class="fas fa-envelope text-primary me-2"></i>
                    <span class="text-muted small fw-bold text-uppercase">Email</span>
                  </div>
                  <div class="fw-bold fs-5 text-break">{{ state.user?.email }}</div>
                </div>
              </div>
              
              <!-- Phone -->
              <div class="col-md-6">
                <div class="p-3 bg-light rounded shadow-sm h-100">
                  <div class="d-flex align-items-center mb-2">
                    <i class="fas fa-phone text-success me-2"></i>
                    <span class="text-muted small fw-bold text-uppercase">Số điện thoại</span>
                  </div>
                  <div class="fw-bold fs-5">{{ state.user?.phone || 'Chưa cập nhật' }}</div>
                </div>
              </div>

              <!-- Address -->
              <div class="col-12">
                <div class="p-3 bg-light rounded shadow-sm h-100">
                  <div class="d-flex align-items-center mb-2">
                    <i class="fas fa-map-marker-alt text-danger me-2"></i>
                    <span class="text-muted small fw-bold text-uppercase">Địa chỉ</span>
                  </div>
                  <div class="fw-bold fs-5">{{ state.user?.address || 'Chưa cập nhật' }}</div>
                </div>
              </div>

              <!-- Status -->
              <div class="col-md-6">
                <div class="p-3 bg-light rounded shadow-sm h-100">
                  <div class="d-flex align-items-center mb-2">
                    <i class="fas fa-check-circle text-info me-2"></i>
                    <span class="text-muted small fw-bold text-uppercase">Trạng thái tài khoản</span>
                  </div>
                  <div class="fw-bold fs-5">
                    <span :class="state.user?.userStatus.name === 'Đang hoạt động' ? 'text-success' : 'text-warning'">
                      {{ state.user?.userStatus.name === 'Đang hoạt động' ? 'Đang hoạt động' : 'Chưa xác thực' }}
                    </span>
                  </div>
                </div>
              </div>

              <!-- Reg Time -->
              <div class="col-md-6">
                <div class="p-3 bg-light rounded shadow-sm h-100">
                  <div class="d-flex align-items-center mb-2">
                    <i class="fas fa-calendar-alt text-secondary me-2"></i>
                    <span class="text-muted small fw-bold text-uppercase">Ngày đăng ký</span>
                  </div>
                  <div class="fw-bold fs-5">{{ state.user?.regtime ? new Date(state.user.regtime).toLocaleDateString() : 'Chưa rõ' }}</div>
                </div>
              </div>
            </div>

            <div class="mt-5 d-flex gap-3 justify-content-center">
              <button class="btn btn-primary px-4 py-2 fw-bold rounded-pill">
                <i class="fas fa-edit me-2"></i>Chỉnh sửa thông tin
              </button>
              <button class="btn btn-outline-dark px-4 py-2 fw-bold rounded-pill" @click="router.push('/')">
                <i class="fas fa-arrow-left me-2"></i>Quay lại trang chủ
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.card {
  transition: transform 0.3s ease;
}
.card:hover {
  transform: translateY(-5px);
}
.bg-light {
  transition: background-color 0.2s ease;
}
.bg-light:hover {
  background-color: #f1f1f1 !important;
}
</style>
