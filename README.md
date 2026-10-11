# luoyang-springboot

洛阳旅游智能问答系统的 Java 后端服务。当前目录提供可启动的 Spring Boot 基础工程、健康检查接口和首版数据库迁移，业务模块会在后续迭代中逐步加入。

## 运行环境

- JDK 17+
- Maven 3.9+
- MySQL 8.x（宿主机端口默认 `3308`）
- Redis 7.x（宿主机端口默认 `6381`）

根目录的 `docker-compose.yml` 会创建数据库 `luoyang` 和应用账号 `luoyang_app`。先在项目根目录复制 `.env.example` 为 `.env`，设置三个密码，再启动依赖服务：

```bash
docker compose up -d mysql redis
docker compose ps
```

## 配置

默认配置见 [`src/main/resources/application.yml`](src/main/resources/application.yml)，可复制 [`src/main/resources/application.yml.example`](src/main/resources/application.yml.example) 作为新环境模板。本地运行时可通过环境变量覆盖：

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3308/luoyang?...` | 从 Java 进程访问 MySQL 的 JDBC 地址 |
| `DB_USERNAME` | `luoyang_app` | Compose 创建的应用用户 |
| `DB_PASSWORD` | `change_this_mysql_password` | 应与根目录 `.env` 的 `MYSQL_PASSWORD` 一致 |
| `REDIS_HOST` | `localhost` | Redis 主机；服务在 Compose 内运行时使用 `redis` |
| `REDIS_PORT` | `6381` | 宿主机端口；Compose 网络内使用 `6379` |
| `REDIS_PASSWORD` | `change_this_redis_password` | 应与根目录 `.env` 的 `REDIS_PASSWORD` 一致 |
| `SERVER_PORT` | `8080` | HTTP 服务端口 |
| `FLYWAY_ENABLED` | `true` | 是否启动时执行数据库迁移 |

生产或共享环境应通过实际环境变量提供密码，不要把密码写入 Git。

## 启动和验证

```bash
mvn spring-boot:run
curl http://localhost:8080/api/v1/health
```

健康接口返回服务状态、版本和 UTC 时间戳；它不依赖业务数据。迁移由 Flyway 自动执行，脚本位于 [`src/main/resources/db/migration`](src/main/resources/db/migration)。全新数据库会依次执行 `V1__init.sql` 和 `V2__seed_categories.sql`，重复启动会由 Flyway 记录表自动跳过。

不连接外部服务也可以运行测试：

```bash
mvn test
mvn package
```

测试配置会临时禁用数据库、Redis 和 MyBatis 自动配置，因此只验证应用上下文能够启动；`package` 会把 Flyway SQL 一并放入构建产物。
