## 개발 환경 실행하기 로컬(MYSQL, REDIS), 토스결제API, S3
1. git clone
2. application-secret.yml.default -> 2. application-secret.yml 파일명 변경/환경변수 세팅
3. 직접 데이터베이스 생성 CREATE DATABASE ${DATABASE_NAME}; USE ${DATABASE_NAME};
4. 레디스 실행
5. 실행
   
## 👖 TrendPick, 취향에 맞게 상품을 추천 해주는 서비스 
<div align="center">
<p align="center">
<img src="https://github.com/TrandPick/TrendPick_Pro/assets/62290451/0ecc6545-4151-472c-9121-f531a4de69ae" width="700" height="400"/>


자신만의 스타일, 선호하는 스타일로 태그 기반 상품을 추천하는 서비스 제공합니다.  

트렌드한 태그들, 상품 추천 **Trend Pick**
</p>
</div>
<br>



## 👩‍💻 기획 **계기**

옷을 고르는 일은 즐겁지만 때때로 까다롭고 시간이 많이 소요될 수 있습니다. 수많은 선택지 중에서 마음에 드는 한 두 벌의 옷을 찾아내는 것은 실제로 상당히 어려운 일입니다.

그럼에도 불구하고, 이미 우리가 좋아하는 스타일이나 코디가 명확하다면, 이러한 과정은 훨씬 더 빠르고 쉽게 진행될 수 있습니다. 이런 생각에서 출발하여, 사용자가 원하는 스타일과 유사한 제품을 추천해주는 패션 거래 시스템을 기획하게 되었습니다.

이렇게 하면, 사용자는 수많은 제품 중에서 직접 골라야 하는 불편함을 줄일 수 있으며, 각자의 개인적인 스타일에 맞는 아이템을 더욱 빠르게 찾아낼 수 있을 것입니다.

<br>


## 💡 설명
<p align="center">

<img src="https://github.com/TrandPick/TrendPick_Pro/assets/62290451/63a4c96c-1aa0-45fa-8ecf-273187a34567" width="700" height="400"/>

**트렌드픽**은 사용자 가입 시 지정한 선호 태그를 활용하여 맞춤형 상품 추천을 제공합니다. 이런 방식은 사용자에게 보다 효율적이고 개인화된 쇼핑 경험을 제공하는 데 초점을 맞추고 있습니다.

사용자가 선호하는 스타일과 코디에 기반하여, 우리의 서비스는 그들에게 가장 적합한 상품들을 추천합니다. 이를 통해 고객은 어떤 상품을 선택하고 구매할지에 대해 더욱 신중하고 효과적으로 결정할 수 있습니다. 이러한 방식은 고객의 쇼핑 경험을 더욱 즐겁고 만족스럽게 만들 것입니다.
</p>
<br>


## 🛠️ 사용 기술 스택

### 프론트엔드

HTML5, CSS3, JavaScript, Thymeleaf

### 백엔드

Java17, Gradle, SpringBoot 3.x. Spring Data JPA, MYSQL, Spring Security, Spring Data Redis, Spring Batch

### 테스트

Apache JMeter, JUnit5, Mockito

### 인프라

Naver Clound Platform - Server(EC2), Object Storage(S3), Cloud DB for MYSQL(RDS), 

Docker, Github Action

### 협업 툴

Git, Discord, Notion

<br>
<br>


## 📝 설계내용

### 요구 사항 도출 과정

1. **`문제 이해`**: 처음으로 문제를 정의하고 이해하는 단계입니다. 패션 이커머스 시스템을 개발하려는 목표를 설정하고 그에 따른 대략적인 요구사항을 도출합니다.
2. **`사용자와의 상호작용`**: 다음으로 사용자의 입장에서 시스템을 어떻게 사용할지 고민합니다. 사용자의 필요한 기능을 파악하고 이를 시스템의 요구사항으로 변환합니다.
3. **`세부 요구사항 도출`**: 각 요구사항의 세부사항을 정의합니다. 예를 들어, 상품에 대한 요구사항을 정의하면서 상품 리스트, 상품 검색, 상품 상세페이지 등의 세부 요구사항을 도출합니다.
4. **`요구사항 검토 및 확정`**: 도출한 요구사항을 검토하고 수정, 보완한 뒤 최종적으로 확정합니다. 이 과정에서 각 요구사항이 시스템의 목표와 잘 부합하는지, 사용자의 요구를 충족시키는지 등을 검토합니다.
5. **`요구사항 문서화`**: 마지막으로 도출한 요구사항을 문서화합니다. 이 문서는 개발 과정에서 참조할 수 있도록 구체적이고 명확해야 합니다.
<br>

