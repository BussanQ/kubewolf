#!/usr/bin/env bash
set -e
A="envoygateway"
NAME=${NAME:-$A}
NAMESPACE=${NAMESPACE:-$A}
CHARTS=${CHARTS:-"./charts/gateway-helm-1.3.2.tgz"}
HELM_OPTS=${HELM_OPTS:-""}

helm upgrade -i ${NAME} ${CHARTS} -n ${NAMESPACE} --create-namespace ${HELM_OPTS}