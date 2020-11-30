# GitLab Template Guide

1. Template 생성
```bash
kubectl apply -f template.yaml
```

2. TemplateInstance 생성
```bash
kubectl apply -f instance.yaml
```

### Parameter 설명
- APP_NAME  
: GitLab Deployment 제목

- STORAGE  
: GitLab용 PersistentVolumeClaim 크기

- SERVICE_TYPE  
: GitLab용 서비스 종류 (ClsuterIP/NodePort/LoadBalancer/Ingress)

- WEB_NODE_IP
: SERVICE_TYPE에 NodePort를 사용하는 경우, 깃랩 접속에 사용할 IP 명시.
: 명시되지 않을 경우 Pod가 배치된 노드의 IP 사용 (접속 자체는 모든 Node의 IP를 통해 가능함)

- WEB_NODE_PORT  
: SERVICE_TYPE에 NodePort를 사용하는 경우, 사용할 NodePort 명시.
: 명시되지 않을 경우 자동으로 배정되는 NodePort 사용 (Service 객체를 통해 포트 확인 필요)

- INGRESS_HOST  
: SERVICE_TYPE에 Ingress를 사용하는 경우, Host 명시.
: 명시되지 않을 경우 `<APP_NAME>.<네임스페이스>.<Ingress 컨트롤러 IP>.nip.io` 사용

- SSH_PORT  
: SSH 포트 포워딩
: 명시되지 않을 경우 2221 포트 사용
