import React, { useState, useEffect } from 'react';
import axios from 'axios';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from 'recharts';

function App() {
  const [data, setData] = useState([]);
  const [time, setTime] = useState(null);
  const [loading, setLoading] = useState(false);

  // 데이터 가져오기 (종로구: 11110)
  const fetchData = async () => {
    try {
      setLoading(true);
      // ★ 중요: 백엔드 서버가 켜져 있어야 합니다!
      const response = await axios.get('http://localhost:8081/api/trend?lawdCd=11110');

      const chartData = response.data.data.map(item => ({
        name: `${item.dealYear}.${item.dealMonth}`,
        price: Math.round(item.averagePrice)
      }));

      setData(chartData);
      setTime(response.data.executionTime);

    } catch (error) {
      console.error("Error:", error);
      alert("백엔드 서버가 켜져 있는지 확인해주세요!\n(http://localhost:8081)");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  return (
    <div style={{ padding: '20px', textAlign: 'center' }}>
      <h1>📊 서울 종로구 아파트 시세 추이</h1>

      {/* 속도 측정 배지 */}
      <div style={{
        background: '#e3f2fd', padding: '15px', display: 'inline-block',
        borderRadius: '10px', marginBottom: '20px', border: '1px solid #90caf9'
      }}>
        {loading ? <span>로딩 중...</span> :
          <h3 style={{ margin: 0, color: '#1565c0' }}>⚡ DB 조회 속도: {time}</h3>
        }
      </div>

      <div style={{ width: '100%', height: 400 }}>
        <ResponsiveContainer>
          <LineChart data={data} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis domain={['auto', 'auto']} />
            <Tooltip formatter={(value) => `${value.toLocaleString()}만원`} />
            <Legend />
            <Line type="monotone" dataKey="price" name="평균 거래가" stroke="#8884d8" strokeWidth={3} />
          </LineChart>
        </ResponsiveContainer>
      </div>

      <button onClick={fetchData} style={{ marginTop: '20px', padding: '10px 20px', cursor: 'pointer' }}>
        🔄 속도 다시 측정하기
      </button>
    </div>
  );
}

export default App;