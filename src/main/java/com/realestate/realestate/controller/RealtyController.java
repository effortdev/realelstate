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
}