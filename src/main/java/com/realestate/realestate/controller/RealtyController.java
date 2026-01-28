package com.realestate.realestate.controller;

import com.realestate.realestate.dto.PriceTrendDto;
import com.realestate.realestate.repository.ApartmentDealRepository;
import com.realestate.realestate.service.RealtyApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
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


    // [신규] 월별 시세 추이 조회 API
    // 사용법: http://localhost:8081/api/trend?lawdCd=11110
    @GetMapping("/api/trend")
    public Map<String, Object> getTrend(@RequestParam String lawdCd) {

        // 1. 시간 측정 시작
        long startTime = System.currentTimeMillis();

        // 2. DB에서 데이터 가져오기 (통계 쿼리)
        List<PriceTrendDto> result = apartmentDealRepository.findMonthlyTrend(lawdCd);

        // 3. 시간 측정 종료
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 4. 결과 포장 (데이터 + 걸린시간)
        Map<String, Object> response = new HashMap<>();
        response.put("data", result);
        response.put("executionTime", duration + "ms"); // 프론트에 '00ms' 라고 시간도 같이 줌

        return response;
    }
}