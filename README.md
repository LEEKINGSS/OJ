# 密码学算法判题系统

## 项目简介

### 项目介绍

基于 Vue 3 + Spring Boot + Docker 的 密码学算法在线评测系统。

在系统前台，管理员可以创建、管理题目；用户可以自由搜索题目、阅读题目、编写并提交代码。

在系统后端，能够根据管理员设定的题目测试用例在 代码沙箱 中对代码进行编译、运行、判断输出是否正确。

其中，代码沙箱可以作为独立服务，提供给其他开发者使用。

题目搜索页面：

![null20231026151906178.png](https://liking.oss-cn-wulanchabu.aliyuncs.com/aurora/articles/df6cad42d0c8bc716a75ba89091e6eaf.png)

在线做题页面，支持代码编辑器、代码高亮：

![null20231026151906503.png](https://liking.oss-cn-wulanchabu.aliyuncs.com/aurora/articles/5ddb804e68e404e9ceb96f17beabad82.png)

题目提交列表页面：

![null20231026151906750.png](https://liking.oss-cn-wulanchabu.aliyuncs.com/aurora/articles/35bb1368740ddc32310d839b939ae515.png)

创建题目页面，包含 Markdown 富文本编辑器、动态增删测试用例：

![null20231026151906966.png](https://liking.oss-cn-wulanchabu.aliyuncs.com/aurora/articles/ca71f7a710b02d20c3bd4970d7925fdb.png)

![null20231026151907197.png](https://liking.oss-cn-wulanchabu.aliyuncs.com/aurora/articles/0c9137adbe7dc3835be9b38d758bf798.png)

项目业务流程图：

![null20231026151907345.png](https://liking.oss-cn-wulanchabu.aliyuncs.com/aurora/articles/cb58cdb6d31f2332e38624a9d67b9176.png)

### 技术选型

#### 前端
- Vue 3
- Vue-CLI 脚手架
- Vuex 状态管理
- Arco Design 组件库
- 前端工程化：ESLint + Prettier + TypeScript
- ⭐️ 手写前端项目模板（通用布局、权限管理、状态管理、菜单生成）
- ⭐️ Markdown 富文本编辑器
- ⭐️ Monaco Editor 代码编辑器
- ⭐️ OpenAPI 前端代码生成
#### 后端
- ⭐️ Java Spring Cloud + Spring Cloud Alibaba 微服务(已裁剪)
- Nacos 注册中心（已裁剪）
- OpenFeign 客户端调用
- GateWay 网关
- 聚合接口文档
- Java Spring Boot（万用后端模板）
- Java 进程控制
- ⭐️ Java 安全管理器
- ⭐️ Docker 代码沙箱实现
- ⭐️ 虚拟机 + 远程开发
- MySQL 数据库
- MyBatis-Plus 及 MyBatis X 自动生成
- Redis 分布式 Session（已裁剪）
- ⭐️ RabbitMQ 消息队列（已裁剪）
⭐️ 多种设计模式
	- 策略模式
	- 工厂模式
	- 代理模式
	- 模板方法模式
- 其他：部分并发编程、JVM 小知识

### 贡献

**感谢**

- **吴飞鸿大佬**提供的word文档上传功能。
- **陈宁宁大佬**提供的latex公式渲染功能