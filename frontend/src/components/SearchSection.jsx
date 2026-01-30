import React from 'react';
import styled from 'styled-components';
import { DISTRICT_MAP } from '../api/realtyApi';

const SearchSection = ({ state, actions }) => {
  return (
    <Card>
      {/* 상단 컨트롤 */}
      <TopRow>
        <SelectGroup>
          <Select value={state.selectedDistrict} onChange={actions.handleDistrictChange}>
            {Object.keys(DISTRICT_MAP).map(d => <option key={d} value={d}>{d}</option>)}
          </Select>
          <GhostButton onClick={() => actions.fetchTrend(null)}>🔄 초기화</GhostButton>
        </SelectGroup>
        <SyncButton onClick={actions.handleSync}>📥 최신 데이터 가져오기</SyncButton>
      </TopRow>

      {/* 검색창 */}
      <SearchRow>
        <Input
          value={state.searchKeyword}
          onChange={(e) => actions.setSearchKeyword(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && actions.handleSearch()}
          placeholder="아파트 이름 (예: 자이)"
        />
        <SearchButton onClick={actions.handleSearch}>검색</SearchButton>
      </SearchRow>

      {/* 결과 태그 */}
      {state.aptList.length > 0 && (
        <TagContainer>
          <Label>검색 결과:</Label>
          <Tags>
            {state.aptList.map((apt, idx) => (
              <TagButton
                key={idx}
                $active={state.selectedApt === apt}
                onClick={() => actions.fetchTrend(apt)}
              >
                {apt}
              </TagButton>
            ))}
          </Tags>
        </TagContainer>
      )}
    </Card>
  );
};
export default SearchSection;

// --- 스타일 ---
const Card = styled.div`
  background: white;
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.03);
  margin-bottom: 20px;
`;

const TopRow = styled.div`
  display: flex; justify-content: space-between; margin-bottom: 16px;
`;

const SelectGroup = styled.div` display: flex; gap: 8px; `;

const Select = styled.select`
  padding: 8px 12px; border-radius: 8px; border: 1px solid #ddd; outline: none;
`;

const GhostButton = styled.button`
  background: white; border: 1px solid #ddd; padding: 8px 12px; border-radius: 8px; cursor: pointer;
  &:hover { background: #f8f9fa; }
`;

const SyncButton = styled.button`
  background: #27ae60; color: white; border: none; padding: 8px 16px; border-radius: 8px; font-weight: bold; cursor: pointer;
  &:hover { background: #219150; }
`;

const SearchRow = styled.div` display: flex; gap: 8px; `;

const Input = styled.input`
  flex: 1; padding: 12px; border: 1px solid #ddd; border-radius: 8px; outline: none;
  &:focus { border-color: #3498db; }
`;

const SearchButton = styled.button`
  background: #3498db; color: white; border: none; padding: 0 20px; border-radius: 8px; font-weight: bold; cursor: pointer;
  &:hover { background: #2980b9; }
`;

const TagContainer = styled.div` margin-top: 16px; border-top: 1px solid #eee; padding-top: 12px; `;
const Label = styled.div` font-size: 12px; color: #888; margin-bottom: 8px; `;
const Tags = styled.div` display: flex; flex-wrap: wrap; gap: 6px; `;

const TagButton = styled.button`
  padding: 6px 12px; border-radius: 20px; cursor: pointer; font-size: 13px;
  border: 1px solid ${props => props.$active ? '#3498db' : '#eee'};
  background: ${props => props.$active ? '#3498db' : 'white'};
  color: ${props => props.$active ? 'white' : '#555'};
  &:hover { transform: translateY(-1px); }
`;