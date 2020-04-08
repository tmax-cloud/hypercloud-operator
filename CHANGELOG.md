# HyperCloudServer changelog!!
All notable changes to this project will be documented in this file.

<!-------------------- v4.1.0.8 start -------------------->

## HyperCloudServer_4.1.0.8 (2020년 04월  8일 수 오후  1:20:10)

### Added

### Changed
  - [mod] 이미지 안에 yaml CRD/latest 폴더 들어 가도록 수정 by seonho_choi
  - [mod] CRD yaml 변경 내용 CHANGELOG.md 에 명시되도록 수정 by seonho_choi
  - [mod] CRD Version 관리 by seonho_choi
  - [mod] catalog museum 이 이미지 내에 포함되도록 수정 by seonho_choi
  - [mod] ResourceQuotaClaimController.java update하는 로직 제거 by taegeon_woo
  - [ims] [221625] 생성되어 있는 CRD와 동일한 이름으로 Claim  생성시 reject 상태로 되게끔 변경, [mod] Claim 이름과 CRD Resource 이름을 분리하여 같은 Resource를 위한 복수개의 Claim을 생성 할 수 있도록 변경 [ims] [221771] ResourceQuota 이름과  nameSpace 이름을 분리 by taegeon_woo
  - [mod] 비밀번호 찾기 기능 구현 완료 by taegeon_woo
  - [mod] Tekton trigger crd pluralMapper 추가 by Sunghyun Kim
  - [mod] 통합 auth password 변경 기능, refresh 기능  추가 by taegeon_woo

### Fixed
  - [ims][221499] namespace-owner.yaml 오타 수정 by taegeon_woo

### CRD yaml
  - [crd] NamespaceClaimCRD : resourceName 추가 ( 필수값 ), 관련 default, example yaml에 반영
  - [crd] ResourceQuotaClaimCRD : resourceName 추가 ( 필수값 ), 관련 default, example yaml에 반영
  - [crd] RoleBindingClaimCRD : resourceName 추가 ( 필수값 ), 관련 default, example yaml에 반영
  - [crd] RegistryCRD.yaml 수정 /spec/persistentVolumeClaim/accessModes를 required로 추가
  
### Etc

<!--------------------- v4.1.0.8 end --------------------->

<!-------------------- v4.1.0.7 start -------------------->

## HyperCloudServer_4.1.0.7 (2020년 04월  6일 월 오후  1:04:45)

### Added

### Changed
  - [mod] 통합 user id찾기 구현 완료 by taegeon_woo
  - [mod] RegistryCRD.yaml 수정 by sukhee_yun
  - [mod] Registry Running 상태 기준 수정(모든 서브 리소스들이 정상일 때 Running) by sukhee_yun
  - [mod] template parameter 의 number value 처리 by seonho_choi
  - [mod] 통합 logout 서비스 구현 완료 by taegeon_woo
  - [mod] template 생성 시 paramters 입력하지 않으면 정상동작하지 않도록 수정. _catalog_museum 의 template 들 모두 수정 by seonho_choi
  - [mod] 통합 로그인 서비스 구현 by taegeon_woo

### Fixed

### Etc

<!--------------------- v4.1.0.7 end --------------------->

<!-------------------- v4.1.0.6 start -------------------->

## HyperCloudServer_4.1.0.6 (2020년 04월  3일 금 오전 11:35:05)

### Added

### Changed
  - [mod] 통합 회원가입 서비스 구현완료 by taegeon_woo
  - [mod] Template Service Broker 의 Namespace 를 HyperCloud4-Operator 생성 시 환경변수로 받을 수 있도록 수정 (기존: default namespace 로 고정) by seonho_choi
  - [mod] template instance 와 object 간 ownerReference 로 연결 추가 by seonho_choi
  - [mod] namespace get 수정 : clusterRole - RoleBinding 가능성 고려 로직 추가 by taegeon_woo

### Fixed
  - [ims][221436] Registry Edit 기능 추가 (loginId, loginPassword, image) by sukhee_yun
  - [ims][221457] Registry PVC accessMode default 설정 제거 by sukhee_yun

### Etc

<!--------------------- v4.1.0.6 end --------------------->

<!-------------------- v4.1.0.5 start -------------------->

## HyperCloudServer_4.1.0.5 (2020년 03월 31일 화 오후  3:58:27)

### Added

