package org.scoula.asset.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class SpendingOverviewDTO {

    private Selected selected;          // {year, month}
    private long totalSpending;         // 이번 달 총액
    private PrevMonth prevMonth;        // {year, month, totalSpending}
    private MomChange momChange;        // {diff, percent}
    private List<Category> categories;  // 도넛
    private List<Trend> trend;          // 최근 N개월 추세

    @AllArgsConstructor @NoArgsConstructor @Getter @Setter
    public static class Selected {
        private int year;
        private int month;
    }

    @AllArgsConstructor @NoArgsConstructor @Getter @Setter
    public static class PrevMonth {
        private int year;
        private int month;
        private long totalSpending;
    }

    @AllArgsConstructor @NoArgsConstructor @Getter @Setter
    public static class MomChange {
        private long diff;       // 이번달 - 전월
        private Double percent;  // 전월 0이면 null
    }

    @Builder @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class Category {
        private Long categoryId;
        private long amount;
        private double percentage; // 소수1자리
    }

    @AllArgsConstructor @NoArgsConstructor @Getter @Setter
    public static class Trend {
        private int year;
        private int month;
        private long totalAmount;
    }
}
