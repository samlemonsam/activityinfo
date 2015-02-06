package org.activityinfo.server.endpoint.jsonrpc;

public class BadRpcRequest extends RuntimeException {
    
    public BadRpcRequest(String message, Object... args) {
        super(String.format(message, args)); 
    }
    
}
