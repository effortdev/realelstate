import React from 'react';
import styled from 'styled-components';
import GlobalStyle from './styles/GlobalStyle'; // 전역 스타일 (폰트, 배경)
import { useApartment } from './hooks/useApartment'; // 로직

// 컴포넌트들
import Header from './components/Header';
import SearchSection from './components/SearchSection';
import RecentInfo from './components/RecentInfo';
import PriceChart from './components/PriceChart';

function App() {
  const { state, actions } = useApartment(); // 로직 한 줄 컷!

  return (
    <>
      <GlobalStyle />
      <Container>
        <Header />

        <Content>
          <SearchSection state={state} actions={actions} />

          <RecentInfo latest={state.latest} selectedApt={state.selectedApt} />

          <PriceChart
            data={state.data}
            loading={state.loading}
            district={state.selectedDistrict}
            apt={state.selectedApt}
            time={state.time}
          />
        </Content>
      </Container>
    </>
  );
}

export default App;

// App.js 레이아웃용 스타일
const Container = styled.div`
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
`;

const Content = styled.div`
  width: 100%;
  max-width: 800px;
  padding: 0 20px 50px 20px;
  margin-top: 20px;
`;