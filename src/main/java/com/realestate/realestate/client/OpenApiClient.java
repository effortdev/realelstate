package com.realestate.realestate.client;

import com.realestate.realestate.domain.ApartmentDeal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
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
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OpenApiClient {

    private final String serviceKey = "+umsD8GnHKfyOt9w/vksdyNtNNKdth3vd1w19zFBN2LjdyaRTbUHWzhDBhLXrshicuNzhMa1Y/E5cUrZmf7b7g==";

    public List<ApartmentDeal> fetchTradeData(String lawdCd, String dealYmd) {
        List<ApartmentDeal> list = new ArrayList<>();
        try {
            String baseUrl = "https://apis.data.go.kr/1613000/RTMSDataSvcAptTrade/getRTMSDataSvcAptTrade";
            String encodedKey = URLEncoder.encode(serviceKey, "UTF-8");

            String fullUrl = baseUrl + "?serviceKey=" + encodedKey
                    + "&LAWD_CD=" + lawdCd
                    + "&DEAL_YMD=" + dealYmd
                    + "&_type=xml"
                    + "&numOfRows=1000";

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

            String xmlResponse = restTemplate.getForObject(new URI(fullUrl), String.class);

            return parseXml(xmlResponse, lawdCd);

        } catch (Exception e) {
            System.err.println("❌ API 호출 실패 (" + dealYmd + "): " + e.getMessage());
            return list;
        }
    }

    private List<ApartmentDeal> parseXml(String xmlResponse, String lawdCd) throws Exception {
        List<ApartmentDeal> list = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8)));

        NodeList itemList = doc.getElementsByTagName("item");

        for (int i = 0; i < itemList.getLength(); i++) {
            Node node = itemList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) node;

                String rawAmount = getTagValue("dealAmount", e).replace(",", "").trim();
                String floorStr = getTagValue("floor", e);
                int floor = floorStr.isEmpty() ? 0 : parseSafeInt(floorStr);

                ApartmentDeal deal = ApartmentDeal.builder()
                        .apartmentName(getTagValue("aptNm", e).trim())
                        .dealAmount(rawAmount)
                        .dealYear(parseSafeInt(getTagValue("dealYear", e)))
                        .dealMonth(parseSafeInt(getTagValue("dealMonth", e)))
                        .dealDay(parseSafeInt(getTagValue("dealDay", e)))
                        .excluUseAr(parseSafeDouble(getTagValue("excluUseAr", e)))
                        .lawdCd(lawdCd)
                        .floor(floor)

                        .dong(getTagValue("umdNm", e).trim())
                        .jibun(getTagValue("jibun", e).trim())

                        .buildYear(parseSafeInt(getTagValue("buildYear", e)))
                        .aptDong(getTagValue("aptDong", e))

                        .dealingGbn(getTagValue("dealingGbn", e))
                        .estateAgentSggNm(getTagValue("estateAgentSggNm", e))
                        .cdealType(getTagValue("cdealType", e))
                        .cdealDay(getTagValue("cdealDay", e))
                        .rgstDate(getTagValue("rgstDate", e))

                        .slerGbn(getTagValue("slerGbn", e))
                        .buyerGbn(getTagValue("buyerGbn", e))
                        .landLeaseholdGbn(getTagValue("landLeaseholdGbn", e))
                        .build();

                list.add(deal);
            }
        }
        return list;
    }

    private String getTagValue(String tag, Element e) {
        NodeList nlList = e.getElementsByTagName(tag);
        if (nlList.getLength() > 0 && nlList.item(0).getChildNodes().getLength() > 0) {
            Node nValue = nlList.item(0).getChildNodes().item(0);
            return (nValue != null) ? nValue.getNodeValue().trim() : "";
        }
        return "";
    }

    private Integer parseSafeInt(String str) {
        try { return Integer.parseInt(str.trim()); } catch (Exception e) { return 0; }
    }

    private Double parseSafeDouble(String str) {
        try { return Double.parseDouble(str.trim()); } catch (Exception e) { return 0.0; }
    }
}