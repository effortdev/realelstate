package com.realestate.realestate.service;

import com.realestate.realestate.domain.ApartmentDeal;
import com.realestate.realestate.repository.ApartmentDealRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RealtyApiService {

    private final ApartmentDealRepository apartmentDealRepository;

    // 1. 공공데이터포털 디코딩 인증키
    private final String serviceKey = "+umsD8GnHKfyOt9w/vksdyNtNNKdth3vd1w19zFBN2LjdyaRTbUHWzhDBhLXrshicuNzhMa1Y/E5cUrZmf7b7g==";

    // 2. 서울시 25개 구 코드
    private static final String[] SEOUL_CODES = {
            "11110", "11140", "11170", "11200", "11215", "11230", "11260", "11290",
            "11305", "11320", "11350", "11380", "11410", "11440", "11470", "11500",
            "11530", "11545", "11560", "11590", "11620", "11650", "11680", "11710", "11740"
    };

    // [기능 1] 초기 데이터 대량 수집 (2022 ~ 2024)
    public void collectSeoulData() {
        int startYear = 2022;
        int endYear = 2024;

        System.out.println("🚀 서울시 데이터 수집 시작 (2022~2024)");
        int totalRequests = 0;

        for (String districtCode : SEOUL_CODES) {
            for (int year = startYear; year <= endYear; year++) {
                for (int month = 1; month <= 12; month++) {
                    String dealYmd = String.format("%04d%02d", year, month);
                    try {
                        System.out.print("⏳ 수집중 [" + districtCode + " / " + dealYmd + "] ... ");

                        // API 호출하여 리스트 받아오기
                        List<ApartmentDeal> deals = fetchApartmentTradeData(districtCode, dealYmd);

                        // 대량 수집이므로 묻지 않고 저장 (초기화 용도)
                        apartmentDealRepository.saveAll(deals);

                        System.out.println("✅ " + deals.size() + "건 저장");
                        totalRequests++;
                        Thread.sleep(500); // 차단 방지

                    } catch (Exception e) {
                        System.err.println("❌ 실패: " + e.getMessage());
                    }
                }
            }
        }
        System.out.println("🏁 서울시 데이터 수집 완료! 총 요청 횟수: " + totalRequests);
    }

    // [기능 2] 스마트 최신 동기화 (마지막 저장된 날짜 이후부터 ~ 오늘까지 채우기)
    @Transactional
    public int syncLatestData(String lawdCd) {
        int totalAddedCount = 0;

        // 1. DB에서 가장 마지막 데이터 날짜 확인
        Optional<ApartmentDeal> lastDeal = apartmentDealRepository.findTop1ByLawdCdOrderByDealYearDescDealMonthDesc(lawdCd);

        LocalDate startDate;
        if (lastDeal.isPresent()) {
            // 마지막 데이터의 "다음 달"부터 수집 시작
            startDate = LocalDate.of(lastDeal.get().getDealYear(), lastDeal.get().getDealMonth(), 1).plusMonths(1);
            System.out.println("📡 마지막 데이터: " + startDate.minusMonths(1).getYear() + "." + startDate.minusMonths(1).getMonthValue() + " -> 동기화 시작: " + startDate);
        } else {
            // 데이터가 아예 없으면 2024년 1월부터 시작
            startDate = LocalDate.of(2024, 1, 1);
            System.out.println("📡 데이터 없음 -> 2024.01부터 시작");
        }

        LocalDate today = LocalDate.now();

        // 2. 시작일부터 오늘까지 월 단위 반복
        while (!startDate.isAfter(today)) {
            int year = startDate.getYear();
            int month = startDate.getMonthValue();
            String dealYmd = String.format("%d%02d", year, month);

            // 3. API 호출
            List<ApartmentDeal> fetchedItems = fetchApartmentTradeData(lawdCd, dealYmd);

            // 4. 중복 체크 후 저장
            for (ApartmentDeal item : fetchedItems) {
                boolean exists = apartmentDealRepository.existsByLawdCdAndApartmentNameAndDealYearAndDealMonthAndDealDayAndDealAmountAndFloor(
                        item.getLawdCd(), item.getApartmentName(),
                        item.getDealYear(), item.getDealMonth(), item.getDealDay(),
                        item.getDealAmount(), item.getFloor()
                );

                if (!exists) {
                    apartmentDealRepository.save(item);
                    totalAddedCount++;
                }
            }

            // 다음 달로 이동
            startDate = startDate.plusMonths(1);
        }

        System.out.println("✨ 동기화 완료! 총 추가된 건수: " + totalAddedCount);
        return totalAddedCount;
    }

    // [핵심 로직] 실제 공공데이터 API 호출 및 파싱 (리스트 반환)
    public List<ApartmentDeal> fetchApartmentTradeData(String lawdCd, String dealYmd) {
        List<ApartmentDeal> list = new ArrayList<>();

        try {
            String baseUrl = "https://apis.data.go.kr/1613000/RTMSDataSvcAptTrade/getRTMSDataSvcAptTrade";
            String encodedKey = URLEncoder.encode(serviceKey, "UTF-8");

            String fullUrl = baseUrl + "?serviceKey=" + encodedKey
                    + "&LAWD_CD=" + lawdCd
                    + "&DEAL_YMD=" + dealYmd
                    + "&_type=xml"
                    + "&numOfRows=1000";

            URI uri = new URI(fullUrl);
            RestTemplate restTemplate = new RestTemplate();
            String xmlResponse = restTemplate.getForObject(uri, String.class);

            // XML 파싱
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes("UTF-8")));

            NodeList itemList = doc.getElementsByTagName("item");

            for (int i = 0; i < itemList.getLength(); i++) {
                Node node = itemList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;

                    // ★ 중요: 공공데이터 실제 태그명은 한글입니다 (영어x)
                    String rawAmount = getTagValue("거래금액", e).replace(",", "").trim();

                    // 층 정보가 없는 경우(저층 등) 방어 로직
                    String floorStr = getTagValue("층", e);
                    int floor = floorStr.isEmpty() ? 0 : safeParseInt(floorStr);

                    ApartmentDeal deal = ApartmentDeal.builder()
                            .apartmentName(getTagValue("아파트", e).trim())
                            .dealAmount(rawAmount)
                            .dealYear(safeParseInt(getTagValue("년", e)))
                            .dealMonth(safeParseInt(getTagValue("월", e)))
                            .dealDay(safeParseInt(getTagValue("일", e)))
                            .excluUseAr(safeParseDouble(getTagValue("전용면적", e))) // Entity 필드명 확인 필요
                            .lawdCd(lawdCd)
                            .dong(getTagValue("법정동", e).trim())
                            .floor(floor)
                            .build();

                    list.add(deal);
                }
            }
        } catch (Exception e) {
            System.err.println("API 호출 에러 (" + dealYmd + "): " + e.getMessage());
        }

        return list;
    }

    // [헬퍼] XML 태그 값 꺼내기
    private String getTagValue(String tag, Element e) {
        NodeList nlList = e.getElementsByTagName(tag);
        if (nlList.getLength() > 0 && nlList.item(0).getChildNodes().getLength() > 0) {
            Node nValue = nlList.item(0).getChildNodes().item(0);
            return (nValue != null) ? nValue.getNodeValue() : "";
        }
        return "";
    }

    // [헬퍼] 안전한 정수 변환
    private Integer safeParseInt(String str) {
        if (str == null || str.trim().isEmpty()) return 0;
        try { return Integer.parseInt(str.trim()); } catch (Exception e) { return 0; }
    }

    // [헬퍼] 안전한 실수 변환
    private Double safeParseDouble(String str) {
        if (str == null || str.trim().isEmpty()) return 0.0;
        try { return Double.parseDouble(str.trim()); } catch (Exception e) { return 0.0; }
    }
}