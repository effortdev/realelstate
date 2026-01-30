package com.realestate.realestate.service;

import com.realestate.realestate.domain.ApartmentDeal;
import com.realestate.realestate.repository.ApartmentDealRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.StringHttpMessageConverter;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RealtyApiService {

    private final ApartmentDealRepository apartmentDealRepository;

    // 공공데이터포털 인증키 (Encoding 된 키를 넣어야 함)
    private final String serviceKey = "+umsD8GnHKfyOt9w/vksdyNtNNKdth3vd1w19zFBN2LjdyaRTbUHWzhDBhLXrshicuNzhMa1Y/E5cUrZmf7b7g==";

    // 서울시 25개 구 코드
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

        for (String districtCode : SEOUL_CODES) {
            for (int year = startYear; year <= endYear; year++) {
                for (int month = 1; month <= 12; month++) {
                    String dealYmd = String.format("%04d%02d", year, month);
                    try {
                        System.out.print("⏳ 수집중 [" + districtCode + " / " + dealYmd + "] ... ");
                        List<ApartmentDeal> deals = fetchApartmentTradeData(districtCode, dealYmd);

                        if (!deals.isEmpty()) {
                            apartmentDealRepository.saveAll(deals);
                            System.out.println("✅ " + deals.size() + "건 저장");
                        } else {
                            System.out.println("⚠️ 데이터 없음");
                        }

                        // ★ 중요: API 차단 방지 (0.5초 대기)
                        Thread.sleep(500);

                    } catch (Exception e) {
                        System.err.println("❌ 실패: " + e.getMessage());
                    }
                }
            }
        }
        System.out.println("🏁 서울시 데이터 수집 완료!");
    }

    // [기능 2] 스마트 최신 동기화 (collectSeoulData 로직을 그대로 사용하되 범위만 자동 조절)
    @Transactional
    public List<String> syncLatestData(String lawdCd) {
        List<String> addedAptNames = new ArrayList<>();

        // 1. DB에서 가장 마지막 데이터 날짜 확인
        Optional<ApartmentDeal> lastDeal = apartmentDealRepository.findTop1ByLawdCdOrderByDealYearDescDealMonthDesc(lawdCd);

        LocalDate startDate;
        if (lastDeal.isPresent()) {
            // 마지막 데이터의 "다음 달"부터 수집 시작
            startDate = LocalDate.of(lastDeal.get().getDealYear(), lastDeal.get().getDealMonth(), 1).plusMonths(1);
            System.out.println("📡 [" + lawdCd + "] 마지막 데이터: " + lastDeal.get().getDealYear() + "." + lastDeal.get().getDealMonth());
        } else {
            // 데이터가 아예 없으면 2024년 1월부터 시작
            startDate = LocalDate.of(2024, 1, 1);
            System.out.println("📡 [" + lawdCd + "] 데이터 없음 -> 2024.01부터 시작");
        }

        LocalDate today = LocalDate.now();
        System.out.println("👉 동기화 기간: " + startDate + " ~ " + today);

        // 2. 시작일부터 오늘까지 월 단위 반복
        while (!startDate.isAfter(today)) {
            int year = startDate.getYear();
            int month = startDate.getMonthValue();
            String dealYmd = String.format("%d%02d", year, month);

            try {
                // 3. API 호출
                System.out.print("🔄 동기화 요청 중 (" + dealYmd + ")... ");
                List<ApartmentDeal> fetchedItems = fetchApartmentTradeData(lawdCd, dealYmd);
                System.out.println("응답 " + fetchedItems.size() + "건");

                // 4. 중복 체크 후 저장
                for (ApartmentDeal item : fetchedItems) {
                    boolean exists = apartmentDealRepository.existsByLawdCdAndApartmentNameAndDealYearAndDealMonthAndDealDayAndDealAmountAndFloor(
                            item.getLawdCd(), item.getApartmentName(),
                            item.getDealYear(), item.getDealMonth(), item.getDealDay(),
                            item.getDealAmount(), item.getFloor()
                    );

                    if (!exists) {
                        apartmentDealRepository.save(item);
                        // 새로 추가된 아파트 이름 기록
                        if (!addedAptNames.contains(item.getApartmentName())) {
                            addedAptNames.add(item.getApartmentName());
                        }
                    }
                }

                // ★ 중요: 너무 빠르게 요청하면 차단당함 (0.5초 휴식)
                Thread.sleep(500);

            } catch (Exception e) {
                System.err.println("❌ 동기화 중 에러 (" + dealYmd + "): " + e.getMessage());
            }

            // 다음 달로 이동
            startDate = startDate.plusMonths(1);
        }

        System.out.println("✨ 동기화 완료! 총 추가된 아파트 종류: " + addedAptNames.size() + "종");
        return addedAptNames;
    }

    // [핵심 로직] 실제 공공데이터 API 호출 및 파싱 (인코딩 문제 해결 버전)
    public List<ApartmentDeal> fetchApartmentTradeData(String lawdCd, String dealYmd) {
        List<ApartmentDeal> list = new ArrayList<>();

        try {
            String baseUrl = "https://apis.data.go.kr/1613000/RTMSDataSvcAptTrade/getRTMSDataSvcAptTrade";

            // ★ 중요: 서비스키 인코딩 이슈 해결
            // 만약 API 에러(SERVICE_KEY_IS_NOT_REGISTERED)가 나면 아래 URLEncoder 부분을 지우고 그냥 serviceKey를 넣으세요.
            String encodedKey = URLEncoder.encode(serviceKey, "UTF-8");

            String fullUrl = baseUrl + "?serviceKey=" + encodedKey
                    + "&LAWD_CD=" + lawdCd
                    + "&DEAL_YMD=" + dealYmd
                    + "&_type=xml"
                    + "&numOfRows=1000";

            URI uri = new URI(fullUrl);

            // ★ 중요: RestTemplate에 UTF-8 강제 설정 (한글 깨짐 방지)
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

            String xmlResponse = restTemplate.getForObject(uri, String.class);

            // XML 파싱
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            // UTF-8로 바이트 변환 후 파싱
            Document doc = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8)));

            NodeList itemList = doc.getElementsByTagName("item");

            for (int i = 0; i < itemList.getLength(); i++) {
                Node node = itemList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;

                    // 1. 거래금액 콤마 제거
                    String rawAmount = getTagValue("dealAmount", e).replace(",", "").trim();

// 2. 층 정보 처리 (값이 없으면 0층으로)
                    String floorStr = getTagValue("floor", e);
                    int floor = floorStr.isEmpty() ? 0 : safeParseInt(floorStr);

// 3. 빌더 생성 (영어 태그 -> 엔티티 필드 매핑)
                    ApartmentDeal deal = ApartmentDeal.builder()
                            .apartmentName(getTagValue("aptNm", e).trim())   // <aptNm> -> apartmentName
                            .dealAmount(rawAmount)                           // <dealAmount> -> dealAmount
                            .dealYear(safeParseInt(getTagValue("dealYear", e)))
                            .dealMonth(safeParseInt(getTagValue("dealMonth", e)))
                            .dealDay(safeParseInt(getTagValue("dealDay", e)))
                            .excluUseAr(safeParseDouble(getTagValue("excluUseAr", e)))
                            .lawdCd(getTagValue("sggCd", e).trim())          // <sggCd> -> lawdCd (또는 파라미터 lawdCd 사용 가능)
                            .dong(getTagValue("umdNm", e).trim())            // <umdNm> -> dong (법정동)
                            .buildYear(safeParseInt(getTagValue("buildYear", e))) // 건축년도 추가
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

    // [헬퍼] 태그 값 꺼내기 (Null Safe)
    private String getTagValue(String tag, Element e) {
        NodeList nlList = e.getElementsByTagName(tag);
        if (nlList.getLength() > 0 && nlList.item(0).getChildNodes().getLength() > 0) {
            Node nValue = nlList.item(0).getChildNodes().item(0);
            return (nValue != null) ? nValue.getNodeValue().trim() : "";
        }
        return "";
    }

    private Integer safeParseInt(String str) {
        if (str == null || str.trim().isEmpty()) return 0;
        try { return Integer.parseInt(str.trim()); } catch (Exception e) { return 0; }
    }

    private Double safeParseDouble(String str) {
        if (str == null || str.trim().isEmpty()) return 0.0;
        try { return Double.parseDouble(str.trim()); } catch (Exception e) { return 0.0; }
    }
}