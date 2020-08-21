#!/bin/bash
id=$1
pw=`echo -n $2 | openssl sha512 | cut -f 2 -d '=' | sed -e 's/^ *//g' -e 's/ *$//g'`
apiserver=$3

echo id : $id
echo pw : $pw
echo apiserver : $apiserver

result=$(curl 'http://'$apiserver'/login' \
  -H 'Content-Type: application/json' \
  --data-binary $'{"id":'\"$id\"',"password":'\"$pw\"'}' \
  --insecure)

echo \< hctoken \> 
echo $result | grep accessToken | cut -f 2 -d ':' | cut -f 2 -d '"' | sed 's/"/ /g' | sed -e 's/^ *//g' -e 's/ *$//g'

hctoken=$(echo $result | grep accessToken | cut -f 2 -d ':' | cut -f 2 -d '"' | sed 's/"/ /g' | sed -e 's/^ *//g' -e 's/ *$//g')

export hctoken

