## 03. Products, devices & MQTT simulation

[简体中文](03-product-device-and-simulation.zh-CN.md)

### 1. Goal

End-to-end on a dev machine:

1. Create an **`MQTT_ALINK_JSON`** product in **`aiot-web`**.
2. Define a **thing model** (Skylark JSON format).
3. Create a **device** and obtain **deviceKey** + **secret**.
4. Use an MQTT client to connect, publish **property** and **event** messages, and publish a **service reply**.
5. Verify records in the device detail UI.

### 2. Preconditions

- Stack running as in [02. Quick start](02-quick-start.md), including **`aiot-web`** and **`permission`** if you need the UI with SSO.
- You can log in to **`aiot-web`** (default mapped port **`9532`** in sample Compose).

### 3. Create a product

In **`aiot-web`**:

1. Open the IoT **Products** list.
2. Create a product with protocol **`MQTT_ALINK_JSON`**.
3. Note the **ProductKey** (example below uses `MQTT_ALINK` — replace with yours).

### 4. Configure the thing model

Open the product, then **Thing model**. You may use **Edit raw JSON**.

The JSON must follow the **Skylark flat format** (see [04. Thing model JSON](04-thing-model.md)): root keys **`properties`**, **`events`**, **`services`**; services use **`inputData`** / **`outputData`**; numeric types need **`minValue` / `maxValue` / `step`** where applicable.

Example aligned with sections 6–8 below:

```json
{
  "properties": [
    {
      "identifier": "Humb",
      "name": "Humidity",
      "dataType": "float",
      "accessMode": "r",
      "description": "",
      "minValue": 0,
      "maxValue": 100,
      "step": 0.1,
      "unit": "%RH"
    },
    {
      "identifier": "Temp",
      "name": "Temperature",
      "dataType": "float",
      "accessMode": "r",
      "description": "",
      "minValue": -40,
      "maxValue": 85,
      "step": 0.1,
      "unit": "°C"
    }
  ],
  "events": [
    {
      "identifier": "Alarm",
      "name": "Alarm",
      "description": "",
      "outputData": [
        {
          "identifier": "level",
          "name": "Level",
          "dataType": "string",
          "description": "",
          "length": 64
        },
        {
          "identifier": "desc",
          "name": "Description",
          "dataType": "string",
          "description": "",
          "length": 256
        }
      ]
    }
  ],
  "services": [
    {
      "identifier": "SetTemp",
      "name": "Set Temperature",
      "description": "",
      "inputData": [
        {
          "identifier": "targetTemp",
          "name": "Target temperature",
          "dataType": "float",
          "description": "",
          "minValue": -40,
          "maxValue": 85,
          "step": 0.1,
          "unit": "°C"
        }
      ],
      "outputData": [
        {
          "identifier": "actualTemp",
          "name": "Actual temperature",
          "dataType": "float",
          "description": "",
          "minValue": -40,
          "maxValue": 85,
          "step": 0.1,
          "unit": "°C"
        },
        {
          "identifier": "result",
          "name": "Result",
          "dataType": "string",
          "description": "",
          "length": 128
        }
      ]
    }
  ]
}
```

### 5. Create a device

Under the product:

1. Open **Devices** and create a device.
2. Save **deviceKey** and **secret** (used as MQTT username/password in the default auth flow).

### 6. MQTT client connection

Use MQTTX, MQTT Explorer, or `mosquitto_pub` / `mosquitto_sub`.

Typical settings:

- **Host**: `127.0.0.1` (or your server IP).
- **Port**: **`1883`** (default EMQX listener in Compose).
- **Username**: `deviceKey`
- **Password**: `secret`
- **Client ID**: recommended to equal **`deviceKey`** for easier troubleshooting.

After a successful connection, **connection logs** should appear under the device detail page (when session webhook is enabled and configured).

### 7. Simulate property report

#### 7.1 Subscribe to ACK (optional)

`/sys/{productKey}/{deviceKey}/thing/event/property/post_reply`

Replace placeholders, e.g.:

`/sys/MQTT_ALINK/myDeviceKey/thing/event/property/post_reply`

#### 7.2 Publish property post

**Topic:**

`/sys/{productKey}/{deviceKey}/thing/event/property/post`

**Payload example:**

```json
{
  "id": "1001",
  "version": "1.0",
  "params": {
    "Humb": 55.3,
    "Temp": 26.1
  },
  "method": "thing.event.property.post"
}
```

