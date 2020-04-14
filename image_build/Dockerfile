FROM 192.168.6.110:5000/hypercloud4-operator:b4.0.0.0

LABEL maintainer="seonho_choi@tmax.co.kr"

ADD ./hypercloud4-operator /home/tmax/hypercloud4-operator
ADD ./start.sh /home/tmax/
RUN chmod 775 -R /home/tmax/hypercloud4-operator ; \
	chmod 775 /home/tmax/start.sh ; \
	sed -i 's/\r$//' /home/tmax/start.sh

CMD ["/bin/sh", "-c", "/home/tmax/start.sh"]
