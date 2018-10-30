#!/usr/bin/env bash
# fail on error
set -e

if [ -z "$GITHUB_USER" ]; then
    echo "ERROR: Environment variable GITHUB_USER is not set"
    exit 1
fi

if [ -z "$GITHUB_PASSWORD" ]; then
    echo "ERROR: Environment variable GITHUB_PASSWORD is not set"
    exit 1
fi

echo "default login ${GITHUB_USERNAME} password ${GITHUB_PASSWORD}" > ${HOME}/.netrc
# trace shell
set -x

git config --global user.email "cicd@concourse.ci"
git config --global user.name "${GITHUB_USERNAME}"

export WORKING_DIR=${PWD}
export VERSION=$(awk -F'=' '/^version\=/ { print $2 }' gradle.properties)
export VERSION_ARR=( ${VERSION//./ } )

# Checks CHANGELOG.md for type of release (major, minor or patch)
if [ "${INCREASE}" == "dynamic" ]; then
CHANGELOGS=$(cat $(pwd)/CHANGELOG.md | awk '{print toupper($0)}' | sed -n '/^## \[UNRELEASED\]/,/^## \[/p' | sed '1d;$d;/^$/d')
    if echo "$CHANGELOGS" | egrep -q "^\- \[MAJOR\] "; then
        INCREASE="major"
    elif echo "$CHANGELOGS" | egrep -q "^\- \[MINOR\] "; then
        INCREASE="minor"
    elif [ $(echo "$CHANGELOGS" | sed '/^\s*$/d' | wc -l) -gt 0 ]; then
        INCREASE="patch"
    else
        echo "No changelogs in Unreleased, please add changelogs before releasing"
        exit 0
    fi
fi

VERSION_ARR[2]=$(echo ${VERSION_ARR[2]} | awk -F'-' '{print $1}')
if [ "${INCREASE}" == "major" ]; then
    ((VERSION_ARR[0]+=1))
    VERSION_ARR[1]=0
    VERSION_ARR[2]=0
elif [ "${INCREASE}" == "minor" ]; then
    ((VERSION_ARR[1]+=1))
    VERSION_ARR[2]=0
fi

RELEASE_VERSION="${VERSION_ARR[0]}.${VERSION_ARR[1]}.${VERSION_ARR[2]}"

((VERSION_ARR[2]+=1))
NEXT_DEV_VERSION="${VERSION_ARR[0]}.${VERSION_ARR[1]}.${VERSION_ARR[2]}-SNAPSHOT"

./scripts/release.sh ${RELEASE_VERSION} ${NEXT_DEV_VERSION}
