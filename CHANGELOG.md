# HyperCloudServer changelog!!
All notable changes to this project will be documented in this file.
-e 
<!-------------------- v4.1.9.9 start -------------------->
-e 
## HyperCloudServer_4.1.9.9 (2021. 05. 21. (금) 15:44:45 KST)
-e 
### Added
-e 
### Changed
-e 
### Fixed
-e 
### CRD yaml
-e 
### Etc
-e 
<!--------------------- v4.1.9.9 end --------------------->

<!-------------------- v4.1.0.30 start -------------------->

## HyperCloudServer_4.1.0.30 (Fri May 29 04:58:29 KST 2020)

### Added

### Changed
  - [mod] guide_usersecuritypolicy 추가 by 윤진수 by taegeon_woo
  - [mod] admin-tmax.co.kr default  USP 생성 yaml 제거 by taegeon_woo
  - [mod] guide_usersecuritypolicy 30버전부터 바뀐 USP 정책에 대한 가이드 추가 by taegeon_woo
  - [mod] private user 생성시, USP crd default로 생성하는 로직 제거, usp가 없을때, otpEnable false로 간주하는 로직 추가 by taegeon_woo
  - [mod] proauth 정책 변경에 따른 userID duplicate 로직 변경 by taegeon_woo
  - [mod] Service Instance 생성 시 Plan 에 있는 parameter 가 Template Instance 의 parameter 로 전달되게 기능 수정 by seonho_choi
  - [mod] MeteringJob.java 로그 정리 by taegeon_woo
  - [mod] UserHandler.java ID 중복체크 API 로직 변경 (List --> Detail) by taegeon_woo

### Fixed
  - [ims][223511] plan 생성 시 ID 의 경우 고유한 UUID 를 넣어서 중복이 없도록 수정 by seonho_choi
  - [ims][226370] registry 삭제시 log 정리 by sukhee_yun

### CRD yaml
  - [crd] guide_registry 수정 (Image Push 방법 추가) by sukhee_yun

### Etc

<!--------------------- v4.1.0.30 end --------------------->

<!-------------------- v4.1.0.29 start -------------------->

## HyperCloudServer_4.1.0.29 (Wed May 27 09:02:41 KST 2020)

### Added

### Changed
  - [mod] 생성된 Registry 객체의 deleteWithPvc 수정시 반영(edit 기능) by sukhee_yun
  - [mod] public환경 회원가입시 id 중복체크 로직 변경 (list --> detail) [mod] public환경 토큰 만료 시간 변경시, refresh Token 만료시간도 연장하는 로직 추가 by taegeon_woo
  - [mod] private 설치 yaml 에 admin-tmax.co.kr USP 추가 by taegeon_woo
  - [mod] default_resourcequotaclaim.yml 에 limits.memory 추가 [mod] metering mysql 각 테이블에 namespace, metering_time으로 unique 속성 추가 by taegeon_woo
  - [mod] public 모델에서 otp 서비스를 불러도 otpEnable false로 반환하도로고 변경 [ims][225701] proauth HTTP port 변경 가능 하게 변경, hypercloud4-operator proauth 콜도 변수로 변경 by taegeon_woo

### Fixed
  - [ims][226364] registry 생성시 유효하지 않은 입력값 입력시에도 operator가 정상 동작 할 수 있도록 수정 by sukhee_yun
  - [mod] public 모델에서 otp 서비스를 불러도 otpEnable false로 반환하도로고 변경 [ims][225701] proauth HTTP port 변경 가능 하게 변경, hypercloud4-operator proauth 콜도 변수로 변경 by taegeon_woo

### CRD yaml
  - [crd] Registry validation 추가(port, tolerationSeconds 필드에 minimum, maximum 설정) by sukhee_yun
  - [crd] default_registry.yaml loadBalancer 사용 예제로 수정 by sukhee_yun

### Etc

<!--------------------- v4.1.0.29 end --------------------->

<!-------------------- v4.1.0.28 start -------------------->

## HyperCloudServer_4.1.0.28 (Fri May 22 09:32:23 KST 2020)

