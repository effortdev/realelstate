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

    @Query("SELECT d.dealYear as dealYear, d.dealMonth as dealMonth, AVG(CAST(d.dealAmount AS double)) as averagePrice " +
            "FROM ApartmentDeal d " +
            "WHERE d.lawdCd = :lawdCd " +
            "GROUP BY d.dealYear, d.dealMonth " +
            "ORDER BY d.dealYear ASC, d.dealMonth ASC")
    List<PriceTrendDto> findMonthlyTrend(@Param("lawdCd") String lawdCd);

    @Query("SELECT d.dealYear as dealYear, d.dealMonth as dealMonth, AVG(CAST(d.dealAmount AS double)) as averagePrice " +
            "FROM ApartmentDeal d " +
            "WHERE d.lawdCd = :lawdCd AND d.apartmentName = :aptName " +
            "GROUP BY d.dealYear, d.dealMonth " +
            "ORDER BY d.dealYear ASC, d.dealMonth ASC")
    List<PriceTrendDto> findMonthlyTrendByApt(@Param("lawdCd") String lawdCd, @Param("aptName") String aptName);

    @Query("SELECT DISTINCT d.apartmentName FROM ApartmentDeal d " +
            "WHERE d.lawdCd = :lawdCd AND d.apartmentName LIKE %:keyword%")
    List<String> findApartmentNames(@Param("lawdCd") String lawdCd, @Param("keyword") String keyword);

    Optional<ApartmentDeal> findTop1ByLawdCdAndApartmentNameOrderByDealYearDescDealMonthDescDealDayDesc(String lawdCd, String apartmentName);

    Optional<ApartmentDeal> findTop1ByLawdCdOrderByDealYearDescDealMonthDesc(String lawdCd);

    boolean existsByLawdCdAndApartmentNameAndDealYearAndDealMonthAndDealDayAndDealAmountAndFloor(
            String lawdCd, String apartmentName, Integer dealYear, Integer dealMonth, Integer dealDay, String dealAmount, Integer floor
    );
}