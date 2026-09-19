#!/bin/sh
# Run from any directory; JAVA_HOME/PATH must point to JDK 23 and Maven.
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-local}"
export K8S_CONFIG="${K8S_CONFIG:-$HOME/.kube/config}"
export SERVER_ADDRESS="${SERVER_ADDRESS:-127.0.0.1}"
export SERVER_PORT="${SERVER_PORT:-8081}"
if [ "${SKIP_BUILD:-false}" != "true" ]; then
    mvn -B -ntp -DskipTests package
fi
exec java -jar target/kubewolf-1.0.0-SNAPSHOT.jar "$@"
