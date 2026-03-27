import React from 'react';
import styled from 'styled-components';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const formatMoney = (value) => {
  if (value >= 10000) {
    const eok = Math.floor(value / 10000);
    const man = value % 10000;
    if (man === 0) return `${eok}억`;
    return `${eok}억 ${man.toLocaleString()}`;
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
        {!loading && data && data.length > 0 ? (
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={data} margin={{ top: 20, right: 30, left: 0, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="name" tick={{fontSize: 12, fill: '#666'}} tickMargin={10} />
              <YAxis
                domain={['auto', 'auto']}
                tick={{fontSize: 12, fill: '#666'}}
                tickFormatter={(value) => `${(value / 10000).toFixed(1)}억`}
                width={60}
              />
              <Tooltip
                contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }}
                formatter={(value) => [formatMoney(value) + '원', '평균 거래가']}
                labelStyle={{ color: '#888', marginBottom: '5px' }}
              />
              <Legend verticalAlign="top" height={36}/>
              <Line type="monotone" dataKey="price" name="평균 거래가" stroke="#8884d8" strokeWidth={3} dot={{ r: 4 }} activeDot={{ r: 7 }} />
            </LineChart>
          </ResponsiveContainer>
        ) : (
          <EmptyState>
            {loading ? "데이터를 불러오는 중입니다..." : "거래 데이터가 없습니다."}
          </EmptyState>
        )}
      </ChartArea>
    </ChartCard>
  );
};

export default PriceChart;

const ChartCard = styled.div`
  background: white; padding: 24px; border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.03); margin-bottom: 50px;
`;
const Header = styled.div` text-align: center; margin-bottom: 24px; `;
const Title = styled.h2` margin: 0 0 8px 0; font-size: 1.3rem; color: #333; font-weight: 800; `;
const Badge = styled.span`
  background: #e3f2fd; color: #1976d2; padding: 4px 10px; border-radius: 12px;
  font-size: 12px; font-weight: bold;
`;

const ChartArea = styled.div`
  width: 100%;
  height: 400px; /* 고정 높이 필수 */
  position: relative;
`;

const EmptyState = styled.div`
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 14px;
`;