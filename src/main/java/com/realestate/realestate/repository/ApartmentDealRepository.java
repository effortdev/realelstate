package com.realestate.realestate.repository;

import com.realestate.realestate.domain.ApartmentDeal;
import com.realestate.realestate.dto.PriceTrendDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}