/*
 * Copyright 2015-2020 msun.com All right reserved.
 */
package org.springframework.boot.context.embedded.netty;

import io.netty.bootstrap.Bootstrap;

import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Random;

import javax.servlet.ServletException;

import org.springframework.boot.context.embedded.*;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;

/**
 * An {@link EmbeddedServletContainerFactory} that can be used to create {@link NettyEmbeddedServletContainer}s.
 *
 * @author zxc Mar 1, 2016 10:31:30 AM
 */
public class NettyEmbeddedServletContainerFactory extends AbstractEmbeddedServletContainerFactory implements ResourceLoaderAware {

    public static final String SERVER_INFO = "netty/servlet";
    private ResourceLoader     resourceLoader;

    @Override
    public EmbeddedServletContainer getEmbeddedServletContainer(ServletContextInitializer... initializers) {
        ClassLoader parentClassLoader = resourceLoader != null ? resourceLoader.getClassLoader() : ClassUtils.getDefaultClassLoader();
        Package nettyPackage = Bootstrap.class.getPackage();
        String title = nettyPackage.getImplementationTitle();
        String version = nettyPackage.getImplementationVersion();
        logger.info("Running with " + title + " " + version);
        NettyEmbeddedContext context = new NettyEmbeddedContext(getContextPath(),
                                                                new URLClassLoader(new URL[] {}, parentClassLoader),
                                                                SERVER_INFO);
        if (isRegisterDefaultServlet()) {
            logger.warn("This container does not support a default servlet");
        }
        if (isRegisterJspServlet()) {
            logger.warn("This container does not support a JSP servlet");
        }
        for (ServletContextInitializer initializer : initializers) {
            try {
                initializer.onStartup(context);
            } catch (ServletException e) {
                throw new RuntimeException(e);
            }
        }
        int port = getPort() > 0 ? getPort() : new Random().nextInt(65535 - 1024) + 1024;
        InetSocketAddress address = new InetSocketAddress(port);
        logger.info("Server initialized with port: " + port);
        return new NettyEmbeddedServletContainer(address, context);
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
