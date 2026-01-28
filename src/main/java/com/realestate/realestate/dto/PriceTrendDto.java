package com.realestate.realestate.dto;

// 인터페이스 기반 Projection (JPA가 알아서 채워줍니다)
public interface PriceTrendDto {
    Integer getDealYear();  // 거래 연도
    Integer getDealMonth(); // 거래 월
    Double getAveragePrice(); // 평균 거래 금액
}