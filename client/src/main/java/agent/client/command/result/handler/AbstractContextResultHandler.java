package agent.client.command.result.handler;

import agent.base.utils.Logger;

import java.util.Map;

abstract class AbstractContextResultHandler extends AbstractExecResultHandler {
    private static final Logger logger = Logger.getLogger(AbstractContextResultHandler.class);

    <T> void write(String msg, Map<String, T> rsMap, WriteFunc<T> func) {
        StringBuilder sb = new StringBuilder();
        if (rsMap.isEmpty()) {
            sb.append("No content.").append("\n");
        } else {
            rsMap.forEach((context, valueObject) -> {
                if (sb.length() > 0)
                    sb.append("-----------------\n");
                sb.append("Context: ").append(context).append("\n");
                func.write(sb, valueObject);
            });
        }
        logger.info("{}: \n{}", msg, sb.toString());
    }

    protected interface WriteFunc<T> {
        void write(StringBuilder sb, T v);
    }
}
