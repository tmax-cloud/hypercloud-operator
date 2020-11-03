# HyperCloudServer changelog!!
All notable changes to this project will be documented in this file.

<!-------------------- v4.1.6.2 start -------------------->

## HyperCloudServer_4.1.6.2 (Wed Nov  4 03:47:41 KST 2020)

### Added
  - [feat] owner labelToAnno.sh 추가 by dnxorjs1

### Changed
  - [mod] instance 생성 시, object에 namespace insert 로직 추가 by jitae_yun

### Fixed
  - [ims][243066] Operator 기동 시 Image 동기화 작업을 별도 thread로 실행 by sukhee_yun

### CRD yaml

### Etc

<!--------------------- v4.1.6.2 end --------------------->

<!-------------------- v4.1.6.1 start -------------------->

## HyperCloudServer_4.1.6.1 (Fri Oct 23 10:48:03 KST 2020)

### Added

### Changed
  - [mod] nsc를 통해 만들어진 ns에 finalizer 로직 추가 by dnxorjs1

### Fixed
  - [ims][242904] nscCRD Status Enum 추가 by dnxorjs1

### CRD yaml

### Etc

<!--------------------- v4.1.6.1 end --------------------->

<!-------------------- v4.1.6.0 start -------------------->

## HyperCloudServer_4.1.6.0 (Fri Oct 23 06:05:35 KST 2020)

### Added

### Changed
  - [mod] add plural in OperatorFunc by jitae_yun
  - [mod] admin-tmax.co.kr --> admin@tmax.co.kr by dnxorjs1

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.6.0 end --------------------->

<!-------------------- v4.1.5.0 start -------------------->

## HyperCloudServer_4.1.5.0 (Fri Oct 16 08:14:24 KST 2020)

### Added
  - [feat] namespaceController 구현 : nsc로부터 만들어진 ns가 지워졌을때, 관련 ClusterRoleBinding 삭제, nsc상태 Deleted로 변경 by dnxorjs1
  - [feat] nscDeleteWatcher 구현 [mod] nscController 로그 정리 & nsc생성시 nsc에 대한 권한 owner에게 주는 부분 구현 by dnxorjs1

### Changed
  - [mod] 메일이 없는 사용자가 nsc신청했을때 방어로직 추가 by dnxorjs1
  - [mod] NamespaceClaim owner label --> owner annotation으로 변경에 따른 로직 수정 by dnxorjs1
  - [mod] 메일 html 한글 깨짐 수정 by dnxorjs1
  - [feat] nscDeleteWatcher 구현 [mod] nscController 로그 정리 & nsc생성시 nsc에 대한 권한 owner에게 주는 부분 구현 by dnxorjs1
  - [mod] nsc 로직 정리 및 patch owner 필터링 추가 by dnxorjs1
  - [mod] 6.default-auth-object-init.yaml 변경 by dnxorjs1

### Fixed

### CRD yaml

### Etc
  - [etc] Delete Audit from operator by sangwon_cho

<!--------------------- v4.1.5.0 end --------------------->

<!-------------------- v4.1.4.7 start -------------------->

## HyperCloudServer_4.1.4.7 (Thu Sep 17 10:53:26 KST 2020)

### Added

### Changed
  - [mod] registry serviceType 변경시 error 상태에 빠지는 버그 수정 by sukhee_yun
  - [mod] registry 생성시 domainName 입력받지 않게 수정 by sukhee_yun

### Fixed

### CRD yaml
  - [crd] RegistryCRD 수정 by sukhee_yun

### Etc

<!--------------------- v4.1.4.7 end --------------------->

<!-------------------- v4.1.4.6 start -------------------->

## HyperCloudServer_4.1.4.6 (Thu Sep 17 06:47:29 KST 2020)

### Added

### Changed
  - [mod] rolebinding list, get 권한 namespace-listget role로 이동 by dnxorjs1

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.4.6 end --------------------->

<!-------------------- v4.1.4.5 start -------------------->

