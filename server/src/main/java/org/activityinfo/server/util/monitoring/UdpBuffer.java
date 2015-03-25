package org.activityinfo.server.util.monitoring;


import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

public class UdpBuffer {
    
    private ByteArrayOutputStream buffer;
    private OutputStreamWriter writer;
    private String prefix;

    public UdpBuffer(String prefix) {
        this.prefix = prefix;
        this.buffer = new ByteArrayOutputStream();
        this.writer = new OutputStreamWriter(buffer);
    }
    
    public void send(String metricId, String value, String type) {
        try {
            writer.write(prefix);
            writer.write(metricId);
            writer.write(':');
            writer.write(value);
            writer.write('|');
            writer.write(type);
            writer.write('\n');
        } catch (Exception e) {
            // Should not happen
            throw new RuntimeException(e);
        }
    }

    public byte[] drain() {
        if(buffer.size() > 0) {
            // annoying that we have to make a copy here...
            byte[] message = buffer.toByteArray();
            buffer.reset();
            return message;
        } else {
            return null;
        }
    }
    
}
