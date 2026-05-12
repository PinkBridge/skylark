## 03. 产品、设备与 MQTT 模拟联调

[English](03-product-device-and-simulation.md)

### 1. 目标

在本地完成一条完整链路：

1. 在 **`aiot-web`** 中创建 **`MQTT_ALINK_JSON`** 产品。
2. 配置 **物模型**（Skylark JSON 形态）。
3. 创建 **设备**，拿到 **deviceKey** 与 **secret**。
4. 使用 MQTT 客户端完成 **属性上报**、**自定义事件上报**、**服务回复**。
5. 在设备详情页核对 **连接日志**、**属性**、**事件**、**服务** 等记录。

### 2. 前置条件

- 已按 [02. 本地快速启动](02-quick-start.zh-CN.md) 拉起依赖；若要用控制台，一般需要 **`aiot-web`** + **`permission`** + **`gateway`**。
- 能登录 **`aiot-web`**（示例 Compose 下本机端口常为 **`9532`**）。

### 3. 创建产品

在 **`aiot-web`** 中：

1. 打开 IoT **产品**列表。
2. 新建产品，协议选择 **`MQTT_ALINK_JSON`**。
3. 记录 **ProductKey**（下文示例用 `MQTT_ALINK`，请替换为你的实际值）。

### 4. 配置物模型

进入产品后打开 **物模型**，可使用 **编辑原始 JSON**。

JSON 必须符合 **Skylark 扁平约定**（见 [04. 物模型 JSON](04-thing-model.zh-CN.md)）：根上为 **`properties` / `events` / `services`** 三个数组；服务入参/出参字段名为 **`inputData` / `outputData`**；数值型需带 **`minValue`、`maxValue`、`step`** 等（与 `thingModelSchema.js` 一致）。

与下文第 7–9 节联调用到的标识符一致的示例如下：

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

### 5. 创建设备

在产品下：

1. 打开 **设备**列表并新增设备。
2. 保存后记录 **deviceKey** 与 **secret**（默认接入里常用作 MQTT 用户名/密码）。

### 6. MQTT 客户端连接

可使用 MQTTX、MQTT Explorer 或 `mosquitto` 等。

典型参数：

- **Host**：`127.0.0.1`（或你的服务器地址）。
- **Port**：**`1883`**（Compose 默认映射）。
- **Username**：`deviceKey`
- **Password**：`secret`
- **ClientId**：建议与 **deviceKey** 一致，便于排障。

连接成功后，若会话 Webhook 配置正确，设备详情中应出现 **连接日志**。

### 7. 模拟属性上报

#### 7.1 订阅 ACK（可选）

`/sys/{productKey}/{deviceKey}/thing/event/property/post_reply`

示例：

`/sys/MQTT_ALINK/myDeviceKey/thing/event/property/post_reply`

#### 7.2 发布属性上报

**Topic：**

`/sys/{productKey}/{deviceKey}/thing/event/property/post`

**Payload 示例：**

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

**预期：** 可收到 `post_reply`（若已订阅）；控制台 **当前属性** 与 **属性记录** 更新。

### 8. 模拟自定义事件

#### 8.1 订阅事件 ACK（可选）

`/sys/{productKey}/{deviceKey}/thing/event/Alarm/post_reply`

#### 8.2 发布事件

**Topic：**

`/sys/{productKey}/{deviceKey}/thing/event/Alarm/post`

**Payload 示例：**

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

**预期：** **事件记录** 中出现 **Alarm**。

### 9. 服务调用与回复（含 HTTP 请求 Demo）

当前树中 **`aiot-service`** 会对 **`/sys/{productKey}/{deviceKey}/thing/service/{serviceIdentifier}/reply`** 的上行进行解析入库（经 EMQX 规则转发至 **`POST /api/aiot-service/access/upstream`**）。

`aiot-service` 已提供独立服务调用接口：**`POST /api/aiot-service/access/invokeThingService`**（同时兼容 `POST /api/aiot-service/access/invoke-thing-service`）。

#### 9.1 设备侧先订阅服务下发 Topic

`/sys/{productKey}/{deviceKey}/thing/service/#`

#### 9.2 服务调用 HTTP 请求 Demo（curl）

> 以下以本地网关默认端口 `80`（`GATEWAY_PORT`）为例，请替换 Token、`X-Tenant-Id`、`productKey`、`deviceName`、服务标识符。

```bash
curl -X POST "http://127.0.0.1:80/api/aiot-service/access/invokeThingService" \
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

**请求字段说明：**

- `productKey`：产品 Key
- `deviceName`：设备 Key
- `identifier`：服务标识符
- `args`：服务入参 JSON 字符串
- `sync`：是否同步等待设备回复（`true` 时返回 `result`）

接口内部会发布到 MQTT Topic：

`/sys/{productKey}/{deviceName}/thing/service/{serviceIdentifier}/invoke`

下发的 MQTT payload 形态：

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

同步调用成功返回示例：

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

#### 9.3 设备回复 Topic 与 Payload

**回复 Topic：**

`/sys/{productKey}/{deviceKey}/thing/service/SetTemp/reply`

**回复 Payload 示例**（`id` 建议与调用侧一致以便追踪）：

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

**预期：** 在规则与 ACL 正常时，**服务调用/回复记录** 中能看到对应数据，并且可收到 `.../reply_ack`。

### 10. 设备详情中建议核对的内容

- **连接状态** 与 **连接日志**。
- **当前属性值** 与 **属性历史**。
- **事件记录**。
- **服务调用/回复记录**（取决于是否产生 invoke 侧记录）。

### 11. 常见问题

- **上报后页面无数据**：核对 **productKey / deviceKey**；检查 **ACL**；确认 EMQX 已将消息转发到 **`/api/aiot-service/access/upstream`**（数据卷清空后需重跑 **`emqx-init`**）。
- **无连接日志**：会话 Webhook 未启用，或 **`IOT_ACCESS_WEBHOOK_SECRET`** 与桥接配置不一致。
- **服务回复未入库**：Topic 必须为 **`.../thing/service/{标识}/reply`** 且通过 ACL。

### 12. 延伸阅读

- 物模型字段细则：[04. 物模型 JSON（Skylark 约定）](04-thing-model.zh-CN.md) 与 `web/apps/aiot-web/src/views/thing-model/THING_MODEL_AUTHORING.md`。
- EMQX 初始化脚本：`service/aiot-service/scripts/init-emqx.sh`。
