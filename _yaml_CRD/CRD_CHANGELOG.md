# HyperCloudServer CRD changelog!!

<!-------------------- v4.1.0.8 start -------------------->

## HyperCloudServer_4.1.0.8

### Added

### Changed

### Fixed
NamespaceClaimCRD : resourceName 추가 ( 필수값 ), 관련 default, example yaml에 반영
ResourceQuotaClaimCRD : resourceName 추가 ( 필수값 ), 관련 default, example yaml에 반영
RoleBindingClaimCRD : resourceName 추가 ( 필수값 ), 관련 default, example yaml에 반영

### Etc

<!--------------------- v4.1.0.8 end --------------------->

<!------------------------ README ------------------------>

## CRD Version 관리
- 목적 : Version 별 CRD 관련 yaml 들을 저장함으로써 yaml 관리
- CRD yaml 수정 방법
	- '_next_version' 및 'latest' 폴더에 수정 사항 반영
	- CRD_CHANGELOG.md 에 수정 내용 명시
- 배포 시 '_next_version' 폴더는 '{version}' 이름으로 복사됨

<!------------------------ README ------------------------>
