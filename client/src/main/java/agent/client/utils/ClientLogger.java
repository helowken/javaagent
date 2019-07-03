package agent.client.utils;

import agent.base.utils.Logger;

public class ClientLogger {
    public static final Logger logger = Logger.getLogger(ClientLogger.class);
    private static final String PREFIX = "[SYS]: ";

    static {
        logger.setPrefix(PREFIX);
        logger.setStream(System.out);
    }


}
