package agent.common.message.result.entity;

import java.util.Collections;
import java.util.List;

public class ErrorEntity {
    private List<String> errMsgs = Collections.emptyList();

    public ErrorEntity() {
    }

    public ErrorEntity(List<String> errMsgs) {
        this.errMsgs = errMsgs;
    }

    public List<String> getErrMsgs() {
        return errMsgs;
    }

    public void setErrMsgs(List<String> errMsgs) {
        this.errMsgs = errMsgs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = errMsgs.size(); i < len; ++i) {
            if (i > 0)
                sb.append("Caused by: ");
            sb.append(errMsgs.get(i)).append("\n");
        }
        return sb.toString();
    }
}
