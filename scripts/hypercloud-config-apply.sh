#!/bin/bash

## THIS FILE WILL BE EXECUTED ON REMOTE NODE BY GRADLE WHEN 'deploy' TASK EXECUTED.
## REMOTE NODE IS DENOTED BY 'gradle.properties' file.
## IT REMOVES OLD application AND APPLIES NEW application ON THE NODE.
##
## TODO: $PO_HOME WILL BE INJECTED FROM 'gradle.properties' 
## $1 = project name, $2 = proobject_home, $3 = gitBranch, $4 = buildVersion

APP_NAME=$1
PO_HOME=$2
PO_APP_DIR=$PO_HOME/application
PO_APP_DIR_OLD=$PO_HOME/application.old
APP_HOME=$PO_APP_DIR/$APP_NAME
TAR_NAME=${APP_NAME}-$3-$4.tar

## Prior tar archive remove
priorArchives=$(ls -1 ${PO_APP_DIR}/${APP_NAME}*.tar 2> /dev/null | grep -v ${TAR_NAME} )
if [ $? == 0 ]; then
	rm $priorArchives;
fi

## Prior application migrate to application.old directory
priorApps=$(ls ${PO_APP_DIR}/${APP_NAME} 2> /dev/null)
if [ $? == 0 ]; then
	rm -r $PO_APP_DIR_OLD/$APP_NAME.old 2> /dev/null;
	mkdir -p $PO_APP_DIR_OLD/$APP_NAME.old;
	mv $APP_HOME/* $PO_APP_DIR_OLD/$APP_NAME.old/ 2> /dev/null;
else
	mkdir -p ${APP_HOME};
fi

## Unarchive tar file.
tar -xf ${PO_APP_DIR}/${TAR_NAME} -C ${APP_HOME}