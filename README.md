# Spring Boot 业务后端（`springboot/`）

> 状态：核心业务后端已实现，正在进入全链路联调与验收阶段。
> 技术栈：Java 17、Spring Boot、Spring Security、MyBatis、Redis、Flyway、Knife4j/Springdoc、阿里云 OSS。
> 本地端口：`8080`。
> 系统角色：**唯一对外业务入口**，负责 JWT、RBAC、限流、审计、业务 CRUD、推荐编排、文件代理和 AI 会话持久化。

## 当前实现状态（2026-11-19）

已落地并通过编译、上下文测试和临时 MySQL 8 运行验证：

- 工程基础设施：统一响应、全局异常、TraceId、CORS、Knife4j/Springdoc、Redis、Flyway、Hikari 连接池配置。
- 数据库：31 张业务表，7 个迁移版本；实测物理外键数量为 0。
- 认证：微信登录、管理员登录、Access/Refresh Token、App/Admin audience 隔离、Redis Refresh 轮换、失败锁定、改密、token_version 失效。
- 内容：分类、标签、景点、攻略、美食、轮播图、软删除、发布/下架、乐观锁、XSS 清洗和公共只读 API。
- 文件：阿里云 OSS 私有上传、对象键生成、SHA-256、签名 URL、引用保护和魔法字节校验；Bucket 位于 oss-cn-beijing，已实测上传和删除。
- 小程序用户：资料、偏好、收藏、行为上报、确定性推荐算法和推荐日志。
- AI：Java 侧会话事实、固定 ULID threadId、Redis 线程锁、幂等、checkpoint 恢复历史、资料不足映射和 Java 内部工具接口。
- 知识库：文档上传、知识文档版本、索引任务、FastAPI HMAC 提交、定时轮询和删除补偿。
- 管理端：小程序用户、管理员、角色权限、权限树、系统配置、操作审计、统计仪表盘。
- 测试：JWT audience/类型隔离、HMAC canonical string、AI 幂等、资料不足、XSS、无物理外键、安全入口、OpenAPI 可用性，共 18 个测试通过。
- 运行冒烟：使用临时 MySQL 8 + 临时 Redis + 真实 OSS，跑通管理员登录、RBAC、分类/标签创建、文件上传、景点创建与发布、小程序登录、收藏、公共详情、推荐、审计日志和仪表盘。
- 启动冒烟：使用临时 MySQL 8 实例执行完整 Flyway 迁移，Spring Boot 成功启动，/actuator/health 返回 UP。
- 配置冒烟：使用 local profile、远程 MySQL/Redis 和本地 Git 忽略凭据文件启动成功，/actuator/health 返回 UP。
- 本地运行策略：Flyway 默认关闭，直接使用远程数据库中已存在的 31 张业务表；Flyway 脚本保留用于空库重建/论文说明。

当前未完成或需要环境配合的部分：

- 真实 Redis 服务需要启动；当前本机未启动 Redis 时，Refresh、限流和 AI 会话锁相关接口无法完成真实链路。
- 本机 application-local.yml 默认连接 192.168.100.128 的 MySQL/Redis；实际密码需通过环境变量注入。
- FastAPI AI 服务未启动时，AI 接口按设计返回 50300；Java 侧不会直接执行 Agent/RAG。
- OSS 真实密钥只保存在 Git 忽略的 config/application-secret.yml，不进入仓库。
- 本地首次启动会读取 Git 忽略的 config/application-secret.yml 并创建 admin 管理员；初始密码和 JWT 密钥记录在项目根的 all password/PASSWORDS.md。创建成功后应在管理端修改密码，并将 BOOTSTRAP_ADMIN_ENABLED 设为 false。

## 1. 开发前门禁（必须阅读）

在修改本目录之前，按顺序阅读：

1. [项目总入口](../README.md)：全局硬约束与启动顺序。
2. [文档中心](../docs/README.md)：文档事实源与变更规则。
3. [系统架构](../docs/architecture/SYSTEM-ARCHITECTURE.md)：模块边界、包结构、网络和缓存。
4. [数据库设计](../docs/database/DATABASE-DESIGN.md)：31 张表、逻辑外键、索引和 Flyway。
5. [API 契约](../docs/api/API-CONTRACT.md)：三类路由、JWT、统一响应、错误码、AI 对话。
6. [AI 服务设计](../docs/ai/AI-SERVICE-DESIGN.md)：Java 与 FastAPI 的职责边界和 HMAC。
7. [开发规范](../docs/development/DEVELOPMENT-STANDARD.md)：命名、分层、事务、测试和 DoD。
8. [实施路线图](../docs/roadmap/IMPLEMENTATION-ROADMAP.md)：当前阶段和验收命令。

