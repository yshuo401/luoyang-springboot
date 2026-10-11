-- 可重复执行的基础分类种子数据；不包含未经核验的景点事实内容
INSERT INTO ly_scenic_category (name, code, sort_order, status, remark)
VALUES
  ('历史文化', 'HISTORICAL_CULTURE', 10, 1, '用于历史遗址、博物馆和文化类景点'),
  ('自然风光', 'NATURAL_SCENERY', 20, 1, '用于山水、公园和自然景观类景点'),
  ('城市休闲', 'CITY_LEISURE', 30, 1, '用于城市休闲和夜游类内容')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  sort_order = VALUES(sort_order),
  updated_at = CURRENT_TIMESTAMP(3);