<details>
<summary>요구사항 명세서</summary>
<div markdown="1">       

| RQ-ID | 화면 명 | 요구사항 명 | 요구사항 내용 | 날짜 | 진행사항 | 버전명 | 우선순위 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RQ-0001 | 공통 | 회원 가입 | 회원을 등록한다.  관리자/판매자/고객 |  | 미반영 | 0.0.1 | 1 |
| RQ-0002 | 공통 | 로그인 | 회원이 로그인을 한다. |  | 미반영 | 0.0.1 | 1 |
| RQ-0003 | 관리자 | 상품 등록 | 판매자가 상품을 등록한다. <br>사진, 상품 명, 단가, 상세페이지, 태그가 포함된다. |  | 미반영 | 0.0.1 | 1 |
| RQ-0004 | 관리자 | 상품 내용 변경 | 판매자가 상품의 내용을 수정한다. |  | 미반영 | 0.0.1 | 2 |
| RQ-0005 | 관리자 | 상품 삭제 | 판매자가 상품을 삭제한다. |  | 미반영 | 0.0.1 | 3 |
| RQ-0006 | 메인 | 상품 진열 | 등록된 전체 상품을 보여준다<br>태그 기반으로 추천된 상품의 리스트를 표시한다. (페이징) |  | 미반영 | 0.0.1 | 1 |
| RQ-0007 | 상품페이지 | 상품 상세 |  등록된 상품의 내용을 보여준다. (상세페이지 포함)  |  | 미반영 | 0.0.1 | 1 |
|  | 상품페이지 | 주문 및 장바구니 | 상품 옵션 선택 및 즉시 주문/ 장바구니  |  |  |  | 1 |
|  | 상품페이지 | 리뷰 및 평점 | 하단에 해당 상품의 후기를 작성하고 볼 수 있도록 한다 |  |  |  | 2 |
|  | 상품페이지 | Q&A | - Q&A를 남길 수 있도록 한다. (브랜드 Q&A) |  |  |  | 3 |
| RQ-0008 | 메인 | 검색 | 검색창을 통해 검색을 하면 해당하는 상품을 보여준다 |  | 미반영 | 0.0.1 | 2 |
|  | 메인 | 정렬 | 최신순, 오래된순, 평점높은순, 평점낮은순, 구매높은순 정렬 기준을 선택하면 상품이 정렬되어 리스팅된다.<br>카테고리를 선택하면 해당 카테고리에 포함된 상품만 리스팅된다. (대→소) |  |  |  | 2 |
| RQ-0009 | 메인  | 상품 재고 | 상품 진열창이나, 상세 페이지에서 어느정도 미만의 재고가 남았을시, 재고를 표시한다. |  | 미반영 | 0.0.1 | 3 |
| RQ-0010 | 장바구니 | 상품 목록 | 장바구니에 추가한 상품들의 수량과 단가가 표시된다.<br>하단에 전체 주문 금액과 결제 버튼이 표시된다. |  | 미반영 | 0.0.1 | 1 |
| RQ-0011 | 장바구니 | 상품 주문 | 장바구니에서 상품을 주문한다. (일부선택가능)<br>기본으로 전체 선택 되어 있게 한다 |  | 미반영 | 0.0.1 | 1 |
| RQ-0012 | 결제 | 성공 | 주문시에 추가한 상품들의 결제가 성공한다.<br>토스 페이먼츠를 활용한다.<br>필요한 데이터: 주문자, 주문 상품명, 지불 방법, 지불 금액<br>결제가 성공한 상품들의 재고가 줄어든다.<br>주문 태그가 포함된 상품들의 추천 점수가 올라간다.                         |  | 미반영 | 0.0.1 | 1 |
| RQ-0013 | 주문 | 주문취소 | 고객이 주문을 취소한다.<br>필요한 데이터: 결제번호, 취소 사유<br>결제를 취소하고 환불을 진행한다.<br>결제가 취소된 상품들의 재고가 추가된다.
  |  | 미반영 | 0.0.1 | 1 |