开工前自检：

- [ ] 我已确认本次改动属于业务后端，而不是管理端、小程序或 AI 服务职责。
- [ ] 我已确认不会新增物理外键、不会让前端直连 FastAPI 或中间件。
- [ ] 我已确认 API 变更会在同一个 PR 中同步 Swagger 注解、API 契约、调用方和测试。
- [ ] 我已确认不会把真实密码、JWT 私钥、OSS AccessKey、微信 AppSecret 或模型 Key 写入仓库。

## 2. 模块职责

### 2.1 本模块负责

| 能力 | 说明 |
|---|---|
| 统一业务入口 | 对管理端和小程序暴露 `/api/v1/**`，Nginx 只代理本服务 |
| 认证与授权 | 微信登录、管理员登录、Access/Refresh Token、JWT audience 隔离、RBAC |
| 公共内容 | 景点、分类、标签、攻略、美食、轮播图、搜索和首页聚合 |
| 用户业务 | 用户资料、偏好、收藏、行为上报、个性化推荐 |
| AI 会话事实 | `ai_conversation`、`ai_message`、`ai_message_source` 的唯一持久化事实源 |
| AI 编排入口 | 校验用户归属、生成固定 `threadId`、幂等控制、调用 FastAPI、映射错误 |
| 文件与 OSS | 上传校验、私有对象键、签名 URL、文件元数据和回收任务 |
| 管理端业务 | 内容发布、知识库任务、用户管理、角色权限、系统配置和审计日志 |
| 横切能力 | 限流、`X-Request-Id`、日志脱敏、统一异常、幂等、乐观锁和缓存 |

### 2.2 本模块不负责

- 不执行 LangChain Agent、RAG 或向量检索；这些能力在 `ai-service/`。
- 不直接向小程序或管理端暴露 FastAPI 地址、Milvus 地址、数据库凭据或 OSS AccessKey。
- 不把 Vue 静态构建产物作为运行时业务依赖；演示时由 Nginx 托管。
- 不把业务应用放入 Docker Compose；本服务始终在 IDEA 或宿主机 JVM 中运行。
- 不在 Controller 中写 SQL、事务、权限判断以外的业务分支或外部 HTTP 调用。

## 3. 系统边界

```text
admin-web :5173 ─┐
                 ├─> Spring Boot :8080 ─> MySQL / Redis / OSS
user-wx ─────────┘          │
                            └─ HMAC HTTP ─> FastAPI 127.0.0.1:8000 ─> Milvus
                                                   │
                                                   └─ HMAC HTTP ─> Spring Boot
                                                      /internal/v1/ai/**（只读）
```

规则：

1. Spring Boot 是唯一业务网关；管理端和小程序不直连 AI 服务。
2. FastAPI 仅监听 `127.0.0.1:8000`，Nginx 不为其配置 `location`。
3. `/internal/v1/ai/**` 只允许 FastAPI 通过 HMAC 调用，Nginx 必须返回 404。
4. AI 服务需要景点、攻略、美食或推荐数据时，只能回调这里的内部只读接口。
5. MySQL 是业务事实源；Redis 只保存缓存、令牌状态、限流状态和 LangGraph checkpoint。

## 4. 目标目录结构

