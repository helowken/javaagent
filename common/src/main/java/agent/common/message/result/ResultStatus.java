package agent.common.message.result;

public enum ResultStatus {
    SUCCESS((byte) 0), ERROR((byte) 1);

    public final byte status;

    ResultStatus(byte status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return status == SUCCESS.status;
    }

    public static ResultStatus parse(byte status) {
        for (ResultStatus rs : ResultStatus.values()) {
            if (rs.status == status)
                return rs;
        }
        throw new RuntimeException("Invalid status: " + status);
    }
}
