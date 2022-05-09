
### tools-sqlite3

- 项目简介：Java操作sqlite的工具包（支持多线程并发读、独占写）

- 主要功能：
   - 简易的sqlite连接管理，封装JdbcTemplate，利于sql操作
   - 简易的模型与数据表映射处理（插入、查询）

- 主要API：

   - 添加maven依赖：
   ```text
      <dependency>
        <groupId>io.github.jiashunx</groupId>
        <artifactId>tools-sqlite3</artifactId>
        <version>${ts.version}</version>
      </dependency>
   ```

- 版本清单（最新版本：<b>1.1.6</b>）：

   - version 1.0.0 (released)
      - feature: 实现主要功能
   - version 1.1.0 (released)
      - feature: 增加对sqlite数据结构管理的工具类及模型（便于sqlite数据库表及视图的初始化）
      - refactor: SQLite3JdbcTemplate增加部分通用查询方法
   - version 1.1.1 (released)
      - feature: 增加对sqlite数据表索引的管理及初始化
      - refactor: 调整SQLite3JdbcTemplate部分方法名称
   - version 1.1.2 (released)
      - fixbug: 修正添加表字段拼接sql的异常
   - version 1.1.3 (released)
      - feature: 增加针对实体的默认Service类，提供增删改查一条龙服务
   - version 1.1.4 (released)
      - fixbug: 针对实体的默认Service类在对数据进行缓存处理时，在insert/update操作后清空缓存
   - version 1.1.5 (released)
      - refactor: SQLite3JdbcTemplate功能重构 -> 增加对事务的控制处理（重点是其 [doTransaction][1] 方法）
      - refactor: SQLite3Service缓存处理优化，支持开启/关闭缓存，支持全局/局部缓存
   - version 1.1.6 (released)
      - feature: 增加对sqlite数据表触发器的管理及初始化
      - feature: 增加对sqlite数据表外键的管理及初始化
      - feature: 增加对sqlite数据表字段not-null及default值的处理
      - feature: 增加对sqlite数据表字段长度的解析处理
      - refactor: 优化对sqlite查询结果的映射处理
      - refactor: 补充优化单元测试用例: [SQLite3MappingTest.java](src/test/java/io/github/jiashunx/tools/sqlite3/SQLite3Test.java)

[1]: src/main/java/io/github/jiashunx/tools/sqlite3/SQLite3JdbcTemplate.java
