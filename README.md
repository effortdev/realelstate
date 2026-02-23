🏠 RealEstate Data Visualization Project
이 프로젝트는 공공데이터 API를 활용하여 약 100만 건의 전국 아파트 실거래가 데이터를 수집하고, 이를 네이버 지도와 연동하여 시각화하는 부동산 데이터 분석 플랫폼입니다.

1. 프로젝트 개요
- 목적: 대용량 부동산 데이터를 효율적으로 처리하고 사용자에게 직관적인 시각화 정보 제공

- 주요 기능:

공공데이터포털 아파트 실거래가 API 연동 및 자동 수집

네이버 지도 API를 활용한 매물 위치 표시 및 클러스터링

지역별/기간별 아파트 거래 트렌드 분석 차트

2. Tech Stack
- Frontend: React, Vercel

- Backend: Spring Boot, Spring Data JPA

- Database: MySQL 8.0 (Docker)

- Infra: Azure (VM), Docker, Docker Compose

- CI/CD: GitHub Actions, Docker Hub

트러블슈팅 및 해결 (핵심 역량)

🚀 인프라 및 배포 최적화
문제: Azure 1GB RAM의 낮은 사양으로 인해 스프링 부트 빌드 및 실행 중 서버 멈춤 현상 발생

해결:

- Linux Swap 메모리(2GB) 설정을 통해 가상 메모리 확보, 저사양 환경에서의 안정적인 구동 환경 구축

- Docker Compose를 도입하여 DB와 App의 독립된 환경 구축 및 인프라 명세 코드화

- 성과: 서버 중단 없이 안정적인 배포 환경 구축

🔒 Mixed Content 및 CORS 이슈 해결
문제: HTTPS(Vercel) 프론트엔드에서 HTTP(Azure) 백엔드 API 호출 시 브라우저 보안 정책으로 인한 통신 차단 발생

해결:

- vercel.json의 Rewrites(Proxy) 기능을 활용하여 프론트엔드와 동일 도메인 경로로 API 요청을 중계하도록 설정

- 백엔드에 CORS 설정을 적용하여 허용된 오리진(Vercel)만 접근 가능하도록 보안 강화

⚡ 대용량 데이터 처리 및 성능 최적화 (100만 건 규모)
문제:

데이터 수집 시 중복 체크용 SELECT 쿼리가 누적될수록 속도가 저하되는 현상

수집 중 무거운 집계(AVG, GROUP BY) 쿼리 동시 발생 시 CPU 부하 급증

해결:

- DB Index 최적화: lawd_cd, apartment_name 등 주요 조회 조건에 인덱스를 생성하여 조회 성능 100배 이상 향상

- Port Mapping 전략: 내부 로직과 외부 노출 포트의 엄격한 분리(8080:8081)로 보안성 및 네트워크 구조 명확화

- Batch processing: 대용량 데이터를 처리하기 위해 메모리 사용량을 모니터링하며 수집 로직 최적화 중

4. 자동화 파이프라인 (CI/CD)
   GitHub Actions를 활용하여 코드 Push 시 빌드 → Docker Image 생성 → Docker Hub 업로드 → Azure 서버 자동 배포 단계를 자동화함

로컬 개발 환경과 상용 배포 환경의 설정을 분리하여 개발 생산성 증대




401 Unauthorized (가장 지독했던 녀석)
   증상: 브라우저에선 되는데, 코드만 돌리면 "인증 안 됨(Unauthorized)" 에러 발생.

원인: '이중 인코딩(Double Encoding)' 문제.

공공데이터 인증키에는 +, /, = 같은 특수문자가 들어있습니다.

스프링(RestTemplate)이 "어? 특수문자네? 내가 안전하게 바꿔줄게!"라며 +를 %2B로, 그걸 또 %252B로 두 번 바꿔버려서 서버가 "틀린 키"라고 인식했습니다.

해결:

스프링에게 맡기지 않고, 자바의 URLEncoder로 우리가 직접 인코딩했습니다.

그리고 URI 객체에 담아서 보내 "이건 내가 검수 끝낸 주소니까 건드리지 마!"라고 명령했습니다.

2. Cannot GET ...
   증상: 브라우저나 콘솔에서 경로를 찾을 수 없다는 에러.

원인: '구버전 주소(Endpoint)' 사용.

블로그나 예전 자료에 있는 openapi.molit.go.kr 주소를 사용했는데, 국토교통부가 최근 apis.data.go.kr로 서버를 이사했습니다.

해결: 공공데이터포털 공식 문서에 있는 최신 URL로 교체했습니다.

3. Content is not allowed in prolog
   증상: 401은 통과했는데, 파싱(Parsing) 시작하자마자 에러 발생.

원인: '데이터 형식 불일치 (JSON vs XML)'.

우리는 자바의 DocumentBuilder(XML 분석기)를 준비했는데, 서버는 요즘 유행하는 JSON 데이터({ "response": ... })를 보내줬습니다.

XML 분석기가 { 괄호를 보고 "이건 XML 형식이 아니야!"라고 뻗어버린 겁니다.

해결: URL 뒤에 **&_type=xml**을 붙여서 "무조건 XML로 줘!"라고 강제했습니다.

4. java.lang.NumberFormatException: empty String
   증상: 데이터가 잘 들어오다가 갑자기 숫자 변환 에러로 멈춤.

원인: '빈 데이터(Null Data)' 처리 미흡.

모든 아파트가 정보가 꽉 차 있진 않습니다. 어떤 데이터는 전용면적 태그가 비어있었죠 (<excluArea/>).

자바는 빈 문자열 ""을 숫자로 바꾸려 하면 에러를 냅니다.

해결: safeParse 메서드를 만들어서, 값이 비어있으면 에러를 내지 않고 0으로 채워 넣도록 안전장치를 걸었습니다.