package com.realestate.realestate.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApartmentDeal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 번호를 1, 2, 3... 자동으로 올려줍니다.
    private Long id;

    private String apartmentName;      // 아파트 명
    private String dealAmount;         // 거래 금액
    private Integer buildYear;         // 건축 년도
    private Integer dealYear;          // 거래 년도
    private Integer dealMonth;         // 거래 월
    private Integer dealDay;           // 거래 일
    private Double areaForExclusiveUse; // 전용 면적
    private String lawdCd;             // 법정동 코드 (지역번호)
}
