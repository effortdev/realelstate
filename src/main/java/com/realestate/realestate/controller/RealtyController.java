package com.realestate.realestate.controller;

import com.realestate.realestate.service.RealtyApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RealtyController {

    private final RealtyApiService realtyApiService;

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
}