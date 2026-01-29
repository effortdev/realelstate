package com.realestate.realestate.repository;

import com.realestate.realestate.domain.ApartmentDeal;
import com.realestate.realestate.dto.PriceTrendDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApartmentDealRepository extends JpaRepository<ApartmentDeal, Long> {

    // [신규 기능] 지역코드(lawdCd)를 받아서 월별 평균 가격 조회
    // dealAmount가 String이라서 숫자(Double)로 변환(CAST)해서 평균을 구합니다.
    @Query("SELECT d.dealYear as dealYear, d.dealMonth as dealMonth, AVG(CAST(d.dealAmount AS double)) as averagePrice " +
            "FROM ApartmentDeal d " +
            "WHERE d.lawdCd = :lawdCd " +
            "GROUP BY d.dealYear, d.dealMonth " +
            "ORDER BY d.dealYear ASC, d.dealMonth ASC")
    List<PriceTrendDto> findMonthlyTrend(@Param("lawdCd") String lawdCd);

    // 2. [신규] 특정 아파트 콕 집어서 월별 시세 조회 (아파트 이름 조건 추가)
    @Query("SELECT d.dealYear as dealYear, d.dealMonth as dealMonth, AVG(CAST(d.dealAmount AS double)) as averagePrice " +
            "FROM ApartmentDeal d " +
            "WHERE d.lawdCd = :lawdCd AND d.apartmentName = :aptName " + // ★ 이름 조건 추가
            "GROUP BY d.dealYear, d.dealMonth " +
            "ORDER BY d.dealYear ASC, d.dealMonth ASC")
    List<PriceTrendDto> findMonthlyTrendByApt(@Param("lawdCd") String lawdCd, @Param("aptName") String aptName);

    // 3. [신규] 아파트 이름 검색 (중복 제거)
    // "종로"라고 치면 "종로"가 포함된 아파트 이름만 싹 긁어옴 (LIKE 검색)
    @Query("SELECT DISTINCT d.apartmentName FROM ApartmentDeal d " +
            "WHERE d.lawdCd = :lawdCd AND d.apartmentName LIKE %:keyword%")
    List<String> findApartmentNames(@Param("lawdCd") String lawdCd, @Param("keyword") String keyword);

    // 4. [프론트 카드용] 특정 아파트의 가장 최신 거래 1건 조회
    // (년 -> 월 -> 일 내림차순 정렬해서 맨 위 1개 가져옴)
    Optional<ApartmentDeal> findTop1ByLawdCdAndApartmentNameOrderByDealYearDescDealMonthDescDealDayDesc(String lawdCd, String apartmentName);

    // 5. [동기화용] 특정 지역 데이터 중 "가장 마지막으로 저장된 데이터" 날짜 조회
    // (스마트 동기화 시작점을 찾기 위함)
    Optional<ApartmentDeal> findTop1ByLawdCdOrderByDealYearDescDealMonthDesc(String lawdCd);

    // 6. [중복 방지용] 이미 DB에 존재하는 거래인지 확인
    boolean existsByLawdCdAndApartmentNameAndDealYearAndDealMonthAndDealDayAndDealAmountAndFloor(
            String lawdCd, String apartmentName, Integer dealYear, Integer dealMonth, Integer dealDay, String dealAmount, Integer floor
    );
}