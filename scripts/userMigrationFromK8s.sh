#!/bin/bash

hyperAuthServer=$1
echo hyperAuthServer : $hyperAuthServer

# get Admin Token
token=$(curl -X POST 'http://'$hyperAuthServer'/auth/realms/master/protocol/openid-connect/token' \
 -H "Content-Type: application/x-www-form-urlencoded" \
 -d "username=admin" \
 -d 'password=admin' \
 -d 'grant_type=password' \
 -d 'client_id=admin-cli' | jq -r '.access_token')


echo accessToken : $token

# User Registration
userIdList=`kubectl get user -o=jsonpath='{.items[*].metadata.name}'`

for userId in $userIdList

do
	# Get User Meta
	email=`kubectl get user $userId -o=jsonpath='{.userInfo.email}'`
	dateOfBirth=`kubectl get user $userId -o=jsonpath='{.userInfo.dateOfBirth}' | tr -d ' '`
	phone=`kubectl get user $userId -o=jsonpath='{.userInfo.phone}' | tr -d ' '`
	department=`kubectl get user $userId -o=jsonpath='{.userInfo.department}' | tr -d ' '`
	position=`kubectl get user $userId -o=jsonpath='{.userInfo.position}' | tr -d ' '`
	description=`kubectl get user $userId -o=jsonpath='{.userInfo.description}' | tr -d ' '`

	echo userId : $userId
	echo email : $email
	echo dateOfBirth : $dateOfBirth
	echo phone : $phone
	echo department : $department
	echo description : $description
	
	curl -g -i -X POST \
   -H "Content-Type:application/json" \
   -H "Authorization:Bearer $token" \
   -d \
		'{
		  "enabled": true,
		  "attributes": {
		  	"dateOfBirth": "'$dateOfBirth'",
		  	"phone": "'$phone'",
		  	"department": "'$department'",
		  	"description": "'$description'"
		  },
		  "username": "'$userId'",
		  "emailVerified": "",
		  "email": "'$email'"
		}' \
		 'http://'$hyperAuthServer'/auth/admin/realms/tmax/users' 
	echo
	echo user registration of $userId Complete!!
	echo ---------------------------------------------------
	echo
done