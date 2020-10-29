#!/bin/bash
nscList=`kubectl get nsc -o=jsonpath='{.items[?(@.metadata.labels.owner)].metadata.name}'`

for nsc in $nscList
do
	kubectl annotate nsc $nsc owner=`kubectl get nsc $nsc -o=jsonpath='{.metadata.labels.owner}'`
	kubectl patch nsc $nsc --type='json' -p='[{"op": "remove", "path": "/metadata/labels/owner"}]'
done
