#!/bin/bash

## THIS FILE WILL BE EXECUTED ON REMOTE NODE BY GRADLE WHEN 'deployPrivateAndTest' TASK EXECUTED.
## REMOTE NODE IS DENOTED BY 'gradle.properties' file.
## $1 = db user account, $2 = db user password 

dbId=$1
dbPw=$2

sed -i 's/<user>impadmin<\/user>/<user>'"${dbId}"'<\/user>/g' /jeus8/domains/jeus_admin/config/domain.xml
sed -i 's/<password>cloudi1<\/password>/<password>'"${dbPw}"'<\/password>/g' /jeus8/domains/jeus_admin/config/domain.xml