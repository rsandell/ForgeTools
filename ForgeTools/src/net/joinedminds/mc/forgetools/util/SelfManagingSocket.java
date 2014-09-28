package net.joinedminds.mc.forgetools.util;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Description
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class SelfManagingSocket {
    public static final int DEFAULT_SHUTDOWN_TIMER_SEC = 1;
    private final ScheduledExecutorService timeoutExecutor;
    private Socket socket;
    private String host;
    private int port;
    private int timeout;
    private PrintStream out;

    public SelfManagingSocket(String host, int port) {
        this(host, port, DEFAULT_SHUTDOWN_TIMER_SEC);
    }

    public SelfManagingSocket(String host, int port, int timeout) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        timeoutExecutor = Executors.newScheduledThreadPool(1);
    }

    public synchronized void printf(String format, Object... args) {
        manageSocket();
        if (out != null) {
            out.printf(format, args);
        }
    }

    private synchronized void closeSocket() {
        try {
            if (out != null && socket != null) {
                System.out.println("Closing socket");
                out.close();
                socket.close();
                out = null;
                socket = null;
            }
        } catch (IOException e) {
            System.err.println("Failed to close socket to " + host + ":" + port);
            e.printStackTrace();
        }
    }

    private synchronized void manageSocket() {
        if (socket == null || socket.isClosed()) {
            try {
                System.out.println("Setting up socket");
                socket = new Socket(host, port);
                out = new PrintStream(socket.getOutputStream());
                timeoutExecutor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        closeSocket();
                    }
                }, timeout, TimeUnit.SECONDS);
            } catch (IOException e) {
                System.err.println("Failed to open socket to " + host + ":" + port);
                e.printStackTrace();
            }
        }
    }
}
