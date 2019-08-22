## Android版本更新
[![](https://www.jitpack.io/v/NewHuLe/AppUpdate.svg)](https://www.jitpack.io/#NewHuLe/AppUpdate)
[![](https://github.com/NewHuLe/AppUpdate/blob/master/author/author_jianshu.svg)](https://www.jianshu.com/u/e87d858e89a4)
[![](https://github.com/NewHuLe/AppUpdate/blob/master/author/author_juejin.svg)](https://juejin.im/user/5823e16c5bbb50005907fdb2/posts) 

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
- 下载失败支持通过系统浏览器下载
## 效果图
![](https://github.com/NewHuLe/AppUpdate/blob/master/screenshots/%E6%88%AA%E5%9B%BE1.jpg)
![](https://github.com/NewHuLe/AppUpdate/blob/master/screenshots/%E6%88%AA%E5%9B%BE2.jpg)
![](https://github.com/NewHuLe/AppUpdate/blob/master/screenshots/%E6%88%AA%E5%9B%BE3.jpg)
## 关于使用
- 工程build.gradle目录添加
```
	allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
```
- 项目build.gradle文件添加
```
 	dependencies {
	       implementation 'com.github.NewHuLe:AppUpdate:v1.0'
	}
```
- 代码调用示例，简单写法，更多配置参考demo
```
 AppUpdateUtils updateUtils = new AppUpdateUtils(MainActivity.this);
        AppUpdate appUpdate = new AppUpdate.Builder()
                //更新地址
                .newVersionUrl("https://imtt.dd.qq.com/16891/8EC4E86B648D57FDF114AF5D3002C09B.apk")
                // 版本号
                .newVersionCode("v1.2")
                // 文件大小
                .fileSize("5.8M")
                .build();
        updateUtils.startUpdate(appUpdate, this);
```
- 下载完成后，默认安装已经发出允许安装未知来源应用权限申请，如果拒绝权限，需要在更新页面增加权限拒绝后的回调处理，详情可见demo
```
 /**
     * 检测到无权限安装未知来源应用，回调接口中需要重新请求安装未知应用来源的权限
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void applyAndroidOInstall() {
        //请求安装未知应用来源的权限
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, INSTALL_PACKAGES_REQUESTCODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 8.0的权限请求结果回调
        if (requestCode == INSTALL_PACKAGES_REQUESTCODE) {
            // 授权成功
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                installApkAgain();
            } else {
                // 授权失败，引导用户去未知应用安装的界面
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    //注意这个是8.0新API
                    Uri packageUri = Uri.parse("package:" + getPackageName());
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageUri);
                    startActivityForResult(intent, GET_UNKNOWN_APP_SOURCES);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //8.0应用设置界面未知安装开源返回时候
        if (requestCode == GET_UNKNOWN_APP_SOURCES) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                boolean allowInstall = getPackageManager().canRequestPackageInstalls();
                if (allowInstall) {
                    installApkAgain();
                } else {
                    Toast.makeText(MainActivity.this, "您拒绝了安装未知来源应用，应用暂时无法更新！", Toast.LENGTH_SHORT).show();
                    if (0 != updateUtils.getAppUpdate().getForceUpdate()) {
                        forceExit();
                    }
                }
            }
        }
    }
```
- 更高级调用写法：
```
 AppUpdateUtils updateUtils = new AppUpdateUtils(MainActivity.this);
        AppUpdate appUpdate = new AppUpdate.Builder()
                //更新地址
                .newVersionUrl("https://imtt.dd.qq.com/16891/8EC4E86B648D57FDF114AF5D3002C09B.apk")
                // 版本号
                .newVersionCode("v1.2")
                // 通过传入资源id来自定义更新对话框，注意取消更新的id要定义为btnUpdateLater，立即更新的id要定义为btnUpdateNow
                .updateResourceId(R.layout.dialog_update)
                // 文件大小
                .fileSize("5.8M")
                //是否采取默认模式（只显示更新提示，后台下载完自动弹出安装界面），否则，显示下载进度，显示下载失败弹框
                .defaultMode(0)
                //默认不采取强制更新，否则，不更新无法使用
                .forceUpdate(0)
                .build();
        updateUtils.startUpdate(appUpdate, this);
```
## 混淆配置
```
-keep class com.open.hule.library.entity.** { *; }
```
## 更新日志
- v1.0
初次提交
## License
```
Copyright 2019 胡乐

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
