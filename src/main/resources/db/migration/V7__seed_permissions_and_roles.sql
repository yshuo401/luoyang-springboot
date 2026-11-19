-- 初始化权限、角色和角色权限关系。
-- 说明：本脚本不声明物理外键，关联完整性由 Service 层维护。

-- 权限字典：菜单权限和操作权限统一登记。
INSERT INTO admin_permission (parent_id, permission_name, permission_code, permission_type, route_path, component_path, sort_order, status)
VALUES
(0, '仪表盘', 'dashboard:read', 2, '/dashboard', 'views/dashboard/index', 10, 1),
(0, '景点管理', 'content:spot:read', 2, '/content/spots', 'views/content/spot/index', 20, 1),
(0, '分类管理', 'content:category:read', 2, '/content/categories', 'views/content/category/index', 21, 1),
(0, '标签管理', 'content:tag:read', 2, '/content/tags', 'views/content/tag/index', 22, 1),
(0, '攻略管理', 'content:guide:read', 2, '/content/guides', 'views/content/guide/index', 23, 1),
(0, '美食管理', 'content:food:read', 2, '/content/foods', 'views/content/food/index', 24, 1),
(0, '美食分类', 'content:food-category:read', 2, '/content/food-categories', 'views/content/food-category/index', 25, 1),
(0, '轮播图管理', 'content:banner:read', 2, '/content/banners', 'views/content/banner/index', 26, 1),
(0, '文件管理', 'file:read', 2, '/files', 'views/file/index', 30, 1),
(0, '知识库', 'knowledge:read', 2, '/knowledge', 'views/knowledge/index', 40, 1),
(0, '小程序用户', 'system:user:read', 2, '/system/users', 'views/system/user/index', 50, 1),
(0, '管理员管理', 'system:admin:read', 2, '/system/admins', 'views/system/admin/index', 51, 1),
(0, '角色管理', 'system:role:read', 2, '/system/roles', 'views/system/role/index', 52, 1),
(0, '权限管理', 'system:permission:read', 2, '/system/permissions', 'views/system/permission/index', 53, 1),
(0, '系统配置', 'system:config:read', 2, '/system/configs', 'views/system/config/index', 54, 1),
(0, '操作日志', 'audit:log:read', 2, '/audit/logs', 'views/audit/log/index', 60, 1),
(0, '新增分类', 'content:category:create', 3, NULL, NULL, 100, 1),
(0, '更新分类', 'content:category:update', 3, NULL, NULL, 101, 1),
(0, '删除分类', 'content:category:delete', 3, NULL, NULL, 102, 1),
(0, '新增景点', 'content:spot:create', 3, NULL, NULL, 110, 1),
(0, '更新景点', 'content:spot:update', 3, NULL, NULL, 111, 1),
(0, '发布景点', 'content:spot:publish', 3, NULL, NULL, 112, 1),
(0, '删除景点', 'content:spot:delete', 3, NULL, NULL, 113, 1),
(0, '新增标签', 'content:tag:create', 3, NULL, NULL, 120, 1),
(0, '更新标签', 'content:tag:update', 3, NULL, NULL, 121, 1),
(0, '删除标签', 'content:tag:delete', 3, NULL, NULL, 122, 1),
(0, '新增攻略', 'content:guide:create', 3, NULL, NULL, 130, 1),
(0, '更新攻略', 'content:guide:update', 3, NULL, NULL, 131, 1),
(0, '发布攻略', 'content:guide:publish', 3, NULL, NULL, 132, 1),
(0, '删除攻略', 'content:guide:delete', 3, NULL, NULL, 133, 1),
(0, '新增美食', 'content:food:create', 3, NULL, NULL, 140, 1),
(0, '更新美食', 'content:food:update', 3, NULL, NULL, 141, 1),
(0, '发布美食', 'content:food:publish', 3, NULL, NULL, 142, 1),
(0, '删除美食', 'content:food:delete', 3, NULL, NULL, 143, 1),
(0, '新增美食分类', 'content:food-category:create', 3, NULL, NULL, 150, 1),
(0, '更新美食分类', 'content:food-category:update', 3, NULL, NULL, 151, 1),
(0, '删除美食分类', 'content:food-category:delete', 3, NULL, NULL, 152, 1),
(0, '新增轮播图', 'content:banner:create', 3, NULL, NULL, 160, 1),
(0, '更新轮播图', 'content:banner:update', 3, NULL, NULL, 161, 1),
(0, '删除轮播图', 'content:banner:delete', 3, NULL, NULL, 162, 1),
(0, '上传文件', 'file:upload', 3, NULL, NULL, 200, 1),
(0, '删除文件', 'file:delete', 3, NULL, NULL, 201, 1),
(0, '上传知识文档', 'knowledge:upload', 3, NULL, NULL, 210, 1),
(0, '提交索引', 'knowledge:index', 3, NULL, NULL, 211, 1),
(0, '更新知识库', 'knowledge:update', 3, NULL, NULL, 212, 1),
(0, '删除知识库', 'knowledge:delete', 3, NULL, NULL, 213, 1),
(0, '更新小程序用户', 'system:user:update', 3, NULL, NULL, 250, 1),
(0, '新增管理员', 'system:admin:create', 3, NULL, NULL, 260, 1),
(0, '更新管理员', 'system:admin:update', 3, NULL, NULL, 261, 1),
(0, '重置密码', 'system:admin:reset-password', 3, NULL, NULL, 262, 1),
(0, '新增角色', 'system:role:create', 3, NULL, NULL, 270, 1),
(0, '更新角色', 'system:role:update', 3, NULL, NULL, 271, 1),
(0, '删除角色', 'system:role:delete', 3, NULL, NULL, 272, 1),
(0, '分配权限', 'system:role:assign', 3, NULL, NULL, 273, 1),
(0, '更新系统配置', 'system:config:update', 3, NULL, NULL, 280, 1);

INSERT INTO admin_role (name, code, description, status, sort_order)
VALUES
('超级管理员', 'SUPER_ADMIN', '拥有全部管理权限', 1, 0),
('内容运营', 'CONTENT_EDITOR', '负责内容、文件与知识库', 1, 10),
('运营观察员', 'OPERATOR', '只读查看运营数据', 1, 20);

INSERT INTO admin_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM admin_role r CROSS JOIN admin_permission p
WHERE r.code = 'SUPER_ADMIN' AND p.status = 1 AND p.is_deleted = 0;

INSERT INTO admin_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM admin_role r CROSS JOIN admin_permission p
WHERE r.code = 'CONTENT_EDITOR'
  AND (p.permission_code LIKE 'content:%' OR p.permission_code LIKE 'file:%'
       OR p.permission_code LIKE 'knowledge:%' OR p.permission_code = 'dashboard:read');

INSERT INTO admin_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM admin_role r CROSS JOIN admin_permission p
WHERE r.code = 'OPERATOR'
  AND p.permission_code IN ('dashboard:read', 'content:spot:read', 'content:category:read',
      'content:guide:read', 'content:food:read', 'system:user:read', 'audit:log:read');
