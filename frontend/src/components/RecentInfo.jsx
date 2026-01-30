import React from 'react';
import styled from 'styled-components';

const RecentInfo = ({ latest, selectedApt }) => {
  if (!latest || !selectedApt) return null;
  return (
    <InfoCard>
      <CardTitle>🔥 최신 실거래 정보</CardTitle>
      <Grid>
        <Item>📅 {latest.date}</Item>
        <Item>💰 {Number(latest.price.replace(/,/g, '')).toLocaleString()}만원</Item>
        <Item>🏢 {latest.floor}층</Item>
        <Item>📏 {latest.area}㎡</Item>
      </Grid>
    </InfoCard>
  );
};
export default RecentInfo;

const InfoCard = styled.div`
  background: #fff8e1; border: 1px solid #ffe0b2; padding: 20px;
  border-radius: 16px; margin-bottom: 20px;
`;
const CardTitle = styled.h3` margin: 0 0 12px 0; color: #f57c00; font-size: 16px; `;
const Grid = styled.div` display: flex; gap: 20px; flex-wrap: wrap; `;
const Item = styled.span` font-weight: 600; color: #444; font-size: 15px; `;