**Expected:**

- ACK on `post_reply` (if subscribed).
- **Current properties** and **property records** updated in **`aiot-web`**.

### 8. Simulate custom event

#### 8.1 Subscribe to event ACK (optional)

`/sys/{productKey}/{deviceKey}/thing/event/Alarm/post_reply`

#### 8.2 Publish event post

**Topic:**

`/sys/{productKey}/{deviceKey}/thing/event/Alarm/post`

**Payload example:**

```json
{
  "id": "1002",
  "version": "1.0",
  "params": {
    "level": "high",
    "desc": "temperature over threshold"
  },
  "method": "thing.event.Alarm.post"
}
```

**Expected:** **Event records** show a new **Alarm** entry.

### 9. Service invoke and reply (with HTTP demo)

This repository’s **`aiot-service`** ingests **service replies** from:

`/sys/{productKey}/{deviceKey}/thing/service/{serviceIdentifier}/reply`

`aiot-service` now provides a dedicated invoke API:
**`POST /api/aiot-service/access/invokeThingService`**
(also compatible with `POST /api/aiot-service/access/invoke-thing-service`).

#### 9.1 Device-side subscription (recommended)

`/sys/{productKey}/{deviceKey}/thing/service/#`

#### 9.2 HTTP invoke request demo (curl)

> Example uses local gateway port `9527`. Replace token, `X-Tenant-Id`, product/device values, and service identifier.

```bash
curl -X POST "http://127.0.0.1:9527/api/aiot-service/access/invokeThingService" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your_token>" \
  -H "X-Tenant-Id: <tenant_id>" \
  -d '{
    "productKey": "MQTT_ALINK",
    "deviceName": "myDeviceKey",
    "identifier": "SetTemp",
    "args": "{\"targetTemp\":26.0}",
    "qos": 1,
    "sync": true,
    "timeoutMs": 8000
  }'
```

Request fields:

- `productKey`: product key
- `deviceName`: device key
- `identifier`: service identifier
- `args`: JSON string for service input params
- `sync`: whether to wait for reply (`true` returns `result`)

Internally, this publishes to MQTT:

`/sys/{productKey}/{deviceName}/thing/service/{serviceIdentifier}/invoke`

MQTT payload shape:

```json
{
  "id": "<messageId>",
  "version": "1.0",
  "params": {
    "targetTemp": 26.0
  },
  "method": "thing.service.SetTemp.invoke"
}
```

Successful sync response example:

```json
{
  "success": true,
  "requestId": "3f0d9d1f7f8d4f5ea1ddf732dca4f8cd",
  "data": {
    "messageId": "db1145f2f9f74f8db0b8a9eb63d2c0cb",
    "result": "{\"id\":\"db1145f2f9f74f8db0b8a9eb63d2c0cb\",\"code\":200,\"data\":{\"actualTemp\":26.0,\"result\":\"ok\"},\"message\":\"success\",\"version\":\"1.0\"}"
  }
}
```

**Reply topic:**

`/sys/{productKey}/{deviceKey}/thing/service/SetTemp/reply`

**Reply payload example** (`id` should match the invoke message when you correlate traces):

```json
{
  "id": "10001",
  "code": 200,
  "data": {
    "actualTemp": 26.0,
    "result": "ok"
  },
  "message": "success",
  "version": "1.0"
}
```

**Expected:** service request/reply records are visible, and `.../reply_ack` can be observed when ACL/rules are correctly configured.

### 10. What to check in device detail

- **Connection status** and **connection logs**.
- **Current property values** and **property history**.
- **Event records**.
- **Service records** (request vs reply, depending on how invokes are generated in your environment).

### 11. Troubleshooting

- **No property rows after publish**: wrong **productKey/deviceKey**; ACL denies publish; EMQX rule not forwarding to **`/api/aiot-service/access/upstream`** (re-run **`emqx-init`** after broker reset).
- **No connection logs**: session webhook disabled or **`IOT_ACCESS_WEBHOOK_SECRET`** mismatch between EMQX bridge headers and `aiot-service`.
- **Service reply not stored**: topic must end with **`/thing/service/{id}/reply`** and pass ACL.

### 12. Reference

- Thing model rules: [04. Thing model JSON](04-thing-model.md) and `web/apps/aiot-web/src/views/thing-model/THING_MODEL_AUTHORING.md`.
- EMQX rule setup: `service/aiot-service/scripts/init-emqx.sh`.
