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

2. **Verify basic reachability**
- Admin UI: `http://localhost:9527` (default)
- Gateway: `http://localhost:80` (default)
- Permission service: `http://localhost:19527` (default)

   **Default account**
   - Username: `superadmin`
   - Password: `123456`

3. **View service logs**

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

4. **Database initialization**

On first start, MySQL will initialize data directory, and `permission` will run Flyway migrations automatically.

### Checklist

- [ ] Containers are running (`docker compose ps`)
- [ ] Permission service can connect to MySQL (no connection errors in logs)
- [ ] Admin UI loads in browser

### Troubleshooting

- **Build is slow on first run**: dependency downloads are expected.
- **DB name changes do not apply**: MySQL `MYSQL_DATABASE` only applies on first init; remove volume with `docker compose down -v` (data loss).

