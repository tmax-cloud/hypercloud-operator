FROM alpine:3

LABEL maintainer="seonho_choi@tmax.co.kr"

ADD ./hypercloud4-operator /home/tmax/hypercloud4-operator
ADD ./start.sh /home/tmax/
RUN apk update ; \
	apk upgrade ; \
	apk add openjdk8 ; \
	apk add openssl ; \
	apk add bash ; \
	chmod 775 -R /home/tmax/hypercloud4-operator ; \
	chmod 775 /home/tmax/start.sh ; \
	sed -i 's/\r$//' /home/tmax/start.sh

CMD ["/bin/sh", "-c", "/home/tmax/start.sh"]
