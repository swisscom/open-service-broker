#!/bin/bash

if [[ -z ${CIRCLE_PULL_REQUEST} ]] && [ ${CIRCLE_BRANCH} = 'develop' ]; then
  ./gradlew publish -x functionalTest -PossrhUsername="${SONATYPE_USERNAME}" -PossrhPassword="${SONATYPE_PASSWORD}"
elif [[ -z ${CIRCLE_PULL_REQUEST} ]] && [ ${CIRCLE_BRANCH} = 'master' ]; then
  ./gradlew publish -x functionalTest -PossrhUsername="${SONATYPE_USERNAME}" -PossrhPassword="${SONATYPE_PASSWORD}" -PsigningSecretKeyRingFile="$(pwd)/local.secring.gpg"
fi
