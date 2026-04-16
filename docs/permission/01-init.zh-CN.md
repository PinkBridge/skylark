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
注意将 localhost 替换成实际部署的服务器ip或者域名

3. **首次初始化（初始化向导）**

当系统处于“未初始化”状态时，管理端会自动跳转到初始化向导页面：`http://localhost:9527/init`。

- **第 1 步：配置默认租户**
  - **域名**为必填，可填写域名或 `IP:端口`，例如 `example.com` 或 `192.168.1.10:8080`。
  - **Logo** 为必填，请在表单中上传图片。
- **第 2 步：配置最高管理员账号**
  - 设置最高管理员的**用户名**与**密码**。
- **提交完成**
  - 提交成功后系统会跳转到欢迎页：`http://localhost:9527/welcome`。

4. **查看模块日志**

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

5. **数据库初始化说明**

首次启动时 MySQL 会初始化数据目录；`permission` 会自动执行 Flyway 迁移，创建表结构与基础数据。

### 重新执行初始化（可选）

如需在本地/开发环境重新执行初始化向导，可在 MySQL 中重置初始化状态：

```sql
UPDATE sys_platform_init_state
SET status = 'PENDING', finished_at = NULL
WHERE id = 1;
```

### 验证清单

- 容器运行正常（`docker compose ps`）
- 权限服务已连接数据库（日志无连接异常）
- 管理端页面可以打开

### 常见问题

- **首次构建很慢**：依赖下载属正常现象。
- **改库名不生效**：`MYSQL_DATABASE` 只在首次初始化生效；如需重建需 `docker compose down -v`（会清空数据）。

