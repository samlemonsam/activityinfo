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
package org.activityinfo.server.util;

import com.google.cloud.trace.Tracer;
import com.google.cloud.trace.core.*;
import com.google.cloud.trace.service.AppEngineTraceService;
import org.activityinfo.server.DeploymentEnvironment;

import java.util.Optional;

/**
 * Provides
 */
public class Trace {

    private static class NoTracer implements Tracer {
        private final SpanContext spanContext = new SpanContext(
                TraceId.invalid(), SpanId.invalid(), new TraceOptions());
        private final TraceContext traceContext = new TraceContext(new NoSpanContextHandle(spanContext));

        @Override
        public TraceContext startSpan(String name) {
            return traceContext;
        }
        @Override
        public TraceContext startSpan(String name, StartSpanOptions options) {
            return traceContext;
        }
        @Override
        public void endSpan(TraceContext traceContext) {}
        @Override
        public void endSpan(TraceContext traceContext, EndSpanOptions options) {}
        @Override
        public void annotateSpan(TraceContext traceContext, Labels labels) {}
        @Override
        public void setStackTrace(TraceContext traceContext, StackTrace stackTrace) {}
    }

    private static class NoSpanContextHandle implements SpanContextHandle {
        private final SpanContext context;

        private NoSpanContextHandle(SpanContext context) {
            this.context = context;
        }

        @Override
        public SpanContext getCurrentSpanContext() {
            return context;
        }

        @Override
        public void detach() {}
    }

    private static  Tracer TRACER;

    static {
        if(DeploymentEnvironment.isAppEngineProduction()) {
            AppEngineTraceService traceService = new AppEngineTraceService();
            TRACER = traceService.getTracer();
        } else {
            TRACER = new NoTracer();
        }
    }

    /**
     * Starts a new span and updates the current context. The new span will be a child of the span in
     * the current context.
     *
     * @param name a string that represents the name of the new span.
     * @return The {@link TraceContext} associated with the newly created span.
     */
    public static Optional<TraceContext> startSpan(String name) {
        try {
            return Optional.ofNullable(TRACER.startSpan(name));
        } catch (Exception ignorable) {
            // Errors creating spans shouldn't cause an overall failure
            return Optional.empty();
        }
    }

    /**
     * Ends the current span in the provided {@link TraceContext}.
     *
     * @param traceContext The {@link TraceContext} associated with the span that will be ended.
     */
    public static void endSpan(Optional<TraceContext> traceContext) {
        try {
            traceContext.ifPresent(TRACER::endSpan);
        } catch (Exception ignorable) {
            // Errors in closing spans shouldn't cause an overall failure
        }
    }

}
