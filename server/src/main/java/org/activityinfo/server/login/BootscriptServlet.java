package org.activityinfo.server.login;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.ByteStreams;
import com.google.inject.Singleton;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * Serves the bootstrap script with aggressive no-cache headers
 */
@Singleton
public class BootscriptServlet extends HttpServlet {

    private static final Cache<String, String> CACHE = CacheBuilder.newBuilder().build();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setHeader("Cache-Control", "no-cache");
        try(InputStream in = getServletContext().getResourceAsStream(req.getRequestURI())) {
            ByteStreams.copy(in, resp.getOutputStream());
        }
    }
}
