# AutoSignInTool

## 自动解锁

说明：**后台挂起，进入打卡范围，自动解锁，拉起飞书，自动打卡，然后息屏**

### Assets文件说明：

1）classes.dex : 服务端APP dex文件

2）server: 启动服务端APP 可执行程序

3）launcher.sh : 启动server程序的脚本

### 启动方法

1）将上述三个文件push到手机目录：

```shell
adb push * /data/local/tmp
```

2)  在terminal执行：

```shell
nohup adb shell "nohup /data/local/tmp/launcher.sh > /dev/null 2>&1" &>/dev/null &
```

