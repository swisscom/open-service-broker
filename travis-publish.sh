#!/bin/bash

if [ ${TRAVIS_PULL_REQUEST} = 'false' ] && [ ${TRAVIS_BRANCH} = 'develop' ]; then
  ./gradlew uploadArchives -x functionalTest -PossrhUsername="${SONATYPE_USERNAME}" -PossrhPassword="${SONATYPE_PASSWORD}"
elif [ ${TRAVIS_PULL_REQUEST} = 'false' ] && [ ${TRAVIS_BRANCH} = 'master' ]; then
  ./gradlew uploadArchives closeAndReleaseRepository -x functionalTest -PossrhUsername="${SONATYPE_USERNAME}" -PossrhPassword="${SONATYPE_PASSWORD}" -PsigningKeyId="${SIGNING_KEY_ID}" -PsigningPassword="${SIGNING_PASSWORD}" -PsigningSecretKeyRingFile="$(pwd)/local.secring.gpg"
fi