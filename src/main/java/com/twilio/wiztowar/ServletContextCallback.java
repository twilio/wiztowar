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

    /**
     * The {@link DWAdapter}.
     */
    private static DWAdapter<?> dwAdapter;

    public static ServletContext getServletContext() {
        return ctxt;
    }

    public static void setDWAdapter(DWAdapter<?> adapter){
        dwAdapter = adapter;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ctxt = sce.getServletContext();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (dwAdapter != null) {
            dwAdapter.shutDown();
        }
    }
}