### Added

### Changed
  - [mod] 설치 yaml operator 버전업 by taegeon_woo
  - [mod] otp 인증번호 발급 버그, registerTime 버그 수정 by taegeon_woo

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.0.28 end --------------------->

<!-------------------- v4.1.0.27 start -------------------->

## HyperCloudServer_4.1.0.27 (Fri May 22 07:12:00 KST 2020)

### Added

### Changed
  - [mod] otp send 서비스 swagger 수정 by taegeon_woo
  - [mod] otpHandler 추가 by taegeon_woo
  - [mod] public 비밀번호 변경시 user의 retryCount를 0으로 만들어주는 로직 추가 by taegeon_woo
  - [mod] retryCount 10회 도달시 에러코드 출력가능하게끔 200ok 출력으로 변경 by taegeon_woo
  - [mod] proauth-server 새로운 이미지 설치에 반영 by taegeon_woo
  - [mod] RefreshHandler.java secret 에서 만료시간 가져오는 버그 수정 by taegeon_woo

### Fixed
  - [ims][220310] TZ 환경변수를 설정 시 date 명령어 에서도 반영되도록 alpine image 에 tzdata package 설치 by seonho_choi
  - [ims][225652] ingress type 사용시 domainName 필수갑으로 설정, Exception 발생시 Error로 상태 변경 처리 by sukhee_yun
  - [ims][225777] exist pvc mount 안되는 버그 수정 / pvc의 exist, create 필드 분리하여 두가지 모두 사용하는 경우가 없도록 수정 by sukhee_yun
  - [ims][223283] Template Instance 생성 시 parameter value 가 빈 스트링인 경우 default value 가 들어갈 수 있도록 수정 by seonho_choi
  - [ims][225516] hypercloud-operator 재기동 전략 수정 Rolling --> Recreate by taegeon_woo

### CRD yaml
  - [crd] default_registry.yaml 수정 by sukhee_yun
  - [crd] Registry service 필드 내용 수정 by sukhee_yun
  - [crd] Registry CRD의 PersistentVolumeClaim 필드 내용 수정 by sukhee_yun

### Etc

<!--------------------- v4.1.0.27 end --------------------->
<!-------------------- v4.1.0.26 start -------------------->

## HyperCloudServer_4.1.0.25 (Mon May 18 08:18:28 KST 2020)

### Added

### Changed
  - [mod] UserSecurityPolicyCRD.yaml 버그 수정 by taegeon_woo

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.0.26 end --------------------->

<!-------------------- v4.1.0.26 start -------------------->

## HyperCloudServer_4.1.0.26 (Tue May 19 02:36:37 KST 2020)

### Added

### Changed

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.0.26 end --------------------->

<!-------------------- v4.1.0.25 start -------------------->

## HyperCloudServer_4.1.0.25 (Mon May 18 08:18:28 KST 2020)

### Added

### Changed
  - [mod] otp 만료시간 추가 (default : 30 min) by taegeon_woo
  - [mod] 이메일을 보내기전에 인증번호 & otp 저장하도록 로직 수정 by taegeon_woo
  - [mod] trial 네임스페이스 스펙 변경 by taegeon_woo

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.0.25 end --------------------->

<!-------------------- v4.1.0.24 start -------------------->

## HyperCloudServer_4.1.0.24 (Fri May 15 03:01:01 KST 2020)

### Added
  - [feat] login/logout 이력관리되도록 추가 by jaihwan
  - [feat] audit 기능 추가 by jaihwan

### Changed
  - [mod] token 만료시간 secret으로 부터 받아오는 부분 구현 by taegeon_woo
  - [mod] otp email html 문서 적용, master 토큰 발급 by taegeon_woo
  - [mod] USP CRD 생성 및 활성화 가이드 추가 guide_usersecuritypolicy by taegeon_woo
  - [mod] 로그인시 otp고려하는 로직 구현 및 테스트 완료 by taegeon_woo

### Fixed
  - [ims][223283] defaultValue 도 template instance 의 parameter 로 추가되어 parameter count 에 포함되도록 수정 by seonho_choi

