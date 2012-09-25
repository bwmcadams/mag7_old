package net.evilmonkeylabs.mag7.bson;

public class BSON {

    public static final byte EOO = 0x00;
    public static final byte DOUBLE = 0x01;
    public static final byte STRING = 0x02;
    public static final byte DOCUMENT = 0x03;
    public static final byte ARRAY = 0x04;
    public static final byte BINARY = 0x05;
    public static final byte UNDEF = 0x06;
    public static final byte OBJECTID = 0x07;
    public static final byte BOOLEAN = 0x08;
    public static final byte UTC_DATETIME = 0x09;
    public static final byte NULL = 0x0A;
    public static final byte REGEX = 0x0B;
    public static final byte DBREF = 0x0C;
    public static final byte JSCODE = 0x0D;
    public static final byte SYMBOL = 0x0E;
    public static final byte JSCODE_W_SCOPE = 0x0F;
    public static final byte INT32 = 0x10;
    public static final byte TIMESTAMP = 0x11;
    public static final byte INT64 = 0x12;
    public static final byte MIN_KEY = (byte) 0xFF;
    public static final byte MAX_KEY = 0x7F;

    public static final byte BINARY_GENERIC = Binary.GENERIC.type;
    public static final byte BINARY_FUNCTION = Binary.FUNCTION.type;
    public static final byte BINARY_OLD = Binary.BINARY_OLD.type;
    public static final byte UUID_OLD = Binary.UUID_OLD.type;
    public static final byte UUID = Binary.UUID.type;
    public static final byte MD5 = Binary.MD5.type;
    public static final byte USER_DEFINED = Binary.USER_DEFINED.type;

    public static enum Bytes {
        EOO(0x00),
        DOUBLE(0x01),
        STRING(0x02),
        DOCUMENT(0x03),
        ARRAY(0x04),
        BINARY(0x05),
        UNDEF(0x06), // deprecated
        OBJECTID(0x07),
        BOOLEAN(0x08),
        UTC_DATETIME(0x09),
        NULL(0x0A),
        REGEX(0x0B),
        DBREF(0x0C), // deprecated
        JSCODE(0x0D),
        SYMBOL(0x0E), // deprecated
        JSCODE_W_SCOPE(0x0F),
        INT32(0x10),
        TIMESTAMP(0x11),
        INT64(0x12),
        MIN_KEY(0xFF),
        MAX_KEY(0x7F);

        public final byte type;

        Bytes(byte _t) {
            type = _t;
        }

        Bytes(int _t) {
            type = (byte) _t;
        }


    }

    public static enum Binary {
        GENERIC(0x00),
        FUNCTION(0x01),
        BINARY_OLD(0x02),
        UUID_OLD(0x03),
        UUID(0x04),
        MD5(0x05),
        USER_DEFINED(0x80);

        public final byte type;

        Binary(byte _t) {
            type = _t;
        }

        Binary(int _t) {
            type = (byte) _t;
        }
    }

    public static class MinKey {
        private static MinKey ourInstance = new MinKey();

        public static MinKey getInstance() {
            return ourInstance;
        }

        private MinKey() { }
    }

    public static class MaxKey {
        private static MaxKey ourInstance = new MaxKey();

        public static MaxKey getInstance() {
            return ourInstance;
        }

        private MaxKey() { }
    }

    // todo regex
}