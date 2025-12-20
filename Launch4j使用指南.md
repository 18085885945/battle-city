# Launch4j 详细使用指南

## 方法一：使用自动构建脚本（最简单）⭐推荐

我已经为你创建了自动构建脚本，直接运行即可：

```bash
build-exe-launch4j.bat
```

这个脚本会：
1. 自动打包项目
2. 自动调用Launch4j
3. 生成exe文件到 `dist` 目录

**如果脚本找不到Launch4j**，请按以下步骤操作：

### 步骤1：找到Launch4j安装路径
Launch4j通常安装在：
- `C:\Program Files\Launch4j\Launch4j.exe`
- `C:\Program Files (x86)\Launch4j\Launch4j.exe`
- 或者你自定义的安装路径

### 步骤2：修改脚本中的路径
打开 `build-exe-launch4j.bat`，找到这一行：
```batch
set LAUNCH4J_PATH=C:\Program Files\Launch4j\Launch4j.exe
```
改成你的实际安装路径。

### 步骤3：运行脚本
双击运行 `build-exe-launch4j.bat` 即可。

---

## 方法二：手动使用Launch4j GUI

如果你想手动操作，按以下步骤：

### 步骤1：先打包项目
在项目根目录打开命令行，运行：
```bash
mvn clean package -DskipTests
```

### 步骤2：打开Launch4j
双击 `Launch4j.exe` 打开程序。

### 步骤3：加载配置文件
1. 点击菜单栏 `File` → `Load config`
2. 选择项目中的 `launch4j-config.xml` 文件
3. 配置文件会自动加载所有设置

### 步骤4：检查配置（重要！）

#### 4.1 基本设置（Basic标签）
- **Output file**: 应该是 `dist/BattleCity.exe`（或你想要的路径）
- **Jar**: 应该是 `target/battle-city.jar`
- **Don't wrap the jar**: 保持未勾选

#### 4.2 JRE设置（JRE标签）
- **Min JRE version**: 设置为 `21`（或更高）
- **Max JRE version**: 留空
- **Initial heap size**: `512` MB
- **Max heap size**: `2048` MB

#### 4.3 版本信息（Version info标签，可选）
- 可以设置exe的版本信息、图标等

### 步骤5：构建exe
1. 点击工具栏的 **"Build wrapper"** 按钮（齿轮图标）
2. 或者点击菜单栏 `Build` → `Build wrapper`
3. 等待构建完成

### 步骤6：测试exe
1. 构建完成后，exe文件会出现在 `dist` 目录
2. 双击 `BattleCity.exe` 测试运行

---

## 方法三：从零开始手动配置

如果你想完全手动配置：

### 步骤1：打开Launch4j
启动 Launch4j.exe

### 步骤2：配置基本设置（Basic标签）

1. **Output file**（输出文件）:
   ```
   dist/BattleCity.exe
   ```
   点击 `...` 按钮选择保存位置

2. **Jar**（JAR文件）:
   ```
   target/battle-city.jar
   ```
   点击 `...` 按钮选择JAR文件
   ⚠️ **注意**：需要先运行 `mvn clean package` 生成这个JAR文件

3. **Don't wrap the jar**: ❌ 不要勾选

4. **Header type**: 选择 `GUI`（图形界面程序）

### 步骤3：配置JRE设置（JRE标签）

1. **Min JRE version**: `21`
2. **Max JRE version**: 留空
3. **JdkPreference**: 选择 `preferJre`
4. **RuntimeBits**: 选择 `64/32`
5. **Initial heap size**: `512`
6. **Max heap size**: `2048`

### 步骤4：配置版本信息（Version info标签，可选）

1. **File version**: `1.0.0.0`
2. **Txt file version**: `1.0`
3. **File description**: `Battle City - JavaFX Edition`
4. **Copyright**: `Copyright 2024`
5. **Product name**: `Battle City`
6. **Company name**: `Battle City Team`

### 步骤5：保存配置（可选）
1. 点击 `File` → `Save config`
2. 保存为 `launch4j-config.xml`
3. 下次可以直接加载这个配置文件

### 步骤6：构建exe
点击 **"Build wrapper"** 按钮（工具栏的齿轮图标）

---

## 常见问题解决

### 问题1：找不到JAR文件
**原因**：项目还没有打包

**解决**：
```bash
mvn clean package -DskipTests
```
然后确保JAR文件在 `target/battle-city.jar`

### 问题2：exe运行时报错"找不到Java"
**原因**：目标机器没有安装Java 21

**解决**：
- 确保目标机器安装了Java 21或更高版本
- 或者使用jpackage创建包含Java运行时的安装包

### 问题3：exe运行时报错"找不到JavaFX模块"
**原因**：JavaFX模块路径不正确

**解决**：
1. 确保 `lib` 目录包含所有JavaFX JAR文件
2. 或者修改Launch4j配置，在JRE选项中添加：
   ```
   --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.media
   ```

### 问题4：Launch4j提示"JAR文件无效"
**原因**：JAR文件可能损坏或未正确打包

**解决**：
1. 重新打包：`mvn clean package -DskipTests`
2. 检查 `target/battle-city.jar` 是否存在且大小正常
3. 尝试用 `java -jar target/battle-city.jar` 直接运行测试

### 问题5：构建时提示"输出目录不存在"
**解决**：
1. 手动创建 `dist` 目录
2. 或者在Launch4j中修改输出路径为已存在的目录

---

## 快速检查清单

在构建exe之前，确保：

- [ ] 已运行 `mvn clean package -DskipTests` 成功打包
- [ ] `target/battle-city.jar` 文件存在
- [ ] `lib` 目录包含所有JavaFX JAR文件
- [ ] Launch4j配置中的JAR路径正确
- [ ] Launch4j配置中的输出路径正确
- [ ] 输出目录（dist）已创建

---

## 推荐工作流程

1. **第一次使用**：
   ```bash
   # 1. 打包项目
   mvn clean package -DskipTests
   
   # 2. 打开Launch4j，加载 launch4j-config.xml
   # 3. 点击"Build wrapper"
   # 4. 测试生成的exe
   ```

2. **后续使用**：
   ```bash
   # 直接运行自动脚本
   build-exe-launch4j.bat
   ```

3. **修改配置后**：
   - 在Launch4j中修改配置
   - 保存配置到 `launch4j-config.xml`
   - 点击"Build wrapper"重新构建

---

## 需要帮助？

如果遇到问题：
1. 检查 `README-EXE.md` 中的故障排除部分
2. 确保所有前置条件都满足
3. 查看Launch4j的错误提示信息

