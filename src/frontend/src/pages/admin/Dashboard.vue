<script setup lang="ts">
import axios from 'axios'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import Chart from 'primevue/chart'
import Skeleton from 'primevue/skeleton'

const router = useRouter()

const API = 'http://localhost:8081/dashboard/stats'
const loading = ref(true)

interface OverviewStats {
  totalRevenue: number
  totalOrders: number
  totalProductsSold: number
  totalCustomers: number
  averageOrderValue: number
  conversionRate: number
  totalVisits: number
  lowStock: number
  totalProducts: number
  totalCategories: number
}

interface OverviewCard {
  label: string
  value: string
  icon: string
  variant: string
  route?: string
}

interface TodayStats {
  revenue: number
  revenueChange: number
  orders: number
  ordersChange: number
  productsSold: number
  productsSoldChange: number
}

interface DailyPoint {
  date: string
  revenue: number
  orders: number
  productsSold: number
}

interface ProductPoint {
  name: string
  revenue: number
  sold: number
}

interface ViewedLowSales {
  name: string
  views: number
  sold: number
}

interface DashboardData {
  overview: OverviewStats
  today: TodayStats
  revenueByDay: DailyPoint[]
  productRevenue: ProductPoint[]
  recentDailyRevenue: DailyPoint[]
  top10Products: ProductPoint[]
  viewedButLowSales: ViewedLowSales[]
}

const stats = ref<DashboardData | null>(null)
const revenueLineData = ref()
const revenueLineOptions = ref()
const productBarData = ref()
const productBarOptions = ref()
const dailyBarData = ref()
const dailyBarOptions = ref()
const top10BarData = ref()
const top10BarOptions = ref()

const formatNumber = (n: number) =>
  new Intl.NumberFormat('vi-VN').format(n ?? 0)

const formatMoney = (n: number) =>
  new Intl.NumberFormat('vi-VN').format(n ?? 0) + ' đ'

const formatTrend = (change: number) => {
  const arrow = change >= 0 ? '▲' : '▼'
  const cls = change >= 0 ? 'trend-up' : 'trend-down'
  return { text: `${arrow} ${Math.abs(change)}% so với hôm qua`, cls }
}

const overviewCards = computed((): OverviewCard[] => {
  const o = stats.value?.overview
  if (!o) return []
  return [
    { label: 'Tổng Doanh thu', value: formatNumber(o.totalRevenue), icon: 'fas fa-dollar-sign', variant: '' },
    { label: 'Tổng Đơn hàng', value: formatNumber(o.totalOrders), icon: 'fas fa-shopping-cart', variant: '', route: '/admin/bill/1' },
    { label: 'Tổng Sản phẩm đã bán', value: formatNumber(o.totalProductsSold), icon: 'fas fa-box-open', variant: '' },
    { label: 'Tổng Khách hàng', value: formatNumber(o.totalCustomers), icon: 'fas fa-user', variant: '', route: '/admin/user' },
    { label: 'Giá trị đơn TB', value: formatNumber(o.averageOrderValue), icon: 'fas fa-calculator', variant: '' },
    { label: 'Tỷ lệ chuyển đổi', value: `${o.conversionRate}%`, icon: 'fas fa-percent', variant: '' },
    { label: 'Tổng lượt truy cập', value: formatNumber(o.totalVisits), icon: 'fas fa-globe', variant: '' },
    { label: 'Sắp hết hàng', value: formatNumber(o.lowStock), icon: 'fas fa-exclamation-triangle', variant: 'warning', route: '/admin/inventory?lowStockOnly=true' },
    { label: 'Tổng sản phẩm hiện có', value: formatNumber(o.totalProducts), icon: 'fas fa-shopping-bag', variant: '', route: '/admin/product' },
    { label: 'Tổng Danh mục', value: formatNumber(o.totalCategories), icon: 'fas fa-list', variant: '', route: '/admin/category' },
  ]
})

const goToCard = (card: OverviewCard) => {
  if (card.route) router.push(card.route)
}

const todayCards = computed(() => {
  const t = stats.value?.today
  if (!t) return []
  return [
    { label: 'Doanh thu', value: formatNumber(t.revenue), icon: 'fas fa-dollar-sign', change: t.revenueChange },
    { label: 'Đơn hàng', value: formatNumber(t.orders), icon: 'fas fa-calendar-alt', change: t.ordersChange },
    { label: 'Sản phẩm đã bán', value: formatNumber(t.productsSold), icon: 'fas fa-box', change: t.productsSoldChange },
  ]
})

