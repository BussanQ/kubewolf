# 本地运行与升级

## 启动与账户

连接池由 JFinal `HikariCpPlugin` 管理，ActiveRecord 与 Flyway 共用同一个 DataSource。启动顺序为连接池 → Flyway 迁移 → ActiveRecord；退出时停止 ActiveRecord 并关闭连接池。项目不依赖 `spring-boot-starter-jdbc`，原有 `spring.datasource.hikari` 配置继续生效。

需要 JDK 23、Maven、MySQL 和 Kubernetes。先创建空数据库 `bqinfra`，应用启动时由 Flyway 建表及升级；不要重新导入包含 `DROP TABLE` 的 `res/bqinfra.sql`。

```sh
export MYSQL_URL='jdbc:mysql://127.0.0.1:3306/bqinfra?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true'
export MYSQL_USERNAME=root
export MYSQL_PASSWORD='本机数据库密码'
export K8S_CONFIG="$HOME/.kube/config"
export K8S_NAMESPACE=default
SERVER_ADDRESS=0.0.0.0 ./scripts/run-local.sh
```

脚本默认使用 `local` 配置，监听 `127.0.0.1:8081`，关闭网关注册。`SERVER_ADDRESS=0.0.0.0` 允许其他设备访问。已有 JAR 可用 `SKIP_BUILD=true` 跳过构建。再次构建前先停止使用 `target/` 下同一 JAR 的进程，避免运行中覆盖归档。终止进程会关闭 HTTP、数据库连接池、Informer 和 Kubernetes 客户端。

管理员用户名为 `admin`。可设置 `KUBEWOLF_ADMIN_PASSWORD`（至少 12 字符）；未设置时首次启动会生成随机密码，保存在 `~/.kubewolf/admin-password`，文件权限为 `0600`。可用 `KUBEWOLF_VIEWER_PASSWORD` 启用只读 `viewer` 账号。密码、kubeconfig 和令牌不要提交到仓库；远程访问应经 HTTPS 反向代理。

页面登录后自动携带 CSRF 校验。API 客户端需保存会话 Cookie，先 GET `/api/auth/csrf`，再携带返回的 `headerName`/`token` POST `/login`（表单字段 `userName`、`passWord`）；登录成功后重新获取 CSRF token，用于后续写请求。GET `/api/v1/health` 返回数据库、集群同步和连接池状态，需要登录。写操作审计记录在应用日志的 `AUDIT` 行。

## 现有数据库升级

1. 停止旧版本写入并备份数据库。检查重复服务名：`SELECT task_name, COUNT(*) FROM serve_task GROUP BY task_name HAVING COUNT(*) > 1;`。先人工处理重复记录，避免唯一约束迁移失败。
2. 启动新版本。Flyway 对无历史记录的旧库以版本 0 建立基线，再执行 V1、V2、V3；V1 使用 `CREATE TABLE IF NOT EXISTS`，V2 增量添加生命周期字段、唯一约束、索引及模型外键，并修正创建时间自动更新问题；V3 增量添加模型版本、描述、默认镜像和启动命令字段。
3. 检查 `flyway_schema_history` 和健康接口。MySQL DDL 不支持整体事务回滚；迁移失败应核对已执行语句并从备份恢复或人工修复后再启动，不能盲目重跑原始建表脚本。

旧任务初始状态为 `unmanaged/unknown`，不会自动修改集群；显式启动、停止、删除后才接管。升级前应核对原命名空间、资源名称、所有权标签及旧网关渠道，保存渠道 ID 到 `gateway_channel_id` 并设置 `gateway_registered=1`。缺少归属标签或网关名称不匹配时，应用保留记录并报错，须先人工核实归属，避免操作无关资源。不要在仍有任务时直接切换 `K8S_NAMESPACE`。

新增生命周期字段手写在 `ServeTask` 扩展类中；没有修改生成的 `Base*` 和 `_MappingKit`。重新运行 `_JFinalGenerator.java` 时应检查生成差异。

## 部署、停止与删除

