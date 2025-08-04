package org.scoula.push.dto.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklySpendingComparison {
    
    private Long thisWeekAmount;
    private Long lastWeekAmount;
    private Double changePercentage;
    private Boolean isIncrease;
    
    public static WeeklySpendingComparison of(Long thisWeek, Long lastWeek) {
        if (lastWeek == null || lastWeek == 0) {
            return WeeklySpendingComparison.builder()
                    .thisWeekAmount(thisWeek != null ? thisWeek : 0L)
                    .lastWeekAmount(0L)
                    .changePercentage(0.0)
                    .isIncrease(false)
                    .build();
        }
        
        double percentage = ((double) (thisWeek - lastWeek) / lastWeek) * 100;
        boolean isIncrease = thisWeek > lastWeek;
        
        return WeeklySpendingComparison.builder()
                .thisWeekAmount(thisWeek != null ? thisWeek : 0L)
                .lastWeekAmount(lastWeek)
                .changePercentage(Math.abs(percentage))
                .isIncrease(isIncrease)
                .build();
    }
    
    public String getChangeDirection() {
        if (changePercentage == 0) {
            return "변화 없음";
        }
        return isIncrease ? "증가" : "감소";
    }
}