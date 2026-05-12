## 04. Thing model JSON (Skylark format)

[简体中文](04-thing-model.zh-CN.md)

### What this is

Skylark stores product thing models as a **single JSON document** (`modelJson`) with:

- Top-level arrays: **`properties`**, **`events`**, **`services`** (each must be an array; can be empty).
- **Flat** `dataType` strings on each feature or parameter (`int`, `float`, `enum`, `bool`, `time`, `string`).
- Services and events use **`inputData`** / **`outputData`** (not Alibaba TSL `dataType.specs` nesting).

The **`aiot-service`** API validates that the document is a JSON object and that each item in the three arrays is an object with a non-empty **`identifier`**.

### Authoritative spec

The full authoring rules (field matrix, examples, validation layers) live next to the console implementation:

- `web/apps/aiot-web/src/views/thing-model/THING_MODEL_AUTHORING.md`

### Minimal valid skeleton

```json
{
  "properties": [],
  "events": [],
  "services": []
}
```

### Short example (one property + one event output)

```json
{
  "properties": [
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
      "identifier": "HiTemp",
      "name": "High temperature",
      "description": "",
      "outputData": [
        {
          "identifier": "value",
          "name": "Reading",
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

### Alibaba Cloud TSL

Alibaba’s exported TSL uses **`dataType: { "type": "...", "specs": { ... } }`**. That is **not** the same shape as Skylark’s stored JSON. Importing from Alibaba requires a **manual mapping** (or a future converter).
