# SingleNet-Robot 闪讯机器人
自2016年9月以来，闪讯已没有方法可以固定密码。这个项目就是通过手机上的App设置定时任务，自动发送短信获取密码，然后通过路由器的接口更新，实现自动更新密码的效果，达到曲线救国的目的。**禁止一切商业用途**  
## 服务端
#### Python版
##### 系统需求
* Python 2.7+
##### 部署步骤
1. 将服务端下载至路由器
2. 修改`INTERFACE`字段为闪讯拨号的接口名
3. 查看是否成功运行
4. 设置开机自启动
5. 开放防火墙对应端口
6. 设置DDNS，使内网、外网都可以访问
#### Go版
暂未实现，自己动手丰衣足食。

## 客户端
#### Android版
##### 系统需求
* Android 4.4+ (仅在Android 9.0下通过测试)
##### 部署步骤
1. 安装apk
2. 设置路由器地址
3. 开放权限
4. 使用
#### iOS版
暂未实现，自己动手丰衣足食。
