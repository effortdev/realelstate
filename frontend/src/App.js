import React from 'react';
import styled from 'styled-components';
import GlobalStyle from './styles/GlobalStyle'; // 전역 스타일
import { useApartment } from './hooks/useApartment';
import { NavermapsProvider } from 'react-naver-maps';

// 컴포넌트들
import Header from './components/Header';
import SearchSection from './components/SearchSection';
import RecentInfo from './components/RecentInfo';
import PriceChart from './components/PriceChart';
import MapSection from './components/MapSection';

function App() {
  const { state, actions } = useApartment();

  // 네이버 클라우드에서 복사한 Client ID (대소문자 정확해야 함)
  const NAVER_CLIENT_ID = "ni1cjf5lr2";

  return (
    <NavermapsProvider ncpClientId={NAVER_CLIENT_ID} submodules={['geocoder']}>
          <GlobalStyle />
          <Container>
            <Header />

            <Content>
              <SearchSection state={state} actions={actions} />

              {/* 👇 지도 섹션에 'selectedApt'(아파트 이름)도 같이 넘겨줍니다 */}
              <MapSection
                  district={state.selectedDistrict}
                  aptName={state.selectedApt}
                  dong={state.latest ? state.latest.dong : null}
                />

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
        </NavermapsProvider>
  );
}

export default App;

// --- 스타일 정의 (이제 진짜 사용됩니다!) ---

// 전체 레이아웃 (배경색, 중앙 정렬)
const Container = styled.div`
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: #f5f7fa; /* 배경색 */
  font-family: 'Pretendard', sans-serif;
`;

// 내용물 영역 (최대 너비 제한, 여백)
const Content = styled.div`
  width: 100%;
  max-width: 800px;
  padding: 20px;
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 20px; /* 컴포넌트 사이 간격 자동 벌리기 */
`;