const buildLineChart = (points: DailyPoint[]) => {
  const labels = points.map(p => p.date)
  const values = points.map(p => p.revenue)

  revenueLineData.value = {
    labels,
    datasets: [{
      label: 'Doanh thu',
      data: values,
      borderColor: '#6366f1',
      backgroundColor: 'rgba(99, 102, 241, 0.08)',
      pointBackgroundColor: '#6366f1',
      pointBorderColor: '#fff',
      pointRadius: 3,
      pointHoverRadius: 5,
      tension: 0.3,
      fill: false,
      borderWidth: 2,
    }],
  }

  revenueLineOptions.value = {
    maintainAspectRatio: false,
    interaction: { mode: 'index', intersect: false },
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: '#fff',
        titleColor: '#334155',
        bodyColor: '#475569',
        borderColor: '#e2e8f0',
        borderWidth: 1,
        padding: 12,
        callbacks: {
          title: (items: any[]) => {
            const idx = items[0]?.dataIndex
            return idx != null ? `Ngày: ${points[idx]?.date}` : ''
          },
          label: () => '',
          afterBody: (items: any[]) => {
            const idx = items[0]?.dataIndex
            if (idx == null) return []
            const p = points[idx]
            return [
              `Doanh thu: ${formatMoney(p.revenue)}`,
              `Đơn hàng: ${p.orders}`,
              `Sản phẩm đã bán: ${p.productsSold}`,
            ]
          },
        },
      },
    },
    scales: {
      x: {
        ticks: { maxTicksLimit: 12, color: '#94a3b8', font: { size: 11 } },
        grid: { color: '#f1f5f9', drawBorder: false },
      },
      y: {
        beginAtZero: true,
        ticks: {
          color: '#94a3b8',
          callback: (v: number) => {
            if (v >= 1_000_000) return `${Math.round(v / 1_000_000)}tr`
            return formatNumber(v)
          },
        },
        grid: { color: '#f1f5f9', drawBorder: false },
      },
    },
  }
}

const buildProductBarChart = (products: ProductPoint[]) => {
  productBarData.value = {
    labels: products.map(p => p.name),
    datasets: [{
      label: 'Doanh thu',
      data: products.map(p => p.revenue),
      backgroundColor: '#14b8a6',
      borderRadius: 4,
      maxBarThickness: 40,
    }],
  }

  productBarOptions.value = {
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: {
        ticks: { maxRotation: 45, minRotation: 45, color: '#64748b', font: { size: 10 } },
        grid: { display: false },
      },
      y: {
        beginAtZero: true,
        ticks: {
          color: '#94a3b8',
          callback: (v: number) => v >= 1_000_000 ? `${Math.round(v / 1_000_000)}tr` : formatNumber(v),
        },
        grid: { color: '#f1f5f9' },
      },
    },
  }
}

const buildDailyBarChart = (points: DailyPoint[]) => {
  const filtered = points.filter(p => p.revenue > 0)
  dailyBarData.value = {
    labels: filtered.map(p => p.date),
    datasets: [{
      label: 'Doanh thu',
      data: filtered.map(p => p.revenue),
      backgroundColor: '#14b8a6',
      borderRadius: 4,
      maxBarThickness: 36,
    }],
  }

  dailyBarOptions.value = {
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: {
        ticks: { maxTicksLimit: 15, maxRotation: 45, minRotation: 45, color: '#64748b', font: { size: 10 } },
        grid: { display: false },
      },
      y: {
        beginAtZero: true,
        ticks: {
          color: '#94a3b8',
          callback: (v: number) => v >= 1_000_000 ? `${Math.round(v / 1_000_000)}tr` : formatNumber(v),
        },
        grid: { color: '#f1f5f9' },
      },
    },
  }
}