## HyperCloudServer_4.1.4.5 (Thu Sep 17 05:47:06 KST 2020)

### Added
  - [feat] adminCMP, defaultCMP 추가 by miri_jo

### Changed
  - [mod] example_user_security_policy.yaml, default_user_security_policy.yaml 삭제 by dnxorjs1

### Fixed
  - [ims][239636] nsc 승인시 유저에게 부여하는 권한 재 설계 by dnxorjs1

### CRD yaml

### Etc

<!--------------------- v4.1.4.5 end --------------------->

<!-------------------- v4.1.4.4 start -------------------->

## HyperCloudServer_4.1.4.4 (Wed Sep 16 06:30:53 KST 2020)

### Added

### Changed
  - [mod] ns-listget role 수정 [mod] namespaceListGet Hyperauth call 방어로직 추가 [mod] nsc 승인시 생기는 Clusterrole 중복생성시 update 될수 있게끔 로직 수정 by dnxorjs1
  - [mod] namespace-listget clusterrole 수정 by dnxorjs1
  - [mod] NamespaceClaimCRD.yaml, ResourceQuotaClaimCRD.yaml Spec Validation 정규표현식에서 Ei 단위 생성 불가능 하게 수정 by dnxorjs1

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.4.4 end --------------------->

<!-------------------- v4.1.4.3 start -------------------->

## HyperCloudServer_4.1.4.3 (Wed Sep  9 11:13:33 KST 2020)

### Added

### Changed
  - [mod] instance operator ADDED시 방어로직 추가 by jitae_yun

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.4.3 end --------------------->

<!-------------------- v4.1.4.2 start -------------------->

## HyperCloudServer_4.1.4.2 (Tue Sep  8 14:15:17 KST 2020)

### Added

### Changed
  - [mod] nsc 승인시 trial 아닐때도, storageclass, catalog에 대한 권한 부여 by dnxorjs1

### Fixed
  - [ims][237725] template, claim에 urlDescription 필수 인자 반영 by jitae_yun

### CRD yaml

### Etc

<!--------------------- v4.1.4.2 end --------------------->

<!-------------------- v4.1.4.1 start -------------------->

## HyperCloudServer_4.1.4.1 (Mon Sep  7 16:40:50 KST 2020)

### Added

### Changed
  - [mod] 유저 최초 생성시 주는 권한 설계 및 수정 [mod] non-trial 승인시 유저에게 부여할 권한 설계 및 수정 by taegeon_woo
  - [mod] install guide 수정 namespace-owner cluster role에 images 추가 by sukhee_yun

### Fixed
  - [ims][233909] code 필터링 기능 추가 by sangwon_cho
  - [ims][237133] template 의 urlDescription 필수값으로 수정 by jitae_yun

### CRD yaml

### Etc

<!--------------------- v4.1.4.1 end --------------------->

<!-------------------- v4.1.4.0 start -------------------->

## HyperCloudServer_4.1.4.0 (Fri Sep  4 16:20:47 KST 2020)

### Added

### Changed
  - [mod] group이 없는 user nslistget안되던 현상 해결 by taegeon_woo
  - [mod] nsListGet에 userGroup 정보 추가 by taegeon_woo

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.4.0 end --------------------->

<!-------------------- v4.1.3.3 start -------------------->

## HyperCloudServer_4.1.3.3 (Thu Sep  3 20:42:43 KST 2020)

### Added

### Changed
  - [mod] nsListGet에 userGroup 정보 추가 by taegeon_woo

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.3.3 end --------------------->

<!-------------------- v4.1.3.2 start -------------------->

## HyperCloudServer_4.1.3.2 (Wed Sep  2 13:53:12 KST 2020)

### Added

### Changed
  - [mod] trial 시 주는 role 수정 by taegeon_woo
  - [mod] k8s java client version 9.00-->8.00 (버그) by taegeon_woo

### Fixed
  - [ims][237725] catalog service claim 으로 template 생성시 에러 발생하는 문제 해결 by jitae_yun

