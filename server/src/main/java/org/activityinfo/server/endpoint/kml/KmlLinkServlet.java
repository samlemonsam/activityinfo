/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.server.endpoint.kml;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serves a simple KML file containing a network link to
 * {@link org.activityinfo.server.endpoint.kml.KmlDataServlet}.
 * <p/>
 * This file will be downloaded to the users computer and can be saved locally,
 * but will assure that all actual data comes live from the server.
 *
 * @author Alex Bertram (akbertram@gmail.com)
 */
@Singleton
public class KmlLinkServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(KmlLinkServlet.class.getName());

    private final Configuration templateCfg;

    @Inject
    public KmlLinkServlet(Configuration templateCfg) {
        this.templateCfg = templateCfg;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Map<String, Object> link = new HashMap<String, Object>();

        link.put("href", (req.isSecure() ? "https" : "http") + "://" + req.getServerName() + ":" +
                         req.getServerPort() + req.getRequestURI() + "/activities");

        Template tpl = templateCfg.getTemplate("kml/NetworkLink.kml.ftl");
        resp.setContentType("application/vnd.google-earth.kml+xml;");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=ActivityInfo.kml");

        try {
            tpl.process(link, resp.getWriter());
        } catch (TemplateException e) {
            resp.setStatus(500);
            LOGGER.log(Level.SEVERE, "Exception serving KML link", e);
        }
    }
}