package com.realestate.realestate.controller;

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

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class RealtyController {

    private final RealtyApiService realtyApiService;
    private final ApartmentDealRepository apartmentDealRepository;

    // 브라우저 주소창에 http://localhost:8081/fetch?lawdCd=11110&dealYmd=202401 입력 시 실행
    @GetMapping("/fetch")
    public String fetchData(
            @RequestParam String lawdCd,
            @RequestParam String dealYmd) {

        realtyApiService.fetchAndSaveData(lawdCd, dealYmd);
        return "데이터 수집 및 저장 완료!";
    }

    // 브라우저 실행 주소: http://localhost:8081/collect/seoul
    @GetMapping("/collect/seoul")
    public String collectSeoul() {
        new Thread(() -> {
            realtyApiService.collectSeoulData();
        }).start();

        return "서울시 3년치 데이터 수집을 시작합니다! (약 10~15분 소요) 콘솔 로그를 확인하세요.";
    }


    // 1. 아파트 목록 검색 API (예: /api/apartments?lawdCd=11110&keyword=종로)
    @GetMapping("/api/apartments")
    public List<String> searchApartments(@RequestParam String lawdCd, @RequestParam String keyword) {
        return apartmentDealRepository.findApartmentNames(lawdCd, keyword);
    }

    // 2. 시세 추이 API (수정: aptName이 있으면 그걸로, 없으면 구 전체 평균)
    @GetMapping("/api/trend")
    public Map<String, Object> getTrend(
            @RequestParam String lawdCd,
            @RequestParam(required = false) String aptName // 아파트 이름은 없을 수도 있음 (Optional)
    ) {
        long startTime = System.currentTimeMillis();

        List<PriceTrendDto> result;
        if (aptName != null && !aptName.isEmpty()) {
            // 아파트 이름이 있으면 -> 그 아파트만 조회
            result = apartmentDealRepository.findMonthlyTrendByApt(lawdCd, aptName);
        } else {
            // 없으면 -> 구 전체 평균 조회
            result = apartmentDealRepository.findMonthlyTrend(lawdCd);
        }

        long endTime = System.currentTimeMillis();

        Map<String, Object> response = new HashMap<>();
        response.put("data", result);
        response.put("executionTime", (endTime - startTime) + "ms");
        return response;
    }
}