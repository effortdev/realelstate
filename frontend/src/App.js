import React, { useState, useEffect } from 'react';
import axios from 'axios';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from 'recharts';

const DISTRICT_MAP = {
  "종로구": "11110", "중구": "11140", "용산구": "11170", "성동구": "11200", "광진구": "11215",
  "동대문구": "11230", "중랑구": "11260", "성북구": "11290", "강북구": "11305", "도봉구": "11320",
  "노원구": "11350", "은평구": "11380", "서대문구": "11410", "마포구": "11440", "양천구": "11470",
  "강서구": "11500", "구로구": "11530", "금천구": "11545", "영등포구": "11560", "동작구": "11590",
  "관악구": "11620", "서초구": "11650", "강남구": "11680", "송파구": "11710", "강동구": "11740"
};

function App() {
  const [data, setData] = useState([]);
  const [time, setTime] = useState(null);
  const [loading, setLoading] = useState(false);

  // [신규] 최신 거래 정보를 담을 그릇
  const [latest, setLatest] = useState(null);

  // 선택 상태들
  const [selectedDistrict, setSelectedDistrict] = useState("종로구");
  const [searchKeyword, setSearchKeyword] = useState("");
  const [aptList, setAptList] = useState([]);
  const [selectedApt, setSelectedApt] = useState(null);

  // 1. 그래프 및 최신 정보 가져오기
  const fetchTrend = async (aptName) => {
    const lawdCd = DISTRICT_MAP[selectedDistrict];
    let url = `http://localhost:8081/api/trend?lawdCd=${lawdCd}`;

    // 아파트 이름이 있으면 URL 뒤에 붙임
    if (aptName) {
      url += `&aptName=${encodeURIComponent(aptName)}`;
    }

    try {
      setLoading(true);
      const response = await axios.get(url);

      // 그래프 데이터 가공
      const chartData = response.data.data.map(item => ({
        name: `${item.dealYear}.${item.dealMonth}`,
        price: Math.round(item.averagePrice)
      }));

      setData(chartData);
      setTime(response.data.executionTime);

      // [신규] 최신 거래 정보 저장 (없으면 null이 들어감)
      setLatest(response.data.latest);

      setSelectedApt(aptName || null); // 아파트 이름 저장 (전체보기면 null)

    } catch (error) {
      console.error("Error:", error);
      alert("데이터 조회 실패");
    } finally {
      setLoading(false);
    }
  };

  // 2. 아파트 이름 검색하기
  const searchApartments = async () => {
    if (!searchKeyword) {
        alert("아파트 이름을 입력해주세요!");
        return;
    }
    const lawdCd = DISTRICT_MAP[selectedDistrict];
    try {
        const res = await axios.get(`http://localhost:8081/api/apartments?lawdCd=${lawdCd}&keyword=${searchKeyword}`);
        setAptList(res.data);
        if(res.data.length === 0) alert("검색 결과가 없습니다.");
    } catch (e) {
        console.error(e);
    }
  };

  // [신규] 3. 국토부 최신 데이터 동기화 함수
  const syncLatestData = async () => {
    if (!window.confirm(`국토부 서버에서 [${selectedDistrict}] 최신 데이터를 확인하시겠습니까?\n(시간이 조금 걸릴 수 있습니다)`)) return;

    const lawdCd = DISTRICT_MAP[selectedDistrict];

    try {
        setLoading(true);
        // 동기화 API 호출
        const res = await axios.get(`http://localhost:8081/api/sync?lawdCd=${lawdCd}`);

        alert(`동기화 완료! ${res.data.addedCount}건의 최신 거래가 추가되었습니다.`);

        // 그래프 새로고침 (현재 보고 있는 아파트 혹은 전체 기준으로 다시 로딩)
        fetchTrend(selectedApt);

    } catch (e) {
        console.error(e);
        alert("동기화 중 오류가 발생했습니다.");
    } finally {
        setLoading(false);
    }
  };

  // 초기화
  useEffect(() => {
    fetchTrend(null);
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto', fontFamily: 'Pretendard' }}>
      <h1 style={{ textAlign: 'center' }}>📊 아파트 시세 조회</h1>

      {/* 1. 컨트롤 박스 */}
      <div style={{ background: '#f8f9fa', padding: '20px', borderRadius: '10px', marginBottom: '20px', border: '1px solid #ddd' }}>

        {/* 구 선택 & 전체보기 & 동기화 버튼 */}
        <div style={{ marginBottom: '15px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <label style={{ marginRight: '10px', fontWeight: 'bold' }}>지역 선택:</label>
            <select
              value={selectedDistrict}
              onChange={(e) => {
                  setSelectedDistrict(e.target.value);
                  setAptList([]);
                  setSearchKeyword("");
                  // 구가 바뀌면 전체 평균으로 리셋하는게 자연스러움
                  setTimeout(() => fetchTrend(null), 0);
              }}
              style={{ padding: '5px', width: '120px' }}
            >
              {Object.keys(DISTRICT_MAP).map(d => <option key={d} value={d}>{d}</option>)}
            </select>
            <button onClick={() => fetchTrend(null)} style={{ marginLeft: '10px', padding: '5px 10px', cursor:'pointer' }}>
              🔄 전체 평균 보기
            </button>
          </div>

          {/* [신규] 동기화 버튼 (오른쪽 배치) */}
          <button
            onClick={syncLatestData}
            style={{
                background: '#4caf50', color: 'white', border: 'none',
                padding: '8px 15px', borderRadius: '5px', cursor: 'pointer',
                fontSize: '13px', fontWeight: 'bold'
            }}
          >
            📥 최신 데이터 가져오기
          </button>
        </div>

        {/* 아파트 검색 Input */}
        <div style={{ display: 'flex', gap: '5px' }}>
            <input
                type="text"
                placeholder="아파트 이름 (예: 자이)"
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && searchApartments()}
                style={{ flex: 1, padding: '10px', borderRadius: '5px', border: '1px solid #ccc' }}
            />
            <button onClick={searchApartments} style={{ padding: '10px 20px', background: '#2196f3', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer', fontWeight: 'bold' }}>
                검색
            </button>
        </div>

        {/* 검색 결과 목록 */}
        {aptList.length > 0 && (
            <div style={{ marginTop: '15px', borderTop: '1px solid #ddd', paddingTop: '10px' }}>
                <small style={{ color: '#666', display: 'block', marginBottom: '5px' }}>검색 결과 (클릭하여 조회):</small>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                    {aptList.map((apt, idx) => (
                        <button
                            key={idx}
                            onClick={() => fetchTrend(apt)}
                            style={{
                                padding: '6px 12px',
                                border: '1px solid #2196f3',
                                background: selectedApt === apt ? '#2196f3' : 'white',
                                color: selectedApt === apt ? 'white' : '#2196f3',
                                borderRadius: '20px',
                                cursor: 'pointer',
                                fontSize: '13px',
                                transition: 'all 0.2s'
                            }}
                        >
                            {apt}
                        </button>
                    ))}
                </div>
            </div>
        )}
      </div>

      {/* [신규] 최신 거래 정보 카드 (아파트가 선택되었고, 정보가 있을 때만 보임) */}
      {selectedApt && latest && (
        <div style={{
          background: '#fff3e0', border: '1px solid #ffe0b2',
          borderRadius: '10px', padding: '15px 20px', marginBottom: '20px',
          boxShadow: '0 2px 5px rgba(0,0,0,0.05)', textAlign: 'left'
        }}>
          <h3 style={{ margin: '0 0 10px 0', color: '#e65100', fontSize: '18px' }}>🔥 최신 실거래 정보</h3>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '20px', fontSize: '16px', color: '#333' }}>
            <span>📅 날짜: <strong>{latest.date}</strong></span>
            <span>💰 가격: <strong>{Number(latest.price.replace(/,/g, '')).toLocaleString()}만원</strong></span>
            <span>🏢 층수: <strong>{latest.floor}층</strong></span>
            <span>📏 면적: <strong>{latest.area}㎡</strong></span>
          </div>
        </div>
      )}

      {/* 2. 그래프 제목 & 성능 지표 */}
      <div style={{ textAlign: 'center', marginBottom: '10px' }}>
        <h2 style={{margin: '10px 0'}}>{selectedDistrict} - {selectedApt || "전체 평균"}</h2>
        <span style={{ background: '#e3f2fd', padding: '5px 12px', borderRadius: '15px', fontSize: '13px', color: '#1565c0', fontWeight: 'bold' }}>
          {loading ? "데이터 로딩 중..." : `⚡ DB 조회 속도: ${time || '0ms'}`}
        </span>
      </div>

      {/* 3. 그래프 영역 */}
      <div style={{ width: '100%', height: 400, background: 'white', padding: '10px', borderRadius: '10px', boxShadow: '0 2px 10px rgba(0,0,0,0.05)' }}>
        <ResponsiveContainer>
          <LineChart data={data} margin={{ top: 10, right: 30, left: 10, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} />
            <XAxis dataKey="name" tick={{fontSize: 12}} />
            <YAxis domain={['auto', 'auto']} tick={{fontSize: 12}} tickFormatter={(val) => `${val/10000}억`} />
            <Tooltip
                contentStyle={{ borderRadius: '10px', border: 'none', boxShadow: '0 2px 10px rgba(0,0,0,0.1)' }}
                formatter={(value) => [`${value.toLocaleString()}만원`, '평균 거래가']}
            />
            <Legend />
            <Line
                type="monotone"
                dataKey="price"
                name="평균 거래가"
                stroke="#8884d8"
                strokeWidth={3}
                dot={{ r: 4 }}
                activeDot={{ r: 8 }}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

export default App;