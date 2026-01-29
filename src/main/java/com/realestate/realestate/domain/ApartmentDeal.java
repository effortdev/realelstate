package com.realestate.realestate.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 보안 설정
@AllArgsConstructor
@Builder
public class ApartmentDeal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String apartmentName;      // 아파트 명
    private String dealAmount;         // 거래 금액
    private Integer buildYear;         // 건축 년도
    private Integer dealYear;          // 거래 년도
    private Integer dealMonth;         // 거래 월
    private Integer dealDay;           // 거래 일
    private String lawdCd;             // 법정동 코드 (지역번호)
    private Integer floor;             // 층수

    // ★ 추가된 필드
    private String dong;               // 법정동 (예: 청운동, 신당동)

    // ★ 필드명 통일 (기존 areaForExclusiveUse 삭제하고 이걸로 통일)
    private Double excluUseAr;         // 전용 면적
}