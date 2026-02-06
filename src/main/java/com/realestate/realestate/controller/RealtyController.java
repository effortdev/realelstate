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

    // ... (fetch, collectSeoul, searchApartments 메서드는 기존과 동일) ...
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

    // ▼▼▼ 여기가 수정된 부분입니다! ▼▼▼
    @GetMapping("/api/trend")
    public Map<String, Object> getTrend(
            @RequestParam String lawdCd,
            @RequestParam(required = false) String aptName
    ) {
        long startTime = System.currentTimeMillis();

        List<PriceTrendDto> result;
        Map<String, Object> latestDeal = null; // 초기화 위치 변경 및 변수명 명확화

        if (aptName != null && !aptName.isEmpty()) {
            // 1. 그래프 데이터 조회
            result = apartmentDealRepository.findMonthlyTrendByApt(lawdCd, aptName);

            // 2. [수정됨] 최신 거래 정보 조회 (지도 및 상단 정보용)
            Optional<ApartmentDeal> dealOpt = apartmentDealRepository.findTop1ByLawdCdAndApartmentNameOrderByDealYearDescDealMonthDescDealDayDesc(lawdCd, aptName);

            if (dealOpt.isPresent()) {
                ApartmentDeal deal = dealOpt.get();
                latestDeal = new HashMap<>();
                latestDeal.put("date", deal.getDealYear() + "." + deal.getDealMonth() + "." + deal.getDealDay());
                latestDeal.put("price", deal.getDealAmount());
                latestDeal.put("floor", deal.getFloor());
                latestDeal.put("area", deal.getExcluUseAr());

                // ★ [핵심] 이 줄이 추가되어야 지도가 '동' 단위로 검색합니다!
                latestDeal.put("dong", deal.getDong());
            }
        } else {
            // 구 전체 평균
            result = apartmentDealRepository.findMonthlyTrend(lawdCd);
        }

        long endTime = System.currentTimeMillis();

        Map<String, Object> response = new HashMap<>();
        response.put("data", result);
        response.put("latest", latestDeal); // 프론트엔드로 최신 정보 전송
        response.put("executionTime", (endTime - startTime) + "ms");
        return response;
    }
    // ▲▲▲ 여기까지 수정 ▲▲▲

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
        apartmentDealRepository.deleteAll(); // 모든 데이터 삭제
        return "🗑️ DB가 깨끗하게 비워졌습니다! /collect/seoul 을 눌러 다시 수집해주세요.";
    }
}