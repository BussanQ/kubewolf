#!/bin/sh
# 系统+gpu插件
sealos run registry.cn-beijing.aliyuncs.com/bqai/kubernetes:v1.28.9-gpu registry.cn-shanghai.aliyuncs.com/labring/calico:v3.28.1 --single
# 算力hami + 存储seaweedfs + 网络网关aibrix
sealos run registry.cn-beijing.aliyuncs.com/bqai/app:hami_2.4.1
sealos run registry.cn-beijing.aliyuncs.com/bqai/app:seaweedfs_3.85 registry.cn-beijing.aliyuncs.com/bqai/app:seaweedfs-csi-driver_20250315
sealos run registry.cn-beijing.aliyuncs.com/bqai/app:aibrix_v0.2.1