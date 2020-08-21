#!/bin/bash
nscList=`kubectl get nsc -o=jsonpath='{.items[?(@.status.status == "Awaiting")].metadata.name}'`

for nsc in $nscList
do
        kubectl patch nsc $nsc --type='json' -p='[{"op": "replace", "path": "/metadata/labels/handled", "value":"f"}]'
done