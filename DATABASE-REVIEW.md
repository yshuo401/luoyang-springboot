# 31 张业务表审查报告

> 审查日期：2026-11-19
> 审查对象：springboot/src/main/resources/db/migration 下的 Flyway 脚本
> 数据库：MySQL 8.0，utf8mb4
> 结论：31 张业务表均已实际执行成功，物理外键数量为 0。

## 1. 表清单核对

内容域：

- scenic_category
- scenic_spot
- scenic_image
- tag
- scenic_spot_tag
- travel_guide
- guide_day
- guide_day_spot
- food_category
- food
- food_image
- banner

用户域：

- app_user
- user_preference
- user_behavior
- user_favorite
- user_recommendation_log

管理域：

- admin_user
- admin_role
- admin_permission
- admin_user_role
- admin_role_permission
- admin_operation_log

系统与 AI 域：

- file_object
- system_config
- ai_conversation
- ai_message
- ai_message_source
- knowledge_document
- knowledge_chunk
- ai_index_job

## 2. 已验证项目

- Flyway 7 个版本从空库顺序执行成功。
- 业务表数量为 31，另有一张 Flyway 历史表 flyway_schema_history。
- information_schema 中 FOREIGN KEY 约束数量为 0。
- 内容表均有 is_deleted 过滤所需的查询路径。
- 逻辑关联字段均有普通索引或位于组合索引最左前缀。
- 所有主键均为 BIGINT UNSIGNED AUTO_INCREMENT。
- 时间字段统一使用 DATETIME(3)。
- 经纬度使用 DECIMAL，不使用 FLOAT/DOUBLE。
- ai_conversation.thread_id 使用 CHAR(26) ASCII，和 Java ULID 对齐。
- ai_message.client_message_id 按 CHAR(36) 设计，Java DTO 已限制为最长 36 字符。
- 收藏、角色关系、角色权限关系和景点标签关系均使用唯一索引防重。
- 对当前 192.168.100.128 的 MySQL 8.0.46 只读检查：luoyang_tourism 中 31 张业务表名称与预期集合完全一致，物理外键数量为 0。

## 3. 需要说明的设计点

### 3.1 攻略分类和攻略标签

31 表基线中没有 guide_category 或 guide_tag 关系表，travel_guide 也没有分类/标签列。原 API 文档曾提出攻略列表按 category/tagId 过滤，这与当前表结构不一致。

当前实现选择：

- 保留 31 表不变；
- 攻略列表只支持 keyword、status、sort；
- GuideCard.tags 返回空数组，保留字段供后续扩展；
- 管理端攻略不接收 category/tagIds。

如果后续必须支持攻略标签，建议新增 guide_tag 关系表并建立 Flyway 新版本，而不是把标签 ID 拼接进字符串字段。

### 3.2 scenic_image.is_cover

景点封面当前以 scenic_spot.cover_file_id 为准。scenic_image.is_cover 保留作为图集内部“封面候选”扩展字段，业务代码不依赖它保证唯一性。

### 3.3 软删除与唯一索引

以下表存在“唯一键包含 is_deleted”或业务关系替换需求：

- guide_day：同一攻略下 day_no 唯一；
- guide_day_spot：同一天内 sort_order 唯一；
- user_preference：同一用户、类型、目标唯一；
- user_favorite：同一用户、业务对象唯一。

当前实现使用以下策略：

- 行程替换使用事务内的物理删除再重建，避免逻辑删除行阻塞同序号重建；
- 显式用户偏好替换使用删除再插入；
- 收藏使用唯一索引和幂等写入，取消收藏时物理删除；
- 内容主表仍使用软删除。

这与数据库设计文档中的“关系表可替换、内容表软删除”原则一致。

### 3.4 分类树循环

scenic_category.parent_id 是逻辑自关联，没有数据库外键。Service 层已加入父分类存在性和循环检测，防止形成 A -> B -> A。

## 4. 事务与外部依赖

- OSS 上传、FastAPI 调用和 Milvus 索引都不放在数据库事务内。
- 知识文档先落库，再提交索引任务；失败保留可重试任务。
- OSS 文件删除前检查业务引用；删除失败保留待删除状态。
- AI 会话消息和来源由 MySQL 持久化，checkpoint 不是用户可见消息的事实源。

## 5. 本地 Flyway 运行策略

当前远程数据库已经存在 31 张表，且 flyway_schema_history 中存在 V2 failed 记录。毕设本地启动默认关闭 Flyway，避免历史校验阻塞 Web 容器启动。只有重建空库时才临时启用 Flyway。

## 6. 结论

当前 31 张表可以作为毕业设计基线使用。需要特别注意的是：攻略分类/标签是原接口设想与 31 表基线之间的唯一明确功能缺口；目前代码已按“不支持攻略标签、保留返回字段”的方式实现，API 文档和本报告已同步。
