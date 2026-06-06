package com.sportshop.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    private OverviewStats overview;
    private TodayStats today;
    private List<DailyRevenuePoint> revenueByDay;
    private List<ProductRevenuePoint> productRevenue;
    private List<DailyRevenuePoint> recentDailyRevenue;
    private List<ProductRevenuePoint> top10Products;
    private List<ViewedLowSalesProduct> viewedButLowSales;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OverviewStats {
        private long totalRevenue;
        private long totalOrders;
        private long totalProductsSold;
        private long totalCustomers;
        private long averageOrderValue;
        private double conversionRate;
        private long totalVisits;
        private long lowStock;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TodayStats {
        private long revenue;
        private double revenueChange;
        private long orders;
        private double ordersChange;
        private long productsSold;
        private double productsSoldChange;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyRevenuePoint {
        private String date;
        private long revenue;
        private long orders;
        private long productsSold;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductRevenuePoint {
        private String name;
        private long revenue;
        private long sold;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ViewedLowSalesProduct {
        private String name;
        private long views;
        private long sold;
    }
}
