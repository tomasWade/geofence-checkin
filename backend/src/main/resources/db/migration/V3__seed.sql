INSERT INTO sys_role (id, role_code, role_name, data_scope, remark) VALUES
(1, 'EMPLOYEE', '员工', 'SELF', '打卡、查看本人记录与日报'),
(2, 'MANAGER', '经理', 'GROUP', '查看本组成员记录与日报'),
(3, 'ADMIN', '管理员', 'ALL', '全量管理');

INSERT INTO sys_permission (id, perm_code, perm_name, type) VALUES
(1,  'sys:user:list',       '用户查询', 'api'),
(2,  'sys:user:add',        '用户新增', 'api'),
(3,  'sys:user:edit',       '用户编辑', 'api'),
(4,  'sys:user:delete',     '用户删除', 'api'),
(5,  'sys:user:assignRole', '分配角色', 'api'),
(6,  'sys:group:list',      '分组查询', 'api'),
(7,  'sys:group:add',       '分组新增', 'api'),
(8,  'sys:group:edit',      '分组编辑', 'api'),
(9,  'sys:group:delete',    '分组删除', 'api'),
(10, 'fence:list',          '围栏查询', 'api'),
(11, 'fence:add',           '围栏新增', 'api'),
(12, 'fence:edit',          '围栏编辑', 'api'),
(13, 'fence:delete',        '围栏删除', 'api'),
(14, 'rule:list',           '规则查询', 'api'),
(15, 'rule:add',            '规则新增', 'api'),
(16, 'rule:edit',           '规则编辑', 'api'),
(17, 'rule:delete',         '规则删除', 'api'),
(18, 'record:list',         '打卡记录查询', 'api'),
(19, 'record:manualFix',    '代打卡/修正', 'api'),
(20, 'report:daily',        '日报查询', 'api'),
(21, 'report:rebuild',      '日报重算', 'api'),
(22, 'audit:list',          '审计日志查询', 'api');

INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(1, 18),
(2, 18), (2, 20),
(3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8), (3, 9),
(3, 10), (3, 11), (3, 12), (3, 13), (3, 14), (3, 15), (3, 16), (3, 17),
(3, 18), (3, 19), (3, 20), (3, 21), (3, 22);

INSERT INTO geo_fence (id, name, lng, lat, radius_m, created_by) VALUES
(1, '示例围栏·天府广场', 104.065735, 30.657008, 200, 1);
