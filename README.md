# Canvas Workflow MVP (Java + Spring Boot)

实现了一个后端 MVP：可视化编排画布的数据模型 + 可执行工作流引擎（DAG）。

## 能力范围
- 资源管理：
  - MySQL 连接（密码加密存储）
  - VPS 主机（私钥加密存储）
- 工作流管理：保存 `nodes + edges` 画布 JSON
- 图校验：节点类型、端口类型匹配、必填配置、无环检测
- 运行引擎：按拓扑执行节点，记录 run / node run / node logs
- 基础节点：
  - `mysql_connection_ref`
  - `mysql_query`（MVP 仅允许 SELECT）
  - `vps_host_ref`
  - `ssh_command`

## 运行
```bash
mvn spring-boot:run
```

## 默认配置
- DB: H2 文件数据库 `./canvasdb`
- 密钥主密码：`CANVAS_MASTER_KEY`（默认 dev 值，仅开发环境）

## 核心 API
- `POST /api/mysql-connections`
- `GET /api/mysql-connections`
- `POST /api/vps-hosts`
- `GET /api/vps-hosts`
- `POST /api/workflows`
- `GET /api/workflows`
- `PUT /api/workflows/{id}`
- `POST /api/workflows/{id}/validate`
- `POST /api/workflows/{id}/runs`
- `GET /api/runs/{runId}`
- `GET /api/runs/{runId}/events?lastId=0`
