# 围栏打卡系统（Geofence Check-in）

员工走到指定地理围栏内，用手机 H5 完成打卡；管理端配置围栏/规则/人员并查看考勤结果。

**产品形态：基座 + 二开**。`core` 是稳定的产品资产（认证、RBAC、数据范围、围栏、打卡事实），`biz` 是按客户需求可整体替换的考勤参考实现（固定班次），`api` 是可改的接口薄壳层——二次开发不动基座。

## 功能

- 登录认证：JWT 双 token（access 2h + refresh 原子轮换/吊销），登录失败限速锁定
- 权限体系：RBAC + 数据范围（SELF / GROUP / ALL），接口注解鉴权 + 查询注入双闸，22 个权限码
- 围栏管理：高德地图选点、半径可调（建议 ≥150m，硬下限 30m）、启停/逻辑删
- 打卡：服务端 Haversine 复算判定（端上判定不可信），命中任一启用围栏（重叠取最近）才落库；mock/真实双轨开关
- 考勤：固定班次规则（生效日 bitmask + 绑定角色/分组/人员），迟到/早退/缺卡/旷工/地点异常判定，日报全量重算
- 审计：成功与失败操作均落审计（独立事务防回滚），密码脱敏
- 代打卡：独立权限码 + 强制备注 + 数据范围校验，禁补录未来时间

## 架构

```
backend  Spring Boot 3 单体（包级分层）
├── core/    产品基座（二开不改）
│   ├── auth      JWT、Security 过滤器、hasPermi、登录限速
│   ├── system    用户/角色/分组、数据范围注入、审计
│   ├── geo       围栏 CRUD + Haversine 纯函数
│   └── checkin   打卡事实（服务端判定，圈外拒绝）
├── biz/     参考实现（客户项目可替换）
│   ├── rule      固定班次规则 CRUD
│   ├── attend    无状态判定器 FixedShiftEvaluator + 重算
│   └── report    日报查询
└── api/     接口薄壳（参数绑定 → core/biz Service → 响应）

frontend  Vue 3 单项目
├── 管理后台（登录/围栏地图选点/规则/记录/日报/用户/分组/审计）
└── H5 打卡页（/checkin，高德定位，无 key 降级手动模式仅开发）
```

**事实与判定分离**：`checkin_record` 只存不可变事实（谁/何时/何地/距围栏多远），`daily_report` 是可删除重建的投影——规则改了全量重算即可。

## 技术栈

后端：Spring Boot 3.3 / Java 21 / MyBatis-Plus / PostgreSQL / Redis / Flyway / jjwt
前端：Vue 3 / Vite / Pinia / Element Plus / 高德 JS API

## 快速开始（开发环境）

前置要求：Docker + **Docker Compose v2**（`docker compose` 子命令；v1 与新版 Docker 不兼容）、Node 18+。

```bash
# 1. 起开发栈（PG + Redis + Maven dev 容器，首次会拉取镜像）
cd deploy && docker compose -f docker-compose.dev.yml up -d

# 2. 容器内启动后端（首次运行需下载 Maven 依赖，约几分钟；之后启动约 1 分钟）
docker exec -d geofence-dev sh -c 'SPRING_DATASOURCE_URL="jdbc:postgresql://postgres:5432/geofence_checkin" \
  SPRING_DATA_REDIS_HOST=redis mvn -s /root/.m2/settings.xml spring-boot:run > /tmp/app.log 2>&1'
docker exec geofence-dev tail -f /tmp/app.log   # 看到 "Started CheckinApplication" 即就绪，Ctrl+C 退出跟踪

# 3. 前端（回到仓库根目录）
cd ../frontend && npm install && npm run dev   # http://localhost:5173
```

默认账号：`admin/admin123`、`manager/manager123`、`employee/employee123`（仅 `app.init-enabled=true` 且空库时创建）

高德地图 key：复制 `frontend/.env.example` 为 `.env` 填入 `VITE_AMAP_KEY`（围栏选点 + H5 定位；不填时 H5 走手动坐标模式，仅开发构建）。

## 测试

```bash
docker exec geofence-dev mvn -s /root/.m2/settings.xml test
```
50 个单测覆盖：权限校验与数据范围（SELF/GROUP/ALL/组长语义）、真实围栏判定（圈内/圈外/重叠取最近/可疑标记）、固定班次判定矩阵（迟到/早退/缺卡/旷工/地点异常）、规则匹配优先级与校验。

## 生产部署

要求 Docker Compose v2（v1 在 `up` 阶段会因 `ContainerConfig` 字段缺失报错，无法使用）。

```bash
cd deploy
PG_PASSWORD=xxx JWT_SECRET=<32+随机串> HTTP_PORT=80 docker compose up -d --build
```

生产强制环境变量注入 `JWT_SECRET`/`PG_PASSWORD`；`CHECKIN_MOCK_ENABLED` 默认 false（真实判定）；`APP_INIT_ENABLED` 默认 false（不自动建初始账号，需 `true` 显式开启做首次引导后改回）。

## 目录

```
backend/    Spring Boot 后端（core/biz/api 分层 + Flyway 迁移）
frontend/   Vue 3 前端（管理后台 + H5 打卡页）
deploy/     docker compose（dev / prod）+ Maven 镜像配置
```
