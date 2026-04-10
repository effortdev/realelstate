# 🏠 RealEstate Data Visualization Platform

> 공공데이터 API 기반 전국 아파트 실거래가 수집 · 분석 · 시각화 플랫폼

[![플랫폼 방문](https://img.shields.io/badge/🌐_Live_Demo-realelstate.vercel.app-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://realelstate.vercel.app/)

<br>

## 📌 프로젝트 개요

공공데이터포털의 아파트 실거래가 API를 활용하여 **약 100만 건**의 전국 부동산 거래 데이터를 수집하고,  
네이버 지도와 연동하여 사용자에게 직관적인 부동산 시각화 정보를 제공하는 데이터 분석 플랫폼입니다.

<br>

## ✨ 주요 기능

| 기능 | 설명 |
|------|------|
| 📡 데이터 수집 | 공공데이터포털 아파트 실거래가 API 자동 수집 |
| 🗺️ 지도 시각화 | 네이버 지도 API 활용 매물 위치 표시 및 클러스터링 |
| 📊 트렌드 분석 | 지역별 / 기간별 아파트 거래 트렌드 차트 제공 |

<br>

## 🛠️ Tech Stack

### Frontend
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Vercel](https://img.shields.io/badge/Vercel-000000?style=for-the-badge&logo=vercel&logoColor=white)

### Backend
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

### Database & Infra
![MySQL](https://img.shields.io/badge/MySQL_8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Azure](https://img.shields.io/badge/Azure_VM-0078D4?style=for-the-badge&logo=microsoft-azure&logoColor=white)

### CI/CD
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white)
![Docker Hub](https://img.shields.io/badge/Docker_Hub-2496ED?style=for-the-badge&logo=docker&logoColor=white)

<br>

## 🔄 CI/CD 파이프라인

```
Code Push → GitHub Actions Build → Docker Image 생성 → Docker Hub 업로드 → Azure 서버 자동 배포
```

- 로컬 개발 환경과 상용 배포 환경의 설정을 **완전 분리**하여 개발 생산성 극대화

<br>

## 🔥 트러블슈팅 & 문제 해결

> 실제 개발 과정에서 겪은 핵심 문제와 해결 방법을 기록합니다.

---

### 🚀 인프라 & 배포 최적화

**문제**
- Azure 1GB RAM 저사양 환경에서 Spring Boot 빌드 및 실행 중 **서버 멈춤 현상** 발생

**해결**
- Linux **Swap 메모리(2GB)** 설정으로 가상 메모리를 확보하여 안정적인 구동 환경 구축
- **Docker Compose** 도입으로 DB와 App을 독립된 컨테이너로 분리, 인프라 명세 코드화

**성과** ✅ 서버 중단 없는 안정적인 배포 환경 구축

---

### 🔒 Mixed Content & CORS 이슈

**문제**
- HTTPS(Vercel) 프론트엔드 → HTTP(Azure) 백엔드 API 호출 시 **브라우저 보안 정책으로 통신 차단**

**해결**
- `vercel.json`의 **Rewrites(Proxy)** 기능을 활용하여 동일 도메인 경로로 API 요청 중계
- 백엔드에 **CORS 설정** 적용 → 허용된 오리진(Vercel)만 접근 가능하도록 보안 강화

---

### 🐛 API 연동 디버깅 기록

#### 1️⃣ `401 Unauthorized` — 이중 인코딩(Double Encoding) 문제

| 항목 | 내용 |
|------|------|
| **증상** | 브라우저에서는 정상 작동하지만, 코드 실행 시 인증 실패 |
| **원인** | 공공데이터 인증키의 특수문자(`+`, `/`, `=`)를 Spring의 `RestTemplate`이 자동 인코딩하여 **이중 인코딩** 발생 (`+` → `%2B` → `%252B`) |
| **해결** | Java의 `URLEncoder`로 직접 인코딩 후 `URI` 객체에 담아 전송 → Spring의 자동 인코딩 차단 |

---

#### 2️⃣ `Cannot GET ...` — API 엔드포인트 변경

| 항목 | 내용 |
|------|------|
| **증상** | 브라우저 / 콘솔에서 경로를 찾을 수 없다는 에러 |
| **원인** | 국토교통부 서버 이전 (`openapi.molit.go.kr` → `apis.data.go.kr`) |
| **해결** | 공공데이터포털 공식 문서의 최신 URL로 교체 |

---

#### 3️⃣ `Content is not allowed in prolog` — 응답 형식 불일치

| 항목 | 내용 |
|------|------|
| **증상** | 401 통과 후 파싱 즉시 에러 발생 |
| **원인** | XML 파서(`DocumentBuilder`) 사용 중 서버가 **JSON 형식**으로 응답 |
| **해결** | URL에 `&_type=xml` 파라미터 추가하여 XML 응답 강제 지정 |

---

#### 4️⃣ `NumberFormatException: empty String` — Null 데이터 처리 미흡

| 항목 | 내용 |
|------|------|
| **증상** | 데이터 수집 중 숫자 변환 에러로 프로세스 중단 |
| **원인** | 일부 아파트 데이터의 전용면적 태그가 비어있어 빈 문자열(`""`)을 숫자로 변환 시도 |
| **해결** | `safeParse()` 메서드 구현 → 값이 비어있을 경우 예외 없이 `0`으로 대체 처리 |