### CRD yaml

### Etc

<!--------------------- v4.1.3.2 end --------------------->

<!-------------------- v4.1.3.1 start -------------------->

## HyperCloudServer_4.1.3.1 (2020년 08월 31일 월 오후  3:47:04)

### Added

### Changed

### Fixed
  - [ims][237838] template instance의 상태가 error로 변경되는 문제 버그 픽스 by jitae_yun

### CRD yaml
  - [crd] RegistryCRD.yaml required 수정 by sukhee_yun

### Etc

<!--------------------- v4.1.3.1 end --------------------->

<!-------------------- v4.1.3.0 start -------------------->

## HyperCloudServer_4.1.3.0 (Fri Aug 28 15:38:07 KST 2020)

### Added

### Changed
  - [mod] java client version 8.0.0 --> 9.0.0 by taegeon_woo

### Fixed
  - [ims][237459] 존재하는 pvc로 registry 생성 시 status update 안되는 버그 수정 by sukhee_yun

### CRD yaml
  - [crd] Registry CRD의 Ingress port 필드 제거 by sukhee_yun

### Etc

<!--------------------- v4.1.3.0 end --------------------->

<!-------------------- v4.1.2.2 start -------------------->

## HyperCloudServer_4.1.2.2 (Wed Aug 26 17:17:34 KST 2020)

### Added

### Changed
  - [mod] _install-guide 오타 수정 by sukhee_yun

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.2.2 end --------------------->

<!-------------------- v4.1.2.1 start -------------------->

## HyperCloudServer_4.1.2.1 (Wed Aug 26 11:16:41 KST 2020)

### Added

### Changed
  - [mod] rbc, rqc Controller Reject 시 handled=t로 처리하는 로직 추가 by taegeon_woo

### Fixed
  - [ims][230023] 서비스 바인딩 생성 실패 문제 버그 픽스 by jitae_yun

### CRD yaml

### Etc

<!--------------------- v4.1.2.1 end --------------------->

<!-------------------- v4.1.2.0 start -------------------->

## HyperCloudServer_4.1.2.0 (Fri Aug 21 18:30:53 KST 2020)

### Added
  - [feat] userMigrationGuide 작성 by taegeon_woo
  - [feat] scripts 추가 by taegeon_woo

### Changed
  - [mod] hyperauth 로 부터 유저 이메일 가져오는 로직 구현 by taegeon_woo
  - [mod] nsc 메일보내는 부분 잠정적 주석처리 by taegeon_woo
  - [mod] nginx-ingress-controller, storageclass 권한 trial 승인시 주는 것으로 변경 by taegeon_woo
  - [mod] userMigrationGuide 추가 by taegeon_woo
  - [mod] 유저 제거시 유저에게 주어진 기본 role을 지워주는 api 추가 by taegeon_woo

### Fixed
  - [ims][236639] nsc를 제외한  claim 류 patch 시 발생하던 버그 픽스 [ims][236117] Trial 만료 & 삭제시 만들어줬던 ClusterRoleBinding 삭제 하는 로직 추가 by taegeon_woo
  - [ims][230842] registry volumemode default 값 설정 by sukhee_yun

### CRD yaml

### Etc

<!--------------------- v4.1.2.0 end --------------------->

<!-------------------- v4.1.1.0 start -------------------->

## HyperCloudServer_4.1.1.0 (Thu Aug 13 17:39:38 KST 2020)

### Added

### Changed

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.1.0 end --------------------->

<!-------------------- v4.1.0.48 start -------------------->

## HyperCloudServer_4.1.0.48 (Fri Aug  7 18:03:13 KST 2020)

### Added
  - [feat] federatedService 생성시, serviceDNSRecord 자동 생성. by mincheol_jeon

### Changed
  - [mod] nsc listget continue 잘못 들어가던 버그 수정 by taegeon_woo
  - [mod] catalogServiceClaim handled=f 반영 by jitae_yun

