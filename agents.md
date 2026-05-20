# Agents Guide

本文件给后续编码代理使用。开始改动前先阅读本文件，再按任务范围读取相关源码。

## 项目概览

这是 CRMEB Java 开源商城仓库，采用前后端分离和多端结构：

- `crmeb/`：Java 后端，Maven 多模块，Spring Boot 2.2.6.RELEASE，Java 8，MyBatis-Plus，MySQL，Redis。
- `admin/`：PC 管理后台，Vue 2.x + Element UI，Vue CLI 工程。
- `app/`：移动端商城，uni-app 工程，面向 H5、公众号、小程序等端。
- `接口文档/`：本地接口文档，包含公共接口、管理端接口、移动端接口。

根目录 README 存在中文编码显示异常的情况，优先以源码、`pom.xml`、`package.json`、`application*.yml` 和接口文档为准。

## 后端结构

后端根目录是 `crmeb/`，父 POM 声明以下模块：

- `crmeb-common`：公共模型、配置、工具、基础依赖。
- `crmeb-service`：业务服务层、DAO、service 实现、第三方服务工具。
- `crmeb-admin`：管理端 API，启动类 `com.zbkj.admin.CrmebAdminApplication`，默认端口 `8080`。
- `crmeb-front`：前台/移动端 API，启动类 `com.zbkj.front.CrmebFrontApplication`，默认端口 `8081`。

常见包结构：

- Controller：`crmeb-admin/src/main/java/com/zbkj/admin/controller`、`crmeb-front/src/main/java/com/zbkj/front/controller`
- 配置：各启动模块的 `config` 包
- 服务接口与实现：`crmeb-service/src/main/java/com/zbkj/service/service`、`service/impl`
- DAO：`crmeb-service/src/main/java/com/zbkj/service/dao`
- Mapper XML：按 `mybatis-plus.mapper-locations=classpath*:mapper/*/*Mapper.xml` 扫描

数据库初始化 SQL 位于 `crmeb/sql/Crmeb_v1.4.sql`。启动脚本位于 `crmeb/shell/`。

## 前端结构

`admin/` 是 Vue 2 管理后台：

- 入口：`src/main.js`
- 路由：`src/router`
- 状态：`src/store`
- API 请求：`src/api`
- 页面：`src/views`
- 公共组件：`src/components`
- 样式：`src/styles`

`app/` 是 uni-app 移动端：

- 入口：`main.js`、`App.vue`
- 页面配置：`pages.json`
- 应用配置：`manifest.json`
- API：`api/`
- 页面：`pages/`
- 公共组件：`components/`
- 静态资源：`static/`

## 常用命令

后端命令在 `crmeb/` 下执行：

```bash
./mvnw clean package
./mvnw -pl crmeb-admin -am package
./mvnw -pl crmeb-front -am package
./mvnw -pl crmeb-admin -am spring-boot:run
./mvnw -pl crmeb-front -am spring-boot:run
```

Windows PowerShell 可使用：

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd -pl crmeb-admin -am package
.\mvnw.cmd -pl crmeb-front -am package
```

管理后台命令在 `admin/` 下执行：

```bash
npm install
npm run dev
npm run build:prod
npm run lint
npm run test:unit
```

移动端通常使用 HBuilderX 打开 `app/` 运行或发行。该目录只有少量 npm 依赖，实际构建方式以 uni-app/HBuilderX 配置为准。

## 本地配置注意事项

后端默认配置写在：

- `crmeb/crmeb-admin/src/main/resources/application.yml`
- `crmeb/crmeb-front/src/main/resources/application.yml`

默认依赖：

- MySQL：`127.0.0.1:3306/single_open`
- Redis：`127.0.0.1:6379`
- 管理端 API：`8080`
- 前台 API：`8081`

配置文件中包含本地数据库账号、Redis 密码、图片路径等示例值。不要把真实生产密钥、账号、证书或私有域名写入仓库。改配置时优先使用环境 profile 或本地未提交文件。

## 编码约定

后端：

- 保持 Java 8 兼容，不使用高版本 Java 语法。
- 遵循现有 Controller -> Service -> DAO/Mapper 分层，不把业务逻辑堆进 Controller。
- 新增接口时同步检查管理端、前台端、接口文档和权限/白名单配置。
- MyBatis-Plus 逻辑删除默认值在配置中声明：删除值 `1`，未删除值 `0`。
- 修改数据库字段时同步更新实体、Mapper XML、SQL 脚本和相关导入导出逻辑。
- 保持现有返回体、异常、分页、校验注解和 Swagger 注解风格。

管理后台：

- 继续使用 Vue 2、Element UI、Vuex、Vue Router 的既有模式。
- API 文件按业务模块放在 `src/api`，页面按模块放在 `src/views`。
- 文件夹和页面命名沿用小驼峰或既有模块名；不要为局部需求重排目录。
- 修改页面时同时检查路由、权限、API 地址、表单校验、列表分页和导出功能。
- 样式优先局部 scoped 或既有公共样式，避免大范围改动 `src/styles`。

移动端：

- 遵循 uni-app 目录和生命周期约定。
- 新页面必须同步 `pages.json`。
- 注意多端兼容，避免只在 H5 可用的浏览器 API 直接进入公共逻辑。

## 验证建议

根据改动范围选择最小但有效的验证：

- 后端公共逻辑或接口改动：在 `crmeb/` 运行对应模块 Maven package 或 test。
- 管理后台页面/API 改动：在 `admin/` 运行 `npm run lint`，必要时运行 `npm run build:prod`。
- 移动端页面改动：用 HBuilderX 或项目既有 uni-app 流程运行目标端。
- 涉及数据库：用本地库验证 SQL 兼容性，并检查初始化脚本是否需要同步。
- 涉及登录、支付、订单、库存、余额、优惠券、分销、权限等核心链路时，必须做端到端手工验证或补充自动化验证。

## 工作准则

- 先用 `rg`/`rg --files` 搜索相关文件，再改动。
- 保持改动聚焦在任务范围内，不做无关格式化和大面积重构。
- 仓库可能有用户未提交改动；不要回滚或覆盖不属于当前任务的变更。
- 不提交 `node_modules/`、`target/`、日志、上传文件、IDE 配置、生产配置或密钥。
- 修改接口契约时，同时考虑 `admin/`、`app/` 和 `接口文档/` 是否需要调整。
- 发现 README 或注释乱码时，不要批量转码整个仓库；只在确有必要的目标文件上处理。


## 任务完成报告

对非纯问答的开发任务，最终回复必须包含：

- `本轮完成度:X%`
- 本轮主目标是否完成
- 已执行的验证
- 剩余缺口
- `下一刀`

如果任务属于路线图、长期任务或多阶段目标，还要包含：

- `整体目标完成度:Y%`
- 百分比计算依据
