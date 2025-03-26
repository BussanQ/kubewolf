#!/bin/sh
# k8s + gpu插件
sealos run registry.cn-beijing.aliyuncs.com/bqai/kubernetes:v1.28.9-gpu registry.cn-shanghai.aliyuncs.com/labring/calico:v3.28.1 --single
# 算力hami + 存储seaweedfs + envoy网关
sealos run registry.cn-beijing.aliyuncs.com/bqai/publicapp:hami_2.4.1
sealos run registry.cn-beijing.aliyuncs.com/bqai/publicapp:seaweedfs_3.85 registry.cn-beijing.aliyuncs.com/bqai/publicapp:seaweedfs-csi-driver_20250315
sealos run registry.cn-beijing.aliyuncs.com/bqai/publicapp:envoygateway_v1.3.2