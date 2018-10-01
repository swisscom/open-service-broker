#!/bin/bash -e
# Modified by Swisscom (Schweiz) AG) on 27th of March 2018

cd `dirname $0`/..

if [ "$#" -lt 2 ]; then
    echo "Usage: $(basename $0) osb_release_version osb_next_dev_version [branch_to_release_from/develop] [branch_to_push_to/master]"
    exit 1
fi

branch_to_release_from=develop
branch_to_push_to=master

if [ "$#" -ge 3 ]; then
    branch_to_release_from=$3
fi

if [ "$#" -ge 4 ]; then
    branch_to_push_to=$4
fi

if [[ -n $(git status -s) ]]; then
    echo "ERROR: Untracked files in directory. Perform 'git status' and modify any files."
    exit 1
fi

if [ -z "$GITHUB_USER" ]; then
    echo "ERROR: Environment variable GITHUB_USER is not set"
    exit 1
fi

if [ -z "$GITHUB_PASSWORD" ]; then
    echo "ERROR: Environment variable GITHUB_PASSWORD is not set"
    exit 1
fi

echo Creating OSB release $1

set -x

CHANGELOGS=$(cat CHANGELOG.md | sed -n '/^## \[Unreleased\]/,/^## \[/p' | sed '1d;$d;/^$/d')

git checkout $branch_to_release_from
set +e
RES=$(git pull origin "$branch_to_release_from")
if [[ ! $RES =~ Already.up.to.date ]]; then
    set -e
    echo "$branch_to_release_from wasn't up-to-date prior to execution. $branch_to_release_from has been pulled now. Please ensure state of $branch_to_release_from is good before executing release."
    exit 1
fi
set -e
git checkout -b releases/$1
./scripts/set-version.sh $1
./scripts/update-changelog.sh $1
git commit --no-verify -am "Bump release version to $1"

set +x
echo Created OSB release branch releases/$1
set -x

set +e
git show-ref --verify --quiet "refs/heads/$branch_to_push_to"
if [ $? -eq 0 ]; then
    set -e
    git checkout $branch_to_push_to
else
    set -e
    git checkout -b $branch_to_push_to
fi
set +e
RES2=$(git pull origin "$branch_to_push_to")
if [[ $RES == *"fail"* ]]; then
    set -e
    echo "$branch_to_push_to failed to pull. $branch_to_push_to has been pulled now. Please ensure state of $branch_to_push_to is good before executing release."
    exit 1
fi
set -e
git merge releases/$1 --no-ff -m "Merge branch 'releases/$1'"
git tag -a v$1 -m "v$1 release of the OSB"
git push origin $branch_to_push_to --tags

git checkout $branch_to_release_from
git merge releases/$1 --no-ff -m "Merge branch 'releases/$1' into $branch_to_release_from"
git branch -d releases/$1
./scripts/set-version.sh $2
git commit --no-verify -am "Bump next $branch_to_release_from version"
git --no-pager diff origin/$branch_to_release_from
git push origin $branch_to_release_from

set +x

echo releases/$1 has been merged into $branch_to_push_to, tagged and pushed
echo
echo releases/$1 has been merged into $branch_to_release_from
echo
echo OSB version bumped to $2 on $branch_to_release_from

curl -u "${GITHUB_USER}:${GITHUB_PASSWORD}" -XPOST  https://api.github.com/repos/swisscom/open-service-broker/releases -d '{
                                                              "tag_name": "v${1}",
                                                              "target_commitish": "master",
                                                              "name": "v${1}",
                                                              "body": "${CHANGELOGS}",
                                                              "draft": false,
                                                              "prerelease": false
                                                            }'
