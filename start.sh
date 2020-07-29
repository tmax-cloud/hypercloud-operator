#!/bin/sh

if [ -z ${LOG_INFO_MAX_HISTORY} ] || [ ${LOG_INFO_MAX_HISTORY} -lt 0 ];then
	LOG_INFO_MAX_HISTORY=30
fi

if [ -z ${LOG_DEBUG_MAX_HISTORY} ] || [ ${LOG_DEBUG_MAX_HISTORY} -lt 0 ];then
	LOG_DEBUG_MAX_HISTORY=1
fi

export K8S_HOME=/home/tmax/hypercloud4-operator
/usr/bin/java -jar -Dlogback.configurationFile=${K8S_HOME}/logback.xml ${K8S_HOME}/lib/hypercloud4-operator.jar
