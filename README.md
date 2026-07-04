# 文化遗产数字平台 - 后台管理子系统

> 基于 Java 21 + Spring Boot 3 + Vue 3 的文化遗产数字平台后台管理系统，提供角色权限管理、文物数据管理、数据备份与恢复、日志管理等功能。

## 技术栈

### 后端
- **Java 21** + **Spring Boot 3.2**
- **Spring MVC** + **Spring AOP**
- **MyBatis-Plus 3.5** (ORM)
- **MySQL 8.0** (数据库)
- **Redis 7** (缓存/会话)
- **Sa-Token 1.37** (权限认证)
- **WebSocket** (实时通知)
- **Lombok** (代码简化)
- **PageHelper** (分页插件)
- **OpenCSV 5.8** (CSV导入导出)
- **阿里云OSS** (对象存储)

### 前端
- **Vue 3** (Composition API)
- **Vite 6** (构建工具)
- **Element Plus** (UI组件库)
- **Vue Router 4** (路由)
- **Pinia** (状态管理)
- **Axios** (HTTP请求)
- **ECharts 5** (数据可视化)

### 部署
- **Docker** + **Docker Compose** (容器化部署)

## 功能模块

### 1. 角色权限管理 (RBAC)
- 三类角色：超级管理员 / 内容审核员 / 数据管理员
- 基于Sa-Token的权限认证
- 动态菜单生成
- 权限树分配

### 2. 文物数据管理
- 文物CRUD操作
- 图片上传(阿里云OSS)
- CSV批量导入导出
- 分类/朝代/状态筛选

### 3. 知识图谱管理
- 三元组(主体-关系-客体)管理
- 关联文物
- 图数据库同步

### 4. 数据备份与恢复
- 手动/定时全量备份
- AES加密备份
- 二次确认(密码验证)恢复
- 备份文件下载

### 5. 日志管理
- 操作日志(AOP自动记录)
- 安全日志(登录/登出/权限变更)
- 系统日志(定时任务/异常)
- 日志导出CSV

## 内置初始数据

### 管理员账号
| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | Admin@123 | 超级管理员 |
| auditor | Audi@123 | 内容审核员 |
| dataadmin | Data@123 | 数据管理员 |

### 测试数据
- 15条文物测试数据
- 33个权限项
- 3个角色及权限映射

## 快速开始

### 方式一：Docker一键启动 (推荐)

**前提条件**：已安装 Docker 和 Docker Compose

```bash
# 在项目根目录执行
docker-compose up -d --build
```

启动完成后：
- 前端访问：http://localhost:8000
- 后端API：http://localhost:8081
- MySQL：localhost:3307
- Redis：localhost:6380

> 注：为避免与本地服务端口冲突，Docker映射端口已调整为非默认端口。容器内部仍使用标准端口，服务间通过Docker网络通信。

### 方式二：本地开发启动

#### 1. 启动MySQL和Redis

```bash
# 使用Docker快速启动MySQL和Redis
docker run -d --name relic-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root123 -e MYSQL_DATABASE=relic_admin mysql:8.0
docker run -d --name relic-redis -p 6379:6379 redis:7-alpine
```

#### 2. 初始化数据库

```bash
# 执行SQL初始化脚本
mysql -h127.0.0.1 -uroot -proot123 relic_admin < backend/src/main/resources/sql/init.sql
```

#### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动后会自动：
- 重新哈希管理员密码(BCrypt)
- 创建必要的目录结构

#### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端访问：http://localhost:5173

## 项目结构

```
AdminPanel/
├── backend/                    # 后端Spring Boot项目
│   ├── src/main/java/com/relic/admin/
│   │   ├── annotation/         # 自定义注解
│   │   ├── aop/                # AOP切面
│   │   ├── common/             # 通用类(异常/响应/常量)
│   │   ├── config/             # 配置类
│   │   ├── controller/         # 控制器
│   │   ├── dto/                # 数据传输对象
│   │   ├── entity/             # 实体类
│   │   ├── init/               # 数据初始化
│   │   ├── mapper/             # MyBatis映射
│   │   ├── properties/         # 配置属性
│   │   ├── service/            # 业务逻辑
│   │   ├── task/               # 定时任务
│   │   ├── util/               # 工具类
│   │   ├── vo/                 # 视图对象
│   │   └── websocket/          # WebSocket
│   ├── src/main/resources/
│   │   ├── mapper/             # MyBatis XML
│   │   ├── sql/init.sql        # 数据库初始化脚本
│   │   ├── application.yml     # 配置文件
│   │   └── application-docker.yml  # Docker配置
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                   # 前端Vue项目
│   ├── src/
│   │   ├── api/                # API请求
│   │   ├── assets/             # 静态资源
│   │   ├── components/         # 公共组件
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # Pinia状态
│   │   ├── utils/              # 工具函数
│   │   └── views/              # 页面
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
├── docker-compose.yml          # Docker编排
└── README.md
```