### Changed
  - [mod] Registry registry-login-url annotation 추가 by sukhee_yun
  - [mod] Template Resource Definition 수정 by seonho_choi
  - [mod] Template Service Broker Catalog API 예외 처리 및 방어 로직 추가 by seonho_choi
  - [mod] controller thread down time 최소화 by seonho_choi
  - [mod] master token 변경, namespaceGet 마스터 토큰으로 되게끔 허용 by taegeon_woo
  - [mod] NameSpaceHandler.java limit 추가 by taegeon_woo
  - [mod] 로그인시 아이디 비번 틀릴시 200ok 와 에러메시지 출력하도록 수정 by taegeon_woo
  - [mod] 로그인시 권한 검사를 통해 admin 판별 하는 로직 추가 by taegeon_woo
  - [mod] user별 NameSpace list 조회 서비스 구현 완료 by taegeon_woo

### Fixed
  - [ims][221320] refreshToken 시 시간 변경 가능하도록 변경 by taegeon_woo

### Etc

<!--------------------- v4.1.0.5 end --------------------->

<!-------------------- v4.1.0.4 start -------------------->

## HyperCloudServer_4.1.0.4 (2020년 03월 30일 월 오전 10:25:55)

### Added

### Changed
  - [mod] service broker -> cluster service broker 로 변경 by seonho_choi
  - [mod] template instance ownerRef -> service instance 추가 by seonho_choi
  - [mod] login 실패시 에러메시지 포맷 변경 by taegeon_woo
  - [mod] service broker 의 namespace 고정 해제 by seonho_choi
  - [mod] publicIp 조회 가능하게 변경 by taegeon_woo
  - [mod] template, template instance crd definition 수정. parameter.value data type 이 number 도 가능하도록 함 by seonho_choi

### Fixed

### Etc

<!--------------------- v4.1.0.4 end --------------------->

<!-------------------- v4.1.0.3 start -------------------->

## HyperCloudServer_4.1.0.3 (2020년 03월 26일 목 오후 12:09:21)

### Added
  - [feat] GET metering service 구현 by seonho_choi
  - [feat] Service Class 객체에 short/long description, provider 필드 추가 by 이승진
  - [feat] metering data 수집 기능 추가 by seonho_choi
  - [feat] extension api swagger yaml 추가 (http://192.168.1.150:8010/?urls.primaryName=4.1%2Fhypercloud-operator-ext%2Foperator-extensiono-api) by seonho_choi
  - [feat] definition yaml, default yaml, example yaml, guide Page (http://192.168.1.150:10080/hypercloud/hypercloud-operator/wikis/home#crds) by seonho_choi
  - [feat] Service Broker Binding 추가 by 이승진
  - [feat] rolebindingclaim CRD & Controller 구현 by seonho_choi
  - [feat] resourcequotaclaim CRD & Controller 구현 by seonho_choi
  - [feat] TemplateCRD 에 imageURL 및 plans 추가, Broker Binding 수정 by 이승진
  - [feat] namespaceclaim CRD & controller 추가 by seonho_choi
  - [feat] Template Service Broker Binding 추가, Template Operator 추가 by 이승진
  - [feat] Template Service Broker deprovisioning 서비스 추가 by 이승진
  - [feat] Template Service Broker provisioning 서비스 추가 by 이승진
  - [feat] registry operator by sukhee_yun

### Changed
  - [mod] 1.initialization.yaml 정리 (최신화) by seonho_choi
  - [mod] template instance 생성 실패 시 status 구문의 message 필드에 에러메세지 추가 by 이승진
  - [mod]  LoginPageHandler.java origin uri 비교로직 추가 by taegeon_woo
  - [mod] LoginPageHandler.java origin URI validation 코드 추가 by taegeon_woo
  - [mod] refresh 된 accesstoken 으로 인증 실패하는 버그 수정 by seonho_choi
  - [mod] 1.initialization.yaml resourcequota 수정 by seonho_choi
  - [mod] loginPageCreate, LoginAsClient 구현 완료 by taegeon_woo
  - [mod] Service Instance 생성 시 parameter 채우는 로직 변경 by 이승진
  - [mod] binding credential 수정 by 이승진
  - [mod] clientRegister 서비스 구현 by taegeon_woo
  - [mod] logout, refresh pre-flight api 추가 by seonho_choi
  - [mod] tekton crd pluralMapper 추가 by seonho_choi

### Fixed

### Etc

<!--------------------- v4.1.0.3 end --------------------->

<!-------------------- v4.1.0.2 start -------------------->

## HyperCloudServer_4.1.0.2 (2020년 03월 13일 금 오후  7:35:21)

### Added

### Changed
  - [mod] allow cors 설정 및 pre-flight api 구현 by seonho_choi
  - [mod] api group 변경 ( tmax.co.kr -> tmax.io ) by seonho_choi

### Fixed

### Etc

<!--------------------- v4.1.0.2 end --------------------->
