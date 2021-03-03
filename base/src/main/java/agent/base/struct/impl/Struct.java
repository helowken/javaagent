package agent.base.struct.impl;

import agent.base.buffer.BufferAllocator;
import agent.base.struct.BBuff;
import agent.base.struct.DefaultBBuff;

import java.nio.ByteBuffer;

@SuppressWarnings("unchecked")
public abstract class Struct {
    public static int bytesSize(Object o, StructContext context) {
        return StructFields.detectField(o).bytesSize(o, context);
    }

    public static void serialize(BBuff bb, Object o, StructContext context) {
        StructFields.detectField(o).serialize(bb, o, context);
    }

    public static ByteBuffer serialize(Object o) {
        StructContext context = new StructContext();
        try {
            return serialize(o, context);
        } finally {
            context.clear();
        }
    }

    public static ByteBuffer serialize(Object o, StructContext context) {
        try {
            StructField field = StructFields.detectField(o);
            ByteBuffer bb = BufferAllocator.allocate(
                    field.bytesSize(o, context)
            );
            field.serialize(
                    new DefaultBBuff(bb),
                    o,
                    context
            );
            return bb;
        } finally {
            context.clearCache();
        }
    }

    public static <T> T deserialize(ByteBuffer bb) {
        return deserialize(
                bb,
                new StructContext()
        );
    }

    public static <T> T deserialize(ByteBuffer bb, StructContext context) {
        return deserialize(
                new DefaultBBuff(bb),
                context
        );
    }

    static <T> T deserialize(BBuff bb, StructContext context) {
        byte type = bb.get();
        return (T) StructFields.getField(type).deserialize(bb, context);
    }

}
