package com.realestate.realestate.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ApartmentDeal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- [기존 필드] ---
    private String apartmentName;      // 아파트 명 (aptNm)
    private String dealAmount;         // 거래 금액
    private Integer dealYear;          // 거래 년도
    private Integer dealMonth;         // 거래 월
    private Integer dealDay;           // 거래 일
    private String lawdCd;             // 법정동 시군구 코드 (sggCd와 동일)
    private Integer floor;             // 층수
    private String dong;               // 법정동 (umdNm)
    private Double excluUseAr;         // 전용 면적

    // --- [★ 신규 추가 필드 (모두 가져오기)] ---
    private String jibun;              // 지번
    private Integer buildYear;         // 건축년도 (숫자로 변환)

    private String dealingGbn;         // 거래유형 (중개거래/직거래)
    private String estateAgentSggNm;   // 중개사 소재지 (중개거래일 경우)

    private String cdealType;          // 해제여부 (O/X)
    private String cdealDay;           // 해제사유 발생일

    private String rgstDate;           // 등기일자
    private String aptDong;            // 아파트 동 (101동 등) - 데이터에 잘 없을 수 있음
    private String slerGbn;            // 매도자 구분
    private String buyerGbn;           // 매수자 구분
    private String landLeaseholdGbn;   // 토지임대부 아파트 여부
}