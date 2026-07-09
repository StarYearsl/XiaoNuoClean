# XiaoNuoClean

XiaoNuoClean 是一个 Minecraft Java 版 Fabric / NeoForge 服务端 MOD，用于定时清理掉落物。

构建产物会分别生成 Fabric 和 NeoForge jar，请根据服务器加载器选择对应文件安装。当前单 jar 兼容范围为 Minecraft `26.1.2` 到 `26.2.x`。

## 构建

默认构建不会解析 NeoForge 依赖，适合只构建 Fabric 版：

```bash
./gradlew build
```

构建 NeoForge 版需要显式启用 NeoForge 模块，并确保当前网络可以访问 NeoForge Maven：

```bash
./gradlew :neoforge:build -PincludeNeoForge=true --no-parallel --max-workers=1
```

## 功能

- 默认每 15 分钟清理一次掉落物。
- 默认在清理前 10、5、4、3、2、1 秒广播提示。
- 支持通过 `config/xiaonuoclean/config.json` 修改语言、清理间隔、提示秒数和白名单。
- 支持服务端 i18n，默认生成 `zh-CN` 和 `en-US` 语言文件，也可以添加自定义语言文件。
- 支持通过 `/xiaonuoclean` 指令查看状态、重载配置、立即清理和修改配置，也可以简写为 `/xnc`。
- 白名单物品不会被清理，例如 `minecraft:diamond` 或简写 `diamond`。

## 默认配置

```json
{
  "language": "zh-CN",
  "intervalSeconds": 900,
  "warningSeconds": [10, 5, 4, 3, 2, 1],
  "whitelist": []
}
```

语言文件位于：

```text
config/xiaonuoclean/lang/<language>.json
```

例如将主配置中的 `"language"` 改为 `"en-US"` 并执行 `/xiaonuoclean reload` 后，会使用英文消息。新增语言时，把同名 JSON 文件放入该目录并在主配置中选择对应文件名即可。

## 指令

所有 `/xiaonuoclean ...` 指令都可以简写为 `/xnc ...`。

```text
/xiaonuoclean status
/xiaonuoclean reload
/xiaonuoclean clean
/xiaonuoclean interval <seconds>
/xiaonuoclean warnings set <seconds...>
/xiaonuoclean whitelist set
/xiaonuoclean whitelist add <item>
/xiaonuoclean whitelist remove <item>
/xiaonuoclean whitelist list
```

修改类指令需要 OP 权限。

`/xiaonuoclean whitelist set` 只能由游戏内 OP/管理员玩家执行，会读取执行者主手物品 ID 并加入白名单；控制台无法使用该命令。

## License

本项目使用 GNU General Public License v3.0 协议开源。

详见 [LICENSE](LICENSE)。
