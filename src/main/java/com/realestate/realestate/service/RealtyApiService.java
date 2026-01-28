package com.realestate.realestate.service;

import com.realestate.realestate.domain.ApartmentDeal;
import com.realestate.realestate.repository.ApartmentDealRepository;
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

@Service
@RequiredArgsConstructor
public class RealtyApiService {

    private final ApartmentDealRepository apartmentDealRepository;

    // 1. 공공데이터포털 디코딩 인증키 (본인의 키 확인 필수)
    private final String serviceKey = "+umsD8GnHKfyOt9w/vksdyNtNNKdth3vd1w19zFBN2LjdyaRTbUHWzhDBhLXrshicuNzhMa1Y/E5cUrZmf7b7g==";

    // 2. 서울시 25개 구 코드
    private static final String[] SEOUL_CODES = {
            "11110", "11140", "11170", "11200", "11215", "11230", "11260", "11290",
            "11305", "11320", "11350", "11380", "11410", "11440", "11470", "11500",
            "11530", "11545", "11560", "11590", "11620", "11650", "11680", "11710", "11740"
    };

    // [기능 1] 서울시 최근 3년치 데이터 수집 (하루 제한 1000회 안쪽으로 맞춤)
    public void collectSeoulData() {
        int startYear = 2022; // 2022년부터
        int endYear = 2024;   // 2024년까지 (총 3년)

        System.out.println("🚀 서울시 데이터 수집 시작 (2022~2024)");
        int totalRequests = 0;

        for (String districtCode : SEOUL_CODES) {
            for (int year = startYear; year <= endYear; year++) {
                for (int month = 1; month <= 12; month++) {
                    String dealYmd = String.format("%04d%02d", year, month);

                    try {
                        System.out.print("⏳ 수집중 [" + districtCode + " / " + dealYmd + "] ... ");

                        // 데이터 수집 실행
                        fetchAndSaveData(districtCode, dealYmd);

                        // 성공 로그
                        totalRequests++;

                        // API 차단 방지를 위한 0.5초 대기
                        Thread.sleep(500);

                    } catch (Exception e) {
                        System.err.println("❌ 실패: " + e.getMessage());
                    }
                }
            }
        }
        System.out.println("🏁 서울시 데이터 수집 완료! 총 요청 횟수: " + totalRequests);
    }

    // [기능 2] 실제 API 호출 및 저장 (핵심 로직)
    public void fetchAndSaveData(String lawdCd, String dealYmd) {
        try {
            String baseUrl = "https://apis.data.go.kr/1613000/RTMSDataSvcAptTrade/getRTMSDataSvcAptTrade";

            // ★ 인증키 수동 인코딩 (401 에러 해결)
            String encodedKey = URLEncoder.encode(serviceKey, "UTF-8");

            // ★ URL 직접 조립 (_type=xml, numOfRows=1000 추가)
            String fullUrl = baseUrl + "?serviceKey=" + encodedKey
                    + "&LAWD_CD=" + lawdCd
                    + "&DEAL_YMD=" + dealYmd
                    + "&_type=xml"       // XML 강제
                    + "&numOfRows=1000"; // 한 번에 최대 1000개

            URI uri = new URI(fullUrl);
            RestTemplate restTemplate = new RestTemplate();
            String xmlResponse = restTemplate.getForObject(uri, String.class);

            // XML 파싱
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes("UTF-8")));

            NodeList itemList = doc.getElementsByTagName("item");

            // 데이터가 없으면 리턴
            if (itemList.getLength() == 0) {
                System.out.println("데이터 없음 (0건)");
                return;
            }

            for (int i = 0; i < itemList.getLength(); i++) {
                Node node = itemList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;

                    String rawAmount = getTagValue("dealAmount", e).replace(",", "").trim();

                    ApartmentDeal deal = ApartmentDeal.builder()
                            .apartmentName(getTagValue("aptNm", e).trim())
                            .dealAmount(rawAmount)
                            .buildYear(safeParseInt(getTagValue("buildYear", e)))
                            .dealYear(safeParseInt(getTagValue("dealYear", e)))
                            .dealMonth(safeParseInt(getTagValue("dealMonth", e)))
                            .dealDay(safeParseInt(getTagValue("dealDay", e)))
                            .areaForExclusiveUse(safeParseDouble(getTagValue("excluArea", e))) // ★ 빈 값 에러 해결
                            .lawdCd(lawdCd)
                            .build();

                    apartmentDealRepository.save(deal);
                }
            }
            System.out.println("✅ 저장 완료 (" + itemList.getLength() + "건)");

        } catch (Exception e) {
            System.out.println("⚠️ 에러 발생: " + e.getMessage());
            // 여기서 throw를 안 해야 반복문이 멈추지 않고 다음 구역으로 넘어갑니다.
        }
    }

    // [헬퍼] 태그 값 꺼내기
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