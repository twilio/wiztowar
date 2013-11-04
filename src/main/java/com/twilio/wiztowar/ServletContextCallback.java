package com.twilio.wiztowar;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * The {@link ServletContextCallback} captures the {@link ServletContext} for use by the {@link DWAdapter}.
 */

public class ServletContextCallback implements ServletContextListener {

    /**
     * The {@link ServletContext} to capture.
     */
    private static ServletContext ctxt;

    public static ServletContext getServletContext() {
        return ctxt;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ctxt = sce.getServletContext();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}