```text
springboot/
├─ README.md
├─ pom.xml
├─ .env.example
├─ src/main/java/com/luoyang/tourism/
│  ├─ TourismApplication.java
│  ├─ common/
│  │  ├─ api/                    # ApiResponse、PageResponse、ResultCode
│  │  ├─ exception/              # BizException、GlobalExceptionHandler
│  │  ├─ enums/
│  │  ├─ trace/                  # TraceId、MDC Filter
│  │  └─ util/                   # ID、时间、脱敏、校验
│  ├─ config/
│  │  ├─ security/               # SecurityFilterChain、JwtEncoder/Decoder
│  │  ├─ mybatis/                # 分页、乐观锁、审计字段填充
│  │  ├─ redis/                  # 序列化、缓存管理器
│  │  ├─ knife4j/                # OpenAPI 分组和 JWT Scheme
│  │  ├─ web/                    # CORS、Jackson、上传限制
│  │  ├─ oss/                    # OSS Client、签名 URL
│  │  ├─ http/                   # RestClient/WebClient 超时
│  │  └─ async/                  # 索引轮询、文件回收线程池
│  ├─ security/
│  │  ├─ jwt/                    # 签发、刷新、密钥加载
│  │  ├─ filter/                 # Trace、限流、审计附加信息
│  │  ├─ handler/                # 401/403 JSON
│  │  └─ principal/              # AppUserPrincipal、AdminPrincipal
│  ├─ integration/
│  │  ├─ ai/                     # FastAPI 客户端、DTO、HMAC、超时/重试
│  │  ├─ wechat/                 # jscode2session 客户端
│  │  └─ oss/                    # 对象键策略、私有签名
│  ├─ module/
│  │  ├─ auth/
│  │  ├─ category/
│  │  ├─ spot/
│  │  ├─ tag/
│  │  ├─ guide/
│  │  ├─ food/
│  │  ├─ banner/
│  │  ├─ file/
│  │  ├─ appuser/
│  │  ├─ favorite/
│  │  ├─ behavior/
│  │  ├─ recommendation/
│  │  ├─ aichat/
│  │  ├─ knowledge/
│  │  ├─ adminuser/
│  │  ├─ rbac/
│  │  ├─ dashboard/
│  │  └─ audit/
│  ├─ job/
│  └─ support/
├─ src/main/resources/
│  ├─ application.yml
│  ├─ application-local.yml
│  ├─ application-test.yml
│  ├─ mapper/<module>/*.xml
│  ├─ db/migration/
│  │  ├─ V1__content_core.sql
│  │  ├─ V2__content_guides_foods_banner.sql
│  │  ├─ V3__user_domain.sql
│  │  ├─ V4__admin_rbac.sql
│  │  ├─ V5__file_and_config.sql
│  │  ├─ V6__ai_and_knowledge.sql
│  │  └─ V7__seed_permissions_and_roles.sql
│  └─ templates/
└─ src/test/java/com/luoyang/tourism/
   ├─ module/
   ├─ integration/
   └─ contract/
```

每个 `module/<name>/` 固定采用：

```text
controller/      # PublicXxxController / AppXxxController / AdminXxxController
service/         # 业务接口（只放接口）
service/impl/    # 接口实现，XxxServiceImpl
mapper/          # MyBatis Mapper 接口
pojo/
├─ dto/          # 接口请求 DTO
├─ entity/       # 数据库实体，不出 Controller
└─ vo/           # 接口响应 VO
```

新增目录前先判断是否真的需要。不要为只有一两个类的模块创建多层空包。所有 Controller、Service 接口、Service 实现、Mapper、Entity、DTO、VO 必须有中文类级注释；Service 公开方法必须有中文 Javadoc；复杂事务、幂等、HMAC、权限和脱敏逻辑必须有中文行内注释。

## 5. 本地启动

### 5.1 前置条件

- JDK 17，`java -version` 输出 17。
- Maven Wrapper 使用 3.9.x；当前锁定 `3.9.11`。不要在 IntelliJ IDEA 尚未验证支持时升级到 Maven 3.10.x，否则可能出现 `No implementation for ArtifactHandlerManager was bound` 的 Guice 初始化错误。
- Linux 虚拟机 `192.168.100.128` 的 Docker Engine 已启动；MySQL、Redis、Milvus 已按 `deploy/README.md` 启动。Nginx 在 Spring Boot 健康接口可用后再启用。
- IDEA 已安装 Lombok、Spring Boot 插件（若团队决定使用 Lombok）。
- FastAPI 可选；只做普通 CRUD 时 AI 服务可以暂不启动，但 AI 接口必须返回明确的 `50300`。

### 5.2 启动基础设施

Linux VM 中执行：

```bash
ssh root@192.168.100.128
cd /opt/luoyang-smart-tourism/deploy
docker compose up -d
docker compose ps -a
```

毕设本地环境默认关闭 Flyway：

```yaml
spring:
  flyway:
    enabled: ${FLYWAY_ENABLED:false}
```

当前远程数据库已经存在 31 张业务表，直接使用这些表启动即可。只有需要重新从空库初始化时，才临时设置 `FLYWAY_ENABLED=true` 并让 V1–V7 执行；如果 `flyway_schema_history` 已有 failed 记录，必须先处理失败记录或重建本地测试库，否则 Flyway 校验会阻止应用启动。

如需手动初始化空库，可执行 `deploy/sql/luoyang_tourism_init.sql`；不要在同一个库混用手动 SQL 和 Flyway 两套初始化流程。

### 5.3 配置与启动

