# Hyper TopCtl

管理小米 HyperOS/MIUI「点击状态栏顶部 → App 列表回顶」机制的 LSPosed 模块，基于现代
libxposed API **102**，自带 Miuix / Material 3 双风格设置界面。

## 功能

- 总开关：拦截或放行「点击状态栏回顶」。
- 名单策略：白名单（名单内放行、其余拦截）/ 黑名单（仅名单内拦截）。
- 默认行为：**全局启用拦截 + 白名单为空**，即默认对所有应用禁用回顶。
- 界面风格可切换：Miuix（HyperOS 风格）与 Material 3。
- 配置通过框架的 remote preferences 下发，改动即时生效，无需重启。

## Hook 点

作用域 `com.android.systemui`，Hook

```
miui.hardware.input.MiuiInputManager.scrollToTop()
```

拦截该方法且不调用 `chain.proceed()` 即可抑制回顶。类或方法不存在时模块只记录日志并放弃
Hook（fail closed），不会盲目匹配其他实现。

## 要求

- 支持 libxposed 模块 API 的框架（LSPosed 2.x，`minApiVersion >= 102`）。
  仅提供旧版 `de.robv.android.xposed` 桥接的框架无法加载本模块。
- 已 root 并可向 `com.android.systemui` 注入。
- minSdk 26。

## 安装

