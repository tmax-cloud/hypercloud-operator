# HyperCloudServer changelog!!
All notable changes to this project will be documented in this file.

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
