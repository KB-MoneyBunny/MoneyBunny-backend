package org.scoula.push.dto.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayOfWeekPeak {
    
    private Integer dayOfWeek;
    private String dayName;
    private Long totalAmount;
    private Integer transactionCount;
    
    public static String getDayName(int dayOfWeek) {
        return switch (dayOfWeek) {
            case 1 -> "일요일";
            case 2 -> "월요일";
            case 3 -> "화요일";
            case 4 -> "수요일";
            case 5 -> "목요일";
            case 6 -> "금요일";
            case 7 -> "토요일";
            default -> "알 수 없음";
        };
    }
    
    public boolean isWeekend() {
        return dayOfWeek == 1 || dayOfWeek == 7;
    }
    
    public boolean isWeekday() {
        return dayOfWeek >= 2 && dayOfWeek <= 6;
    }
}