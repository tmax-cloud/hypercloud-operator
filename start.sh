#!/bin/sh

export K8S_HOME=/home/tmax/hypercloud4-operator
/usr/bin/java -jar -Dlogback.configurationFile=${K8S_HOME}/logback.xml ${K8S_HOME}/lib/hypercloud4-operator.jar
