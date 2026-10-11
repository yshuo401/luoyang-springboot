# Ubuntu 数据库操作指南

## 方式一：使用项目 Compose

在包含 `docker-compose.yml` 的项目根目录执行：

```bash
cp .env.example .env
chmod 600 .env
# 编辑 .env，设置三个密码
docker compose up -d mysql redis
docker compose ps
```

首次启动时 MySQL 会创建数据库 `luoyang` 和应用账号。迁移由 Spring Boot/Flyway 执行，推荐不要手工复制建表语句。

## 方式二：手工执行迁移 SQL

确认 MySQL 已健康后，从 Ubuntu 主机执行：

```bash
mysql -h 127.0.0.1 -P 3308 -u root -p luoyang \
  < luoyang-springboot/src/main/resources/db/migration/V1__init.sql
```

若使用应用账号执行，请先给该账号创建/修改表的权限；开发阶段通常让 Flyway 使用 `MYSQL_USER` 账号即可。当前迁移文件不包含真实管理员密码，管理员初始化将在认证阶段通过环境变量和 BCrypt 完成。

## 验证

```sql
SELECT VERSION();
SHOW TABLES;
SELECT table_name, table_collation
FROM information_schema.tables
WHERE table_schema = 'luoyang';
```

应看到 15 张 `ly_` 核心表，表引擎为 InnoDB，字符集为 utf8mb4。重复执行同一 Flyway 迁移不应手工进行；若数据库是全新开发库，删除卷后再重建会清空数据，执行前必须确认。

开发期联调数据示例见同目录的 `DEMO_DATA.md`。示例明确标注为演示记录，正式景点资料应由管理端录入并附来源。
