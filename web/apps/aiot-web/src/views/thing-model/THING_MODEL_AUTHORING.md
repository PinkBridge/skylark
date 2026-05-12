# Skylark 物模型编写规范

适用于 **`MQTT_ALINK_JSON`** 协议产品在控制台维护、并经 `/api/aiot-service/mgmt/products/{productKey}/thing-model` 持久化的物模型 JSON。

本文描述的是 **Skylark 当前实现的扁平格式**，与阿里云控制台导出的 **TSL（`dataType.type` + `specs` 嵌套）** 不同；若从阿里云迁移需先做字段映射。

---

## 1. 根结构

- 顶层必须是 **JSON 对象**。
- 必须包含三个键，且值均为 **数组**（可为空数组）：
  - `properties`：属性
  - `events`：事件
  - `services`：服务

版本号由接口字段 **`version`**（如 `v1`）单独传递，**不必**写在根对象里（界面保存时生成的 `modelJson` 也不含 `version`）。

---

## 2. 标识符 `identifier`

- **必填**，不能为空字符串。
- 在同一数组内（属性之间 / 事件之间 / 服务之间）**不得重复**。
- 建议使用 **小驼峰或 snake_case**，英文开头；避免与协议关键字冲突（参考阿里云惯例勿使用 `set`、`get`、`property`、`event`、`time`、`value` 等纯保留词作唯一标识）。

后端仅校验：**三项数组存在、每项为对象且含非空 `identifier`**。

---

## 3. 属性 `properties[]`

每条属性至少包含：

| 字段 | 说明 |
|------|------|
| `identifier` | 见上 |
| `name` | 展示名称 |
| `dataType` | 见下表 |
| `accessMode` | `r` 只上报、`rw` 可下发可上报、`w` 只下发 |
| `description` | 可选说明 |

### 3.1 `dataType` 取值与附加字段

持久化 JSON 为 **扁平结构**：`dataType` 为字符串，其它字段按类型挂在同一对象上（实现见 `buildThingModelJson` / `buildParamPayload`）。

| dataType | 必须填写的附加字段 |
|----------|---------------------|
| `int`、`float` | `minValue`、`maxValue`、`step`（数值）；`unit` 建议字符串，可为空串 |
| `bool` | `trueText`、`falseText` |
| `string` | `length`（正整数，字符长度约束） |
| `enum` | `enumValueType`（常用 `int` 或 `string`）；`enumItems` 为非空数组，每项含 `value`、`description` |
| `time` | 无数值附加字段；语义为 UTC 毫秒时间戳，与界面文案一致即可 |

解析时会把 `double`、`text` 分别归一为 `float`、`string`（见 `normalizeDataType`）。

---

## 4. 事件 `events[]`

每条事件包含：

| 字段 | 说明 |
|------|------|
| `identifier`、`name`、`description` | `description` 可空 |
| **`outputData`** | **数组**：事件输出参数列表；结构与「服务参数」相同（见 §5） |

界面编辑用的是内存字段 `outputParams`，保存时写入 **`outputData`**。手写 JSON 时请使用 **`outputData`**。

保存时会 **去掉没有 `identifier` 的参数项**（空标识参数不会进入 JSON）。

---

## 5. 服务 `services[]`

每条服务包含：

| 字段 | 说明 |
|------|------|
| `identifier`、`name`、`description` | |
| **`inputData`** | 入参数组 |
| **`outputData`** | 出参数组 |

每个参数对象的写法与 **属性** 的类型规则一致（同样使用扁平 `dataType` 及 §3.1 的附加字段）。

保存时同样 **过滤掉没有 `identifier` 的参数字段**。

---

## 6. 枚举 `enum`

示例：

```json
{
  "identifier": "mode",
  "name": "模式",
  "dataType": "enum",
  "accessMode": "rw",
  "description": "",
  "enumValueType": "int",
  "enumItems": [
    { "value": 0, "description": "自动" },
    { "value": 1, "description": "手动" }
  ]
}
```

- `enumItems[].value` 需与 `enumValueType` 一致（整型或字符串等）。
- `description` 为展示文案。

---

## 7. 校验摘要

| 层级 | 规则 |
|------|------|
| 前端表单 | 见 `ThingModelPanel.vue`（属性必填项、数值范围、枚举个数等） |
| `validateThingModelForm` | 属性 / 事件 / 服务各自内部 **`identifier` 不重复且非空** |
| 后端 `ThingModelServiceImpl` | `properties`、`events`、`services` 必须为数组；每项必有非空 **`identifier`** |

---

## 8. 与设备侧约定

物模型 **identifier** 应与设备上报 Topic 载荷里的属性名、事件名、服务名 **一致或可映射**，否则运行时的解析与告警规则无法对齐。具体 Topic 与载荷格式以 **`MQTT_ALINK_JSON`** 接入文档与 `aiot-service` 上行处理为准。

---

## 9. 参考实现

- 序列化 / 反序列化：`thingModelSchema.js`（`buildThingModelJson`、`parseThingModelJson`）
- 后端校验：`ThingModelServiceImpl.validateModelJson`
