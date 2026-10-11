# 开发期数据操作示例

迁移完成后先检查基础分类：

```sql
SELECT id, name, code, status
FROM ly_scenic_category
WHERE is_deleted = 0
ORDER BY sort_order, id;
```

如果需要让公开列表马上有一条可用于联调的记录，可以插入一条明确标注为演示内容的草稿。正式内容应由管理端录入，并使用已核验的洛阳资料：

```sql
INSERT INTO ly_scenic_spot
  (category_id, name, short_description, address, description, status, is_recommended)
SELECT id,
       '演示景点（请替换为核验资料）',
       '仅用于前后端联调的演示记录',
       '请填写已核验的洛阳地址',
       '请替换为来源明确的景点介绍。门票和开放时间应注明以官方最新公告为准。',
       'DRAFT',
       0
FROM ly_scenic_category
WHERE code = 'HISTORICAL_CULTURE'
  AND is_deleted = 0
LIMIT 1;
```

阶段 2 完成管理员 API 后，不建议继续直接改业务表；应通过管理端创建、校验和发布。发布前必须补齐分类、简介、封面等必填项，并由服务层校验景点状态和文件用途。

检查记录：

```sql
SELECT id, name, status, is_deleted
FROM ly_scenic_spot
ORDER BY id DESC
LIMIT 10;
```
