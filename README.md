# NewVpnCore
封装了OpenVpn和Ikev2两种协议，只需要输入服务器IP和端口号即可实现连接和状态显示，同时可以显示连接的具体情况。


# UniteVpn  Core（第一版）


## 1. 模块说明

- unitevpn：封装了vpn的操作和统一不同协议信息的模块
- ikev2：IKEV2协议的源码
- openvpn：OpenVpn协议的源码
- app：DEMO模块，提供使用参考

## 2. 引入步骤

(1) 模块引入

复制unitevpn，ikev2，openvpn三个模块到自己的工程根目录，在settings.gradle中加入以下代码引入

```kotlin
include (":unitevpn")
include (":ikev2")
include (":openvpn")
```

然后在app的build.gradle中引入

```kotlin
dependencies {
    implementation(project(mapOf("path" to ":unitevpn")))
    implementation(project(mapOf("path" to ":ikev2")))
    implementation(project(mapOf("path" to ":openvpn")))
}
```

(2) aar引入（待定）

## 3. 使用步骤

（1）通知设置

- 设置vpn的通知图标
- 设置vpn连接成功和未连接的状态小图标
- 设置通知点击跳转的activity

```kotlin
class App : Application() {
    init {
        DefaultNotification.notificationIcon = R.mipmap.ic_launcher_round
        DefaultNotification.connectedSmallIcon = R.drawable.ic_vpn_connect_key
        DefaultNotification.unconnectedSmallIcon = R.drawable.ic_vpn_unconnect_key
        UniteVpnManager.pendingClass = MainActivity::class.java
    }
}
```

(2) 设置证书相关的东西，可随时修改它，重新连接时便生效

```kotlin
class App : Application() {
    override fun onCreate() {
	super.onCreate()
	//放在协程中去执行
	CoroutineScope(Dispatchers.Default).launch {
	    //设置Ikev2的证书和属性
	    Ikev2CertHelper.userName = "****"
            Ikev2CertHelper.password = "****"
            Ikev2CertHelper.remoteId = "****"
            Ikev2CertHelper.storeCertificate("****")
            
            //设置OpenVpn的证书
            OpenCertHelper.setupCert("****")
	}
    }
}
```

（3）初始化实例

- 初始化操作类，需传入Context（最好是FragmentActivity的子类）

```kotlin
private val uniteVpnInstance = UniteVpnInstance(this)
```

- 添加状态监听和字节监听（可选）

```kotlin
class <model类> {
    init {
        UniteVpnManager.addStatusListener(this)
        UniteVpnManager.addByteCountListener(this)
    }
}
```

(4) 调用

- 连接

```kotlin
//创建连接数据
val listOf = listOf(
    AutoCombineInfo(OpenVpnImpl.TYPE, listOf(AutoInfo("服务器名", "ip", "端口", "是否使用udp", "超时时长"))),
    AutoCombineInfo(Ikev2Impl.TYPE, listOf(AutoInfo(OPEN_NAME, "服务器名", "ip", "端口", "是否使用udp", "超时时长")))
)
//调用自动连接
uniteVpnInstance.autoConnect(listOf)
```

- 断开

```kotlin
uniteVpnInstance.disconnect()
```

> 注意：以上调用Api均未挂起函数，需在协程作用域中调用

（5）设置应用过滤

```kotlin
//添加应用白名单
UniteVpnManager.filterHelper.setAllowAppList(this, HashSet<String>())
//添加黑名单（一般为后台控制下发黑名单数据）
UniteVpnManager.filterHelper.setBlackList(this, HashSet<String>())
...
```

其它细节参考Demo工程

## 4. 关键类

| 类名                                          | 作用                                                         |
| --------------------------------------------- | ------------------------------------------------------------ |
| com.core.unitevpn.UniteVpnInstance            | 对外提供vpn的连接和断开操作                                  |
| com.core.unitevpn.helper.UniteVpnStatusHelper | 提供vpn当前状态和接口添加，不对外开放，如需操作可通过#UniteVpnManager |
| com.core.unitevpn.helper.UniteVpnNotifyHelper | Vpn的通知管理类，不对外开放，如需操作可通过#UniteVpnManager  |
| com.core.unitevpn.UniteVpnManager             | 供外部调用的VpnManager                                       |
| com.core.unitevpn.UniteVpnStatusService       | 统一管理Vpn状态的Service                                     |
| com.core.unitevpn.base.VpnStatus              | Vpn状态类                                                    |
| com.core.unitevpn.sdk.UniteVpnSdk             | Vpn初始化类                                                  |
| ....                                          |                                                              |

## 5.VPN状态

| 状态          | 说明       | 交互   | 允许的操作     |
| ------------- | ---------- | ------ | -------------- |
| CONNECTED     | 已连接     | 允许   | 断开连接       |
| CONNECTING    | 连接中     | 允许   | 断开连接       |
| CONNECT_FAIL  | 连接失败   | 允许   | 连接、切换协议 |
| DISCONNECTING | 断开连接中 | 不允许 | -              |
| NOT_CONNECTED | 未连接     | 允许   | 连接、切换协议 |