const buildTop10Chart = (products: ProductPoint[]) => {
  top10BarData.value = {
    labels: products.map(p => p.name),
    datasets: [{
      label: 'Doanh thu',
      data: products.map(p => p.revenue),
      backgroundColor: '#3b82f6',
      borderRadius: 4,
    }],
  }

  top10BarOptions.value = {
    indexAxis: 'y',
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: {
        beginAtZero: true,
        ticks: {
          color: '#94a3b8',
          callback: (v: number) => v >= 1_000_000 ? `${Math.round(v / 1_000_000)}tr` : formatNumber(v),
        },
        grid: { color: '#f1f5f9' },
      },
      y: {
        ticks: { color: '#475569', font: { size: 11 } },
        grid: { display: false },
      },
    },
  }
}

const fetchStats = async () => {
  loading.value = true
  try {
    const { data } = await axios.get<DashboardData>(API)
    stats.value = data
    buildLineChart(data.revenueByDay)
    buildProductBarChart(data.productRevenue)
    buildDailyBarChart(data.recentDailyRevenue)
    buildTop10Chart(data.top10Products)
  } catch (err) {
    console.error('Lỗi khi tải thống kê dashboard:', err)
  } finally {
    loading.value = false
  }
}

onMounted(fetchStats)
</script>

<template>
  <div class="content-header">
    <div class="container-fluid">
      <div class="row mb-2">
        <div class="col-sm-6">
          <h1 class="m-0">Tổng quan</h1>
        </div>
        <div class="col-sm-6">
          <ol class="breadcrumb float-sm-right">
            <li class="breadcrumb-item"><a href="/">Home</a></li>
            <li class="breadcrumb-item active">Dashboard</li>
          </ol>
        </div>
      </div>
    </div>
  </div>

  <div class="content dash-page">
    <div class="container-fluid">

      <!-- Thống kê tổng quan -->
      <section class="dash-section">
        <h5 class="dash-section-title">Thống kê tổng quan</h5>
        <div class="dash-overview-grid">
          <template v-if="loading">
            <Skeleton v-for="i in 10" :key="i" height="90px" borderRadius="10px" />
          </template>
          <template v-else>
            <div
              v-for="(card, i) in overviewCards"
              :key="i"
              class="dash-stat-card"
              :class="{
                'dash-stat-warning': card.variant === 'warning',
                'dash-stat-clickable': !!card.route
              }"
              @click="goToCard(card)"
            >
              <div class="dash-stat-body">
                <span class="dash-stat-label">{{ card.label }}</span>
                <span class="dash-stat-value">{{ card.value }}</span>
              </div>
              <div class="dash-stat-icon" :class="{ warning: card.variant === 'warning' }">
                <i :class="card.icon"></i>
              </div>
            </div>
          </template>
        </div>
      </section>

      <!-- Thống kê ngày hôm nay -->
      <section class="dash-section">
        <h5 class="dash-section-title">Thống kê ngày hôm nay</h5>
        <div class="dash-today-grid">
          <template v-if="loading">
            <Skeleton v-for="i in 3" :key="i" height="110px" borderRadius="10px" />
          </template>
          <template v-else>
            <div v-for="(card, i) in todayCards" :key="i" class="dash-today-card">
              <div class="dash-today-top">
                <span class="dash-stat-label">{{ card.label }}</span>
                <div class="dash-stat-icon"><i :class="card.icon"></i></div>
              </div>
              <div class="dash-today-value">{{ card.value }}</div>
              <div :class="formatTrend(card.change).cls">{{ formatTrend(card.change).text }}</div>
            </div>
          </template>
        </div>
      </section>

      <!-- Biểu đồ doanh thu 365 ngày -->
      <section class="dash-section">
        <h5 class="dash-section-title">Thống kê doanh thu theo 365 ngày qua</h5>
        <div class="dash-chart-card">
          <Skeleton v-if="loading" height="320px" borderRadius="10px" />
          <Chart v-else type="line" :data="revenueLineData" :options="revenueLineOptions" class="dash-chart" />
        </div>
      </section>

      <!-- Hiệu suất doanh thu sản phẩm -->
      <section class="dash-section">
        <h5 class="dash-section-title">Hiệu suất doanh thu tất cả sản phẩm</h5>
        <div class="dash-chart-card">
          <Skeleton v-if="loading" height="360px" borderRadius="10px" />
          <Chart v-else type="bar" :data="productBarData" :options="productBarOptions" class="dash-chart dash-chart-tall" />
        </div>
      </section>

      <!-- Doanh thu theo ngày gần đây -->
      <section class="dash-section">
        <h5 class="dash-section-title">Doanh thu theo ngày (90 ngày gần nhất)</h5>
        <div class="dash-chart-card">
          <Skeleton v-if="loading" height="320px" borderRadius="10px" />
          <Chart v-else type="bar" :data="dailyBarData" :options="dailyBarOptions" class="dash-chart" />
        </div>
      </section>

      <!-- Top 10 + Xem nhiều bán ít -->
      <section class="dash-section">
        <div class="dash-bottom-grid">
          <div>
            <h5 class="dash-section-title">Top 10 Sản phẩm bán chạy (Doanh thu)</h5>
            <div class="dash-chart-card">
              <Skeleton v-if="loading" height="400px" borderRadius="10px" />
              <Chart v-else type="bar" :data="top10BarData" :options="top10BarOptions" class="dash-chart dash-chart-top10" />
            </div>
          </div>
          <div>
            <h5 class="dash-section-title">Sản phẩm "Xem nhiều nhưng bán ít"</h5>
            <div class="dash-table-card">
              <table class="dash-table">
                <thead>
                  <tr>
                    <th>Tên sản phẩm</th>
                    <th>Số lượt xem</th>
                    <th>Số lượt bán</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="loading">
                    <td colspan="3" class="text-center text-muted py-4">Đang tải...</td>
                  </tr>
                  <tr v-else-if="!stats?.viewedButLowSales?.length">
                    <td colspan="3" class="text-center text-muted py-4">Không có dữ liệu</td>
                  </tr>
                  <tr v-for="(item, idx) in stats?.viewedButLowSales" :key="idx">
                    <td>{{ item.name }}</td>
                    <td><i class="fas fa-eye text-danger mr-1"></i>{{ item.views }}</td>
                    <td><i class="fas fa-shopping-bag text-danger mr-1"></i>{{ item.sold }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </section>

    </div>
  </div>
</template>

<style scoped>
.dash-page {
  background: #f4f6f9;
}

.dash-section {
  margin-bottom: 1.5rem;
}

.dash-section-title {
  font-weight: 600;
  color: #334155;
  margin-bottom: 0.75rem;
  font-size: 0.95rem;
}

.dash-overview-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

@media (max-width: 1200px) {
  .dash-overview-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 576px) {
  .dash-overview-grid { grid-template-columns: 1fr; }
}

.dash-stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border: 1px solid #eef2f7;
  min-height: 90px;
}

