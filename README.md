# 轨迹导航

一款基于高德地图的Android轨迹导航应用，支持导入KML轨迹文件、显示轨迹信息、记录GPS定位等功能。

## 功能特性

- **KML轨迹导入**：支持导入KML格式的轨迹文件，支持LineString和gx:Track两种格式
- **轨迹显示**：在地图上显示导入的轨迹路线，支持自动缩放适配屏幕
- **轨迹信息**：显示轨迹名称、总距离、总上升、总下降、轨迹点数等信息
- **公里标记**：在轨迹上显示每公里的标记点
- **GPS定位**：实时显示当前位置
- **轨迹历史**：记录打开过的轨迹，方便再次打开
- **历史管理**：支持删除历史记录，相同文件名只保存一份

## 技术栈

- **开发语言**：Kotlin
- **最低SDK**：Android 7.0 (API 24)
- **目标SDK**：Android 13 (API 33)
- **地图SDK**：高德地图 SDK 9.8.3
- **UI框架**：Material Design + AppCompat

## 项目结构

```
app/src/main/java/com/yjx/navigation/
├── model/
│   ├── KmlData.kt          # KML数据模型
│   ├── TrackPoint.kt       # 轨迹点模型
│   └── TrackHistory.kt     # 轨迹历史模型
├── util/
│   ├── DistanceCalculator.kt   # 距离计算工具
│   ├── FilePicker.kt           # 文件选择器
│   ├── KmlParser.kt            # KML解析器
│   ├── LocationManager.kt      # 位置管理器
│   ├── MapManager.kt           # 地图管理器
│   ├── PermissionManager.kt    # 权限管理器
│   └── TrackHistoryManager.kt  # 轨迹历史管理器
├── MainActivity.kt         # 主界面
├── HistoryActivity.kt      # 历史记录界面
└── NavigationApplication.kt # 应用入口
```

## 权限说明

应用需要以下权限：

- `ACCESS_FINE_LOCATION` - 精确位置权限，用于GPS定位
- `ACCESS_COARSE_LOCATION` - 粗略位置权限
- `READ_EXTERNAL_STORAGE` - 读取存储权限（Android 12及以下）
- `READ_MEDIA_IMAGES` - 读取图片权限（Android 13+）
- `READ_MEDIA_VIDEO` - 读取视频权限（Android 13+）
- `READ_MEDIA_AUDIO` - 读取音频权限（Android 13+）
- `INTERNET` - 网络权限，用于地图加载

## 构建与运行

### 环境要求

- Android Studio Arctic Fox 或更高版本
- JDK 8
- Android SDK 33

### 构建步骤

1. 克隆项目
```bash
git clone <repository-url>
cd Navigation
```

2. 配置高德地图API Key

在 `app/src/main/AndroidManifest.xml` 中替换你的高德地图API Key：
```xml
<meta-data
    android:name="com.amap.api.v2.apikey"
    android:value="你的API Key" />
```

3. 构建项目
```bash
./gradlew assembleDebug
```

4. 安装到设备
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 使用说明

1. **导入轨迹**：点击右下角的"+"按钮，选择KML文件
2. **查看轨迹**：导入后自动显示轨迹，可查看距离、上升、下降等信息
3. **当前位置**：点击定位按钮可跳转到当前位置
4. **清除轨迹**：点击清除按钮可移除当前轨迹
5. **历史记录**：点击历史按钮可查看和打开之前导入过的轨迹

## KML格式支持

应用支持以下KML格式：

### LineString格式
```xml
<Placemark>
    <LineString>
        <coordinates>
            116.32288,40.444831,355.25 
            116.322837,40.4448,368.88
            ...
        </coordinates>
    </LineString>
</Placemark>
```

### gx:Track格式
```xml
<Placemark>
    <gx:Track>
        <when>2024-01-01T00:00:00Z</when>
        <coord>116.32288 40.444831 355.25</coord>
        <when>2024-01-01T00:01:00Z</when>
        <coord>116.322837 40.4448 368.88</coord>
        ...
    </gx:Track>
</Placemark>
```

## 距离计算

使用Haversine公式计算两点之间的距离，考虑地球曲率，精度较高。

## 版本历史

### v1.0
- 初始版本
- 支持KML轨迹导入和显示
- 支持GPS定位
- 支持轨迹历史记录

## 许可证

MIT License

## 贡献

欢迎提交Issue和Pull Request。
