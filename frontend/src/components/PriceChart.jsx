import React from 'react';
import styled from 'styled-components';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

// [헬퍼 함수] 금액을 '억/만원' 단위로 변환 (예: 125000 -> 12억 5,000만원)
const formatMoney = (value) => {
  if (value >= 10000) {
    const eok = Math.floor(value / 10000); // 억 단위
    const man = value % 10000;             // 만원 단위

    if (man === 0) return `${eok}억`;
    return `${eok}억 ${man.toLocaleString()}`; // 뒤에 '만원' 글자는 그래프 툴팁 공간상 생략하거나 붙여도 됨
  }
  return `${value.toLocaleString()}만`;
};

const PriceChart = ({ data, loading, district, apt, time }) => {
  return (
    <ChartCard>
      <Header>
        <Title>{district} - {apt || "전체 평균"}</Title>
        <Badge>{loading ? "로딩 중..." : `⚡ ${time || '0ms'}`}</Badge>
      </Header>

      <ChartArea>
        <ResponsiveContainer>
          <LineChart data={data} margin={{ top: 20, right: 30, left: 0, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} />

            {/* X축: 날짜 */}
            <XAxis
              dataKey="name"
              tick={{fontSize: 12, fill: '#666'}}
              tickMargin={10}
            />

            {/* Y축: '억' 단위로 변환해서 표시 */}
            <YAxis
              domain={['auto', 'auto']}
              tick={{fontSize: 12, fill: '#666'}}
              tickFormatter={(value) => `${(value / 10000).toFixed(1)}억`} // 예: 15.5억
              width={60} // Y축 글자 잘리지 않게 너비 확보
            />

            {/* 툴팁: 마우스 올렸을 때 상세 가격 표시 */}
            <Tooltip
              contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }}
              formatter={(value) => [formatMoney(value) + '원', '평균 거래가']}
              labelStyle={{ color: '#888', marginBottom: '5px' }}
            />

            <Legend verticalAlign="top" height={36}/>

            <Line
              type="monotone"
              dataKey="price"
              name="평균 거래가"
              stroke="#8884d8"
              strokeWidth={3}
              dot={{ r: 4, strokeWidth: 2 }}
              activeDot={{ r: 7 }}
              animationDuration={1000}
            />
          </LineChart>
        </ResponsiveContainer>
      </ChartArea>
    </ChartCard>
  );
};

export default PriceChart;

// --- 스타일 (기존과 동일하지만 차트 영역 높이 등을 미세 조정) ---
const ChartCard = styled.div`
  background: white; padding: 24px; border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.03);
  margin-bottom: 50px; /* 하단 여백 추가 */
`;
const Header = styled.div` text-align: center; margin-bottom: 24px; `;
const Title = styled.h2` margin: 0 0 8px 0; font-size: 1.3rem; color: #333; font-weight: 800; `;
const Badge = styled.span`
  background: #e3f2fd; color: #1976d2; padding: 4px 10px; border-radius: 12px;
  font-size: 12px; font-weight: bold; letter-spacing: -0.5px;
`;
const ChartArea = styled.div` width: 100%; height: 400px; `;