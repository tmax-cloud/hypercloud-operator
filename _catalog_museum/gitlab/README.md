# GitLab Template Guide

GitLab의 정상적인 작동을 위해 GitLab 생성 이후 `external_url` 변수를 지정하는 방법은 아래와 같음.

1.  Template 생성
```bash
kubectl apply -f template.yaml
```

2. TemplateInstance 생성
```bash
kubectl apply -f instance.yaml
```

3. Deployment 수정
(2단계에서 APP_NAME이 gitlab-test-deploy로 설정되고 SERVICE_TYPE이 LoadBalancer로 설정됨을 가정)
```bash
APP_NAME=gitlab-test-deploy
IP=$(kubectl get svc gitlab-test-deploy-service -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
kubectl set env deployment $APP_NAME GITLAB_OMNIBUS_CONFIG="external_url 'http://$IP/';"
```

