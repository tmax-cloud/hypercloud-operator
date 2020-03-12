#!/bin/sh

nohup /usr/bin/java -jar /home/tmax/hypercloud4-server/hypercloud4-server.jar >> /home/tmax/hypercloud4-server/stdout.log &

tail -f /dev/null