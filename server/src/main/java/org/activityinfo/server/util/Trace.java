package org.activityinfo.server.util;

import com.google.cloud.trace.Tracer;
import com.google.cloud.trace.core.*;
import com.google.cloud.trace.service.AppEngineTraceService;
import org.activityinfo.server.DeploymentEnvironment;

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
    public static TraceContext startSpan(String name) {
        return TRACER.startSpan(name);
    }

    /**
     * Ends the current span in the provided {@link TraceContext}.
     *
     * @param traceContext The {@link TraceContext} associated with the span that will be ended.
     */
    public static void endSpan(TraceContext traceContext) {
        TRACER.endSpan(traceContext);
    }

}
