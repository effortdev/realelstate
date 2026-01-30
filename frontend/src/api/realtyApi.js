import axios from 'axios';

export const DISTRICT_MAP = {
  "종로구": "11110", "중구": "11140", "용산구": "11170", "성동구": "11200", "광진구": "11215",
  "동대문구": "11230", "중랑구": "11260", "성북구": "11290", "강북구": "11305", "도봉구": "11320",
  "노원구": "11350", "은평구": "11380", "서대문구": "11410", "마포구": "11440", "양천구": "11470",
  "강서구": "11500", "구로구": "11530", "금천구": "11545", "영등포구": "11560", "동작구": "11590",
  "관악구": "11620", "서초구": "11650", "강남구": "11680", "송파구": "11710", "강동구": "11740"
};

const BASE_URL = 'http://localhost:8081/api';

// 1. 시세 추이 및 최신 정보 조회
export const getTrendData = async (lawdCd, aptName) => {
  let url = `${BASE_URL}/trend?lawdCd=${lawdCd}`;
  if (aptName) url += `&aptName=${encodeURIComponent(aptName)}`;
  return await axios.get(url);
};

// 2. 아파트 검색
export const searchAptList = async (lawdCd, keyword) => {
  return await axios.get(`${BASE_URL}/apartments?lawdCd=${lawdCd}&keyword=${keyword}`);
};

// 3. 최신 데이터 동기화
export const syncData = async (lawdCd) => {
  return await axios.get(`${BASE_URL}/sync?lawdCd=${lawdCd}`);
};