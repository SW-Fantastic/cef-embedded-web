package org.swdc.cef;

import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import me.friwi.jcefmaven.impl.step.check.CefInstallationChecker;
import me.friwi.jcefmaven.impl.step.extract.TarGzExtractor;
import me.friwi.jcefmaven.impl.util.FileUtils;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.callback.CefSchemeRegistrar;
import org.cef.handler.CefResourceHandler;
import org.cef.network.CefRequest;
import org.swdc.cef.schema.CEFResourceSchema;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * 本类负责CEF应用的正确启动，并且提供
 * 一个CEF的Client。
 */
public class CEFHelper {

    private static CefApp cefApp;


    public static CefAppBuilder createBuilder(File assetFolder, Class caller) {
        File dist = new File(assetFolder.getAbsolutePath() + File.separator + "CEF");
        try {

            String osName = System.getProperty("os.name").trim().toLowerCase();
            String nativePath = assetFolder.getAbsolutePath();
            if (osName.contains("mac")) {
                String url = caller.getProtectionDomain().getCodeSource().getLocation().getFile();
                String base = URLDecoder.decode(url, StandardCharsets.UTF_8);
                if (base.indexOf(".app") > 0) {
                    // 位于MacOS的Bundle（.app软件包）内部，特殊处理以获取正确的路径。
                    String location = base.substring(0,base.indexOf(".app")) + ".app/Contents/";
                    Path target = new File(location).toPath();
                    nativePath = target.resolve(nativePath).toFile().getAbsolutePath();
                }
            }
            String resourceName = "jcef-natives-";
            if (osName.contains("windows")) {
                resourceName = resourceName + "windows";
            } else if (osName.contains("linux")) {
                resourceName = resourceName + "linux";
            } else if (osName.contains("mac")) {
                resourceName = resourceName + "macos";
            }
            String osArch = System.getProperty("os.arch");
            List<String> arch64 = Arrays.asList("x64","amd64","x86_64");
            if (arch64.contains(osArch.toLowerCase())) {
                osArch = "x64";
            }
            resourceName = resourceName + "-" + osArch + ".tar.gz";
            if (!installCef(
                    dist, new File(nativePath + File.separator + resourceName)
            )) {
                return null;
            }
        } catch (Exception e ) {
            throw new RuntimeException(e);
        }
        // (0) Initialize CEF using the maven loader
        CefAppBuilder builder = new CefAppBuilder();
        builder.getCefSettings().windowless_rendering_enabled = false;
        builder.setInstallDir(dist);
        builder.setAppHandler(new MavenCefAppHandlerAdapter() {
            @Override
            public void stateHasChanged(org.cef.CefApp.CefAppState state) {
                if (state == CefApp.CefAppState.TERMINATED) {
                    System.err.println("CEF App Terminated.");
                    cefApp.dispose();
                    cefApp = null;
                }
            }
        });
        return builder;
    }


    private static boolean installCef(File libraryPath,File archive) throws UnsupportedPlatformException {
        if (libraryPath == null || !archive.exists()) {
            return false;
        }
       boolean installOk = CefInstallationChecker.checkInstallation(libraryPath);
       if (installOk) {
           return true;
       }
       if (libraryPath.exists()) {
           FileUtils.deleteDir(libraryPath);
       }
       libraryPath.mkdirs();
       try(FileInputStream fin = new FileInputStream(archive)) {
           TarGzExtractor.extractTarGZ(libraryPath, fin);
           if (!(new File(libraryPath, "install.lock").createNewFile())) {
               throw new IOException("Could not create install.lock to complete installation");
           }
       } catch (Exception e) {
           throw new RuntimeException(e);
       }
       return true;
    }

}
