## 02. Quick start (local)

[简体中文](02-quick-start.zh-CN.md)

### 1. Prerequisites

- Docker Desktop (or Docker Engine) with **Compose v2**.
- Clone this repository and work at the **repository root** (where `docker-compose.yml` lives).
- Free host ports (defaults in this doc): **`3307`**, **`1883`**, **`18083`**, **`9533`**, optionally **`80`**, **`9532`**.

### 2. Environment variables

Compose wires `aiot-service` to MySQL and EMQX. Common variables (see `docker-compose.yml` and `service/aiot-service/src/main/resources/application.yml`):

- **MySQL**: `SPRING_DATASOURCE_*` for `aiot-service` (defaults use root password `123456` and DB `skylark_aiot`).
- **EMQX dashboard**: `EMQX_DASHBOARD_USERNAME` / `EMQX_DASHBOARD_PASSWORD` (defaults `admin` / `public`).
- **Webhook secret** (recommended): `IOT_ACCESS_WEBHOOK_SECRET` — must match the value passed into `emqx-init` so session webhooks can be verified.
- **EMQX management API** (optional for advanced ops): `EMQX_API_KEY` / `EMQX_API_SECRET` as used by `aiot-service` when configuring broker-side resources from the app.

For a **minimal bring-up** of IoT only, you still need MySQL healthy before `aiot-service` starts.

### 3. Start core services

From the **repository root**:

```bash
docker compose up -d mysql emqx aiot-service emqx-init
```

Notes:

- **`emqx-init`** runs `init-emqx.sh` once: HTTP auth/ACL, bridges, and rules pointing at `aiot-service`.
- If `emqx-init` exits `0` quickly, check logs: `docker logs emqx-init`.

To use the **web console** (`aiot-web`), you typically also need **`permission`**, **`gateway`**, and **`aiot-web`** (OAuth and menus). Example:

```bash
docker compose up -d mysql permission gateway aiot-web emqx aiot-service emqx-init
```

### 4. Health checks

- **`aiot-service`**: `http://127.0.0.1:9533/actuator/health` should report **UP** (path may differ if you remap the host port).
- **EMQX Dashboard**: `http://127.0.0.1:18083` — log in with dashboard credentials from your env.

### 5. After code or script changes

Rebuild the affected images:

```bash
docker compose up -d --build aiot-service emqx-init
```

If Flyway migrations changed, restarting `aiot-service` applies them on boot.

### 6. Common failures

- **`emqx-init` / `set: illegal option`**: ensure `init-emqx.sh` uses **LF** line endings (see `.gitattributes` in repo).
- **EMQX rules missing**: rerun `emqx-init` after EMQX data volume reset, or re-run the script manually against the broker API.
- **`aiot-service` cannot reach MySQL**: wait for `mysql` healthcheck **healthy** before `aiot-service` starts (Compose `depends_on`).

### 7. Next steps

- Operate products/devices in **`aiot-web`** (default host port **`9532`** when using sample Compose).
- Follow [03. Products, devices & MQTT simulation](03-product-device-and-simulation.md) for end-to-end MQTT exercises.