### Fixed
  - [ims][234738] 일반유저일때, nsc는 존재하는데, 자기가 만든 nsc가 없을때, 403에러 나던 현상 해결 by taegeon_woo

### CRD yaml

### Etc

<!--------------------- v4.1.0.48 end --------------------->

<!-------------------- v4.1.0.47 start -------------------->

## HyperCloudServer_4.1.0.47 (Fri Aug  7 13:14:04 KST 2020)

### Added

### Changed
  - [mod] user 생성시 cmp get 권한 부여 by taegeon_woo
  - [mod] ns, nsc List resource가 하나도 없거나, 잘못된 label을 입력했을때, ui를 통해 1초에 5번 불리던 현상 해결 by taegeon_woo
  - [mod] 메일에 쓰일 html, img 파일 관리 by taegeon_woo
  - [mod] 메일 html 에 포함된 이미지 첨부 파일로 보내는 형식으로 변경 by taegeon_woo
  - [mod] install-guiide 수정 및 9.nginx-controller.yaml 추가 by sukhee_yun
  - [mod] _yaml_Install/8.default-auth-object-init.yaml 파일에 ingress-nginx-shared-read-clusterrole 추가 by taegeon_woo
  - [mod] 유저 생성시 ingress-nginx-shared namespace read 권한 주기 구현 by taegeon_woo
  - [mod] NamespaceClaim 사용자가 handled=t 라벨을 handled=f 라벨로 임의로 업데이트할때 를 위한 방어로직 추가 by taegeon_woo
  - [mod] NamespaceClaim cli 를 통해 생성할 경우, 유저 정보가 없을때 에러나는 현상 수정 [mod] 다운타임동안에 namespaceClaim 생성하고, 관리자가 상태까지 변경했을 경우, 고려하는 로직 추가 by taegeon_woo

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.0.47 end --------------------->

<!-------------------- v4.1.0.46 start -------------------->

## HyperCloudServer_4.1.0.46 (Fri Jul 31 17:38:05 KST 2020)

### Added

### Changed
  - [mod] tmax crd resourceVersion 최신으로 올리는 로직 다시 추가 by taegeon_woo

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.0.46 end --------------------->

<!-------------------- v4.1.0.45 start -------------------->

## HyperCloudServer_4.1.0.45 (Fri Jul 31 16:00:44 KST 2020)

### Added

### Changed
  - [mod] template operator handled label 남은 부분 제거 by jitae_yun
  - [mod] template operator handled label 제거 by jitae_yun
  - [mod] image watcher handled=f 삭제 by sukhee_yun

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.0.45 end --------------------->

<!-------------------- v4.1.0.44 start -------------------->

## HyperCloudServer_4.1.0.44 (Fri Jul 31 13:56:34 KST 2020)

### Added

### Changed
  - [mod] capi-azure의 service instance 사용을 위한 capi-azure resource 등록 by mincheol_jeon
  - [mod] audit log 레벨 변경 by sangwon_cho
  - [mod] watcher handled label yaml 에 반영 by taegeon_woo

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.0.44 end --------------------->

<!-------------------- v4.1.0.43 start -------------------->

## HyperCloudServer_4.1.0.43 (Thu Jul 30 17:22:19 KST 2020)

### Added

### Changed
  - [mod] _yaml_Install/6.hypercloud4-operator.yaml 수정(env 추가) by sukhee_yun
  - [mod] debug용 log file 분리(위치: /home/tmax/hypercloud4-operator/logs/debug 디렉토리) & info log 간소화 by sukhee_yun
  - [mod] Handler 로그 정리 by taegeon_woo
  - [mod] watcher 기동 로직 정리 by taegeon_woo
  - [mod] log 레벨 정리중 by taegeon_woo
  - [mod] seperate log level and parameterize day of keeping log files by sukhee_yun
  - [mod] instance operator log level debug로 변경 by jitae_yun
  - [mod] template operator log level debug로 변경 by jitae_yun
  - [mod] k8sApiCaller service catalog 메소드 log level debug로 변경 by jitae_yun
  - [mod] service catalog handler log level debug로 변경 by jitae_yun
  - [mod] NamespaceClaimController.java 처리 한 부분 체크 하는 로직 구현 by taegeon_woo
  - [mod] Registry Status에 ConfigMap 추가 by sukhee_yun