Spring Boot 默认不会自动读取任意 `.env` 文件。推荐在 IDEA Run Configuration 的 Environment variables 中注入变量，或在当前 PowerShell 会话中设置：

```powershell
cd D:\luoyang-smart-tourism\springboot
$env:SPRING_PROFILES_ACTIVE = 'local'
.\mvnw.cmd spring-boot:run
```

测试与打包：

```powershell
cd D:\luoyang-smart-tourism\springboot
.\mvnw.cmd test
.\mvnw.cmd clean package
```

启动后检查：

| 地址 | 用途 |
|---|---|
| `http://localhost:8080/api/v1/public/...` | 业务 API |
| `http://localhost:8080/doc.html` | Knife4j 页面 |
| `http://localhost:8080/v3/api-docs` | OpenAPI 原始文档 |
| `http://localhost:8080/actuator/health` | 本机健康检查，禁止公网暴露 |

`application-local.yml` 必须配置：

```yaml
server:
  address: 0.0.0.0
  port: 8080
  shutdown: graceful
```

只绑定 `127.0.0.1` 会导致 Linux VM 中的 Nginx 容器无法通过 `192.168.100.1:8080` 访问。

## 6. 环境变量

真实值不写入公开 Markdown、源码或 Git 历史。本地开发可放在 Git 忽略的 `config/application-secret.yml`，首次配置参考 `config/application-secret.yml.example`；凭据清单位于项目根目录的 `all password/PASSWORDS.md`。生产环境必须改用服务器环境变量或密钥管理服务。

| 变量 | 示例 | 说明 |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `local` | 本地 profile |
| `FLYWAY_ENABLED` | `false` | 毕设本地默认关闭 Flyway；仅在重新初始化空库时临时设为 `true` |
| `MYSQL_HOST` / `MYSQL_PORT` | `192.168.100.128` / `3306` | 连接 Linux VM 中的 MySQL |
| `MYSQL_DATABASE` | `luoyang_tourism` | 库名 |
| `MYSQL_USERNAME` / `MYSQL_PASSWORD` | 当前 `root` / 本地密码 | 当前隔离 VM 实验可临时使用 root；目标环境必须改为最小权限应用账号 |
| `REDIS_HOST` / `REDIS_PORT` | `192.168.100.128` / `6379` | 连接 Linux VM 中的 Redis |
| `REDIS_PASSWORD` | 当前为空 | 当前隔离 VM 无密码；启用密码后必须与 Linux 配置一致 |
| `JWT_ISSUER` | `luoyang-tourism` | 统一签发者 |
| `JWT_APP_SECRET` / `JWT_ADMIN_SECRET` | 不提交 | App/Admin 使用不同 HMAC 密钥，至少 32 字节 |
| `APP_OSS_ENABLED` / `OSS_ENDPOINT` / `OSS_BUCKET` | `true` / `https://oss-cn-beijing.aliyuncs.com` / `luoyang-smart-tourism` | Bucket 必须私有；本地 `application-local.yml` 默认启用 |
| `OSS_ACCESS_KEY_ID` / `OSS_ACCESS_KEY_SECRET` | 不提交 | 最小权限 RAM 用户 |
| `WECHAT_APP_ID` / `WECHAT_APP_SECRET` | 不提交 | `jscode2session` |
| `AI_SERVICE_BASE_URL` | `http://127.0.0.1:8000` | 仅 Java 使用 |
| `INTERNAL_SHARED_SECRET` | 不提交 | Java/FastAPI HMAC，至少 32 字节 |
| `BOOTSTRAP_ADMIN_USERNAME` / `BOOTSTRAP_ADMIN_PASSWORD` | `admin` / 不提交 | 仅首次本地初始化管理员使用 |
| `INTERNAL_TIMESTAMP_TOLERANCE_SECONDS` | `300` | 防重放时间窗 |

约定：

- AI 服务的 Qwen Key 只放 `ai-service`，Spring Boot 不持有 `QWEN_API_KEY`。
- 针对 AI 服务，Spring Boot 只持有 `AI_SERVICE_BASE_URL` 和 `INTERNAL_SHARED_SECRET`；Qwen Key 不进入 Java 进程。
- 本机真实凭据位于 Git 忽略的 `springboot/config/application-secret.yml`，凭据清单位于项目根的 `all password/PASSWORDS.md`，两者都不得提交。
- 不同环境使用不同密钥；轮换密钥必须同时更新 Java 与 Python，并先验证 HMAC 契约测试。
- 日志、Swagger 示例和异常响应不得输出变量值。