模型版本 `version`、描述 `description` 与 PVC 配置独立保存。模型登记时可不填写 `code`；部署时 `code` 必须是目标命名空间中已有 PVC 的名称，`modelPath` 必须为容器内绝对路径（默认 `/model`），框架命令使用同一路径。模型的默认镜像和启动命令为空时采用框架默认值；指定值仅在部署框架与模型配置一致时生效，切换框架使用所选框架的默认值。支持 vLLM、SGLang，镜像可通过 `VLLM_IMAGE`、`SGLANG_IMAGE` 设置；模型部署默认每副本 1 GPU、1000m CPU、16000Mi 内存。服务编辑/API 可调整资源，GPU 扩展资源名称默认 `nvidia.com/gpu`。

写接口返回“已受理”，不表示模型已就绪。任务保存 `desiredState`、`actualStatus`、`lastError`、重试次数及渠道 ID；后台批量调谐，错误按 5 秒至 300 秒退避重试。页面显示异步状态。启动探针允许模型加载，推理就绪后才注册网关。

停止/删除都会清理 Deployment、Service 和网关渠道；网关故障时先释放计算资源，保留任务及错误，恢复后继续清理。删除只有在资源和渠道均清理后才移除数据库记录。重复操作幂等，进程重启后会恢复未完成的任务。数据库会话锁串行化同一任务的调谐，版本号避免旧结果覆盖新请求。

启用 One-API 兼容网关需设置 `ONEAPI_ENABLED=true`、`ONEAPI_URL`、`ONEAPI_ACCESS_TOKEN`。实现使用 `/api/channel/` 分页及 CRUD，校验 HTTP 状态和 `success` 字段，并处理查询不存在渠道时的 `record not found` 业务响应（参照 [One-API 控制器](https://github.com/songquanpeng/one-api/blob/main/controller/channel.go)）。网关必须能够访问集群内 Service DNS。已有已注册任务时不可直接关闭网关配置，应先停止相关任务并确认渠道清理。

## 必要模块测试

```sh
mvn -Dtest=TaskValidationTest,FrameWorkServiceTest,ServingResourcesTest,TaskReconcilerTest,ApiSecurityTest,GatewayServiceTest,HttpKitTest,ServeServiceListTest test
```

只运行受影响模块测试。真实推理还需要 GPU、模型 PVC 和网关；本地 CPU 容器联调不等同于 vLLM/SGLang 推理验证。

数据库配置变更可单独运行 `DatabaseLifecycleTest`：先准备可丢弃的空 MySQL 数据库，再设置 `KUBEWOLF_TEST_DB_URL`、`KUBEWOLF_TEST_DB_USERNAME`、`KUBEWOLF_TEST_DB_PASSWORD`，执行 `mvn -Dtest=DatabaseLifecycleTest test`。测试验证实际迁移、ActiveRecord 读写、连接池配置和关闭；未设置测试数据库 URL 时跳过。


## 主页真实数据

`/console` 保留原有统计卡和图表，在页面加载时请求只读接口 `GET /api/v1/console`；沿用现有登录及只读角色权限。页面没有新增按钮、统计项或定时刷新功能。

- `imageCount`：已登记模型总数，与模型管理列表一致，不用 Pod 数量或副本数量替代。
- `nodes`、`cpu`、`memory`：集群 Node 数、`status.capacity` 的 CPU 总核数和内存总量（GiB）。
- `cpuRate`、`memRate`、`memoryUsed`：优先读取 Metrics API 的节点用量；不可用或节点数据缺失时，经 API Server 的 `nodes/{name}/proxy/stats/summary` 读取 `usageNanoCores` 和 `workingSetBytes`。利用率为集群总用量除以总容量，不平均各节点百分比。
- 超过 5 分钟的采样不参与计算；任一节点缺少某项用量，该项集群用量/比例标记为未知，其他可用指标仍返回。真实的零值正常展示。
- 当前集群没有 GPU 利用率监控源，`gpuRate` 为未知，原图表位置显示“数据异常”；不以 GPU 资源请求数或已分配数量冒充 GPU 利用率。标准 Metrics API 仅包含 CPU 和内存，见 [Kubernetes 资源指标文档](https://kubernetes.io/docs/tasks/debug/debug-cluster/resource-metrics-pipeline/)。

集群查询使用现有 Kubernetes 客户端与认证配置：需要 `nodes` 的 `list` 权限；Metrics API 需要 `metrics.k8s.io` 中 `nodes` 的 `list` 权限；节点统计回退需要 `nodes/proxy` 的 `get` 权限。不会自动安装监控组件或修改集群权限。接口无法获取的字段为空或省略，页面用 `--` / 原“数据异常”提示展示。