### Fixed
  - [ims][221436] Registry customConfigYml field edit 안되는 버그 수정 by sukhee_yun

### CRD yaml

### Etc

<!--------------------- v4.1.0.43 end --------------------->

<!-------------------- v4.1.0.42 start -------------------->

## HyperCloudServer_4.1.0.42 (Tue Jul 28 16:45:22 KST 2020)

### Added

### Changed
  - [mod] 기동 로직 정리 by taegeon_woo
  - [mod] trial namespace 메일 중복 발송 현상 방어로직 추가 by taegeon_woo

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.0.42 end --------------------->

<!-------------------- v4.1.0.41 start -------------------->

## HyperCloudServer_4.1.0.41 (Fri Jul 24 15:46:09 KST 2020)

### Added

### Changed
  - [mod] 서비스인스턴스 생성시, cluster scope 검사 로직 수정 by jitae_yun
  - [mod] json 기존 package로 변경 by jitae_yun
  - [mod] resourceVersion 관리 안하는 것으로 재 변경 by taegeon_woo
  - [mod] install yaml 수정 (변수 처리 및 파일 내용 분리) by sukhee_yun

### Fixed
  - [ims][230023] 서비스 바인딩 생성 실패 문제 by jitae_yun
  - [ims][230111]  namespace claim 승인시 해당 namespace에 공통 network policy 생성 by taegeon_woo

### CRD yaml

### Etc

<!--------------------- v4.1.0.41 end --------------------->

<!-------------------- v4.1.0.40 start -------------------->

## HyperCloudServer_4.1.0.40 (Fri Jul 17 17:04:12 KST 2020)

### Added

### Changed
  - [mod] resource version 관리 오류로 인한 bug fix by dnxorjs1 by sukhee_yun

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.0.40 end --------------------->

<!-------------------- v4.1.0.39 start -------------------->

## HyperCloudServer_4.1.0.39 (Fri Jul 17 13:47:48 KST 2020)

### Added

### Changed
  - [mod] clusterMenuPolicyCRD.yaml 추가, 설치 가이드 수정 by dnxorjs1
  - [mod] admin 계정 정보 변경 by dnxorjs1

### Fixed

### CRD yaml
 
### Etc

<!--------------------- v4.1.0.39 end --------------------->

<!-------------------- v4.1.0.38 start -------------------->

## HyperCloudServer_4.1.0.38 (Fri Jul 10 14:02:53 KST 2020)

### Added

### Changed
  - [mod] lastestHandledResourceVersion 저장할때, 기존꺼보다 높은 경우에만 저장하도록 로직 수정 [mod] Trial Timer 메일 보내는 시간 지났을때, 재기동시 메일이 보내지는 현상 수정 by dnxorjs1
  - [mod] resourceVersion 가져오는 로직 수정 [mod] nsListGet, nscListGet label Selector를 잘못 입려했을 경우 에러가 아니게끔 처리 by dnxorjs1
  - [mod] <installer guide> 5.proauth-server의 node selector 부분 삭제 by mincheol_jeon
  - [mod] watcher resourceVersion configMap에 깨어날 때마다 저장하고 재기동시 해당 resourceVersion 바라보게 수정 by dnxorjs1
  - [mod] Template/CatalogServiceCalimCRD_v1beta1.yaml install guide에 추가 by dnxorjs1
  - [mod] Audit log timestamp 에 timezone 적용 by gyeongyeol-choi
  - [mod] status 파라미터를 전달받지 못했을 때 status를 정의하는 로직 제거 by gyeongyeol-choi
  - [mod] Audit - GET API 페이지네이션 기능 수정 (totalNum 추가) by gyeongyeol-choi
  - [feat] CapiFed - Capi Cluster의 federation join 자동화 by mincheol_jeon

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.0.38 end --------------------->

