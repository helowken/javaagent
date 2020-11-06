package agent.common.struct;

public interface BBuff {
    void put(byte v);

    byte get();

    void putShort(short v);

    short getShort();

    void putInt(int v);

    int getInt();

    float getFloat();

    void putFloat(float v);

    double getDouble();

    void putDouble(double v);

    long getLong();

    void putLong(long v);

    long getSize();
}
