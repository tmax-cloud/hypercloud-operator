#!/bin/bash
# by seonho_choi

# destination file
version=$1

if [ -z $version ]; then
version=4.1.0.0
fi

# destination directory
latestDir="_yaml_CRD/latest"
nextDir="_yaml_CRD/$version"

echo "next crd directory : $nextDir"

if [ -d $nextDir ]; then
echo "delete next crd version directory"
rm -rf $nextDir
fi

echo "copy next crd version directory"
cp -R $latestDir $nextDir

echo "sed ${latest} to version"
echo "file : '*CRD*.yaml'"
find $nextDir/ -type f -name "*CRD*.yaml" -exec sed -i "s/operator.version: %latest%/operator.version: $version/g" {} \;

echo "!!!done"