<!-------------------- v4.1.0.37 start -------------------->

## HyperCloudServer_4.1.0.37 (Thu Jul  2 18:24:45 KST 2020)

### Added

### Changed
  - [mod] audit로 부터 owner 라벨 추가해주는 로직 수정 by dnxorjs1
  - [mod] Aduit - status 파라미터를 전달받지 못했을 때 status를 정의하는 로직 추가 by gyeongyeol-choi
  - [mod] Audit - GET API limit 기본값 제거 (UI 요건) by gyeongyeol-choi
  - [mod] nsc List Get 버그 수정 by dnxorjs1
  - [mod] trial 기간 연장 api 중복 update로 인한 에러코드 수정 by dnxorjs1

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.0.37 end --------------------->

<!-------------------- v4.1.0.36 start -------------------->

## HyperCloudServer_4.1.0.36 (Wed Jul  1 13:59:52 KST 2020)

### Added

### Changed
  - [mod] nsc listGet 서비스 버그 수정 by dnxorjs1
  - [mod] install 가이드 변경 by dnxorjs1
  - [mod] swagger.yaml 수정 (Audit GET API 추가) by gyeongyeol-choi

### Fixed

### CRD yaml
  - [crd] default_registry.yaml 수정: image 버전 수정(2.7.1 -> 2.6.2) by sukhee_yun

### Etc

<!--------------------- v4.1.0.36 end --------------------->

<!-------------------- v4.1.0.35 start -------------------->

## HyperCloudServer_4.1.0.35 (Fri Jun 26 14:06:08 KST 2020)

### Added

### Changed
  - [mod][5.proauth-server.yaml] nodeSelector to podAffinity by mincheol_jeon
  - [mod] Audit API - ResourceType 파라미터 추가 by gyeongyeol-choi
  - [mod] public 새로운 유저에게 claim 류 list, get, create 권한 부여 by dnxorjs1

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.0.35 end --------------------->

<!-------------------- v4.1.0.34 start -------------------->

## HyperCloudServer_4.1.0.34 (Fri Jun 19 15:22:32 KST 2020)

### Added
  - [feat] Audit GET 서비스에서 namespace 필터링 기능 추가 by sangwon_cho

### Changed
  - [mod] trial namespace에 mailSendTime, Deletion Time label로 추가 by dnxorjs1
  - [mod] SetTimer 로그 보충 by dnxorjs1
  - [mod] UserSecurityPolicy DataObject 수정 by dnxorjs1
  - [mod] Trial 기간 연장 API 방어로직 추가 by dnxorjs1
  - [mod] namespace Get, namespaceclaim Get 서비스 label Selector로 검색 가능하게 로직 추가 by dnxorjs1

### Fixed
  - [ims][226364] Registry port 범위 수정(1~65535) by sukhee_yun
  - [ims][226888] Registry Creating중 pvc 상태가 pending이더라도 Error로 변경하지 않고 Creating 상태로 남도록 수정 by sukhee_yun
  - [ims][221436] Registry Edit 기능 추가 by sukhee_yun

### CRD yaml
  - [crd] Registry port validation 범위 수정 by sukhee_yun
  - [crd] Registry status에 capacity 추가 by sukhee_yun
  - [crd] Registry status 수정(phaseChangedAt 추가) by sukhee_yun
  - [crd] registry guide 수정(Resource 상태, 필수 요구 사항) by sukhee_yun

### Etc

<!--------------------- v4.1.0.34 end --------------------->

<!-------------------- v4.1.0.33 start -------------------->

## HyperCloudServer_4.1.0.33 (Fri Jun 12 10:41:01 KST 2020)

### Added

