package org.activityinfo.server.util.monitoring;


import com.google.common.base.Strings;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class UdpReporter implements MetricsReporter {
    public static final String COUNTER_SUFFIX = "c";
    public static final String TIMER_SUFFIX = "ms";
    
    private final InetAddress address;
    private final int port;
    private final String prefix;
    
    private static final Logger LOGGER = Logger.getLogger(UdpReporter.class.getName());
    
    private boolean errorLogged = false;

    /**
     * A per-thread buffer for UDP messages to allow batching of packets within a 
     * single request
     */
    private ThreadLocal<UdpBuffer> buffer = new ThreadLocal<>();
    
    private NonBlockingRateLimiter rateLimiter = new NonBlockingRateLimiter(10, TimeUnit.SECONDS);
    
    public UdpReporter(String prefix, String hostname, int port) throws IOException {
        this.address = InetAddress.getByName(hostname);
        this.port = port;
        this.prefix = Strings.nullToEmpty(prefix);
    }

    @Override
    public void increment(String metricId, long count) {
        send(metricId, Long.toString(count), COUNTER_SUFFIX);
    }

    @Override
    public void time(String metricId, long milliseconds) {
        send(metricId, Long.toString(milliseconds), TIMER_SUFFIX);
    }

    @Override
    public void set(String metricId, String id) {
        send(metricId, id, "s");
    }

    @Override
    public void histogram(String metricId, double value) {
        send(metricId, Double.toString(value), "h");
    }

    private void send(String metricId, String value, String metricType) {
        UdpBuffer localBuffer = buffer.get();
        if(localBuffer == null) {
            localBuffer = new UdpBuffer(prefix);
            buffer.set(localBuffer);
        }
        localBuffer.send(metricId, value, metricType);
        if(rateLimiter.tryAcquire()) {
            flush();
        }
    }

    public void flush() {
        byte[] messageBytes = buffer.get().drain();

        if(messageBytes != null) {
            try (DatagramSocket socket = new DatagramSocket()) {
                DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, address, port);
                socket.send(packet);

            } catch (IOException e) {
                if (!errorLogged) {
                    LOGGER.log(Level.SEVERE, "Failed to send UDP packet: " + e.getMessage() +
                            " (will not be reported again)", e);
                    errorLogged = true;
                }
            }
        }
    }
}