# 合租记账系统 (PublicPay)

一个专为合租房屋设计的费用分摊管理系统，支持多人费用记录、智能分摊计算和实时余额统计。

## ✨ 功能特点

- 🏠 **室友管理**：添加、编辑、删除室友信息，支持唯一性验证
- 💰 **费用记录**：记录公共费用，支持多付款人模式
- 🏷️ **分类管理**：支持水电费、物业费、生活费等自定义分类
- ⚖️ **灵活分摊**：支持平均分摊和自定义分摊两种方式
- 📊 **余额统计**：实时计算每人的收支情况和欠款明细
- ✅ **支付状态**：标记分摊费用的支付状态，支持批量操作
- 📱 **响应式设计**：完美支持桌面和移动设备访问
- 🔄 **实时同步**：前后端数据实时同步，支持多人同时操作

## 🛠️ 技术栈

### 后端技术
- **Spring Boot 3.3.0** - 企业级Java后端框架
- **Spring Data JPA** - 数据访问层，支持自动建表
- **MySQL 8.0+** - 关系型数据库
- **Lombok** - 简化Java代码编写
- **Spring Validation** - 数据校验
- **Maven** - 项目管理和构建工具

### 前端技术
- **Vue 3.5.18** - 渐进式JavaScript框架
- **Element Plus 2.11.2** - 企业级UI组件库
- **Axios 1.6.0** - HTTP客户端库
- **Vite 7.0.6** - 现代化构建工具
- **Vue DevTools** - 开发调试工具

## 🚀 快速开始

### 环境要求

- **Java 17+** - 后端运行环境
- **Node.js 20.19.0+** - 前端运行环境
- **MySQL 8.0+** - 数据库服务
- **Maven 3.6+** - 后端构建工具
- **npm** - 前端包管理器

### 数据库配置

1. **创建MySQL数据库**：
```sql
CREATE DATABASE publicPay CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **修改后端配置文件** `backend/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/publicPay
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update  # 自动建表
    show-sql: true      # 显示SQL语句
