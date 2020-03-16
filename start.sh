#!/bin/sh

nohup /usr/bin/java -jar /home/tmax/hypercloud4-operator/hypercloud4-operator.jar >> /home/tmax/hypercloud4-operator/stdout.log &

tail -f /dev/null