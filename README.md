# XiaoNuoClean

XiaoNuoClean 是一个 Minecraft Java 版 Fabric 服务端 MOD，用于定时清理掉落物。

## 功能

- 默认每 15 分钟清理一次掉落物。
- 默认在清理前 10、5、4、3、2、1 秒广播提示。
- 支持通过 `config/xiaonuoclean.json` 修改清理间隔、提示秒数和白名单。
- 支持通过 `/xiaonuoclean` 指令查看状态、重载配置、立即清理和修改配置。
- 白名单物品不会被清理，例如 `minecraft:diamond` 或简写 `diamond`。

## 默认配置

```json
{
  "intervalSeconds": 900,
  "warningSeconds": [10, 5, 4, 3, 2, 1],
  "whitelist": []
}
```

## 指令

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

`/xiaonuoclean whitelist set` 只能由游戏内 OP/管理员执行，会读取执行者主手物品 ID 并加入白名单；控制台无法使用该命令。

## License

本项目使用 GNU General Public License v3.0 协议开源。

详见 [LICENSE](LICENSE)。
