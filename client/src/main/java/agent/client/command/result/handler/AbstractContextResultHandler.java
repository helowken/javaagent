package agent.client.command.result.handler;

import agent.client.utils.ClientLogger;

import java.util.Map;

abstract class AbstractContextResultHandler extends AbstractExecResultHandler {

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
        ClientLogger.logger.info("{}: \n{}", msg, sb.toString());
    }

    protected interface WriteFunc<T> {
        void write(StringBuilder sb, T v);
    }
}
