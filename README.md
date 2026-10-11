# luoyang-springboot

洛阳旅游智能问答系统的 Java 后端服务：Spring Boot 3.3.5 + Java 17 + 原生 MyBatis + Flyway。当前提供可启动骨架、统一响应结构和 `/api/v1/health`，业务模块在阶段 2 起逐步加入。

## 运行环境

- JDK 17+
- Maven 3.9+
- MySQL 8.x（开发容器宿主机端口 `3308`）
- Redis 7.x（开发容器宿主机端口 `6381`，开发环境无密码）

数据库容器由项目根目录 `docker-compose.yml` 创建，首次启动会自动执行 `src/main/resources/db/migration/` 下的 V1/V2 脚本完成建表。

## 配置

默认配置见 [`src/main/resources/application.yml`](src/main/resources/application.yml)，可用环境变量覆盖：

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3308/luoyang?...` | 从 Java 进程访问 MySQL 的 JDBC 地址 |
| `DB_USERNAME` | `luoyang_app` | Compose 创建的应用用户 |
| `DB_PASSWORD` | `luoyang1234` | 与 compose 的 `MYSQL_PASSWORD` 一致 |
| `REDIS_HOST` | `localhost` | Redis 主机；服务在 Compose 网络内运行时使用 `redis` |
| `REDIS_PORT` | `6381` | 宿主机端口；Compose 网络内使用 `6379` |
| `REDIS_PASSWORD` | 空 | 开发 compose 的 Redis 无密码；启用 requirepass 后填写 |
| `SERVER_PORT` | `8080` | HTTP 服务端口 |
| `FLYWAY_ENABLED` | `true` | 是否启动时执行数据库迁移 |

## 启动和验证

```bash
mvn spring-boot:run
curl http://localhost:8080/api/v1/health
```

健康接口返回 `{"code":0,"message":"success","data":{...},"requestId":"..."}` 统一结构。

## 数据库迁移

迁移脚本位于 [`src/main/resources/db/migration`](src/main/resources/db/migration)：V1 创建 15 张核心表，V2 写入 3 个初始分类。三种使用路径都受支持：

1. 开发 compose 首次启动已通过 initdb 建表：Flyway 启动时自动 baseline 到 V1，跳过已执行脚本；
2. 手工执行过 SQL 的已有库：同样自动 baseline；
3. 全新空库：Flyway 从 V1 顺序执行并记录 `flyway_schema_history`。

不使用 Hibernate 自动建表；新增变更一律追加 V3、V4 版本脚本。手工执行与验证步骤见 [`database/README.md`](database/README.md)。

## 测试

```bash
mvn test
mvn package
```

测试通过排除数据源、Redis 和 MyBatis 自动配置，在不依赖外部服务的情况下验证应用上下文和 `/api/v1/health` 的响应契约。