### CRD yaml
  - [crd] Registry Service Type 수정: ClusterIP, NodePort 삭제 및 Ingress 타입 추가 by sukhee_yun
  - [crd] Registry 필드추가: ingressDomain, existPvcName, deleteWithPvc by sukhee_yun

### Etc

<!--------------------- v4.1.0.24 end --------------------->

<!-------------------- v4.1.0.23 start -------------------->

## HyperCloudServer_4.1.0.23 (Mon May  4 08:21:10 KST 2020)

### Added

### Changed
  - [mod] trial 신청 승인 메일 수정 by taegeon_woo
  - [mod] UserSecurityPolicyCRD.yaml 추가 by taegeon_woo

### Fixed
  - [ims][223799] ResourceQuotaClaimCRD.yaml 필수 field 추가 by taegeon_woo

### CRD yaml

### Etc

<!--------------------- v4.1.0.23 end --------------------->

<!-------------------- v4.1.0.22 start -------------------->

## HyperCloudServer_4.1.0.22 (Wed Apr 29 13:13:49 KST 2020)

### Added

### Changed
  - [mod] template instance 생성 시 parameter 가 하나도 없는 경우 default value 가 들어갈 수 있도록 수정 by seonho_choi
  - [mod] trial 메일 형식 변경 by taegeon_woo

### Fixed

### CRD yaml
  - [crd] NamespaceClaimCRD.yaml 수정. hard:limits.cpu & hard:limits.memory required 처리 by seonho_choi

### Etc

<!--------------------- v4.1.0.22 end --------------------->

<!-------------------- v4.1.0.21 start -------------------->

## HyperCloudServer_4.1.0.21 (Wed Apr 29 11:32:10 KST 2020)

### Added

### Changed
  - [mod] trial 관련 메일 html 파일 적용, trial user에게 부여할 clusterRoleBinding설계 및 구현 by taegeon_woo
  - [mod] trial 신청 승인 / 미승인시 메일 전송 기능 추가, 인증번호 인증 서비스 리스폰스 변경 by taegeon_woo

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.0.21 end --------------------->

<!-------------------- v4.1.0.20 start -------------------->

## HyperCloudServer_4.1.0.20 (Tue Apr 28 12:18:57 KST 2020)

### Added

### Changed
  - [mod] 보내는 공식 메일 변경 by taegeon_woo

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.0.20 end --------------------->

<!-------------------- v4.1.0.19 start -------------------->

## HyperCloudServer_4.1.0.19 (Tue Apr 28 11:36:59 KST 2020)

### Added

### Changed
  - [mod] 메일 서비스 송신자 수정 by taegeon_woo
  - [mod] openAuth 환경 id, e-mail 분리 작업 구현 by taegeon_woo
  - [mod] trial 최초 요청시 user에게 해당 trial에 관한 권한만 주는 로직 구현 by taegeon_woo
  - [mod] namespace GET 에서 rolebinding은 있는데 role만 지웠을 경우 고려 코드 추가 by taegeon_woo
  - [mod] 유저 생성시 줘야할 clusterrole설계 by taegeon_woo
  - [mod] service instance -> template instance 생성 시 template instance 이름에 service instance 이름이 들어가도록 수정 by seonho_choi
  - [mod] Template/example_instance.yaml Template/example_template.yaml 수정 by seonho_choi
  - [mod]  swagger update by taegeon_woo
  - [mod] Trial Timer 1달 시간 연장 기능 구현 by taegeon_woo
  - [mod] Registry NodePort로 만들었을 때 docker login 안되는 버그 수정 by sukhee_yun
  - [mod] trial meta annotation으로 처리 by taegeon_woo
  - [mod] swagger Update by taegeon_woo
  - [mod] 통합auth 토큰 만료 시간 변경 서비스 구현 by taegeon_woo
  - [mod] metering publicIp 조회 가능하도록 변경 by taegeon_woo

### Fixed

### CRD yaml
  - [crd] Registry volumeMode 필드 추가 by sukhee_yun

### Etc

