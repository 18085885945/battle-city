# Battle City - 技术框架概述

## 目标
在 Java 21 + JavaFX 环境下实现 MVC 架构的坦克大战，覆盖经典 / 无尽 / 限时三种模式，具备 AI、碰撞、地图、配置与编辑器扩展能力。

## 模块划分
- `config`：JSON 驱动的参数系统，支持模式、玩家、敌人配置。
- `engine`：游戏循环、上下文、状态管理。
- `model`：世界、实体、坦克、子弹、障碍物。
- `ai`：敌人行为状态机与寻路扩展位。
- `physics`：AABB 碰撞检测。
- `map`：关卡定义及仓库，解析 JSON 关卡。
- `controller`：输入处理与流程控制。
- `ui`：JavaFX 场景路由、HUD/Menu。
- `util`：资源定位、工具集。

## 运行流程
1. `Launcher` 启动 JavaFX，`BattleCityApplication` 初始化。
2. `GameBootstrapper` 载入 `GameConfig` 与默认关卡仓库。
3. `SceneRouter` 构建主菜单界面，调用 `GameEngine` 启动指定模式。
4. `GameEngine` 利用 `GameLoop` 固定帧率驱动 `GameWorld`、控制器与 AI。
5. `GameWorld` 按关卡定义生成障碍、基地、玩家等实体。

## 扩展点
- `LevelRepository` 支持用户自定义地图与编辑器。
- `EnemyAIController` 后续接入行为树 / BFS / A*。
- `CollisionDetector` 可替换为网格 / 四叉树以优化性能。
- `SceneRouter` 可挂接 HUD、Pause、GameOver 等 FXML 视图。

## 测试策略
- `config`、`map` 模块使用 JUnit + JSON 样例覆盖。
- 物理系统使用参数化测试验证碰撞响应。
- 控制器与 AI 通过 mock 世界对象验证状态切换。

