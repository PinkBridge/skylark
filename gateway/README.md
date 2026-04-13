# skylark-gateway

Spring Cloud Gateway entry for `permission`, `iot`, `xiaolvxingqiu`.

## Run

```bash
mvn spring-boot:run
```

Default port: `8080`

## Route prefixes

- `/api/permission/**` -> `services.permission.uri` (default `http://localhost:8087`)
- `/api/iot/**` -> `services.iot.uri` (default `http://localhost:8088`)
- `/api/xiaolvxingqiu/**` -> `services.xiaolvxingqiu.uri` (default `http://localhost:8086/xiaolvxingqiu`)

## Local override examples

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--services.permission.uri=http://localhost:9001 --services.iot.uri=http://localhost:9002"
```
