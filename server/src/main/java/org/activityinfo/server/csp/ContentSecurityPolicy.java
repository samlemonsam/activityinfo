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
package org.activityinfo.server.csp;

import org.activityinfo.server.DeploymentEnvironment;

import javax.ws.rs.core.Response;

/**
 * Defines and applies a Content Security Policy to responses that limit the 
 * consequences of XSS vulnerabilities.
 */
public class ContentSecurityPolicy {

    private static final String SELF = "'self'";
    private static final String DATA = "data:";
    private static final String NONE = "'none'";
    
    private static final String UNSAFE_INLINE = "'unsafe-inline'";
    private static final String UNSAFE_EVAL = "'unsafe-eval'";

    private static final String GOOGLE_ANALYTICS = "https://www.google-analytics.com";

    private static final String GOOGLE_STORAGE = "https://storage.googleapis.com";

    private static final String HTTPS_ONLY = "https:";
    
    private static final String MAPBOX_TILES = "*.tiles.mapbox.com";

    private static final String MAPBOX_API = "https://api.mapbox.com";

    private final String policy;

    public ContentSecurityPolicy() {
        StringBuilder sb = new StringBuilder();
        
        
        append(sb, "img-src", SELF, 
            
            // We use data: images extensively
            DATA,

            HTTPS_ONLY,
            MAPBOX_TILES,
                
            // Google analytics add images to the page to track
            // Events
            GOOGLE_ANALYTICS

        
        );

        append(sb, "font-src", SELF,

            // Typotheque fonts are served from unpredictable
            // hosts, so we have to be pretty open in what we accept.
            SELF,
            HTTPS_ONLY,
            DATA
        );
        
        append(sb, "script-src", SELF,

            // Unfortunately GWT makes extensive use of inline
            // scripts so we need to allow this
            UNSAFE_INLINE, 
            UNSAFE_EVAL,
                
            // Allow google analytics scripts
            GOOGLE_ANALYTICS,
            MAPBOX_API
        );
        append(sb, "style-src", SELF, 
            
            // GWT makes extensive use of inline style-sheets
            UNSAFE_INLINE,
            MAPBOX_API
        );
        
        append(sb, "connect-src", SELF, GOOGLE_ANALYTICS);
        
        append(sb, "object-src", SELF);
        
        // We are not currently embedding any audio/video
        append(sb, "media-src", NONE);
        
        append(sb, "frame-src", SELF);
        
        // Only allow iframes containing content from this domain
        append(sb, "child-src", SELF);


        // Do not allow the application to be hosted in frame
        // on another domain
        append(sb, "frame-ancestors", SELF);
        
        // Report violations to the server so they can be logged and monitored
        sb.append("report-uri /csp-violation;");
        
        
        this.policy = sb.toString();
    }


    private static void append(StringBuilder sb, String type, String... allowed) {
        sb.append(type);
        for (String s : allowed) {
            sb.append(' ');
            sb.append(s);
        }
        sb.append(';');
        sb.append(' ');
    }

    public void applyTo(Response.ResponseBuilder response) {

        // Do not apply when running the development server as it will
        // block the GWT codeserver and other development tools
        if(DeploymentEnvironment.isAppEngineDevelopment()) {
            return;
        }

        response.header("Content-Security-Policy", policy);

        // For older browsers, make sure that this site can't be embedded in 
        // a third party site for the purposes of click jacking
        // https://www.owasp.org/index.php/Clickjacking_Defense_Cheat_Sheet
        response.header("X-Frame-Options", "SAMEORIGIN");
    }
}