| RQ-0014 | 마이페이지 | 주문 내역 | 주문 내역이 보여진다.<br>진행 중인 주문의 경우 배송 상태가 보여진다<br>주문 취소가 가능한 단계인 상품의 경우 주문 취소가 가능하다r |  | 미반영 | 0.0.1 | 1 |
| RQ-0015 | 마이페이지 - 주문 내역 | 배송상태 | 배송 상태를 보여준다. |  | 미반영 | 0.0.1 | 3 |
| RQ-0016 | 마이페이지 - 주문 내역 | 환불 | 주문 내역에 대해 일정기간 내에 환불을 할 수 있어야 한다.<br>주문 내역 하단에 환불 버튼이 있다.<br>관리자에게 환불 요청 내역이 전달된다 |  | 미반영 | 0.0.1 | 1 |
| RQ-0017 | 마이페이지 | 개인 정보 수정 | 회원은 개인정보를 변경할 수 있다.<br>배송지를 관리할 수 있다.<br>비밀번호를 변경할 수 있다.<br>환불계좌를 설정할 수 있다. |  | 미반영 | 0.0.1 | 2 |
| RQ-0018 | 고객센터(트렌드픽 관리자) | 게시판 | 관리자는 질문이 많은 주제에 대해 FAQ를 정리하여 보여준다. |  | 미반영 | 0.0.1 | 3 |
| RQ-0019 | 고객센터(트렌드픽 관리자) | 게시판 | 관리자는 고객센터 게시판에 적힌 질문에 대한 답변을 빠르게 작성해서 보여준다. |  | 미반영 | 0.0.1 | 2 |
| RQ-0020 | 고객센터 | 게시판 | 회원은 게시판의 사이트에 관련한 질문을 남길 수 있다.<br>질문이 게시된 날짜가 포함되어야 한다. |  | 미반영 | 0.0.1 | 2 |
| RQ-0021 | 고객센터 | 게시판 | 회원은 본인이 남긴 질문의 답변을 확인 한다. |  | 미반영 | 0.0.1 | 2 |
| RQ-0022 | 메인 | 알림 | 회원은 본인의 주문의 대한 알림, 사이트의 대한 알림을 받는다. |  | 미반영 | 0.01 | 3 |
</div>
</details>

<details>
<summary>API 명세서</summary>
<div markdown="1">

