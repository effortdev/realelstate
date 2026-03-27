import React from 'react';
import styled from 'styled-components';
import GlobalStyle from './styles/GlobalStyle';
import { useApartment } from './hooks/useApartment';
import { NavermapsProvider } from 'react-naver-maps';

import Header from './components/Header';
import SearchSection from './components/SearchSection';
import RecentInfo from './components/RecentInfo';
import PriceChart from './components/PriceChart';
import MapSection from './components/MapSection';

function App() {
  const { state, actions } = useApartment();

  const NAVER_CLIENT_ID = "ni1cjf5lr2";

  return (
    <NavermapsProvider ncpClientId={NAVER_CLIENT_ID} submodules={['geocoder']}>
          <GlobalStyle />
          <Container>
            <Header />

            <Content>
              <SearchSection state={state} actions={actions} />

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


const Container = styled.div`
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: #f5f7fa;
  font-family: 'Pretendard', sans-serif;
`;

const Content = styled.div`
  width: 100%;
  max-width: 800px;
  padding: 20px;
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 20px;
`;