# Spring Boot 业务后端（Lean MVP）

> 当前版本面向本科毕业设计。详细化简范围见 [SIMPLIFICATION-PLAN.md](../SIMPLIFICATION-PLAN.md)。

## 1. 职责

Spring Boot 是唯一对外业务入口，负责：

- 管理员/小程序 JWT 登录；
- 景点、分类、标签、攻略、美食、轮播管理；
- 公共内容查询与搜索；
- 小程序用户资料、偏好、收藏、行为；
- 个性化推荐；
- 文件与 OSS；
- 知识文档与索引任务记录；
- AI 会话、消息、来源的 MySQL 持久化；
- 通过 `X-Internal-Token` 调用 FastAPI。

它不负责：

- LangChain Agent/RAG；
- Milvus 向量检索；
- 前端页面渲染；
- Refresh Token、RBAC 角色编辑、审计日志和系统配置。

## 2. 当前认证方式

```text
管理员/用户登录
  -> BCrypt 校验
  -> 签发一个 Access Token
  -> 前端保存
  -> 请求头 Authorization: Bearer <token>
  -> 401 后重新登录
```

- App Token 的 audience 是 `user-wx`；
- Admin Token 的 audience 是 `admin-web`；
- 管理员固定角色：`ADMIN`；
- 小程序固定角色：`APP_USER`；
- 不再使用 Refresh Token、token_version 失效过滤器或 Redis 登录状态。

## 3. 包结构

```text
src/main/java/com/luoyang/tourism/
├─ common/         统一响应、错误码、异常、traceId、工具类
├─ config/         AppProperties、Security、CORS、Knife4j、OSS、启动管理员
├─ integration/    AI 客户端、OSS、微信客户端
├─ module/
│  ├─ auth/        登录、JWT、用户
│  ├─ content/     景点/分类/标签/攻略/美食/轮播
│  ├─ appuser/     小程序用户、收藏、行为、推荐
│  ├─ aichat/      AI 会话与消息
│  ├─ knowledge/   知识文档和索引任务
│  ├─ internal/    FastAPI 回调/查询接口
│  ├─ file/        OSS 文件
│  └─ system/      管理员账号、用户管理、Dashboard
├─ security/       JWT 解码、内部 token filter
└─ support/        少量请求辅助类
```

## 4. 配置文件

- `src/main/resources/application.yml`：通用配置；
- `src/main/resources/application-local.yml`：本地默认配置；
- `config/application-secret.yml`：本机密码/Key，已被 Git 忽略；
- `config/application-secret.yml.example`：示例模板；
- `.env.example`：环境变量模板。

关键变量：

```text
MYSQL_HOST / MYSQL_PORT / MYSQL_DATABASE / MYSQL_USERNAME / MYSQL_PASSWORD
JWT_APP_SECRET / JWT_ADMIN_SECRET
INTERNAL_TOKEN
AI_SERVICE_BASE_URL
OSS_*
WECHAT_*
BOOTSTRAP_ADMIN_*
```

`INTERNAL_TOKEN` 必须和 `ai-service/.env` 中的值完全一致。

## 5. 启动

```powershell
cd D:\luoyang-smart-tourism\springboot
mvn spring-boot:run
```

常用地址：

- `http://127.0.0.1:8080/actuator/health`
- `http://127.0.0.1:8080/doc.html`
- `http://127.0.0.1:8080/v3/api-docs`

## 6. 路由分组

| 分组 | 鉴权 |
|---|---|
| `/api/v1/public/**` | 公开 |
| `/api/v1/app/**` | App JWT (`ROLE_APP_USER`) |
| `/api/v1/admin/**` | Admin JWT (`ROLE_ADMIN`) |
| `/internal/v1/ai/**` | `X-Internal-Token` |

管理端不再使用 76 个 `@PreAuthorize` 权限码，路由前缀 + JWT 角色已经足够满足毕业设计。

## 7. 数据库

- MySQL 8；
- MyBatis XML；
- 严禁物理外键，逻辑外键和索引为主；
- Flyway 默认关闭，历史迁移文件保留作为数据库设计材料；
- RBAC、审计、系统配置相关表作为历史表保留，新代码不再依赖。

## 8. 测试

```powershell
mvn test
```

当前测试覆盖：

- 上下文启动；
- OpenAPI 可用；
- 公开/App/Admin/内部接口边界；
- JWT audience；
- 内容服务；
- AI 聊天幂等和错误映射；
- 迁移/安全基础检查。

## 9. 当前实现取舍

- 单实例本地演示，不做多节点分布式锁；
- Access Token 过期后重新登录；
- 索引任务仍保留异步状态查询，但 AI 侧任务状态存于进程内存；
- AI 多轮记忆在 FastAPI 进程内，不跨重启。

## 10. 相关文档

- [简化方案](../SIMPLIFICATION-PLAN.md)
- [项目总入口](../README.md)
- [API 历史契约](../docs/api/API-CONTRACT.md)
- [AI 服务](../ai-service/README.md)