.dash-stat-warning {
  background: #fff8f0;
  border-color: #fed7aa;
}

.dash-stat-clickable {
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.dash-stat-clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.dash-stat-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.dash-stat-label {
  font-size: 0.78rem;
  color: #64748b;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dash-stat-value {
  font-size: 1.15rem;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.2;
}

.dash-stat-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #ecfdf5;
  color: #10b981;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.95rem;
  flex-shrink: 0;
  margin-left: 8px;
}

.dash-stat-icon.warning {
  background: #fff7ed;
  color: #f97316;
}

.dash-today-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

@media (max-width: 768px) {
  .dash-today-grid { grid-template-columns: 1fr; }
}

.dash-today-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 18px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border: 1px solid #eef2f7;
}

.dash-today-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.dash-today-value {
  font-size: 1.6rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 6px;
}

.trend-up {
  font-size: 0.78rem;
  color: #10b981;
  font-weight: 500;
}

.trend-down {
  font-size: 0.78rem;
  color: #ef4444;
  font-weight: 500;
}

.dash-chart-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border: 1px solid #eef2f7;
}

.dash-chart {
  height: 320px;
}

.dash-chart-tall {
  height: 360px;
}

.dash-chart-top10 {
  height: 420px;
}

.dash-bottom-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

@media (max-width: 992px) {
  .dash-bottom-grid { grid-template-columns: 1fr; }
}

.dash-table-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border: 1px solid #eef2f7;
}

.dash-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}

.dash-table thead tr {
  background: #fef08a;
}

.dash-table th {
  padding: 10px 14px;
  text-align: left;
  font-weight: 600;
  color: #713f12;
  border-bottom: 2px solid #fde047;
}

.dash-table td {
  padding: 10px 14px;
  color: #334155;
  border-bottom: 1px solid #f1f5f9;
}

.dash-table tbody tr:hover {
  background: #fafafa;
}
</style>