[API명세서 보러가기](https://www.notion.so/API-ceb28ac33799464f8983486c33d9cb5d)

</div>
</details>

<details>
<summary>Git 컨벤션</summary>
<div markdown="1">    
  
### Git-Flow 전략을 사용

main / development / feat, refactor, …/이슈번호_기능명

development 브랜치에서 실제 개발을 완료하여, 최종 배포코드를 main에 Pull Request

<br>

### 브랜치 컨벤션

메소드 별로 issue생성

브랜치 명 : **feat, refactor, …/issue번호_issue명**

PR 생성 명 : **[Feat, Refactor, …/issue번호] 이슈 내용**

<br>

### Commit 컨벤션

**Commit 메세지 구조**

```java
Message Type : Subject // 작업 내용을 간단하게 요약해서 기술합니다.

Body // 왜, 무엇을 변경하였는지 기술합니다. 작성하지 않아도 괜찮습니다.

Footer // issue tracker를 사용하는 경우 참조한 issue tracker ID를 기술합니다. 작성하지 않아도 됩니다.
```

</div>
</details>

<details>
<summary>코딩 컨벤션</summary>
<div markdown="1">       

**개별 패키지 구조**

도메인 → controller, service, repository, (domain→ entity, dto), exception
<br>

**인텔리제이 네이버 코딩 컨벤션**

[캠퍼스 핵데이 Java 코딩 컨벤션](https://naver.github.io/hackday-conventions-java/)

</div>
</details>

<details>
<summary>그라운드룰</summary>
<div markdown="1">       
<br>
💡 1. 사소한 것이라도 말을 하자!<br><br>
💡 2. 나만 아는 지식이 없도록 하자!<br><br>
💡 3. 어떠한 것을 하고자 할 때는 동료들에게 근거를 제시하자!<br><br>
💡 4. 지각하지 말자!<br><br>
💡 5. 타인의 발언을 존중하자!<br><br>
💡 6. 시간은 모두에게나 중요하다. 약속에 늦지말자!<br><br>
💡 7. 개발중인 부분을 모두 공유하자!

</div>
</details>

<br>

### ERD 설계도

![image](https://github.com/TrandPick/TrendPick_Pro/assets/62290451/555f5b17-9a28-455a-a9db-08536e8e5454)

<br>


## ✅ 기능요약

- 회원가입/로그인
- 핵심 기능: **선호태그 기반 상품 추천**
- 상품 검색
- 상품 메인/서브 카테고리
- 전체 관리자일 경우
    - 출금 승인/취소
- 브랜드 관리자일 경우
    - 상품 등록, 수정, 삭제/ 할인 적용/ 쿠폰 발급 / 정산 데이터 생성/ 출금 신청
- 일반 유저일 경우
    - 구매 / 리뷰 작성 / 개인 회원 정보 관리
    

<br>


## ⭕ 부하테스트 및 개선

 쇼핑몰은 이벤트나 타임 세일 등 어떤 기간 동안 갑자기 사용자가 몰리는 경우, 최대 몇 명의 사용자들이 서버를 사용할 수 있는지 상황을 확인하고 대비하고자 부하 테스트를 진행하여 로직을 개선해보고자 했습니다. 

<br>

 테스트 시나리오: 상품 2000건에 대한 요청 처리
 
 부하 조건: 초당 트래픽 1000, 총 테스트 기간 300초

<br>

<p align="center">
 
![image](https://github.com/TrandPick/TrendPick_Pro/assets/62290451/71eb122d-e143-4674-9ba0-eb733ffd572e)
### 초기 평균 응답 시간에 비해 약 19.8%, TPS는 약 22.5%의 개선이 이루어졌습니다.
<br>
 
 ![image](https://github.com/TrandPick/TrendPick_Pro/assets/62290451/cb5ba7e4-6a33-4454-bc2b-4224d08934e2)
 ### 초기 평균 응답 시간에 비해 약 1.65%, TPS는 약 9.8%의 개선이 이루어졌습니다.
<br>

</p>


이를 개선하고자 상품 별로 캐시를 적용하고 상품의 값이 자주 바뀌는 특성 상 수정이나 삭제 로직  수행 시 캐시의 내용도 변경되도록 했습니다.

캐시 변경 시에도 요청 시간이 그대로인지, 늘어났는지 상황을 부여하여 확인했고 멤버 email에 DB인덱싱을 적용하여 개선을 이끌어내고자 했습니다. 전체적으로는 필요한 데이터만 받으면서 최적화된 데이터 전송을 하도록 반환 값들을 dto 객체로 수정했습니다.

<br>

<br>

## 👏 팀원 소개
| [<img src="https://avatars.githubusercontent.com/u/26915908?v=4" width="200">](https://github.com/angelSooho) | [<img src="https://avatars.githubusercontent.com/u/71963159?v=4" width="200">](https://github.com/hye-0000) | [<img src="https://avatars.githubusercontent.com/u/110995932?v=4">](https://github.com/jooooonj) | [<img src="https://avatars.githubusercontent.com/u/62290451?v=4" width="200">](https://github.com/mmunkyeong) |  
|:-----------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------------:|
|                                [이수호](https://github.com/angelSooho)                                |                                  [권혜영](https://github.com/hye-0000)                                   |                                   [이재준](https://github.com/jooooonj)                                   |                                  [제문경](https://github.com/mmunkyeong)                                  |
