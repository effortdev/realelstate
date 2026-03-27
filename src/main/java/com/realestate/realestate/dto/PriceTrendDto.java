package com.realestate.realestate.dto;

public interface PriceTrendDto {
    Integer getDealYear();  // 거래 연도
    Integer getDealMonth(); // 거래 월
    Double getAveragePrice(); // 평균 거래 금액
}