```

### 启动应用

#### 方法一：使用启动脚本（推荐）

1. **启动后端服务**：
   - 双击 `backend/start.bat` 文件
   - 后端服务将在 http://localhost:8080 启动
   - 脚本会自动检查Java和Maven环境

2. **启动前端服务**：
   - 双击 `forent/start.bat` 文件
   - 前端服务将在 http://localhost:3000 启动
   - 脚本会自动安装依赖并启动开发服务器

#### 方法二：手动启动

1. **启动后端**：
```bash
cd backend
mvn spring-boot:run
```

2. **启动前端**：
```bash
cd forent
npm install
npm run dev
```

### 访问应用

打开浏览器访问 **http://localhost:3000** 即可使用系统

## 📖 使用说明

### 1. 添加室友
- 点击右上角「添加室友」按钮
- 输入室友姓名并保存（系统会验证姓名唯一性）

### 2. 管理费用分类
- 在「分类管理」页面添加、编辑费用分类
- 系统预设水电费、物业费、生活费等常用分类
- 支持自定义分类名称和描述

### 3. 记录费用
- 在「费用记录」页面点击「添加费用」
- 选择费用分类、填写描述、金额、付款人
- 选择分摊方式：
  - **平均分摊**：费用平均分配给所有室友
  - **自定义分摊**：可设置每人具体分摊金额
- 选择分摊室友，自定义分摊可设置每人具体金额

### 4. 查看余额
- 在「余额统计」页面查看每人的收支情况
- **正数**：表示别人欠你钱
- **负数**：表示你欠别人钱
- 支持按时间范围筛选统计

### 5. 标记支付
- 在费用记录中点击「标记已付」按钮
- 实时更新支付状态
- 支持批量标记支付状态

## 📁 项目结构

```
publicPay/
├── backend/                    # 后端Spring Boot项目
│   ├── src/main/java/
│   │   └── com/ruoyi/backend/
│   │       ├── entity/         # 实体类（数据模型）
│   │       │   ├── User.java           # 用户实体
│   │       │   ├── Expense.java        # 费用实体
│   │       │   ├── ExpenseCategory.java # 费用分类实体
│   │       │   ├── ExpenseShare.java   # 费用分摊实体
│   │       │   └── ExpensePayment.java # 费用支付实体
│   │       ├── repository/     # 数据访问层
│   │       ├── service/        # 业务逻辑层
│   │       ├── controller/     # 控制器层（REST API）
│   │       ├── dto/           # 数据传输对象
│   │       ├── enums/         # 枚举类
│   │       └── exception/     # 异常处理
│   ├── src/main/resources/
│   │   └── application.yml    # 配置文件
│   ├── start.bat             # 后端启动脚本
│   └── pom.xml               # Maven配置文件
├── forent/                    # 前端Vue3项目
│   ├── src/
│   │   ├── components/        # Vue组件
│   │   │   ├── ExpenseList.vue    # 费用列表组件
│   │   │   ├── UserList.vue       # 用户列表组件
│   │   │   ├── CategoryList.vue   # 分类列表组件
│   │   │   └── BalanceList.vue    # 余额统计组件
│   │   ├── api/              # API接口封装
│   │   ├── assets/           # 静态资源
│   │   └── main.js           # 应用入口
│   ├── start.bat             # 前端启动脚本
│   ├── package.json          # 项目依赖配置
│   └── vite.config.js        # Vite配置文件
└── README.md                 # 项目说明文档
```

## 🔌 API接口

### 用户管理
- `GET /api/users` - 获取所有用户列表
- `POST /api/users` - 创建新用户
- `PUT /api/users/{id}` - 更新用户信息
- `DELETE /api/users/{id}` - 删除用户

### 费用管理
- `GET /api/expenses` - 获取所有费用记录
- `POST /api/expenses` - 添加费用记录
- `GET /api/expenses/{id}` - 获取费用详情
- `PUT /api/expenses/shares/{shareId}/pay` - 标记分摊为已支付
- `GET /api/expenses/balances` - 获取用户余额统计

### 费用分类管理
- `GET /api/categories` - 获取所有费用分类
- `POST /api/categories` - 创建费用分类
- `PUT /api/categories/{id}` - 更新费用分类
- `DELETE /api/categories/{id}` - 删除费用分类

## 💻 开发说明

### 后端开发
- 使用 **Spring Boot 3.3.0** + **Spring Data JPA** 进行开发
- 数据库表会自动创建（`ddl-auto: update`）
- 支持跨域请求（CORS）配置
- 使用 **Lombok** 简化代码编写
- 集成 **Spring Validation** 进行数据校验
- 统一的异常处理机制

### 前端开发
- 使用 **Vue 3 Composition API** 进行开发
- **Element Plus** 提供企业级UI组件
- **Axios** 处理HTTP请求和响应拦截
- **Vite** 提供快速的开发服务器和构建工具
- 支持热重载和Vue DevTools调试
- 响应式设计，支持移动端适配

## ❓ 常见问题

### 1. 数据库连接失败
- 检查MySQL服务是否启动
- 确认数据库配置信息是否正确
- 确保数据库用户有足够权限
- 验证数据库名称 `publicPay` 是否存在

### 2. 前端无法连接后端
- 确认后端服务已启动（http://localhost:8080）
- 检查Vite代理配置是否正确
- 查看浏览器控制台是否有CORS错误
- 确认防火墙设置允许端口访问

### 3. 依赖安装失败
- 检查网络连接是否正常
- 尝试使用国内镜像源：
  ```bash
  npm config set registry https://registry.npmmirror.com
  ```
- 清除npm缓存：`npm cache clean --force`
- 删除 `node_modules` 文件夹后重新安装

### 4. 启动脚本执行失败
- 确认Java环境变量配置正确
- 检查Maven是否已安装并配置环境变量
- 确认Node.js版本符合要求（20.19.0+）
- 以管理员权限运行启动脚本

## 🤝 贡献

欢迎提交Issue和Pull Request来改进这个项目！

### 贡献指南
1. Fork 本仓库
2. 创建你的特性分支 
3. 提交你的更改 
4. 推送到分支 
5. 打开一个 Pull Request
---

⭐ 如果这个项目对你有帮助，请给它一个星标！
