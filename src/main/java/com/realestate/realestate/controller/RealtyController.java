package com.realestate.realestate.controller;

import com.realestate.realestate.domain.ApartmentDeal;
import com.realestate.realestate.dto.PriceTrendDto;
import com.realestate.realestate.repository.ApartmentDealRepository;
import com.realestate.realestate.service.RealtyApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class RealtyController {

    private final RealtyApiService realtyApiService;
    private final ApartmentDealRepository apartmentDealRepository;

    @GetMapping("/fetch")
    public String fetchData(@RequestParam String lawdCd, @RequestParam String dealYmd) {
        realtyApiService.fetchApartmentTradeData(lawdCd, dealYmd);
        return "데이터 수집 및 저장 완료!";
    }

    @GetMapping("/collect/seoul")
    public String collectSeoul() {
        new Thread(() -> realtyApiService.collectSeoulData()).start();
        return "서울시 3년치 데이터 수집 시작 (콘솔 확인)";
    }

    @GetMapping("/api/apartments")
    public List<String> searchApartments(@RequestParam String lawdCd, @RequestParam String keyword) {
        return apartmentDealRepository.findApartmentNames(lawdCd, keyword);
    }

    @GetMapping("/api/trend")
    public Map<String, Object> getTrend(
            @RequestParam String lawdCd,
            @RequestParam(required = false) String aptName
    ) {
        long startTime = System.currentTimeMillis();

        List<PriceTrendDto> result;
        Map<String, Object> latestDeal = null;

        if (aptName != null && !aptName.isEmpty()) {
            result = apartmentDealRepository.findMonthlyTrendByApt(lawdCd, aptName);

            Optional<ApartmentDeal> dealOpt = apartmentDealRepository.findTop1ByLawdCdAndApartmentNameOrderByDealYearDescDealMonthDescDealDayDesc(lawdCd, aptName);

            if (dealOpt.isPresent()) {
                ApartmentDeal deal = dealOpt.get();
                latestDeal = new HashMap<>();
                latestDeal.put("date", deal.getDealYear() + "." + deal.getDealMonth() + "." + deal.getDealDay());
                latestDeal.put("price", deal.getDealAmount());
                latestDeal.put("floor", deal.getFloor());
                latestDeal.put("area", deal.getExcluUseAr());

                latestDeal.put("dong", deal.getDong());
            }
        } else {
            result = apartmentDealRepository.findMonthlyTrend(lawdCd);
        }

        long endTime = System.currentTimeMillis();

        Map<String, Object> response = new HashMap<>();
        response.put("data", result);
        response.put("latest", latestDeal);
        response.put("executionTime", (endTime - startTime) + "ms");
        return response;
    }

    @GetMapping("/api/sync")
    public Map<String, Object> syncLatest(@RequestParam String lawdCd) {
        List<String> addedApts = realtyApiService.syncLatestData(lawdCd);
        Map<String, Object> response = new HashMap<>();
        response.put("addedCount", addedApts.size());
        response.put("addedApts", addedApts);
        return response;
    }

    @GetMapping("/reset")
    public String resetData() {
        apartmentDealRepository.deleteAll();
        return "🗑️ DB가 깨끗하게 비워졌습니다! /collect/seoul 을 눌러 다시 수집해주세요.";
    }
}