<!--------------------- v4.1.0.19 end --------------------->

<!-------------------- v4.1.0.18 start -------------------->

## HyperCloudServer_4.1.0.18 (Fri Apr 24 04:58:08 KST 2020)

### Added
  - [feat] catalog museum - private package server 추가 by Sunghyun Kim

### Changed
  - [mod] 로그인 서비스 버그 수정 by taegeon_woo
  - [mod] default_namespaceclaim.yaml 변경 by taegeon_woo
  - [mod] swagger수정 by taegeon_woo
  - [mod] namespaceGet user의 경우 kind 추가 by taegeon_woo
  - [mod] trial 기간 1달 --> 30일로 변경, owner label 추가 by taegeon_woo
  - [mod] trial NameSpaceClaim 생성 서비스 및 Timer 서비스 구현 완료 by taegeon_woo
  - [mod] 'template.parameter.valueType : number' 인 경우 json generate 할 때 숫자 특수 처리 되도록 수정 by seonho_choi
  - [mod] 인증번호 메일 html 문서 적용 by taegeon_woo
  - [mod] 메일인증번호 서비스 유효시간 30분 체크 로직 구현 by taegeon_woo
  - [mod] 인증번호 유효시간 고려 by taegeon_woo
  - [mod] login retryCount 버그 수정 by taegeon_woo
  - [mod] 이메일 전송 서비스 버그 수정 by taegeon_woo
  - [mod] EmailHandler 수정 : 가입된 유저에게도 이메일 보낼수 있게 validation 제거 by taegeon_woo

### Fixed
  - [ims][223283] template instance 생성 시 parameter 입력 값이 없는 경우 default value 가 들어가도록 구현. by seonho_choi
  - [ims][223271] template object 들에 namespace 가 명시되어 있지 않는 경우 Template Instance Namespace 에 생성되도록 수정 by seonho_choi
  - [ims][223234] operator 기동중 image sync를 위한 로직 수행할때 비정상 registry에 의한 오류발생하더라도 정상 기동 되도록 수정 by sukhee_yun

### CRD yaml
  - [crd] RegistryCRD.yaml 수정: nodeselector, matchlabels&matchExpressions, toleration 추가 by sukhee_yun
  - [crd] Template/TemplateCRD_v1beta1.yaml parameter 에 valueType field 추가 by seonho_choi

### Etc

<!--------------------- v4.1.0.18 end --------------------->

<!-------------------- v4.1.0.17 start -------------------->

## HyperCloudServer_4.1.0.17 (Mon Apr 20 06:30:14 KST 2020)

### Added

### Changed
  - [mod] user email 중복 체크 서비스 개발 by taegeon_woo
  - [mod] user login retrycount 구현 완료 by taegeon_woo
  - [mod] user id 중복 체크 기능 추가 by taegeon_woo
  - [mod] 비밀번호 변경기능 id로 가능하게끔 변경 by taegeon_woo
  - [mod] user meta에 dateOfBirth 추가 by taegeon_woo
  - [mod] 메일 전송 서비스 한글화 해결 by taegeon_woo
  - [mod] email Handler swagger정리 by taegeon_woo
  - [mod] user Email send & verify 서비스 구현 by taegeon_woo
  - [mod] Catalog museum 각 템플릿에 SERVICE_TYPE 파라미터 추가 by Sunghyun Kim
  - [mod] template, template instance 객체에 operatorStartTime 추가 by seonho_choi

### Fixed

### CRD yaml
  - [crd] Template/TemplateCRD_v1beta1.yaml : urlDescription, markdownDescription 필드 추가 by seonho_choi
  - [crd] Template/TemplateCRD_v1beta1.yaml 버그 수정 by seonho_choi

### Etc

<!--------------------- v4.1.0.17 end --------------------->

<!-------------------- v4.1.0.16 start -------------------->

## HyperCloudServer_4.1.0.16 (2020년 04월 17일 금 오후  2:13:50)

### Added

