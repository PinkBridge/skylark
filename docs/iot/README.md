## IoT (AIoT) Manual

English | [简体中文](README.zh-CN.md)

This manual describes how to run and operate the **Skylark IoT stack** in this repository: **`aiot-service`** (device access + management APIs), **`aiot-web`** (console), **EMQX** (MQTT broker + rules), and **MySQL** (metadata and runtime records). It complements the permission/business docs under `docs/permission`.

### Scope

- **Protocols**: primary focus on **`MQTT_ALINK_JSON`** (Alink-style topics under `/sys/{productKey}/{deviceKey}/...`).
- **Deployment**: Docker Compose at the **repository root** (`docker-compose.yml`).
- **Not in scope**: Alibaba Cloud IoT public-cloud console import/export (TSL `specs`); see [04. Thing model JSON (Skylark format)](04-thing-model.md) for the format this project stores.

### Chapters

- [01. Architecture](01-architecture.md)
- [02. Quick start (local)](02-quick-start.md)
- [03. Products, devices & MQTT simulation](03-product-device-and-simulation.md)
- [04. Thing model JSON (Skylark format)](04-thing-model.md)

### Related paths in repo

- IoT service: `service/aiot-service/`
- IoT console: `web/apps/aiot-web/`
- EMQX init script: `service/aiot-service/scripts/init-emqx.sh`
- Thing model authoring reference: `web/apps/aiot-web/src/views/thing-model/THING_MODEL_AUTHORING.md`
