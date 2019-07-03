package agent.server;

import agent.base.utils.LockObject;
import agent.base.utils.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class AgentServer {
    private static final Logger logger = Logger.getLogger(AgentServer.class);
    private final ExecutorService executorService = new ThreadPoolExecutor(10, 20, 30,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>());
    private boolean running = false;
    private boolean end = false;
    private volatile boolean shutdown = false;
    private ServerSocket serverSocket;
    private Thread t;
    private int port;
    private LockObject lockObject = new LockObject();

    AgentServer(int port) {
        this.port = port;
    }

    boolean isRunning() {
        return lockObject.syncValue(lock -> running);
    }

    boolean startup() {
        t = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                lockObject.syncAndNotifyAll(lock -> {
                    running = true;
                    end = true;
                });
                logger.info("Agent server started at port: {}", port);
                while (!shutdown) {
                    try {
                        Socket socket = serverSocket.accept();
                        executorService.execute(new AgentEndpoint(socket));
                    } catch (Exception e) {
                        logger.error("Server accept failed.", e);
                    }
                }
                executorService.shutdownNow();
            } catch (Exception e) {
                logger.error("Server startup failed.", e);
                lockObject.syncAndNotifyAll(lock -> end = true);
            }
        });
        t.setDaemon(true);
        t.start();
        return lockObject.syncValue(lock -> {
            while (!end) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                }
            }
            return running;
        });
    }

    void shutdown() {
        lockObject.syncAndNotifyAll(lock -> {
            if (!shutdown) {
                shutdown = true;
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                        serverSocket = null;
                    } catch (IOException e) {
                        logger.error("Close agent server failed.", e);
                    }
                }
                t.interrupt();
            }
        });
        try {
            t.join();
        } catch (InterruptedException e) {
        }
    }

}
