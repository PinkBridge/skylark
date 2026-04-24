## 01. System initialization

English | [简体中文](01-init.zh-CN.md)

### Goal

Bring up the permission subsystem from scratch and verify that it works.

### Prerequisites

- Docker Desktop (or Docker + Compose on Linux)

### Steps

1. **Start all services**

```bash
docker compose up -d --build
```

2. **Database initialization**

On first start, MySQL will initialize the data directory, and `permission` will run Flyway migrations automatically.

3. **Verify basic reachability**

- Admin UI: `http://localhost:9527` (default)  
Please replace localhost to the real ip or domain.

4. **System initialization (first run wizard)**

If the system is not initialized, the Admin UI will automatically redirect to the initialization wizard page: `http://localhost:9527/init`.

- **Step 1: Default tenant information**
  - **Domain** is required. It can be a domain or `ip:port`, for example `example.com` or `192.168.1.10:8080`.
  - **Logo** is required. Upload an image in the form.
- **Step 2: Super admin account**
  - Set the **username** and **password** for the highest administrator.
- **Submit**
  - After submitting successfully, the system  redirects to the Welcome page: `http://localhost:9527/welcome`.

5. **View service logs**

```bash
# All services
docker compose logs -f

# Only permission service
docker compose logs -f permission

# Only gateway
docker compose logs -f gateway

# Only mysql
docker compose logs -f mysql

# Last 200 lines
docker compose logs --tail=200 permission
```

### Re-run initialization (optional)

If you need to rerun the initialization wizard on a local/dev environment, you can reset the init state in MySQL:

```sql
UPDATE sys_platform_init_state
SET status = 'PENDING', finished_at = NULL
WHERE id = 1;
```

### Checklist

- Containers are running (`docker compose ps`)
- Permission service can connect to MySQL (no connection errors in logs)
- Admin UI loads in browser

### Troubleshooting

- **Build is slow on first run**: dependency downloads are expected.
- **DB name changes do not apply**: MySQL `MYSQL_DATABASE` only applies on first init; remove volume with `docker compose down -v` (data loss).

