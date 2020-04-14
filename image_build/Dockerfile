FROM 192.168.6.110:5000/hypercloud4-operator:b4.0.0.0

LABEL maintainer="seonho_choi@tmax.co.kr"

	
# Install language pack
RUN apk --no-cache add ca-certificates wget 
RUN wget -q -O /etc/apk/keys/sgerrand.rsa.pub https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub 
RUN wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.25-r0/glibc-2.25-r0.apk 
RUN wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.25-r0/glibc-bin-2.25-r0.apk 
RUN wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.25-r0/glibc-i18n-2.25-r0.apk 
RUN apk add glibc-bin-2.25-r0.apk glibc-i18n-2.25-r0.apk glibc-2.25-r0.apk

ENV LANG=en_US.UTF-8 \
    LANGUAGE=en_US.UTF-8

ADD ./hypercloud4-operator /home/tmax/hypercloud4-operator
ADD ./start.sh /home/tmax/
RUN chmod 775 -R /home/tmax/hypercloud4-operator ; \
	chmod 775 /home/tmax/start.sh ; \
	sed -i 's/\r$//' /home/tmax/start.sh

CMD ["/bin/sh", "-c", "/home/tmax/start.sh"]
