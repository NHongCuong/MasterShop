package com.sportshop.service;

import com.sportshop.dto.DashboardStatsDTO;
import com.sportshop.entity.*;
import com.sportshop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private BillRepository billRepo;
    @Autowired
    private OderDetailRepository orderDetailRepo;
    @Autowired
    private OderRepository orderRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private ProductRepository productRepo;
    @Autowired
    private CartDetailRepository cartDetailRepo;
    @Autowired
    private TrafficService trafficService;

    public DashboardStatsDTO getStats() {
        List<BillEntity> bills = billRepo.findAll();
        List<OderDetailEntity> orderDetails = orderDetailRepo.findAll();
        List<UserEntity> users = userRepo.findAll();
        List<ProductEntity> products = productRepo.findAll();
        List<CartDetailEntity> cartDetails = cartDetailRepo.findAllIncludeNulls();

        long totalRevenue = bills.stream()
                .mapToLong(b -> b.getTotalMoneyaftersaleoff() != null ? b.getTotalMoneyaftersaleoff() : 0L)
                .sum();

        long totalOrders = orderRepo.count();

        long totalProductsSold = orderDetails.stream()
                .mapToLong(od -> od.getAmount() != null ? od.getAmount() : 0L)
                .sum();

        long totalCustomers = users.stream()
                .filter(u -> u.getUserType() == null || u.getUserType().getId() == null || u.getUserType().getId() != 2L)
                .count();

        long averageOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0L;

        long totalVisits = trafficService.getTotalVisits();
        double conversionRate = totalCustomers > 0
                ? Math.round((double) totalOrders / totalCustomers * 10000.0) / 100.0
                : 0.0;

        long lowStock = products.stream()
                .filter(p -> parseAmount(p.getAmount()) <= LOW_STOCK_THRESHOLD)
                .count();

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        TodaySnapshot todaySnap = buildDaySnapshot(bills, orderDetails, today);
        TodaySnapshot yesterdaySnap = buildDaySnapshot(bills, orderDetails, yesterday);

        DashboardStatsDTO.TodayStats todayStats = DashboardStatsDTO.TodayStats.builder()
                .revenue(todaySnap.revenue)
                .revenueChange(calcChange(todaySnap.revenue, yesterdaySnap.revenue))
                .orders(todaySnap.orders)
                .ordersChange(calcChange(todaySnap.orders, yesterdaySnap.orders))
                .productsSold(todaySnap.productsSold)
                .productsSoldChange(calcChange(todaySnap.productsSold, yesterdaySnap.productsSold))
                .build();

        List<DashboardStatsDTO.DailyRevenuePoint> revenueByDay = buildDailyRevenueSeries(bills, orderDetails, 365);
        List<DashboardStatsDTO.DailyRevenuePoint> recentDailyRevenue = buildDailyRevenueSeries(bills, orderDetails, 90);
        List<DashboardStatsDTO.ProductRevenuePoint> productRevenue = buildProductRevenue(orderDetails);
        List<DashboardStatsDTO.ProductRevenuePoint> top10 = productRevenue.stream()
                .sorted(Comparator.comparingLong(DashboardStatsDTO.ProductRevenuePoint::getRevenue).reversed())
                .limit(10)
                .collect(Collectors.toList());

        List<DashboardStatsDTO.ViewedLowSalesProduct> viewedButLowSales = buildViewedButLowSales(cartDetails, orderDetails);

        return DashboardStatsDTO.builder()
                .overview(DashboardStatsDTO.OverviewStats.builder()
                        .totalRevenue(totalRevenue)
                        .totalOrders(totalOrders)
                        .totalProductsSold(totalProductsSold)
                        .totalCustomers(totalCustomers)
                        .averageOrderValue(averageOrderValue)
                        .conversionRate(conversionRate)
                        .totalVisits(totalVisits)
                        .lowStock(lowStock)
                        .build())
                .today(todayStats)
                .revenueByDay(revenueByDay)
                .productRevenue(productRevenue)
                .recentDailyRevenue(recentDailyRevenue)
                .top10Products(top10)
                .viewedButLowSales(viewedButLowSales)
                .build();
    }

    private static class TodaySnapshot {
        long revenue;
        long orders;
        long productsSold;
    }

    private TodaySnapshot buildDaySnapshot(List<BillEntity> bills, List<OderDetailEntity> orderDetails, LocalDate day) {
        TodaySnapshot snap = new TodaySnapshot();

        Set<Long> orderIds = new HashSet<>();
        for (BillEntity bill : bills) {
            LocalDate billDate = toLocalDate(bill.getCreateDate());
            if (billDate != null && billDate.equals(day)) {
                snap.revenue += bill.getTotalMoneyaftersaleoff() != null ? bill.getTotalMoneyaftersaleoff() : 0L;
                if (bill.getOrderbill() != null) {
                    orderIds.add(bill.getOrderbill().getId());
                }
            }
        }
        snap.orders = orderIds.size();

        for (OderDetailEntity od : orderDetails) {
            if (od.getOrder() == null || od.getOrder().getCreatedDate() == null) continue;
            LocalDate orderDate = toLocalDate(od.getOrder().getCreatedDate());
            if (orderDate != null && orderDate.equals(day)) {
                snap.productsSold += od.getAmount() != null ? od.getAmount() : 0L;
            }
        }
        return snap;
    }

    private double calcChange(long current, long previous) {
        if (previous == 0) return current > 0 ? 100.0 : 0.0;
        return Math.round(((double) (current - previous) / previous) * 10000.0) / 100.0;
    }

    private List<DashboardStatsDTO.DailyRevenuePoint> buildDailyRevenueSeries(
            List<BillEntity> bills, List<OderDetailEntity> orderDetails, int days) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1L);

        Map<LocalDate, Long> revenueMap = new TreeMap<>();
        Map<LocalDate, Set<Long>> ordersMap = new TreeMap<>();
        Map<LocalDate, Long> soldMap = new TreeMap<>();

        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            revenueMap.put(d, 0L);
            ordersMap.put(d, new HashSet<>());
            soldMap.put(d, 0L);
        }

        for (BillEntity bill : bills) {
            LocalDate billDate = toLocalDate(bill.getCreateDate());
            if (billDate == null || billDate.isBefore(start) || billDate.isAfter(end)) continue;
            revenueMap.merge(billDate, bill.getTotalMoneyaftersaleoff() != null ? bill.getTotalMoneyaftersaleoff() : 0L, Long::sum);
            if (bill.getOrderbill() != null) {
                ordersMap.get(billDate).add(bill.getOrderbill().getId());
            }
        }

        for (OderDetailEntity od : orderDetails) {
            if (od.getOrder() == null || od.getOrder().getCreatedDate() == null) continue;
            LocalDate orderDate = toLocalDate(od.getOrder().getCreatedDate());
            if (orderDate == null || orderDate.isBefore(start) || orderDate.isAfter(end)) continue;
            soldMap.merge(orderDate, od.getAmount() != null ? od.getAmount() : 0L, Long::sum);
        }

        List<DashboardStatsDTO.DailyRevenuePoint> result = new ArrayList<>();
        for (LocalDate d : revenueMap.keySet()) {
            result.add(DashboardStatsDTO.DailyRevenuePoint.builder()
                    .date(d.format(DATE_FMT))
                    .revenue(revenueMap.get(d))
                    .orders((long) ordersMap.get(d).size())
                    .productsSold(soldMap.get(d))
                    .build());
        }
        return result;
    }

    private List<DashboardStatsDTO.ProductRevenuePoint> buildProductRevenue(List<OderDetailEntity> orderDetails) {
        Map<String, long[]> map = new LinkedHashMap<>();
        for (OderDetailEntity od : orderDetails) {
            if (od.getProduct() == null) continue;
            String name = od.getProduct().getName();
            long amount = od.getAmount() != null ? od.getAmount() : 0L;
            long price = od.getPrice() != null ? od.getPrice() : 0L;
            long[] acc = map.computeIfAbsent(name, k -> new long[2]);
            acc[0] += amount * price;
            acc[1] += amount;
        }

        return map.entrySet().stream()
                .map(e -> DashboardStatsDTO.ProductRevenuePoint.builder()
                        .name(e.getKey())
                        .revenue(e.getValue()[0])
                        .sold(e.getValue()[1])
                        .build())
                .sorted(Comparator.comparingLong(DashboardStatsDTO.ProductRevenuePoint::getRevenue).reversed())
                .collect(Collectors.toList());
    }

    private List<DashboardStatsDTO.ViewedLowSalesProduct> buildViewedButLowSales(
            List<CartDetailEntity> cartDetails, List<OderDetailEntity> orderDetails) {
        Map<Long, String> productNames = new HashMap<>();
        Map<Long, Long> cartCount = new HashMap<>();
        Map<Long, Long> soldCount = new HashMap<>();

        for (CartDetailEntity cd : cartDetails) {
            if (cd.getProductcartdetail() == null) continue;
            Long pid = cd.getProductcartdetail().getId();
            productNames.put(pid, cd.getProductcartdetail().getName());
            cartCount.merge(pid, 1L, Long::sum);
        }

        for (OderDetailEntity od : orderDetails) {
            if (od.getProduct() == null) continue;
            Long pid = od.getProduct().getId();
            productNames.put(pid, od.getProduct().getName());
            soldCount.merge(pid, od.getAmount() != null ? od.getAmount() : 0L, Long::sum);
        }

        return cartCount.entrySet().stream()
                .map(e -> {
                    long sold = soldCount.getOrDefault(e.getKey(), 0L);
                    return DashboardStatsDTO.ViewedLowSalesProduct.builder()
                            .name(productNames.getOrDefault(e.getKey(), "N/A"))
                            .views(e.getValue())
                            .sold(sold)
                            .build();
                })
                .filter(p -> p.getViews() >= 2 && p.getSold() <= p.getViews() / 2)
                .sorted(Comparator.comparingLong(DashboardStatsDTO.ViewedLowSalesProduct::getViews).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private long parseAmount(String amount) {
        if (amount == null || amount.isBlank()) return 0L;
        try {
            return Long.parseLong(amount.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
