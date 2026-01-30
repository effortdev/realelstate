import React from 'react';
import styled from 'styled-components';

const Header = () => (
  <HeaderWrapper>
    <Title>📊 아파트 시세 조회</Title>
  </HeaderWrapper>
);

export default Header;

const HeaderWrapper = styled.header`
  width: 100%;
  height: 60px;
  background: white;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  position: sticky;
  top: 0;
  z-index: 100;
`;

const Title = styled.h1`
  font-size: 1.2rem;
  font-weight: 800;
  color: #2c3e50;
  margin: 0;
`;