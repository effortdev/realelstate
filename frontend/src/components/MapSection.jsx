import React, { useEffect, useState } from 'react';
import { Container as MapDiv, NaverMap, Marker, useNavermaps } from 'react-naver-maps';
import styled from 'styled-components';

const DISTRICT_COORDS = {
  "종로구": { lat: 37.5729, lng: 126.9793 },
  "중구": { lat: 37.5637, lng: 126.9975 },
  "용산구": { lat: 37.5323, lng: 126.9907 },
  "성동구": { lat: 37.5634, lng: 127.0369 },
  "광진구": { lat: 37.5386, lng: 127.0824 },
  "동대문구": { lat: 37.5745, lng: 127.0398 },
  "중랑구": { lat: 37.6065, lng: 127.0928 },
  "성북구": { lat: 37.5893, lng: 127.0167 },
  "강북구": { lat: 37.6397, lng: 127.0255 },
  "도봉구": { lat: 37.6687, lng: 127.0471 },
  "노원구": { lat: 37.6543, lng: 127.0565 },
  "은평구": { lat: 37.6027, lng: 126.9291 },
  "서대문구": { lat: 37.5790, lng: 126.9366 },
  "마포구": { lat: 37.5662, lng: 126.9016 },
  "양천구": { lat: 37.5170, lng: 126.8665 },
  "강서구": { lat: 37.5509, lng: 126.8496 },
  "구로구": { lat: 37.4954, lng: 126.8875 },
  "금천구": { lat: 37.4568, lng: 126.8955 },
  "영등포구": { lat: 37.5262, lng: 126.8961 },
  "동작구": { lat: 37.5122, lng: 126.9393 },
  "관악구": { lat: 37.4783, lng: 126.9515 },
  "서초구": { lat: 37.4836, lng: 127.0327 },
  "강남구": { lat: 37.5173, lng: 127.0475 },
  "송파구": { lat: 37.5144, lng: 127.1059 },
  "강동구": { lat: 37.5303, lng: 127.1238 }
};

const MapSection = ({ district, aptName, dong }) => {
  const navermaps = useNavermaps();
  const [center, setCenter] = useState(DISTRICT_COORDS["종로구"]);
  const [zoom, setZoom] = useState(13);

  useEffect(() => {
    if (!aptName) {
      if (district && DISTRICT_COORDS[district]) {
        setCenter(DISTRICT_COORDS[district]);
        setZoom(14);
      }
      return;
    }

    if (!navermaps || !navermaps.Service) return;

    let cleanName = aptName.split('(')[0].trim();

    const hasAptSuffix = cleanName.endsWith('아파트');
    if (hasAptSuffix) {
        cleanName = cleanName.replace('아파트', '').trim();
    }

    console.log(`ℹ️ 정보 수신: 구=[${district}], 동=[${dong}], 이름=[${cleanName}]`);

    const searchQueries = [];

    if (dong) {
      searchQueries.push(`${district} ${dong} ${cleanName} 아파트`);
      searchQueries.push(`${district} ${dong} ${cleanName}`);
    }

    searchQueries.push(`${district} ${cleanName} 아파트`);

    searchQueries.push(`${district} ${cleanName}`);

    if (dong) {
        searchQueries.push(`${district} ${dong}`);
    }

    runSequentialSearch(searchQueries, 0);

  }, [district, aptName, dong, navermaps]);

  const runSequentialSearch = (queries, index) => {
    if (index >= queries.length) {
      console.warn("❌ 모든 검색 시도 실패 (지도 이동 안 함)");
      return;
    }

    const currentQuery = queries[index];

    navermaps.Service.geocode({ query: currentQuery }, function (status, response) {

      if (status !== navermaps.Service.Status.OK || !response.v2.addresses || response.v2.addresses.length === 0) {
        runSequentialSearch(queries, index + 1);
        return;
      }

      // 성공!
      const result = response.v2.addresses[0];
      const newLat = parseFloat(result.y);
      const newLng = parseFloat(result.x);

      console.log(`✅ 지도 이동 성공! ("${currentQuery}")`);
      setCenter({ lat: newLat, lng: newLng });
      setZoom(17);
    });
  };

  return (
    <MapWrapper>
      <MapDiv style={{ width: '100%', height: '100%' }}>
        <NaverMap
          center={new navermaps.LatLng(center.lat, center.lng)}
          zoom={zoom}
          onZoomChanged={(zoom) => setZoom(zoom)}
        >
          <Marker position={new navermaps.LatLng(center.lat, center.lng)} />
        </NaverMap>
      </MapDiv>
    </MapWrapper>
  );
};

export default MapSection;

const MapWrapper = styled.div`
  width: 100%; height: 400px; background: #f0f0f0; border-radius: 16px;
  overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.05);
  margin-bottom: 20px; border: 1px solid #ddd;
`;