## API文档

### 认证相关
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/admin/auth/login | 登录 |
| POST | /api/admin/auth/logout | 登出 |
| GET | /api/admin/auth/info | 获取当前用户信息 |
| GET | /api/admin/auth/menus | 获取动态菜单 |
| GET | /api/admin/auth/captcha | 获取图形验证码 |
| PUT | /api/admin/auth/password | 修改密码 |

### 文物管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/admin/artifacts | 文物列表 |
| GET | /api/admin/artifacts/{id} | 文物详情 |
| POST | /api/admin/artifacts | 新增文物 |
| PUT | /api/admin/artifacts/{id} | 更新文物 |
| DELETE | /api/admin/artifacts/{id} | 删除文物 |
| POST | /api/admin/artifacts/import | CSV导入 |
| GET | /api/admin/artifacts/export | CSV导出 |
| POST | /api/admin/artifacts/images | 图片上传 |

### 知识图谱
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/admin/knowledge | 三元组列表 |
| POST | /api/admin/knowledge | 新增三元组 |
| PUT | /api/admin/knowledge/{id} | 更新三元组 |
| DELETE | /api/admin/knowledge/{id} | 删除三元组 |
| POST | /api/admin/knowledge/{id}/sync | 同步图数据库 |

### 备份管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/admin/backups | 备份列表 |
| POST | /api/admin/backups | 创建备份 |
| POST | /api/admin/backups/{id}/restore | 恢复备份(2FA) |
| GET | /api/admin/backups/{id}/download | 下载备份 |
| GET | /api/admin/backups/status | 维护状态 |

### 日志管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/admin/logs/operation | 操作日志 |
| GET | /api/admin/logs/security | 安全日志 |
| GET | /api/admin/logs/system | 系统日志 |
| GET | /api/admin/logs/{id} | 日志详情 |
| GET | /api/admin/logs/export | 导出CSV |
| DELETE | /api/admin/logs/{id} | 删除日志 |

### 系统管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/admin/admins | 管理员列表 |
| POST | /api/admin/admins | 新增管理员 |
| PUT | /api/admin/admins/{id} | 更新管理员 |
| DELETE | /api/admin/admins/{id} | 删除管理员 |
| PUT | /api/admin/admins/{id}/password | 重置密码 |
| PUT | /api/admin/admins/{id}/status | 切换状态 |
| GET | /api/admin/roles | 角色列表 |
| POST | /api/admin/roles | 新增角色 |
| PUT | /api/admin/roles/{id} | 更新角色 |
| DELETE | /api/admin/roles/{id} | 删除角色 |
| GET | /api/admin/roles/{id}/permissions | 角色权限 |
| PUT | /api/admin/roles/{id}/permissions | 分配权限 |
| GET | /api/admin/permissions/tree | 权限树 |

## 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

## 配置说明

### 阿里云OSS配置
在 `application.yml` 或环境变量中配置：
```yaml
oss:
  endpoint: oss-cn-hangzhou.aliyuncs.com
  access-key-id: your-access-key-id
  access-key-secret: your-access-key-secret
  bucket-name: relic-admin
  dir: relic
```

### 备份配置
```yaml
backup:
  path: /data/backups
  aes-key: RelicAdmin2024AES!
  cron: 0 0 2 * * ?    # 每天凌晨2点定时备份
```

## 停止服务

```bash
# Docker方式
docker-compose down

# 清理数据卷(谨慎操作)
docker-compose down -v
```

## 许可证

本项目仅用于学习和教学目的。
