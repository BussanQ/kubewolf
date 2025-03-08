#!/usr/bin/env bash
set -e

NAME=${NAME:-"hami"}
NAMESPACE=${NAMESPACE:-"hami"}
CHARTS=${CHARTS:-"./charts/hami"}
HELM_OPTS=${HELM_OPTS:-""}

helm upgrade -i ${NAME} ${CHARTS} -n ${NAMESPACE} --create-namespace ${HELM_OPTS}