# GPS速度屏蔽模块

这是一个可导入 Android Studio 的 LSPosed/Xposed 模块工程。

## 功能

- Hook `android.location.Location` 的速度相关方法
- 固定返回自定义速度值，单位为 `m/s`
- 支持 `0`（静止）、`1.4`（约步行）和 `27.8`（约 100 km/h）等配置
- 可通过包名白名单限制生效应用
- 包名留空时，模块跟随 LSPosed 的作用域

## 构建

1. 使用 Android Studio 打开本目录。
2. 等待 Gradle 同步完成。
3. 构建 `app` 的 Debug APK。
4. 将 APK 安装到已安装 LSPosed 的设备。

模块使用传统 Xposed 入口，入口文件为：

```text
app/src/main/assets/xposed_init
```

## 启用

1. 在 LSPosed 中启用 `GPS速度屏蔽模块`。
2. 勾选需要修改的目标应用。
3. 打开模块设置，启用速度拦截并填写速度值。
4. 保存配置后，完全重启目标应用。

如果填写了包名，包名支持逗号、分号或换行分隔，例如：

```text
com.example.maps
com.example.navigation
```

## 说明

当前环境没有 Android SDK、JDK 和 Gradle，因此这里只生成了完整工程源码，未在本机产出 APK。安装前请在 Android Studio 中完成一次构建。
