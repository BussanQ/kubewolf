cd "$(dirname "$0")" >/dev/null 2>&1 || exit
source common.sh
[ -d /etc/docker/ ] || mkdir /etc/docker/ -p
lsb_dist=$(get_distribution)
lsb_dist="$(echo "$lsb_dist" | tr '[:upper:]' '[:lower:]')"
if ! command_exists docker; then
  echo "current system is $lsb_dist"
  case "$lsb_dist" in
  alios)
    ip link add name docker0 type bridge
    ip addr add dev docker0 172.17.0.1/16
    ;;
  esac

  cp ../etc/docker.service /etc/systemd/system/
  tar --strip-components=1 -zxvf ../cri/docker.tgz -C /usr/bin
  # shellcheck disable=SC2046
  chmod a+x $(tar -tf ../cri/docker.tgz | while read -r binary; do echo "/usr/bin/${binary##*/}"; done | xargs)
  systemctl enable docker.service
  systemctl restart docker.service
  cp ../etc/daemon.json /etc/docker
fi
if command_exists nvidia-smi; then
	echo "current system is $lsb_dist"
	case "$lsb_dist" in
		ubuntu|deepin|debian|raspbian)
			dpkg -i --force-all ../cri/nvidia-docker/ubuntu/*.deb
		;;
		centos|rhel|ol|sles)
			rpm -Uvh --force --nodeps ../cri/nvidia-docker/centos/*.rpm
		;;

		*)
			echo "current system not support"
			exit 1
		;;
	esac
  cp ../etc/nvidia-daemon.json /etc/docker/daemon.json
fi
systemctl daemon-reload
systemctl restart docker.service
check_status docker
logger "init docker/nvidia-docker success"