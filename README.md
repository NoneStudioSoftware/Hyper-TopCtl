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

1. 安装 APK。
2. 在 LSPosed 中启用模块，作用域保持「系统界面」(`com.android.systemui`)。
3. 重启 SystemUI 或重启设备。
4. 打开 Hyper TopCtl，确认显示「模块已激活」，按需调整开关与名单。

## 构建

```bash
./gradlew :app:assembleDebug
```

产物：`app/build/outputs/apk/debug/app-debug.apk`

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

## 已知限制

名单精度依赖「当前前台应用包名」。`scrollToTop()` 运行在 SystemUI 进程内，跨 HyperOS 版本
可靠解析前台包名并无保证，因此当前实现采用**全局开关优先**策略：
`resolveForegroundPackage()` 暂返回 `null`，此时白名单判定回落为「拦截」、黑名单判定回落为
「放行」，总开关始终可靠。名单为增强项，待确认可靠的解析方式后再启用。

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
