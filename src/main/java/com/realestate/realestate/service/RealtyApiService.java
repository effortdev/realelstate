package com.realestate.realestate.service;

import com.realestate.realestate.domain.ApartmentDeal;
import com.realestate.realestate.repository.ApartmentDealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
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


    private final String serviceKey = "+umsD8GnHKfyOt9w/vksdyNtNNKdth3vd1w19zFBN2LjdyaRTbUHWzhDBhLXrshicuNzhMa1Y/E5cUrZmf7b7g==";

    public void fetchAndSaveData(String lawdCd, String dealYmd) {
        String xmlResponse = null;
        try {
            // 1. 공공데이터포털 최신 주소
            String baseUrl = "https://apis.data.go.kr/1613000/RTMSDataSvcAptTrade/getRTMSDataSvcAptTrade";

            // 2. [핵심] 서비스 키만 따로 강제로 인코딩합니다. (UTF-8)
            // 이렇게 하면 '+'는 무조건 '%2B'로 변합니다.
            String encodedKey = URLEncoder.encode(serviceKey, "UTF-8");

            // 3. 인코딩된 키를 문자열 더하기로 주소에 넣습니다.
            String fullUrl = baseUrl + "?serviceKey=" + encodedKey
                    + "&LAWD_CD=" + lawdCd
                    + "&DEAL_YMD=" + dealYmd
                    + "&_type=xml";

            // 4. 로그 확인 (여기서 %2B가 보여야 합니다)
            System.out.println("진짜_최종_전송_URL: " + fullUrl);

            // 5. URI 객체로 변환하여 RestTemplate에 전달
            URI uri = new URI(fullUrl);
            RestTemplate restTemplate = new RestTemplate();
            xmlResponse = restTemplate.getForObject(uri, String.class);

            System.out.println("====== 수신된 데이터 내용 ======");
            System.out.println(xmlResponse);
            System.out.println("==============================");

            // 2. XML 데이터 파싱 준비
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes("UTF-8")));

            NodeList itemList = doc.getElementsByTagName("item");

            for (int i = 0; i < itemList.getLength(); i++) {
                Node node = itemList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;

                    // 3. XML 데이터를 Entity로 변환 (데이터 가공 포함)
                    // 거래금액에서 쉼표(,)를 제거하고 공백을 없앱니다.
                    String rawAmount = getTagValue("dealAmount", e).replace(",", "").trim();

                    ApartmentDeal deal = ApartmentDeal.builder()
                            .apartmentName(getTagValue("aptNm", e).trim())
                            .dealAmount(rawAmount)
                            .buildYear(safeParseInt(getTagValue("buildYear", e)))   // 안전 변환 적용
                            .dealYear(safeParseInt(getTagValue("dealYear", e)))     // 안전 변환 적용
                            .dealMonth(safeParseInt(getTagValue("dealMonth", e)))   // 안전 변환 적용
                            .dealDay(safeParseInt(getTagValue("dealDay", e)))       // 안전 변환 적용
                            .areaForExclusiveUse(safeParseDouble(getTagValue("excluArea", e)))
                            .lawdCd(lawdCd)
                            .build();

                    // 4. DB에 한 건씩 저장
                    apartmentDealRepository.save(deal);
                }
            }
            System.out.println("✅ " + dealYmd + " 데이터 저장 완료! (총 " + itemList.getLength() + "건)");

        } catch (Exception e) {
            System.err.println("데이터 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // [수정됨] XML 태그 값을 안전하게 가져오는 메서드
    private String getTagValue(String tag, Element e) {
        NodeList nlList = e.getElementsByTagName(tag);
        if (nlList.getLength() > 0 && nlList.item(0).getChildNodes().getLength() > 0) {
            Node nValue = nlList.item(0).getChildNodes().item(0);
            return (nValue != null) ? nValue.getNodeValue() : "";
        }
        return "";
    }

    private Integer safeParseInt(String str) {
        if (str == null || str.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // [신규] 빈 문자열("")이 들어오면 0.0을 반환하는 안전한 Double 변환기
    private Double safeParseDouble(String str) {
        if (str == null || str.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}