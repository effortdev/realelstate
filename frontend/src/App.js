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

  // 선택 상태들
  const [selectedDistrict, setSelectedDistrict] = useState("종로구");
  const [searchKeyword, setSearchKeyword] = useState("");   // 검색어 (예: 종로)
  const [aptList, setAptList] = useState([]);             // 검색된 아파트 목록
  const [selectedApt, setSelectedApt] = useState(null);   // 최종 선택한 아파트

  // 1. 그래프 데이터 가져오기 (특정 아파트 or 전체)
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

      const chartData = response.data.data.map(item => ({
        name: `${item.dealYear}.${item.dealMonth}`,
        price: Math.round(item.averagePrice)
      }));

      setData(chartData);
      setTime(response.data.executionTime);
      setSelectedApt(aptName || "구 전체 평균"); // 제목 표시용

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
        setAptList(res.data); // 목록 채우기
        if(res.data.length === 0) alert("검색 결과가 없습니다.");
    } catch (e) {
        console.error(e);
    }
  };

  // 초기화 (맨 처음엔 전체 평균 보여줌)
  useEffect(() => {
    fetchTrend(null);
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto', fontFamily: 'Pretendard' }}>
      <h1 style={{ textAlign: 'center' }}>📊 아파트 시세 조회</h1>

      {/* 1. 컨트롤 박스 */}
      <div style={{ background: '#f8f9fa', padding: '20px', borderRadius: '10px', marginBottom: '20px' }}>

        {/* 구 선택 */}
        <div style={{ marginBottom: '10px' }}>
          <label style={{ marginRight: '10px', fontWeight: 'bold' }}>지역 선택:</label>
          <select
            value={selectedDistrict}
            onChange={(e) => {
                setSelectedDistrict(e.target.value);
                setAptList([]); // 구 바뀌면 아파트 목록 초기화
                setSearchKeyword("");
            }}
            style={{ padding: '5px', width: '150px' }}
          >
            {Object.keys(DISTRICT_MAP).map(d => <option key={d} value={d}>{d}</option>)}
          </select>
          <button onClick={() => fetchTrend(null)} style={{ marginLeft: '10px', padding: '5px 10px', cursor:'pointer' }}>
            🔄 전체 평균 보기
          </button>
        </div>

        {/* 아파트 검색 */}
        <div style={{ display: 'flex', gap: '5px' }}>
            <input
                type="text"
                placeholder="아파트 이름 (예: 자이)"
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && searchApartments()}
                style={{ flex: 1, padding: '8px' }}
            />
            <button onClick={searchApartments} style={{ padding: '8px 15px', background: '#2196f3', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                검색
            </button>
        </div>

        {/* 검색 결과 목록 (태그 형태) */}
        {aptList.length > 0 && (
            <div style={{ marginTop: '10px', borderTop: '1px solid #ddd', paddingTop: '10px' }}>
                <small style={{ color: '#666' }}>검색 결과 (클릭하면 그래프가 바뀝니다):</small>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', marginTop: '5px' }}>
                    {aptList.map((apt, idx) => (
                        <button
                            key={idx}
                            onClick={() => fetchTrend(apt)}
                            style={{
                                padding: '5px 10px',
                                border: '1px solid #2196f3',
                                background: selectedApt === apt ? '#2196f3' : 'white',
                                color: selectedApt === apt ? 'white' : '#2196f3',
                                borderRadius: '15px',
                                cursor: 'pointer',
                                fontSize: '13px'
                            }}
                        >
                            {apt}
                        </button>
                    ))}
                </div>
            </div>
        )}
      </div>

      {/* 2. 그래프 제목 & 성능 지표 */}
      <div style={{ textAlign: 'center', marginBottom: '10px' }}>
        <h2>{selectedDistrict} - {selectedApt || "전체 평균"}</h2>
        <span style={{ background: '#eee', padding: '5px 10px', borderRadius: '5px', fontSize: '14px', color: '#d32f2f' }}>
          ⚡ 조회 속도: {time || '0ms'}
        </span>
      </div>

      {/* 3. 그래프 영역 */}
      <div style={{ width: '100%', height: 400 }}>
        <ResponsiveContainer>
          <LineChart data={data}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis domain={['auto', 'auto']} />
            <Tooltip formatter={(val) => `${val.toLocaleString()}만원`} />
            <Legend />
            <Line type="monotone" dataKey="price" stroke="#8884d8" strokeWidth={3} activeDot={{ r: 8 }} />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

export default App;