### Changed
  - [mod] email handler 추가 by taegeon_woo
  - [mod] install_guide 추가 by taegeon_woo
  - [mod] admin 권한 최초 유저 정보 변경 by taegeon_woo
  - [mod] namespaceGET 403 에러시 에러메시지 출력되도록 return 타입 변경 by taegeon_woo
  - [mod] 모든 ns에 권한이 없는 user일때 namespaceGet 처리 해주기 by taegeon_woo
  - [mod] User Class에 profile Pic 추가 by taegeon_woo
  - [mod] NamespaceClaimController.java 정책 변경 : NamespaceClaim은 중복 이름이면 reject by taegeon_woo
  - [mod] default_user_group.yaml 수정 by taegeon_woo
  - [mod] serviceinstance 에서 plan 조회 시 namespaced 가 아닌 cluster scope 로 조회하도록 수정 by seonho_choi
  - [mod]  dockerFile 테스트 by taegeon_woo
  - [mod] UserDeleteWatcher.java 구현 by taegeon_woo

### Fixed
  - [ims][222485] clusterRole에 resourceName을 가진 Namespace에 관해서만 권한을 가지도록 수정 by taegeon_woo

### CRD yaml
  - [crd] Template/TemplateCRD_v1beta1.yaml paramters 필수값 표시 by seonho_choi

### Etc

<!--------------------- v4.1.0.16 end --------------------->

<!-------------------- v4.1.0.15 start -------------------->

## HyperCloudServer_4.1.0.15 (2020년 04월 14일 화 오후  6:45:20)

### Added

### Changed
  - [mod] userCreate 수정 by taegeon_woo
  - [mod] Dockerfile by taegeon_woo
  - [mod] Dockerfile 수정 by taegeon_woo
  - [mod] edit Dockerfile by taegeon_woo
  - [mod] Image Name에서 '_'문자가 아닌 '.'을 -u-로 바꾸는 parsing bug fix by sukhee_yun

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.0.15 end --------------------->

<!-------------------- v4.1.0.14 start -------------------->

## HyperCloudServer_4.1.0.14 (2020년 04월 14일 화 오후  2:41:08)

### Added

### Changed
  - [mod] Image Tag 중복 제거 by sukhee_yun
  - [mod] proauth-server.yaml수정 by taegeon_woo
  - [mod] WAS Catalog museum 템플릿 수정 by Sunghyun Kim
  - [mod] rolebinding user 형식 변경 by taegeon_woo
  - [mod] admin 계정 rolebinding수정 by taegeon_woo
  - [mod] rolebinding 수정 가이드 추가 by taegeon_woo
  - [mod] 통합 Auth 전환 가이드 by taegeon_woo
  - [mod] ProAuth 설치 가이드 보완 by taegeon_woo
  - [mod] NameSpaceHandler.java 분기처리 by taegeon_woo
  - [mod] proauth 업데이트 to 27 버전 by taegeon_woo

### Fixed
  - [ims][222564] Registry에 push한 이미지 이름에 path기호('/')가 있을 경우 Image CR 생성 안되는 버그 수정(수정된 guide_image 파일 참고) by sukhee_yun

### CRD yaml

### Etc

<!--------------------- v4.1.0.14 end --------------------->

<!-------------------- v4.1.0.13 start -------------------->

## HyperCloudServer_4.1.0.13 (2020년 04월 13일 월 오후  4:05:04)

### Added

### Changed
  - [mod] claim 관련 정책 변경 ( duplicated name 삭제 ) by taegeon_woo
  - [mod] hypercloud Integrated Auth  설치 메뉴얼 추가 by taegeon_woo
  - [mod] integrated AUth 환경 로그인 오류메시지 변경 by taegeon_woo

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.0.13 end --------------------->

<!-------------------- v4.1.0.12 start -------------------->

## HyperCloudServer_4.1.0.12 (2020년 04월 13일 월 오후  2:06:47)

### Added

