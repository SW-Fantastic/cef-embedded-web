# CEFEmbedded

一个与Java整合的Google CEF模块，可以方便的在各种地方使用，可以很方便（自动）的导出Java
的Method到javascript环境，让javascript直接访问Java的层的能力。

## 快速开始

首先，你需要在Java项目的根目录创建一个文件夹，用于存放JCEF的二进制文件以及其他项目中
需要的各种资源文件，请按需要在这里下载对应版本的JCEF的二进制jar文件，解压它并且复制其中的tar
文件到刚刚创建的资源目录，按照以下规则命名：

jcef-natives-[操作系统名：windows/macos/linux]-[操作系统的架构：例如x64].tar.gz

如：Windows10,64位系统应当命名为：jcef-natives-windows-x64.tar.gz。

未来Release中也将会提供正确的CEF二进制包，可以直接下载并且解压，可以在里面选择自己需要的。

接下来，请clone本项目，install到本地的Maven仓库，你需要 Java 11 或更高版本的JDK。
然后请在项目中依赖刚刚install完毕的本项目。

```java

import com.formdev.flatlaf.*;
import org.swdc.cef.*;

import javax.swing.*;
import java.io.File;
import java.util.Map;

public class Main {
    
    public static void main(String[] args) {

        // 初始化FlatLookAndFeel，不想用这个可以不初始化它。
        setupFlatLAF();

        // 创建CEFApplication，提供AssetFolder以及启动它的Class以便加载资源。
        CEFApplication application = new CEFApplication(new File("assets"),"static", Main.class);
        // 窗口管理器，用于创建窗口
        // res:// 是一个自定义协议，它用于加载JPMS（Jar包）内部的资源文件。
        // JPMS的模块的资源，需要向本JPMS模块开放，
        // 即需要再module-info中对embedCEF声明为open。
        // 当然，如果你只需要加载网络资源，也就是仅仅使用Http协议加载页面，则不需要这样做。
        // 这里的URL是BaseURL，所有的CEFView都会以此为Prefix。
        CEFWindowManager manager = new CEFWindowManager(application,"res://");
        // 创建主窗口
        CEFView view = manager.createView(MainView.class);
        // 关闭窗口时退出
        view.exitOnClose(true);
        // 打开窗口
        view.show();

    }

    public static void setupFlatLAF(){
        try {
            class LAF extends FlatLightLaf {

                @Override
                public UIDefaults getDefaults() {
                    // 不知道为什么FlatLAF无法找到一些UI的Class，我这里
                    // 继承了LAF并且提供了来自LAF的JPMS module的ClassLoader‘
                    // 此时问题得到了解决。
                    UIDefaults defaults = super.getDefaults();
                    defaults.put("ClassLoader",FlatLightLaf.class.getModule().getClassLoader());
                    return defaults;
                }
            }
            System.setProperty("flatlaf.useWindowDecorations","true" );
            System.setProperty("flatlaf.menuBarEmbedded","true");

            LAF laf = new LAF();
            UIManager.setLookAndFeel(laf);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
} 
```

通过继承CEFView，就能够得到一个新的窗口，CEFView以及其子类的所有返回值为
CEFResult的方法会被以反射的形式加载到CEF的Window对象中，通过以下方法能够实现
在CEF中调用它们：

```javascript 
class CEFRequest {

    // 传入一个数组，用作Java方法的参数。
    constructor(params) {
        this.parameters = [];
        for(let idx = 0; idx < params.length; idx ++) {
           this.parameters[idx] = JSON.stringify(params[idx]);
        }
    }

    toJson() {
        return JSON.stringify(this);
    }

    // 执行CEFQuery，需要提供CEFView中的方法名，
    // 通过then的Callback可以得到来自Java的执行结果。
    doRequest(methodName) {
        return new Promise((resolve,reject) => {
            window["$" + methodName]({
                request: this.toJson(),
                onSuccess: res => resolve(res),
                onFailure: e => reject(e)
            })
        })
    }
    
}


```