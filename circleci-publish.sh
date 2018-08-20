#!/bin/bash

if [[ ! -z ${CIRCLE_PULL_REQUEST} ]] && [ ${CIRCLE_BRANCH} = 'develop' ]; then
  ./gradlew uploadArchives -x functionalTest -PossrhUsername="${SONATYPE_USERNAME}" -PossrhPassword="${SONATYPE_PASSWORD}"
elif [[ ! -z ${CIRCLE_PULL_REQUEST} ]] && [ ${CIRCLE_BRANCH} = 'master' ]; then
  ./gradlew uploadArchives closeAndReleaseRepository -x functionalTest -PossrhUsername="${SONATYPE_USERNAME}" -PossrhPassword="${SONATYPE_PASSWORD}" -PsigningKeyId="${SIGNING_KEY_ID}" -PsigningPassword="${SIGNING_PASSWORD}" -PsigningSecretKeyRingFile="$(pwd)/local.secring.gpg"
fi