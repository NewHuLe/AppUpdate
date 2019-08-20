## Android版本更新
[![](https://www.jitpack.io/v/NewHuLe/AppUpdate.svg)](https://www.jitpack.io/#NewHuLe/AppUpdate)  

原生DownloadManager实现版本的检测更新，自由控制下载进度、下载失败弹框、是否强制更新、是否MD5校验、完美适配Android M/N/O/P/Q
## 功能介绍
- 适配Android M，处理关于存储文件的运行时权限
- 适配Android N，安卓增强了文件访问的安全性，利用FileProvider来访问文件
- 适配Android O，增加未知来源应用的安装提示
- 适配Android Q，关于Q增加沙箱，改变了应用程序访问设备外部存储上文件的方式如SD卡
- 默认采取DownloadManager+系统通知实现后台下载，安装完毕自动弹出安装界面，也可以自由配置增加下载进度框与下载失败的提示框
- 支持强制更新，未更新无法使用应用
- 支持MD5文件防篡改及完整性校验
- 支持自定义更新提示界面
## 效果图
![](https://github.com/NewHuLe/AppUpdate/blob/master/screenshots/%E6%88%AA%E5%9B%BE1.jpg)
![](https://github.com/NewHuLe/AppUpdate/blob/master/screenshots/%E6%88%AA%E5%9B%BE2.jpg)
![](https://github.com/NewHuLe/AppUpdate/blob/master/screenshots/%E6%88%AA%E5%9B%BE3.jpg)
## 关于使用
- 工程build.gradle目录添加
	allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
- 项目build.gradle文件添加
  dependencies {
	        implementation 'com.github.NewHuLe:AppUpdate:Tag'
	}
- 
