## 01. 系统初始化

[English](01-init.md) | 简体中文

### 目标

从零启动 `permission` 子系统，并完成最小可用验证。

### 前置条件

- Windows：Docker Desktop
- Linux：Docker + docker compose

### 操作步骤

1. **启动全部服务**

```bash
docker compose up -d --build
```

2. **验证基础访问**

- 管理端：`http://localhost:9527`（默认）
- 网关：`http://localhost:80`（默认）
- 权限服务：`http://localhost:19527`（默认）

   **系统默认账号**
   - 用户名：`superadmin`
   - 密码：`123456`

3. **查看模块日志**

```bash
# 查看全部服务日志（持续跟踪）
docker compose logs -f

# 仅查看 permission 服务
docker compose logs -f permission

# 仅查看网关
docker compose logs -f gateway

# 仅查看数据库
docker compose logs -f mysql

# 查看 permission 最近 200 行
docker compose logs --tail=200 permission
```

4. **数据库初始化说明**

首次启动时 MySQL 会初始化数据目录；`permission` 会自动执行 Flyway 迁移，创建表结构与基础数据。

### 验证清单

- [ ] 容器运行正常（`docker compose ps`）
- [ ] 权限服务已连接数据库（日志无连接异常）
- [ ] 管理端页面可以打开

### 常见问题

- **首次构建很慢**：依赖下载属正常现象。
- **改库名不生效**：`MYSQL_DATABASE` 只在首次初始化生效；如需重建需 `docker compose down -v`（会清空数据）。

