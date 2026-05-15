## 01. Architecture

[简体中文](01-architecture.zh-CN.md)

### Overview

The IoT stack in this repository centers on three runtime components:

- **`aiot-service`**: Spring Boot service combining **device access** (EMQX callbacks, upstream ingest) and **management** (products, devices, thing model, alarms, outbound integration).
- **EMQX**: MQTT broker; HTTP authentication/authorization callbacks to `aiot-service`; rules forward device publishes to the upstream webhook.
- **MySQL**: stores products, devices, ACL policies, thing models, property/event/service records, and related tables (database name defaults to `skylark_aiot` for the IoT module).

Typical Compose layout: `aiot-service` resolves `emqx` and `mysql` by **Docker service name** on the internal network.

### Main responsibilities

#### 1. Authentication and ACL

- EMQX calls **`POST /api/aiot-service/access/emqx/auth`** for username/password authentication.
- EMQX calls **`POST /api/aiot-service/access/emqx/acl`** for publish/subscribe authorization.
- `aiot-service` evaluates DB-backed policies (`iot_acl_policy`, etc.) when `IOT_ACCESS_AUTH_USE_DB` / `IOT_ACCESS_ACL_USE_DB` are enabled (see `application.yml` / Compose env).

#### 2. Upstream (device to cloud)

- EMQX **rules** forward matching publishes to **`POST /api/aiot-service/access/upstream`** (webhook body carries topic and payload; see `init-emqx.sh` for rule names).
- `aiot-service` parses Alink-style topics, persists **property / event / service-reply** records, and publishes **normalized internal events** for outbound integration and alarms.

#### 3. Session webhooks

- EMQX forwards client connect/disconnect events to **`POST /api/aiot-service/access/emqx/webhook/session`** (optional secret header when configured).

#### 4. Management API

- REST APIs under **`/api/aiot-service/mgmt/...`** (products, devices, groups, thing model, alarms, etc.) are protected by Skylark authz when enabled; use `aiot-web` or an authenticated client.

### Core data objects (conceptual)

- **Product** and **device** master data.
- **Thing model** JSON per product (and optional per-device snapshot).
- **Runtime tables**: property records, event records, service invocation/reply records, connection logs.
- **ACL policies**: templates at product level and overrides at device level.

### Key message flows

#### A. Property report

1. Device publishes to `/sys/{productKey}/{deviceKey}/thing/event/property/post` (Alink payload with `params`).
2. EMQX rule matches `/sys/+/+/thing/event/+/post` and forwards to upstream webhook.
3. `aiot-service` parses the topic and payload, inserts **property records**, emits integration events.

#### B. Custom event

1. Device publishes to `/sys/{productKey}/{deviceKey}/thing/event/{eventIdentifier}/post`.
2. Same rule family forwards to upstream; **event records** are written.

#### C. Service reply

1. Device publishes to `/sys/{productKey}/{deviceKey}/thing/service/{serviceIdentifier}/reply`.
2. EMQX rule forwards `.../thing/service/+/reply` to upstream; **service records** are updated/inserted as implemented.

### Design notes

- **Separation**: EMQX owns connections and topic routing; business rules live in `aiot-service`.
- **Traceability**: payloads may carry `id` / `messageId` used for correlation (see ingest implementation).
- **Repeatable EMQX setup**: `emqx-init` runs `init-emqx.sh` (idempotent where possible).

### Ports (default Compose mapping)

| Component        | Host port (default) | Notes                          |
|------------------|---------------------|--------------------------------|
| `aiot-service`   | `9533` → 80         | Direct access to IoT HTTP API |
| `emqx`           | `1883`, `18083`     | MQTT / Dashboard               |
| `mysql`          | `3307` → 3306       | All Skylark DBs                |
| `aiot-web`       | `9532` → 80         | IoT console UI                 |
| `gateway`        | `80` (configurable) | Optional unified entry        |

Adjust if your `.env` overrides host ports.