## 7. API 与安全约定

### 7.1 路由分组

| 前缀 | 使用者 | 鉴权 | 说明 |
|---|---|---|---|
| `/api/v1/public/**` | 所有人 | 无 | 只读，过滤未发布/软删除 |
| `/api/v1/app/**` | 小程序 | `aud=user-wx` 的 Access Token | 用户资料、收藏、推荐、AI |
| `/api/v1/admin/**` | 管理端 | `aud=admin-web` 的 Access Token + 权限码 | 内容、系统、审计 |
| `/internal/v1/ai/**` | FastAPI | HMAC + requestId | 只读业务摘要，禁止公网 |

Swagger 固定分组：`public`、`app`、`admin`、`internal`。`internal` 分组只在 `local`/受控内网启用。

### 7.2 响应与字段

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "traceId": "01JABCDEFGHJKLMNPQRSTUVWXY",
  "timestamp": "2026-11-18T14:30:00+08:00"
}
```

- HTTP 状态码和业务 `code` 同时正确，禁止全部返回 HTTP 200。
- BIGINT ID 在 JSON 中统一为字符串。
- 时间为 ISO-8601 带偏移。
- 分页统一 `page`、`pageSize`、`records`、`total`，`pageSize <= 50`。
- 创建返回 200 和新资源；删除返回 200 且 `data=null`。

### 7.3 JWT

- App Token：`aud=user-wx`、`token_type=access`。
- Admin Token：`aud=admin-web`、`token_type=access`。
- Refresh Token 只能调用刷新接口；管理端 Refresh 使用 HttpOnly Cookie，小程序使用 JSON 返回。
- 使用 Spring Security OAuth2 Resource Server/Nimbus 官方能力，禁止手写 JWT 解析器。
- 管理员改密、禁用、角色变化必须递增 `token_version` 或清除刷新状态。
- 401/403 必须返回统一 JSON，不返回 HTML 登录页。

### 7.4 AI 对话治理

- 小程序只传 `conversationId`，不能生成或指定新 `threadId`。
- `ai_conversation.thread_id` 与外部 `conversationId` 的值相同，由 Java 用 26 位 ULID 生成一次。
- Java 校验会话属于当前用户后，把同一个值作为 FastAPI `thread_id`。
- `clientMessageId` 是幂等键；同会话同键且内容一致时返回已有结果。
- 同一 `threadId` 同时只允许一个生成请求，使用 Redis 锁；冲突返回 `40903`。
- Java 保存用户可见消息和来源；FastAPI checkpoint 丢失不能导致消息事实丢失。
- 资料不足返回结构化 `insufficient=true`，前端展示 `insufficient-card`，后端不得编造来源。

### 7.5 内部 HMAC

Java 调用 FastAPI 和 FastAPI 回调 Java 使用同一 canonical string：

```text
timestamp + "\n" + METHOD + "\n" + pathWithSortedQuery + "\n" + requestId + "\n" + rawBody
```

要求：

- 查询参数稳定排序；`rawBody` 使用实际发送的 UTF-8 原始字节。
- 时间窗默认 ±300 秒。
- 使用常量时间比较。
- 连接超时 2 秒、读超时 10 秒；重试只用于幂等请求且有上限。
- `/internal/**` 在 Nginx 层 404，在 Spring Security 层再次校验 HMAC。

## 8. 数据库与持久化规则

31 张表的实际审查结果见 [DATABASE-REVIEW.md](DATABASE-REVIEW.md)。数据库迁移以 `src/main/resources/db/migration/` 为唯一事实源。

1. 表结构事实源是 `src/main/resources/db/migration/V*.sql`，不是 Markdown。
2. **严禁物理外键、`REFERENCES`、数据库级联删除**；关联字段只做逻辑外键，由 Service 校验。
3. 每个逻辑外键列必须有普通索引；组合索引遵循最左前缀。
4. 内容查询必须过滤 `is_deleted=0`，公开查询必须过滤已发布状态。
5. 已执行 Flyway 脚本不修改；新增变更建立新版本文件。
6. Service 层写操作先校验目标存在、状态合法、归属正确，再落库。
7. 删除采用软删除；外部文件/向量通过任务补偿，不把网络调用包在数据库事务中。
8. 乐观锁字段 `version` 必须参与更新条件，冲突返回 `40902`。
9. 收藏等重复写入使用唯一索引或原子 upsert，不能只靠“先查再插”。
10. 数据库迁移前后执行无外键检查：

```sql
SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = 'luoyang_tourism'
  AND CONSTRAINT_TYPE = 'FOREIGN KEY';
-- 期望 0
```

## 9. 编码规则摘要

- Controller 只做参数校验、权限注解、Swagger 注解、调用 Service 和统一响应。
- Service 管理事务、业务规则、逻辑外键校验、缓存失效和领域事件。
- Mapper 只写 SQL；动态参数使用 `#{}`，禁止 `${}` 拼接用户输入。
- Entity 不直接作为 API 响应；使用 Converter 转 DTO/VO。
- 外部 HTTP（FastAPI、微信、OSS）不得放在数据库事务内。
- 缓存 key 使用 `tourism:spring:<domain>:<version>:<id>`，更新后 `afterCommit` 失效。
- 敏感日志必须脱敏：Token、密码、OpenID、手机号、完整问题和 HMAC 原文。
- 管理员写操作写 `admin_operation_log`；查询日志不得记录请求体中的密码等敏感字段。
- 所有新增错误码加入 `ResultCode` 和 API 契约，并补至少一个反向测试。

## 10. 测试要求

```powershell
cd D:\luoyang-smart-tourism\springboot
.\mvnw.cmd test
```

至少覆盖：

- JWT：过期、伪造、错误 audience、Refresh 重放、App/Admin 串用。
- 权限：未登录 401、无权限 403、资源归属 40301。
- 内容：未发布/软删除不可见；逻辑外键不存在时写入失败。
- 收藏：重复收藏幂等；只能操作自己的数据。
- 推荐：冷启动、候选为空、AI 不可用不影响基础推荐。
- AI：会话归属、threadId 固定、clientMessageId 幂等、并发锁、资料不足映射。
- 数据库：Flyway 从空库可执行；`FOREIGN KEY` 数量为 0。
- 契约：OpenAPI 快照、统一错误结构、内部 HMAC 向量。

## 11. Definition of Done

- [ ] 功能和异常场景已实现，权限边界正确。
- [ ] Swagger/Knife4j 注解、示例和错误响应完整。
- [ ] API 契约、前端请求封装和测试在同一 PR 中同步。
- [ ] Flyway 迁移已提交且未修改历史脚本，无物理外键。
- [ ] 逻辑外键有索引，Service 完整性校验生效。
- [ ] 单元/切片/集成/契约测试通过。
- [ ] 日志带 `traceId`，敏感字段已脱敏。
- [ ] 失败、空数据、401、403、409、429、503 有明确响应。
- [ ] 没有将 Spring Boot 或任何业务应用加入 Compose。
- [ ] 本 README 或相关 ADR/API 文档已更新。

## 12. 常见问题

| 现象 | 优先检查 |
|---|---|
| Flyway 启动失败 | 本地默认应设置 `FLYWAY_ENABLED=false`；若启用，检查 `flyway_schema_history` 是否有 failed 记录、历史脚本是否被修改、字符集是否为 `utf8mb4` |
| 401 但 Token 看似正确 | `aud`、`token_type`、issuer、公钥、时钟偏差、Redis 黑名单 |
| 管理端跨域/Cookie 丢失 | Vite `withCredentials`、后端 CORS、Cookie `SameSite/Secure`、HTTPS |
| Nginx 返回 502 | Spring Boot 是否绑定 `0.0.0.0:8080`、Windows 防火墙、Linux VM 是否能访问 `192.168.100.1:8080` |
| AI 接口 503 | FastAPI 是否启动、HMAC 是否一致、请求是否超时、Milvus/Redis 是否可用 |
| 数据重复 | 是否有唯一索引、是否实现幂等键、是否在事务外做了“先查后写” |
| BIGINT 精度错误 | API 是否把 ID 序列化为字符串，前端是否按字符串处理 |

## 13. 变更记录责任

以下变更必须在同一 PR 内完成：

- API：Controller/DTO 注解 + `docs/api/API-CONTRACT.md` + 调用方 + 契约测试。
- 数据库：新 Flyway 文件 + `docs/database/DATABASE-DESIGN.md` + 索引检查。
- AI 编排：`integration/ai` + AI 契约 + `docs/ai/AI-SERVICE-DESIGN.md`。
- 端口、环境变量、安全策略：本 README + 根 README + `deploy/README.md`。

提交示例：

```text
feat(springboot): add public scenic spot query api
fix(springboot): reject admin token on app routes
docs(springboot): document ai conversation ownership checks
```