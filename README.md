# 이음인사이트 (EUM Insight) 홈페이지

Spring Boot 4 + Thymeleaf + Tailwind CSS, 운영은 AWS EC2 + Nginx + RDS(MariaDB).
H크리에이터(hcreator-backend)와 같은 구조입니다.

## 구조

```
src/main/java/com/euminsight/eum
 ├─ controller/PageController.java        화면 라우팅 (/, /about, /global, /domestic, /clinical, /regulations, /contact, /privacy)
 ├─ controller/AdminDashboardController   관리자 대시보드 통계
 ├─ domain/regulation/                    국가별 최신규정 (Region enum, 게시글, 관리자 API)
 ├─ domain/inquiry/                       문의하기 (공개 접수 API, 관리자 API)
 ├─ domain/member/                        관리자 로그인 (세션)
 ├─ config/                               SecurityConfig, AdminAuthFilter, AdminSeeder, RegulationSeeder
 └─ common/ApiExceptionHandler            API 오류 → {"message": "..."}

src/main/resources
 ├─ templates/fragments/layout.html       공통 head / 헤더(+모바일 메뉴, KR/EN) / 하단 CTA / 푸터
 ├─ templates/*.html                      각 페이지
 ├─ static/css/site.css                   Tailwind 빌드 결과물 (직접 수정 X, git 커밋 O)
 ├─ static/js/site.js                     모바일 메뉴, 구글 번역 KR/EN, 탭, 문의 폼
 └─ static/admin/                         관리자 페이지 (정적 HTML + REST API)

src/main/frontend/site.css                Tailwind 원본 (공통 컴포넌트 클래스 정의)
tailwind.config.js                        브랜드 팔레트 (Neon Mint & Deep Teal)
```

## URL

| 경로 | 내용 |
|---|---|
| `/` | 메인 |
| `/about` | 회사 소개 |
| `/global` | 글로벌 규제대응 (`/global#us`, `#eu`, `#cn`, `#asean` 으로 탭 바로가기) |
| `/domestic` | 국내 서비스 |
| `/clinical` | 시험·임상 |
| `/regulations` | 국가별 최신규정 (`?region=US`, `?page=1`) |
| `/contact` | 문의하기 (`?type=GLOBAL\|DOMESTIC\|CLINICAL\|LETTER\|ETC` 로 유형 자동 선택) |
| `/admin` | 관리자 (최초 계정: 환경변수 `ADMIN_INIT_USERNAME` / `ADMIN_INIT_PASSWORD`) |

## 로컬 실행 (H2 파일 DB)

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
# http://localhost:8080  /  관리자 http://localhost:8080/admin  (admin / changeme1234)
```

## 화면 수정 시 CSS 빌드

템플릿에 **새로운 Tailwind 클래스**를 쓰면 CSS를 다시 빌드해야 반영됩니다.

```bash
npm install          # 최초 1회
npm run watch:css    # 작업 중 자동 빌드
npm run build:css    # 커밋 전 최종 빌드 (minify)
```

빌드된 `static/css/site.css`는 git에 함께 커밋합니다 (서버에서는 npm 불필요).
CSS를 바꾼 뒤에는 `layout.html`의 `site.css?v=...` 숫자를 올려 브라우저 캐시를 갱신하세요.

**아이콘**: Material Symbols는 쓰는 아이콘만 받아오도록 `layout.html`의 `icon_names=`에 목록이 있습니다.
새 아이콘을 쓰면 이 목록에 이름을 알파벳 순서로 추가해야 표시됩니다.

## KR / EN

구글 번역 위젯 방식(강서휴먼피부과와 동일). EN 클릭 시 `googtrans=/ko/en` 쿠키 저장 후 새로고침,
KR 클릭 시 쿠키 삭제. 영어 상태일 때만 번역 스크립트를 불러옵니다.
번역되면 안 되는 요소(로고, 아이콘, 약어)는 `notranslate` 클래스 + `translate="no"` 를 붙여둡니다.

## AWS 배포 (H크리에이터와 동일 절차)

1. EC2 (Amazon Linux 2023, t3.small) 생성 → Java 17, git 설치 → 저장소 clone
2. RDS MariaDB 생성 후 DB `eumdb` 생성
3. `/etc/eum.env`
   ```
   DB_HOST=xxxx.ap-northeast-2.rds.amazonaws.com
   DB_NAME=eumdb
   DB_USERNAME=...
   DB_PASSWORD=...
   ADMIN_INIT_USERNAME=admin
   ADMIN_INIT_PASSWORD=...
   ```
4. 빌드: `./gradlew clean bootJar -x test` → `build/libs/eum-0.0.1-SNAPSHOT.jar`
5. `/etc/systemd/system/eum.service`
   ```
   [Unit]
   Description=EUM Insight
   After=network.target

   [Service]
   User=ec2-user
   WorkingDirectory=/home/ec2-user/eum-backend
   EnvironmentFile=/etc/eum.env
   ExecStart=/usr/bin/java -Xms256m -Xmx768m -jar build/libs/eum-0.0.1-SNAPSHOT.jar
   Restart=always

   [Install]
   WantedBy=multi-user.target
   ```
   `sudo systemctl daemon-reload && sudo systemctl enable --now eum`
6. Nginx 80 → 8080 리버스 프록시, 보안그룹에서 8080은 닫기
7. 도메인 확정 후 DNS 연결 + certbot으로 HTTPS