### Changed
  - [mod] user CRD 에 usergroup 예시 추가 by taegeon_woo
  - [mod] guide_usergroup 추가 by taegeon_woo
  - [mod] userGroup 구현완료 by taegeon_woo
  - [mod] cors Access-Control-Allow-Headers 복수개 적용 되도록 변경 by taegeon_woo
  - [mod] userGroup CRD 추가 by taegeon_woo
  - [mod] 1.initialization.yaml 에서 CRD 적용 yaml 들 제거 및 install_guide 수정 by seonho_choi
  - [mod] claim 관련 정책 변경 : 사용자는 awaiting 일때만 edit 가능 by taegeon_woo
  - [mod] latest crd 치환용 버전 명 일괄 변경 by seonho_choi

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.0.12 end --------------------->

<!-------------------- v4.1.0.11 start -------------------->

## HyperCloudServer_4.1.0.11 (2020년 04월  9일 목 오후  4:37:39)

### Added
  - [feat] CRD 4.1.0.11 version by seonho_choi

### Changed
  - [mod] 모든 resource DO 에 operatorStartTime 추가 by seonho_choi

### Fixed

### CRD yaml
  - [crd] ImageCRD.yaml, RegistryCRD.yaml : annoatation 및 operatorStartTime 필드 추가 by sukhee_yun

### Etc

<!--------------------- v4.1.0.11 end --------------------->

<!-------------------- v4.1.0.10 start -------------------->

## HyperCloudServer_4.1.0.10 (2020년 04월  9일 목 오후  3:16:06)

### Added
  - [feat] ImageCRD 추가 및 ImageList 조회 기능 추가 (ImageCRD의 Create, Delete, Edit 기능 미지원) by sukhee_yun

### Changed
  - [mod] install 1.initialization.yaml 수정 by seonho_choi
  - [mod] Definition 수정 후 Operator 재기동 시 기존 Resource 를 지우지 않아도 패치 가능하도록 수정 by seonho_choi
  - [mod] Blocked user clear job 구현 완료 by taegeon_woo

### Fixed

### CRD yaml
  - [crd] annotation 에 operator.version 명시되도록 수정 by seonho_choi
  - [crd] guide_image 추가 by sukhee_yun
  - [crd] RegistryCRD.yaml, guide_registry 수정: customConfigYml 필드 추가 by sukhee_yun
  - [crd] ImageCRD 추가 by sukhee_yun
  - [crd] 모든 CRD definition 에 'operatorStartTime' Field 추가 (모두 재 apply 필요) by seonho_choi

### Etc

<!--------------------- v4.1.0.10 end --------------------->

<!-------------------- v4.1.0.9 start -------------------->

## HyperCloudServer_4.1.0.9 (2020년 04월  8일 수 오후  3:02:23)

### Added

### Changed

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.0.9 end --------------------->

<!-------------------- v4.1.0.8 start -------------------->

## HyperCloudServer_4.1.0.8 (2020년 04월  8일 수 오후  1:20:10)

### Added

### Changed
  - [mod] 이미지 안에 yaml CRD/latest 폴더 들어 가도록 수정 by seonho_choi
  - [mod] CRD yaml 변경 내용 CHANGELOG.md 에 명시되도록 수정 by seonho_choi
  - [mod] CRD Version 관리 by seonho_choi
  - [mod] catalog museum 이 이미지 내에 포함되도록 수정 by seonho_choi
  - [mod] ResourceQuotaClaimController.java update하는 로직 제거 by taegeon_woo
  - [mod] Claim 이름과 CRD Resource 이름을 분리하여 같은 Resource를 위한 복수개의 Claim을 생성 할 수 있도록 변경  by taegeon_woo
  - [mod] 비밀번호 찾기 기능 구현 완료 by taegeon_woo
  - [mod] Tekton trigger crd pluralMapper 추가 by Sunghyun Kim
  - [mod] 통합 auth password 변경 기능, refresh 기능  추가 by taegeon_woo

### Fixed
  - [ims][221499] namespace-owner.yaml 오타 수정 by taegeon_woo
  - [ims] [221625] 생성되어 있는 CRD와 동일한 이름으로 Claim  생성시 reject 상태로 되게끔 변경  by taegeon_woo
  - [ims] [221771] ResourceQuota 이름과  nameSpace 이름을 분리  by taegeon_woo

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