1. 从 [Releases](https://github.com/NoneStudioSoftware/Hyper-TopCtl/releases) 下载 APK 并安装。
2. 在 LSPosed 中启用模块，作用域保持「系统界面」(`com.android.systemui`)。
3. 重启 SystemUI 或重启设备。
4. 打开 Hyper TopCtl，确认显示「模块已激活」，按需调整开关与名单。

遇到问题时请改用同一 Release 中的 Debug 包复现，并通过应用内「设置 → 诊断 → 应用日志」
查看、保存或分享日志后附在 Issue 中。

## 构建

```bash
./gradlew :app:assembleDebug      # Debug 包
./gradlew :app:assembleRelease    # Release 包（本地未配置签名时为未签名）
```

产物位于 `app/build/outputs/apk/<buildType>/`，文件名为
`Hyper-TopCtl_<版本名>_<短提交哈希>-<buildType>.apk`，例如
`Hyper-TopCtl_1.0.0.15_3ea94eb-release.apk`。

### 版本号

版本号由 git 提交数自动生成，无需手动维护：

| 字段 | 取值 | 示例 |
| --- | --- | --- |
| `versionCode` | 提交数 | `15` |
| `versionName` | `1.0.0.<提交数>` | `1.0.0.15` |

界面与诊断日志中按 `1.0.0（提交数）` 展示，例如 `1.0.0（15）`；产物名额外带 7 位短提交哈希，
用于区分同一版本号下的不同构建。

没有 `.git` 的源码包构建时回退为 `1`。CI 使用 `fetch-depth: 0` 拉取完整历史，浅克隆会让提交数
退化为 1，导致 `versionCode` 不再递增、无法覆盖安装。

由于 `versionCode` 等于提交数，**改写历史（rebase / squash / 强制推送）可能让它变小**，此时已
安装的旧版本无法被新版本覆盖，需要先卸载再装。

### 工具链

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| Gradle | 9.7.1 | AGP 9.4.1 要求 ≥ 9.6.0 |
| AGP | 9.4.1 | 启用新 DSL（`compileSdk { version = release(37) }`） |
| Kotlin | 2.4.20 | 由 Compose 编译器插件版本决定（见下） |
| Miuix | 0.9.4 | 用 Kotlin 2.4.0 构建，需要 2.4+ 编译器 |
| libxposed api / service | 102.0.0 | api 为 `compileOnly`，service 打包进 APK |
| JDK | 21 | 可用 Android Studio 自带 JBR |

AGP 9 使用**内置 Kotlin**，禁止再应用独立的 `org.jetbrains.kotlin.android` 插件（应用后会直接
报错要求移除）。注意 AGP POM 中声明的 `kotlin-gradle-plugin 2.2.10` 是**下限而非上限**：内置
Kotlin 编译器版本跟随 `org.jetbrains.kotlin.plugin.compose` 插件版本，因此在
`libs.versions.toml` 中把 `kotlin` 提到 2.4.20 即可让编译器升级到 2.4.20，
`kotlin-stdlib` 也会随之统一上抬。

升级 Kotlin 时同步调整该 `kotlin` 版本即可；若编译报
`incompatible version of Kotlin ... metadata version X`，说明依赖的构建版本高于当前编译器，
提升 `kotlin` 版本而不是降级依赖。

## 发布

发布由 GitHub Actions 完成，推送 `v*` 标签即自动构建并创建 GitHub Release：

```bash
git tag v1.0.0
git push origin v1.0.0
```

| 操作 | 结果 |
| --- | --- |
| 推送到 `main`、提交 PR、手动运行 Build | 只构建校验，产物在 Actions 运行的 Artifacts 区，**不发布** |
| 推送 `v*` 标签 | 构建并发布，Release 附件含 Release 与 Debug 两个 APK |

标签名含 `-rc` 时（如 `v1.0.0-rc1`）会标记为预发布。发布后若仍需发新版本，改动提交后另打一个
新标签即可，`versionCode` 会随提交数自动递增。

### Release 签名

发布的 APK 使用仓库 Secrets 中的密钥签名。未配置 Secrets 时**发布流程直接失败**，不会发布未
签名包；普通构建（`main` / PR）则降级为未签名 APK 并给出告警。

首次准备签名密钥：

```bash
keytool -genkeypair -v -keystore hyper-topctl-release.jks -alias my-alias \
  -keyalg RSA -keysize 4096 -validity 10000 -dname "CN=Hyper TopCtl User"
```

取 base64（Windows PowerShell）：

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("hyper-topctl-release.jks")) | Set-Content -NoNewline keystore.b64
```

在仓库 Settings → Secrets and variables → Actions 中添加：

| Secret | 值 |
| --- | --- |
| `KEYSTORE` | `keystore.b64` 的内容（单行 base64） |
| `KEYSTORE_PASSWORD` | 生成时设置的 keystore 口令 |
| `KEY_ALIAS` | `my-alias` |
| `KEY_PASSWORD` | 生成时设置的密钥口令 |

> 注意：keytool 默认使用 **PKCS12** 密钥库，它不支持「存储口令」与「密钥口令」不同。用默认类型
> 生成时，`KEY_PASSWORD` 必须与 `KEYSTORE_PASSWORD` 填相同值（`-keypass` 会被 keytool 忽略）。
> 若确实需要两个不同的口令，生成时显式加 `-storetype JKS`。

本地如需构建已签名的 Release 包，可在用户级 `~/.gradle/gradle.properties` 中配置同名四项
（另加 `KEYSTORE_FILE` 指向 `.jks` 路径），签名文件不应放入仓库。请离线备份 keystore：一旦丢失，
已安装用户将无法收到可覆盖升级的版本。

## 已知限制

名单精度依赖「当前前台应用包名」。解析方式是在 SystemUI 进程内通过
`ActivityThread.currentApplication()` 取得 Context，再调用 `ActivityManager.getRunningTasks(1)`
读取栈顶包名。该路径在部分 HyperOS 版本或权限受限时可能失败，此时
`resolveForegroundPackage()` 返回 `null`，判定**回落为全局策略**：白名单回落为「拦截」、黑名单
回落为「放行」。总开关不依赖该解析，始终可靠。

## 项目结构

```
app/src/main/
  AndroidManifest.xml
  resources/META-INF/xposed/{java_init.list,module.prop,scope.list}   # API 102 元数据
  java/io/github/hypertopctl/
    App.kt                       # 绑定 XposedService
    data/SettingsRepository.kt   # 读写 remote preferences
    lsp/
      Constant.kt                # 共享键名与默认值
      config/ModuleConfig.kt     # 配置快照 + shouldIntercept 决策
      module/ScrollTopModule.kt  # API 102 入口，Hook scrollToTop()
    ui/
      MainActivity.kt / MainViewModel.kt
      theme/{UiMode,Theme}.kt    # 双框架切换
      screen/{MainScreen,MiuixMainScreen,MaterialMainScreen}.kt
```

## 致谢

- [xiaoyvyv/fuck-hyperos-scroll-top](https://github.com/xiaoyvyv/fuck-hyperos-scroll-top) — 验证了
  `MiuiInputManager.scrollToTop()` 这一 Hook 点与 libxposed 模块骨架。
- [tiann/KernelSU](https://github.com/tiann/KernelSU) Manager — Miuix / Material 双框架 UI 组织方式。
- [miuix](https://github.com/miuix-kotlin-multiplatform/miuix) — HyperOS 风格 Compose 组件库。