### Changed
  - [mod] nsName 중복 검사 서비스 구현 by dnxorjs1
  - [mod] Host OS 가 CentOS일때, Proauth-db 설치 법 가이드 및 Yaml 파일 추가 by dnxorjs1
  - [mod] nscList get 버그 수정, NamespaceClaimList metadat V1ObjectMeta --> V1ListMeta로 수정 by dnxorjs1
  - [mod] _install-guide USP yaml 적용 변경 by dnxorjs1
  - [mod] nscListGet 형식 k8s에 맞추어서 변경, NamespaceClaimList.java 추가 by dnxorjs1
  - [mod] trial namespace spec 메일 내용 수정 by dnxorjs1
  - [mod] trial 기간 연장 api trial version이 아닐때 연장 못하게 하는 로직 추가 by dnxorjs1
  - [mod] USP CRD 자체가 없을때 로직 구현 [mod] trial 생성시 admin 일때 role 수정 사항 고려 by dnxorjs1
  - [mod] nscCRD, rqcCRD validation 필드 추가 by dnxorjs1
  - [mod] user 기본권한 변경 nsc list --> get by dnxorjs1

### Fixed
  - [ims][223234] PVC 삭제시 Image 목록 동기화 및 Running으로 복구 되었을 때에도 새롭게 Image 동기화할 수 있도록 수정 by sukhee_yun
  - [ims][227875] USP crd ipRange 배열로 추가할 수 있도록 변경 by dnxorjs1

### CRD yaml
  - [crd] default_registry.yaml image 필드값 수정 by sukhee_yun

### Etc

<!--------------------- v4.1.0.33 end --------------------->

<!-------------------- v4.1.0.32 start -------------------->

## HyperCloudServer_4.1.0.32 (Fri Jun  5 09:44:17 KST 2020)

### Added

### Changed
  - [mod] nsc get 서비스 swagger 수정 by dnxorjs1
  - [mod] creator annotation nsc owner 라벨로 옮기기 구현 by dnxorjs1
  - [mod] UserDeleteWatcher.java usp 존재할 시에 지워주는 로직 추가 [mod] NameSpaceClaimHandler.java. 추가  : NSC Get 서비스 추가 by dnxorjs1
  - [mod] registry:b004 image를 쓰지 않고 dockerhub에 있는 registry image를 사용할수 있도록 수정(예: registry:2.7.1) by sukhee_yun

### Fixed

### CRD yaml

### Etc

<!--------------------- v4.1.0.32 end --------------------->

<!-------------------- v4.1.0.31 start -------------------->

## HyperCloudServer_4.1.0.31 (Fri Jun  5 04:49:27 KST 2020)

### Added

### Changed
  - [mod] swagger.yaml 수정 (CatalogServiceClaim Status Update API 추가) by seonho_choi
  - [mod] proauth-system resource quota 수정 by dnxorjs1
  - [mod] Registry subresource가 삭제되었을때 복구할 수 있도록 수정 by sukhee_yun
  - [mod][227191] resourceQuota Spec Validation 추가 by dnxorjs1
  - [mod][227307] otp 인증시간 10분으로 변경 by dnxorjs1
  - [mod] hypercloud_install_guide proauth-db 최초기동 확인법 추가 by dnxorjs1
  - [mod] cluster.x-k8s resources 추가 by GitHub

### Fixed
  - [ims][223613] Catalog Service Claim 기능 추가 by seonho_choi
  - [ims][226888] Registry Status 관리 로직 수정 (container가 Ready가 아닌 상태인 경우에만 NotReady) by sukhee_yun
  - [ims][223234] operator 기동시 registry와 image 목록 동기화 안되는 버그 수정 by sukhee_yun

### CRD yaml
  - [crd] CatalogServiceClaim CRD 추가 (ims-223613) by seonho_choi
  - [crd] Registry tolerationSeconds integer 유효 범위로 수정(0 ~ 2147483647) by sukhee_yun

### Etc

<!--------------------- v4.1.0.31 end --------------------->

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
