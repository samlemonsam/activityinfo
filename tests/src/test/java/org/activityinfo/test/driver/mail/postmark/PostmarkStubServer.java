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
package org.activityinfo.test.driver.mail.postmark;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.simple.container.SimpleServerFactory;
import org.activityinfo.test.config.ConfigProperty;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Provides a locally-running double of Postmark's API to allow testing of use cases that involve sending
 * and receiving email
 */
public class PostmarkStubServer  {

    public static final ConfigProperty POSTMARK_STUB_PORT = new ConfigProperty("postmarkStubPort",
        "The port number on which to run the postmark stub server");
    
    private static Closeable SERVER;
    
    static final ConcurrentLinkedQueue<Message> SENT_MESSAGES = new ConcurrentLinkedQueue<Message>();
    
    
    public static void start() throws IOException {
        synchronized (PostmarkStubServer.class) {
            if (SERVER == null) {
                SERVER = SimpleServerFactory.create("http://localhost:" + POSTMARK_STUB_PORT.get(),
                        new DefaultResourceConfig(PostmarkApi.class));
            }
        }
    }
    
    public static void stop() {
        synchronized (PostmarkStubServer.class) {
            if (SERVER != null) {
                try {
                    SERVER.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    SERVER = null;
                }
            }
        }
    }
}
