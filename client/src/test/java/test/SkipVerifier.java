package test;

import agent.base.utils.ReflectionUtils;
import sun.misc.Unsafe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkipVerifier {
    private static Unsafe unsafe;
    private static char c = 'a';

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static void main(String[] args) throws Exception {
        if (true) {
            byte[] bs = hexStringToByteArray("E68891");
            System.out.println(new String(bs));
            return;
        }

        unsafe = ReflectionUtils.getStaticFieldValue(
                Unsafe.class,
                "theUnsafe"
        );
//        long gHotSpotVMStructs = findNative("gHotSpotVMStructs");
//        System.out.println(
//                "0x" + Long.toHexString(gHotSpotVMStructs) + ", 0x" +
//                        Long.toHexString(
//                                unsafe.getLong(gHotSpotVMStructs)
//                        )
//        );

//        JVMFlag.getFlags(
//                JVMType.getTypeMap(
//                        JVMStruct.getStructMap()
//                )
//        ).forEach(
//                flag -> System.out.println(flag.name)
//        );


        JVMType.getTypeMap(
                JVMStruct.getStructMap()
        ).forEach(
                (name, jvmType) -> {
                    if (SkipVerifier.class.getName().equals(name)) {

                    }
                }
        );
    }

    private static long findNative(String name) throws Exception {
        return ReflectionUtils.invokeStatic(
                ClassLoader.class,
                "findNative",
                new Class<?>[]{
                        ClassLoader.class,
                        String.class
                },
                null,
                name
        );
    }

    private static long symbol(String name) throws Exception {
        return unsafe.getLong(
                findNative(name)
        );
    }

    private static String readString(long addr) {
        if (addr == 0)
            return null;
        StringBuilder sb = new StringBuilder();
        int offset = 0;
        while (true) {
            byte b = unsafe.getByte(addr + offset++);
            if (b == 0)
                break;
            sb.append((char) b);
        }
        return sb.toString();
    }

    private static String derefReadString(long addr) {
        return readString(
                unsafe.getLong(addr)
        );
    }

    private static class JVMStruct {
        private final String name;
        private final Map<String, JVMField> fieldMap = new HashMap<>();

        private JVMStruct(String name) {
            this.name = name;
        }

        JVMField getField(String fieldName) {
            return fieldMap.get(fieldName);
        }

        void setField(String fieldName, JVMField field) {
            fieldMap.put(fieldName, field);
        }

        private static long offsetSymbol(String name) throws Exception {
            return symbol(
                    "gHotSpotVMStructEntry" + name + "Offset"
            );
        }

        private static Map<String, JVMStruct> getStructMap() throws Exception {
            Map<String, JVMStruct> structMap = new HashMap<>();
            long currentEntry = symbol("gHotSpotVMStructs");
            long arrayStride = symbol("gHotSpotVMStructEntryArrayStride");
            while (true) {
                String typeName = derefReadString(
                        currentEntry + offsetSymbol("TypeName")
                );
                String fieldName = derefReadString(
                        currentEntry + offsetSymbol("FieldName")
                );
                if (typeName == null || fieldName == null)
                    break;

                String typeString = derefReadString(
                        currentEntry + offsetSymbol("TypeString")
                );
                boolean isStatic = unsafe.getInt(
                        currentEntry + offsetSymbol("IsStatic")
                ) != 0;
                long offsetOffset = isStatic ? offsetSymbol("Address") : offsetSymbol("Offset");
                long offset = unsafe.getLong(currentEntry + offsetOffset);

                JVMStruct struct = structMap.computeIfAbsent(
                        typeName,
                        key -> new JVMStruct(typeName)
                );
                struct.setField(
                        fieldName,
                        new JVMField(fieldName, typeString, offset, isStatic)
                );

                currentEntry += arrayStride;
            }
            return structMap;
        }
    }

    private static class JVMField {
        private final String name;
        private final String type;
        private final long offset;
        private final boolean isStatic;

        private JVMField(String name, String type, long offset, boolean isStatic) {
            this.name = name;
            this.type = type;
            this.offset = offset;
            this.isStatic = isStatic;
        }
    }

    private static class JVMType {
        private final String type;
        private final String superClass;
        private final boolean isOop;
        private final boolean isInt;
        private final boolean isUnsigned;
        private final long size;
        private final Map<String, JVMField> fieldMap = new HashMap<>();

        private JVMType(String type, String superClass, boolean isOop, boolean isInt, boolean isUnsigned, long size) {
            this.type = type;
            this.superClass = superClass;
            this.isOop = isOop;
            this.isInt = isInt;
            this.isUnsigned = isUnsigned;
            this.size = size;
        }

        private JVMField getField(String fieldName) {
            return fieldMap.get(fieldName);
        }

        private static long offsetSymbol(String name) throws Exception {
            return symbol(
                    "gHotSpotVMTypeEntry" + name + "Offset"
            );
        }

        private static Map<String, JVMType> getTypeMap(Map<String, JVMStruct> structMap) throws Exception {
            Map<String, JVMType> typeMap = new HashMap<>();
            long entry = symbol("gHotSpotVMTypes");
            long arrayStride = symbol("gHotSpotVMTypeEntryArrayStride");
            while (true) {
                String typeName = derefReadString(
                        entry + offsetSymbol("TypeName")
                );
                String superClassName = derefReadString(
                        entry + offsetSymbol("SuperclassName")
                );
                if (typeName == null)
                    break;

                boolean isOop = unsafe.getInt(
                        entry + offsetSymbol("IsOopType")
                ) != 0;
                boolean isInt = unsafe.getInt(
                        entry + offsetSymbol("IsIntegerType")
                ) != 0;
                boolean isUnsigned = unsafe.getInt(
                        entry + offsetSymbol("IsUnsigned")
                ) != 0;
                long size = unsafe.getLong(
                        entry + offsetSymbol("Size")
                );

                JVMType type = new JVMType(typeName, superClassName, isOop, isInt, isUnsigned, size);
                typeMap.put(typeName, type);

                JVMStruct struct = structMap.get(typeName);
                if (struct != null)
                    type.fieldMap.putAll(struct.fieldMap);

                entry += arrayStride;
            }
            return typeMap;
        }
    }

    private static class JVMFlag {
        private final String name;
        private final long addr;

        private JVMFlag(String name, long addr) {
            this.name = name;
            this.addr = addr;
        }

        private static List<JVMFlag> getFlags(Map<String, JVMType> typeMap) {
            List<JVMFlag> jvmFlags = new ArrayList<>();
            JVMType flagType = typeMap.get("Flag");
            if (flagType == null) {
                flagType = typeMap.get("JVMFlag");
                if (flagType == null)
                    throw new RuntimeException("Could not resolve type 'Flag'.");
            }
            JVMField flagField = flagType.getField("flags");
            if (flagField == null)
                throw new RuntimeException("Could not resolve field 'Flag.flags'.");
            long flags = unsafe.getAddress(flagField.offset);

            JVMField numFlagsField = flagType.getField("numFlags");
            if (numFlagsField == null)
                throw new RuntimeException("Could not resolve field 'Flag.numFlags'.");
            long numFlags = unsafe.getInt(numFlagsField.offset);

            JVMField nameField = flagType.getField("_name");
            if (nameField == null)
                throw new RuntimeException("Could not resolve field 'Flag._name'.");

            JVMField addrField = flagType.getField("_addr");
            if (addrField == null)
                throw new RuntimeException("Could not resolve field 'Flag._addr'.");

            for (int i = 0; i < numFlags; ++i) {
                long flagAddr = flags + flagType.size * i;
                long flagValueAddr = unsafe.getAddress(flagAddr + addrField.offset);
                long flagNameAddr = unsafe.getAddress(flagAddr + nameField.offset);
                String flagName = readString(flagNameAddr);
                if (flagName != null)
                    jvmFlags.add(
                            new JVMFlag(flagName, flagValueAddr)
                    );
            }

            return jvmFlags;
        }
    }
}
