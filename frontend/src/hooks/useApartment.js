import { useState, useEffect } from 'react';
import { DISTRICT_MAP, getTrendData, searchAptList, syncData } from '../api/realtyApi';

export const useApartment = () => {
  // --- 상태 관리 (State) ---
  const [data, setData] = useState([]);
  const [latest, setLatest] = useState(null);
  const [time, setTime] = useState(null);
  const [loading, setLoading] = useState(false);

  const [selectedDistrict, setSelectedDistrict] = useState("종로구");
  const [searchKeyword, setSearchKeyword] = useState("");
  const [aptList, setAptList] = useState([]);
  const [selectedApt, setSelectedApt] = useState(null);

  // --- 기능 1: 데이터 조회 (fetchTrend) ---
  const fetchTrend = async (aptName) => {
    const lawdCd = DISTRICT_MAP[selectedDistrict];

    try {
      setLoading(true);
      // API 호출
      const res = await getTrendData(lawdCd, aptName);

      // 그래프 데이터 가공
      const chartData = res.data.data.map(item => ({
        name: `${item.dealYear}.${item.dealMonth}`,
        price: Math.round(item.averagePrice)
      }));

      // 상태 업데이트 (여기서 setData 등을 사용하므로 에러가 사라집니다)
      setData(chartData);
      setLatest(res.data.latest);
      setTime(res.data.executionTime);
      setSelectedApt(aptName || null);

    } catch (error) {
      console.error(error);
      alert("데이터 조회 실패");
    } finally {
      setLoading(false);
    }
  };

  // --- 기능 2: 아파트 검색 (handleSearch) ---
  const handleSearch = async () => {
    if (!searchKeyword) return alert("검색어를 입력하세요");

    const lawdCd = DISTRICT_MAP[selectedDistrict];
    try {
      // API 호출
      const res = await searchAptList(lawdCd, searchKeyword);
      setAptList(res.data);
      if(res.data.length === 0) alert("검색 결과가 없습니다.");
    } catch (e) {
      console.error(e);
    }
  };

  // --- 기능 3: 동기화 (handleSync) - 아파트 이름 나오도록 수정됨 ---
  const handleSync = async () => {
    if (!window.confirm(`[${selectedDistrict}] 최신 데이터를 가져올까요?`)) return;

    try {
      setLoading(true);
      const res = await syncData(DISTRICT_MAP[selectedDistrict]);

      const { addedCount, addedApts } = res.data;

      if (addedCount > 0) {
        // 아파트 이름 목록 표시 (5개 넘으면 '외 X건' 처리)
        const aptNamesDisplay = addedApts.length > 5
          ? addedApts.slice(0, 5).join(", ") + ` 외 ${addedApts.length - 5}건`
          : addedApts.join(", ");

        alert(
          `✅ [${selectedDistrict}] 동기화 완료!\n\n` +
          `📌 추가 건수: ${addedCount}건\n` +
          `🏢 추가된 아파트: ${aptNamesDisplay}`
        );
      } else {
        alert("이미 최신 상태입니다.");
      }

      // 그래프 갱신
      fetchTrend(selectedApt);

    } catch (e) {
      console.error(e);
      alert("동기화 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  // --- 기타 핸들러 ---
  const handleDistrictChange = (e) => {
    setSelectedDistrict(e.target.value);
    setAptList([]);
    setSearchKeyword("");
    // 구 변경 시 전체 평균 자동 조회
    setTimeout(() => fetchTrend(null), 0);
  };

  // 초기 실행
  useEffect(() => {
    fetchTrend(null);
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // App.js로 내보낼 것들
  return {
    state: { data, latest, time, loading, selectedDistrict, searchKeyword, aptList, selectedApt },
    actions: { fetchTrend, handleSearch, handleSync, handleDistrictChange, setSearchKeyword }
  };
};