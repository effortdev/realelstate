package com.realestate.realestate.service;

import com.realestate.realestate.client.OpenApiClient;
import com.realestate.realestate.domain.ApartmentDeal;
import com.realestate.realestate.repository.ApartmentDealRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RealtyApiService {

    private final ApartmentDealRepository apartmentDealRepository;
    private final OpenApiClient openApiClient;

    private static final String[] SEOUL_CODES = {
            "11110", "11140", "11170", "11200", "11215", "11230", "11260", "11290",
            "11305", "11320", "11350", "11380", "11410", "11440", "11470", "11500",
            "11530", "11545", "11560", "11590", "11620", "11650", "11680", "11710", "11740"
    };

    public void collectSeoulData() {
        int startYear = 2023;
        int endYear = 2025;
        System.out.println("🚀 서울시 데이터 수집 시작 (2022~2024)");

        for (String districtCode : SEOUL_CODES) {
            for (int year = startYear; year <= endYear; year++) {
                for (int month = 1; month <= 12; month++) {
                    String dealYmd = String.format("%04d%02d", year, month);
                    try {
                        System.out.print("⏳ 수집중 [" + districtCode + " / " + dealYmd + "] ... ");

                        List<ApartmentDeal> deals = openApiClient.fetchTradeData(districtCode, dealYmd);

                        if (!deals.isEmpty()) {
                            apartmentDealRepository.saveAll(deals);
                            System.out.println("✅ " + deals.size() + "건 저장");
                        } else {
                            System.out.println("⚠️ 데이터 없음");
                        }

                        Thread.sleep(300);

                    } catch (Exception e) {
                        System.err.println("❌ 실패: " + e.getMessage());
                    }
                }
            }
        }
        System.out.println("🏁 서울시 데이터 수집 완료!");
    }

    @Transactional
    public List<String> syncLatestData(String lawdCd) {
        List<String> addedAptNames = new ArrayList<>();

        Optional<ApartmentDeal> lastDeal = apartmentDealRepository.findTop1ByLawdCdOrderByDealYearDescDealMonthDesc(lawdCd);

        LocalDate startDate = lastDeal.map(deal -> LocalDate.of(deal.getDealYear(), deal.getDealMonth(), 1).plusMonths(1))
                .orElse(LocalDate.of(2024, 1, 1));
        LocalDate today = LocalDate.now();

        while (!startDate.isAfter(today)) {
            String dealYmd = String.format("%d%02d", startDate.getYear(), startDate.getMonthValue());

            List<ApartmentDeal> fetchedItems = openApiClient.fetchTradeData(lawdCd, dealYmd);

            for (ApartmentDeal item : fetchedItems) {
                if (!isDuplicate(item)) {
                    apartmentDealRepository.save(item);
                    if (!addedAptNames.contains(item.getApartmentName())) {
                        addedAptNames.add(item.getApartmentName());
                    }
                }
            }
            try { Thread.sleep(300); } catch (InterruptedException e) {}
            startDate = startDate.plusMonths(1);
        }
        return addedAptNames;
    }



    // 중복 체크 헬퍼 메소드
    private boolean isDuplicate(ApartmentDeal item) {
        return apartmentDealRepository.existsByLawdCdAndApartmentNameAndDealYearAndDealMonthAndDealDayAndDealAmountAndFloor(
                item.getLawdCd(), item.getApartmentName(),
                item.getDealYear(), item.getDealMonth(), item.getDealDay(),
                item.getDealAmount(), item.getFloor()
        );
    }

    // [추가] 컨트롤러(테스트용 /fetch)를 위한 연결 다리 메서드
    public List<ApartmentDeal> fetchApartmentTradeData(String lawdCd, String dealYmd) {
        // 서비스가 직접 안 하고, 전문가(Client)에게 시킵니다.
        return openApiClient.fetchTradeData(lawdCd, dealYmd);
    }
}