#!/bin/bash

if [ "$#" -lt 1 ]; then
    echo "Usage: $(basename $0) version"
    echo "Example: $(basename $0) 2.1.1"
    exit 1
fi

set -x
set -e

cd `dirname $0`/..

newline="\[Unreleased\]§§## \[$1\] - `date +%Y-%m-%d`"

sed "s/\[Unreleased\]/${newline}/" CHANGELOG.md | tr '§' '\n\n' > CHANGELOG.md.new
mv CHANGELOG.md.new CHANGELOG.md
