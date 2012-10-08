package net.evilmonkeylabs.mag7.bson;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;

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

	private static final int RE_GLOBAL_FLAG = 256;

	public static enum Bytes {
		EOO(0x00), DOUBLE(0x01), STRING(0x02), DOCUMENT(0x03), ARRAY(0x04), BINARY(
				0x05), UNDEF(0x06), // deprecated
		OBJECTID(0x07), BOOLEAN(0x08), UTC_DATETIME(0x09), NULL(0x0A), REGEX(
				0x0B), DBREF(0x0C), // deprecated
		JSCODE(0x0D), SYMBOL(0x0E), // deprecated
		JSCODE_W_SCOPE(0x0F), INT32(0x10), TIMESTAMP(0x11), INT64(0x12), MIN_KEY(
				0xFF), MAX_KEY(0x7F);

		public final byte type;

		Bytes(byte _t) {
			type = _t;
		}

		Bytes(int _t) {
			type = (byte) _t;
		}

	}

	public static enum Binary {
		GENERIC(0x00), FUNCTION(0x01), BINARY_OLD(0x02), UUID_OLD(0x03), UUID(
				0x04), MD5(0x05), USER_DEFINED(0x80);

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

		private MinKey() {
		}
	}

	public static class MaxKey {
		private static MaxKey ourInstance = new MaxKey();

		public static MaxKey getInstance() {
			return ourInstance;
		}

		private MaxKey() {
		}
	}

	// ---- regular expression handling ----

	/**
	 * Converts a string of regular expression flags from the database in Java
	 * regular expression flags.
	 * 
	 * @param flags
	 *            flags from database
	 * @return the Java flags
	 */
	public static int regexFlags(String flags) {
		int fint = 0;
		if (flags == null || flags.length() == 0)
			return fint;

		flags = flags.toLowerCase();

		for (int i = 0; i < flags.length(); i++) {
			RegexFlag flag = RegexFlag.getByCharacter(flags.charAt(i));
			if (flag != null) {
				fint |= flag.javaFlag;
				if (flag.unsupported != null)
					_warnUnsupportedRegex(flag.unsupported);
			} else {
				throw new IllegalArgumentException("unrecognized flag ["
						+ flags.charAt(i) + "] " + (int) flags.charAt(i));
			}
		}
		return fint;
	}

	public static int regexFlag(char c) {
		RegexFlag flag = RegexFlag.getByCharacter(c);
		if (flag == null)
			throw new IllegalArgumentException("unrecognized flag [" + c + "]");

		if (flag.unsupported != null) {
			_warnUnsupportedRegex(flag.unsupported);
			return 0;
		}

		return flag.javaFlag;
	}

	/**
	 * Converts Java regular expression flags into a string of flags for the
	 * database
	 * 
	 * @param flags
	 *            Java flags
	 * @return the flags for the database
	 */
	public static String regexFlags(int flags) {
		StringBuilder buf = new StringBuilder();

		for (RegexFlag flag : RegexFlag.values()) {
			if ((flags & flag.javaFlag) > 0) {
				buf.append(flag.flagChar);
				flags -= flag.javaFlag;
			}
		}

		if (flags > 0)
			throw new IllegalArgumentException(
					"some flags could not be recognized.");

		return buf.toString();
	}

	private static enum RegexFlag {
		CANON_EQ(Pattern.CANON_EQ, 'c', "Pattern.CANON_EQ"), UNIX_LINES(
				Pattern.UNIX_LINES, 'd', "Pattern.UNIX_LINES"), GLOBAL(
				RE_GLOBAL_FLAG, 'g', null), CASE_INSENSITIVE(
				Pattern.CASE_INSENSITIVE, 'i', null), MULTILINE(
				Pattern.MULTILINE, 'm', null), DOTALL(Pattern.DOTALL, 's',
				"Pattern.DOTALL"), LITERAL(Pattern.LITERAL, 't',
				"Pattern.LITERAL"), UNICODE_CASE(Pattern.UNICODE_CASE, 'u',
				"Pattern.UNICODE_CASE"), COMMENTS(Pattern.COMMENTS, 'x', null);

		private static final Map<Character, RegexFlag> byCharacter = new HashMap<Character, RegexFlag>();

		static {
			for (RegexFlag flag : values()) {
				byCharacter.put(flag.flagChar, flag);
			}
		}

		public static RegexFlag getByCharacter(char ch) {
			return byCharacter.get(ch);
		}

		public final int javaFlag;
		public final char flagChar;
		public final String unsupported;

		RegexFlag(int f, char ch, String u) {
			javaFlag = f;
			flagChar = ch;
			unsupported = u;
		}
	}

	private static void _warnUnsupportedRegex(String flag) {
		log.info("flag " + flag + " not supported by db.");
	}

	public static String toB64String(byte[] array) {
		return DatatypeConverter.printBase64Binary(array);
	}

	public static String toHexString(byte[] array) {
		return DatatypeConverter.printHexBinary(array);
	}

	public static byte[] toByteArray(String s) {
		return DatatypeConverter.parseHexBinary(s);
	}

	/**
	 * Helper function that dump an array of bytes in hex form
	 * 
	 * @param buffer
	 *            The bytes array to dump
	 * @return A string representation of the array of bytes
	 */
	public static final String dumpBytes(byte[] buffer) {
		if (buffer == null) {
			return "";
		}

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < buffer.length; i++) {
			sb.append("0x")
					.append((char) (HEX_CHAR[(buffer[i] & 0x00F0) >> 4]))
					.append((char) (HEX_CHAR[buffer[i] & 0x000F])).append(" ");
		}

		return sb.toString();
	}

	private static final byte[] HEX_CHAR = new byte[] { '0', '1', '2', '3',
			'4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private static final Logger log = Logger
			.getLogger("net.evilmonkeylabs.mag7.bson.BSON");

}