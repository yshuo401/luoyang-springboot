# 数据库操作指南

## 方式一：随容器自动建表（推荐）

在项目根目录执行：

```bash
docker compose up -d mysql redis
docker compose ps
```

首次初始化会创建数据库 `luoyang`、应用账号 `luoyang_app`，并按顺序执行 `V1__init.sql`（15 张核心表）与 `V2__seed_categories.sql`（3 个初始分类）。验证：

```bash
docker compose exec mysql mysql -uroot -pluoyang1234 luoyang -e "SHOW TABLES; SELECT id,name,code FROM ly_scenic_category;"
```

## 方式二：对已有库手工执行迁移 SQL

适用于容器卷已存在、或连接的是 compose 之外的 MySQL：

```bash
docker compose exec -i mysql mysql -uroot -pluoyang1234 luoyang \
  < luoyang-springboot/src/main/resources/db/migration/V1__init.sql
docker compose exec -i mysql mysql -uroot -pluoyang1234 luoyang \
  < luoyang-springboot/src/main/resources/db/migration/V2__seed_categories.sql
```

或在宿主机安装了 mysql 客户端时：

```bash
mysql -h 127.0.0.1 -P 3308 -u root -p luoyang \
  < luoyang-springboot/src/main/resources/db/migration/V1__init.sql
```

手工建表后无需维护 Flyway 历史：后端启动时 Flyway 检测到“非空且无历史”的库会自动 baseline 到 V1，后续新版本脚本照常追加执行。不要对同一个库重复手工执行 V1。

## 验证清单

```sql
SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'luoyang';  -- 期望 15
SELECT table_name, engine, table_collation
FROM information_schema.tables WHERE table_schema = 'luoyang' ORDER BY table_name;  -- InnoDB / utf8mb4_0900_ai_ci
SELECT * FROM ly_scenic_category ORDER BY sort_order;  -- 期望 3 条
```

开发期联调数据示例见同目录 `DEMO_DATA.md`；正式景点资料应由管理端录入并附来源，不直接改业务表。
