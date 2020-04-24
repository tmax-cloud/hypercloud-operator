# Private Package Server
Private package server는 클러스터 내부에 배포되는 패키지 서버로, 폐쇄망 환경에서도 CI/CD 기능의 정상 작동이 가능함.

현재는 아래의 언어/패키지 서버를 지원함.
* Python - Pypi  
* Pyton - devpi (추천)  
: pypi.org 자동 미러링 가능, `192.168.6.110:5000/devpi:latest` 이미지 필요
* Node.js - Verdaccio  
: npmjs.org 자동 미러링 가능
* Java(maven) - Nexus3  
: Maven central repository 자동 미러링 가능

#### 주의: 폐쇄망 환경에서는 위 패키지 서버를 배포한 이후 CI/CD에 필요한 패키지들을 해당 서버에 배포해 주어야 CI/CD가 정상 작동함.

## Usage Guide
`아래 예시는 Python-devpi를 이용하는 예시`
### Step.1 Devpi 서버 생성
1. devpi 서버 생성
```bash
kubectl apply -f python-devpi/template.yaml
kubectl apply -f python-devpi/instance.yaml
```
2. devpi 서비스 IP 확인
```bash
kubectl get svc devpi-private-1-svc -o jsonpath='{.spec.clusterIP}'
```
### Step.2 Django CI/CD 파이프라인 생성
1. Django CI/CD 파이프라인 생성
```bash
kubectl apply -f ../was/django/django-template.yaml
```
2. Django CI/CD Template Instance 수정
```bash
vi ../was/django/django-instance.yaml
```
```yaml
...
spec:
  template:
    ...
    parameters:
    ...
    - name: PACKAGE_SERVER_URL
      value: [URL]
...
```
[URL]은 패키지 서버 별로 다르며, 아래와 같음.  
([ip]는 Step.1-2에서 확인한 서비스 IP)
* Python-pypi : `http://[ip]:8080`
* Python-devpi : `http://[ip]:3141/root/pypi`
* Node.js-verdaccio : `http://[ip]:4873`
* Java-nexus: `http://[ip]:8081/repository/maven-central`

3. Django CI/CD 파이프라인 실행
```bash
kubectl apply -f ../was/django/django-instance.yaml
```

본 과정을 통해 생성된 Pipeline 실행 시 (PipelineRun 생성 시) 항상 설정된 package server를 이용함.
