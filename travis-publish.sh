#!/bin/bash

if [ ${TRAVIS_PULL_REQUEST} = 'false' ] && [ ${TRAVIS_BRANCH} = 'develop' ]; then
  ./gradlew -PossrhUsername="${SONATYPE_USERNAME}" -PossrhPassword="${SONATYPE_PASSWORD}" build uploadArchives -x functionalTest
elif [ ${TRAVIS_PULL_REQUEST} = 'false' ] && [ ${TRAVIS_BRANCH} = 'master' ]; then
  ./gradlew -PossrhUsername="${SONATYPE_USERNAME}" -PossrhPassword="${SONATYPE_PASSWORD}" -PsigningKeyId="${SIGNING_KEY_ID}" -PsigningPassword="${SIGNING_PASSWORD}" -PsigningSecretKeyRingFile="$(pwd)/local.secring.gpg" uploadArchives closeAndReleaseRepository -x functionalTest
fi