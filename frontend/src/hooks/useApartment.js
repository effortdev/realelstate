import { useState, useEffect } from 'react';
import { DISTRICT_MAP, getTrendData, searchAptList, syncData } from '../api/realtyApi';

export const useApartment = () => {
  const [data, setData] = useState([]);
  const [latest, setLatest] = useState(null);
  const [time, setTime] = useState(null);
  const [loading, setLoading] = useState(false);

  const [selectedDistrict, setSelectedDistrict] = useState("종로구");
  const [searchKeyword, setSearchKeyword] = useState("");
  const [aptList, setAptList] = useState([]);
  const [selectedApt, setSelectedApt] = useState(null);

  const fetchTrend = async (aptName) => {
    const lawdCd = DISTRICT_MAP[selectedDistrict];

    try {
      setLoading(true);
      const res = await getTrendData(lawdCd, aptName);

      const chartData = res.data.data.map(item => ({
        name: `${item.dealYear}.${item.dealMonth}`,
        price: Math.round(item.averagePrice)
      }));

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

  const handleSearch = async () => {
    if (!searchKeyword) return alert("검색어를 입력하세요");

    const lawdCd = DISTRICT_MAP[selectedDistrict];
    try {
      const res = await searchAptList(lawdCd, searchKeyword);
      setAptList(res.data);
      if(res.data.length === 0) alert("검색 결과가 없습니다.");
    } catch (e) {
      console.error(e);
    }
  };

  const handleSync = async () => {
    if (!window.confirm(`[${selectedDistrict}] 최신 데이터를 가져올까요?`)) return;

    try {
      setLoading(true);
      const res = await syncData(DISTRICT_MAP[selectedDistrict]);

      const { addedCount, addedApts } = res.data;

      if (addedCount > 0) {
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

  const handleDistrictChange = (e) => {
    setSelectedDistrict(e.target.value);
    setAptList([]);
    setSearchKeyword("");
    setTimeout(() => fetchTrend(null), 0);
  };

  useEffect(() => {
    fetchTrend(null);
  }, []);

  return {
    state: { data, latest, time, loading, selectedDistrict, searchKeyword, aptList, selectedApt },
    actions: { fetchTrend, handleSearch, handleSync, handleDistrictChange, setSearchKeyword }
  };
};