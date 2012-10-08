package net.evilmonkeylabs.mag7.bson.doc;

import java.util.Date;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import net.evilmonkeylabs.mag7.bson.BSON;
import net.evilmonkeylabs.mag7.bson.types.BSONTimestamp;
import net.evilmonkeylabs.mag7.bson.types.Code;
import net.evilmonkeylabs.mag7.bson.types.CodeWScope;
import net.evilmonkeylabs.mag7.bson.types.ObjectID;

/**
 * Copyright (c) 2008 - 2012 10gen, Inc. <http://10gen.com>
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

public abstract class BSONDocumentBuilder<T> {
	/**
	 * The core "default" method. If any special conversions are called like
	 * "putDate", in the default implementation they are passed into here.
	 * 
	 * Think of this as "put generic type".
	 * 
	 * @param key
	 *            A string representing the field name
	 * @param value
	 *            A java object representing the field's value
	 */
	public abstract void put(String key, Object value);

	/**
	 * Method to fetch the 'result' of the builder operation, returning a fully
	 * formed instance of T.
	 * 
	 * Behavior of the builder after result is called is undefined.
	 * 
	 * @return A full instance of T
	 */
	public abstract T result();

	/**
	 * Places an undefined value (of BSON type Undef) in the document. Typically
	 * "null" and rarely needs overriding
	 * 
	 * @param key
	 */
	public void putNull(String key) {
		put(key, null);
	}

	/**
	 * Place a value of type 'double' into the document.
	 * 
	 * @param key
	 * @param dbl
	 *            A JVM double representing the value
	 */
	public void putDouble(String key, double dbl) {
		put(key, dbl);
	}

	/**
	 * Place a value of type 'string' into the document
	 * 
	 * TODO: Should we return just the bytes for those looking to optimise?
	 * 
	 * @param key
	 * @param str
	 *            A UTF8 String representing the value
	 */
	public void putString(String key, String str) {
		put(key, str);
	}

	/**
	 * Place a value of an embedded document inside.
	 * 
	 * @param key
	 * @param subDoc
	 *            A Document of type T representing a subdoc
	 */
	public void putDocument(String key, T subDoc) {
		put(key, subDoc);
	}

	/**
	 * Place a value of an embedded list inside.
	 * 
	 * TODO - Allow user to customise list types. (my kingdom for abstract types
	 * in Java)
	 * 
	 * @param key
	 * @param array
	 *            A DB Array
	 */
	public void putList(String key, Object array) {
		put(key, array);
	}

	/**
	 * Place a value of an embedded list inside
	 * 
	 * @param name
	 * @param time
	 * @param machine
	 * @param inc
	 */
	public void putObjectID(String key, int time, int machine, int inc) {
		ObjectID oid = new ObjectID(time, machine, inc);
		log.info("Parsed OID: " + oid);
		put(key, oid);
	}

	/**
	 * Places a boolean value of "true" into the document
	 * 
	 * @param key
	 */
	public void putBoolTrue(String key) {
		put(key, true);
	}

	/**
	 * Places a boolean value of "false" into the document
	 * 
	 * @param key
	 */
	public void putBoolFalse(String key) {
		put(key, false);
	}

	/**
	 * Places a BSON UTC DateTime into the document
	 * 
	 * @param key
	 * @param tsp
	 *            A long representing the # of milliseconds since the Unix Epoch
	 *            (Jan 1 '70)
	 */
	public void putDateTime(String key, long tsp) {
		put(key, new Date(tsp));
	}

	/**
	 * Place a BSON regular expression into the document
	 * 
	 * From the BSON Spec:
	 * 
	 * "Regular expression - The first cstring is the regex pattern, the second
	 * is the regex options string. Options are identified by characters, which
	 * must be stored in alphabetical order. Valid options are 'i' for case
	 * insensitive matching, 'm' for multiline matching, 'x' for verbose mode,
	 * 'l' to make \w, \W, etc. locale dependent, 's' for dotall mode ('.'
	 * matches everything), and 'u' to make \w, \W, etc. match unicode."
	 * 
	 * @param key
	 * @param pattern
	 *            A string representing the regular expression pattern
	 * @param flags
	 *            A String representing the regex flags, as outlined below. See
	 *            BSON.regexFlags for a flags parser.
	 */
	public void putRegex(String key, String pattern, String flags) {
		put(key, Pattern.compile(pattern, BSON.regexFlags(flags)));
	}

	/**
	 * Put a BSON Wrapped block of JS Code into the document
	 * 
	 * @param key
	 * @param jsCode
	 *            a String representing the JSON Code
	 */
	public void putCode(String key, String jsCode) {
		put(key, new Code(jsCode));
	}

	/**
	 * Put a BSON Wrapped block of Scoped JS Code into the document
	 * 
	 * @param key
	 * @param jsCode
	 *            A String representing the JavaScript Code
	 * @param scope
	 *            A document T representing the Scope
	 */
	public void putScopedCode(String key, String jsCode, T scope) {
		put(key, new CodeWScope<T>(jsCode, scope));
	}

	public void putSymbol(String key, String symbol) {
		// Scala, clojure etc probably want something other than a string.
		put(key, symbol);
	}

	/**
	 * Put an int32 (typically a JVM int) into the document
	 * 
	 * @param key
	 * @param int32
	 *            An int representing the int32
	 */
	public void putInteger(String key, int int32) {
		put(key, int32);
	}

	/**
	 * Put an int64 (typically a JVM long) into the document
	 * 
	 * @param key
	 * @param int64
	 *            A long representing the int64
	 */
	public void putLong(String key, long int64) {
		put(key, int64);
	}

	/**
	 * Put a BSON Timestamp into the document
	 * 
	 * Note that BSON Timestamps are a *special type* related to sharding and
	 * not the same as a standard UTC DateTime.
	 * 
	 * BSON Spec: "Timestamp - Special internal type used by MongoDB replication
	 * and sharding. First 4 bytes are an increment, second 4 are a timestamp.
	 * Setting the timestamp to 0 has special semantics."
	 * 
	 * @param key
	 * @param time
	 * @param inc
	 */
	public void putTimestamp(String key, int time, int inc) {
		put(key, new BSONTimestamp(time, inc));
	}

	/**
	 * Put a BSON "Minimum Possible Key" marker in the document
	 * 
	 * @param key
	 */
	public void putMinKey(String key) {
		put(key, BSON.MinKey.getInstance());
	}

	/**
	 * Put a BSON "Maximum Possible Key" marker in the document
	 * 
	 * @param key
	 */
	public void putMaxKey(String key) {
		put(key, BSON.MaxKey.getInstance());
	}

	private static final Logger log = Logger.getLogger("BSONDocumentBuilder");
}
