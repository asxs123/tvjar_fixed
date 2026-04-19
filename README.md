# CatVod Spider 本地测试环境 - 使用教程

## 📖 项目简介

在不依赖 Android 设备的情况下，**本地桌面测试** CatVod 视频爬虫 Java 代码。

- ✅ 兼容项目一（CatVodSpider-larn）的所有 Spider
- ✅ 兼容项目二（tvjar_test）的所有 Spider
- ✅ 支持 Windows / macOS / Linux
- ✅ JDK 便携式集成，不修改系统环境

---

## 🚀 快速开始（Windows 10）

### 第 1 步：下载项目

解压 `tvjar_fixed.zip` 到任意目录，例如 `D:\tvjar_fixed\`

### 第 2 步：安装项目专用 JDK

打开 **cmd** 或 **PowerShell**，进入项目目录：

```cmd
cd D:\tvjar_fixed
python setup_jdk.py
```

> 这会自动下载 JDK 21 到项目 `jdk\` 目录下（约 195MB）。
> **不会修改你的系统环境**，仅本项目可用。

如果 `python` 不可用，试试 `python3` 或 `py`：
```cmd
py setup_jdk.py
```

### 第 3 步：运行测试

```cmd
:: 自动测试豆瓣 spider
gradlew.bat run --args="Douban"

:: 自动测试 AppYsV2 spider
gradlew.bat run --args="AppYsV2 https://example.com/api.php/app/"

:: 交互式调试模式
gradlew.bat run
```

### 交互式模式命令

```
> home true              :: 获取首页数据
> cat 1 1                :: 获取分类 ID=1 第1页
> detail 12345           :: 获取视频详情
> search 测试            :: 搜索关键词
> player qiepian 12345-1-1  :: 获取播放地址
> spider Bili            :: 切换 spider
> exit                   :: 退出
```

---

## 🔧 常见问题

### Q: `python` 命令找不到？

Windows 10 不一定自带 Python。解决方法：
1. 打开 Microsoft Store，搜索 Python 3.11 安装
2. 或下载安装：https://www.python.org/downloads/
3. 安装时勾选 **"Add Python to PATH"**

### Q: `setup_jdk.py` 下载太慢或失败？

手动下载：
1. 浏览器打开 https://adoptium.net/releases.html
2. 选择 **Temurin 21 LTS** → **Windows x64** → **JDK** → **.zip**
3. 下载完成后，将 `.zip` 文件放到 `tvjar_fixed\jdk\` 目录下
4. 再次运行 `python setup_jdk.py`（会自动解压）

国内镜像加速：
- https://mirrors.tuna.tsinghua.edu.cn/Adoptium/21/jdk/x64/windows/

### Q: gradlew.bat 报错 "JAVA_HOME is not set"?

说明 JDK 没安装好。运行 `python setup_jdk.py` 安装。

### Q: 我想用自己的 JDK？

设置环境变量即可（仅当前 cmd 窗口有效）：
```cmd
set JAVA_HOME=C:\Program Files\Java\jdk-21
gradlew.bat run --args="Douban"
```

### Q: 网络代理问题？

如果需要代理，在 cmd 中设置：
```cmd
set JAVA_OPTS=-Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7890
gradlew.bat run --args="Douban"
```

---

## 📁 项目结构

```
tvjar_fixed\
├── app\
│   ├── build.gradle                 # Gradle 构建配置
│   └── src\main\java\
│       ├── android\                 # Android API 模拟层
│       ├── androidx\                # AndroidX 注解模拟
│       └── com\github\catvod\
│           ├── bean\                # 数据模型（Result/Vod/Filter...）
│           ├── crawler\             # Spider 基类
│           ├── net\                 # HTTP 客户端
│           ├── spider\              # 🎯 37 个 Spider 实现
│           ├── utils\               # 工具类（Crypto/Json/...）
│           └── demo\
│               └── TestRunner.java  # 测试入口
├── jdk\                             # 便携式 JDK（setup_jdk.py 生成）
├── gradle\wrapper\                  # Gradle Wrapper
├── gradlew.bat                      # Windows 启动脚本
├── gradlew                          # Linux/macOS 启动脚本
├── setup_jdk.py                     # JDK 安装脚本
├── README.md                        # 本文件
└── .devcontainer\                    # GitHub Codespaces 配置
```

---

## 🧪 Spider 接口说明

Spider 必须实现以下 5 个核心方法，返回值均为 JSON 字符串：

| 方法 | 用途 | 返回 |
|------|------|------|
| `homeContent(filter)` | 首页分类+筛选 | `{"class":[...], "filters":{...}, "list":[...]}` |
| `categoryContent(tid, pg, filter, extend)` | 分类列表 | `{"page":1, "pagecount":10, "list":[...]}` |
| `detailContent(ids)` | 视频详情 | `{"list":[{...播放源...}]}` |
| `playerContent(flag, id, vipFlags)` | 播放地址 | `{"parse":0, "url":"..."}` |
| `searchContent(key, quick)` | 搜索 | `{"list":[...]}` |

示例 Spider 模板：

```java
package com.github.catvod.spider;

import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;
import com.github.catvod.bean.*;
import android.content.Context;
import org.json.*;
import java.util.*;

public class MySpider extends Spider {
    private String api;

    @Override
    public void init(Context context, String extend) throws Exception {
        this.api = extend; // API 地址
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        String json = OkHttp.string(api + "/types");
        JSONObject obj = new JSONObject(json);
        // ... 构造分类和筛选
        return Result.string(classes, vods);
    }

    // 实现其他方法...
}
```

---

## 📦 可用 Spider 列表（37个）

| Spider | 来源 | 说明 | Extend 示例 |
|--------|------|------|-------------|
| `AppYsV2` | 项目二 | 影视源 V2 | `http://xxx.com/api.php/app/` |
| `Alist3` | 项目二 | AList 网盘 | JSON 配置 |
| `CaiHong` | 项目二 | 彩虹影视 | API 地址 |
| `Kunyu77` | 项目二 | 鲸鱼影视 | API 地址 |
| `Wogg` | 项目二 | Wogg | API 地址 |
| `Douban` | 项目一 | 豆瓣 | JSON 配置 |
| `Bili` | 项目一 | 哔哩哔哩 | Cookie |
| `IQIYI` | 项目一 | 爱奇艺 | - |
| `MGTV` | 项目一 | 芒果TV | - |
| `YHDM` | 项目一 | 樱花动漫 | - |
| ... | | 共 37 个 | |

---

## 🔄 更新 Spider

将新的 `.java` 文件复制到 `app\src\main\java\com\github\catvod\spider\` 目录，然后重新运行：

```cmd
gradlew.bat run --args="NewSpider"
```
