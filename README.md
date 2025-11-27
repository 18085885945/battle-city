# 测试版本
坦克大战的一些测试版本

11.22日（0.0.1）：
## 目录结构

- `src/main/java`：核心代码
    - `config`：JSON 配置
    - `engine`：游戏引擎/循环/状态
    - `model`：世界与实体
    - `ai`：敌人 AI 占位
    - `physics`：碰撞系统
    - `map`：关卡解析
    - `controller`：输入/流程控制
    - `ui`：JavaFX 场景
- `src/main/resources`
    - `config/game-config.json`
    - `levels/*.json`
    - `styles/main.css`
- `docs/ARCHITECTURE.md`：框架说明

## 启动器

提供 `Launcher` 主类（配合 `javafx-maven-plugin`）以及可选脚本：

```bash
# Windows
run-game.bat
# macOS/Linux
./run-game.sh
```

脚本会执行 `mvn clean javafx:run`，方便快速体验。