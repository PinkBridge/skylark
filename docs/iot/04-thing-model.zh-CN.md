## 04. 物模型 JSON（Skylark 约定）

[English](04-thing-model.md)

### 说明

Skylark 将产品物模型保存为一份 **JSON 文档**（接口字段 **`modelJson`**），约定如下：

- 根对象包含三个数组：**`properties`**、**`events`**、**`services`**（必须存在且为数组，可为空）。
- 属性与参数使用 **扁平** 的 **`dataType`** 字符串（`int`、`float`、`enum`、`bool`、`time`、`string` 等）。
- 事件输出使用 **`outputData`**；服务入参/出参使用 **`inputData` / `outputData`**。
- 与阿里云控制台导出的 **TSL（`dataType` + `specs` 嵌套）** 不是同一种 JSON，导入需自行映射或后续做转换工具。

**`aiot-service`** 侧校验：根为对象；上述三个键均为数组；数组内每项为对象且 **`identifier` 非空**。

### 权威细则

完整字段表、各 `dataType` 附加字段、枚举写法、与表单/序列化实现的关系，见控制台同目录文档：

- `web/apps/aiot-web/src/views/thing-model/THING_MODEL_AUTHORING.md`

### 最小合法骨架

```json
{
  "properties": [],
  "events": [],
  "services": []
}
```

### 简短示例（一个属性 + 一个事件输出）

```json
{
  "properties": [
    {
      "identifier": "Temp",
      "name": "温度",
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
      "identifier": "HiTemp",
      "name": "高温告警",
      "description": "",
      "outputData": [
        {
          "identifier": "value",
          "name": "读数",
          "dataType": "float",
          "description": "",
          "minValue": -40,
          "maxValue": 125,
          "step": 0.1,
          "unit": "°C"
        }
      ]
    }
  ],
  "services": []
}
```

### 与阿里云 TSL 的关系

阿里云 TSL 使用 **`dataType: { "type": "...", "specs": { ... } }`** 表达规格；Skylark 当前持久化格式为 **扁平字段**，二者 **不能** 直接互拷使用。
