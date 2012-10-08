package net.evilmonkeylabs.mag7.bson;

import net.evilmonkeylabs.mag7.bson.doc.BSONDocumentBuilder;
import net.evilmonkeylabs.mag7.bson.doc.BSONList;
import net.evilmonkeylabs.mag7.bson.io.BSONByteBuffer;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Copyright (c) 2008 - 2012 10gen, Inc. <http://10gen.com>
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * 
 * @param <T> The type of "Document" returned.
 */
public abstract class BSONReader<T> {

	public BSONReader(final ByteBuffer _buf) {
		buf = new BSONByteBuffer(_buf);
		startPos = _buf.position(); 
		pos = new AtomicInteger(startPos);
		len = buf.getInt(pos.getAndAdd(4));
	}

	public DefaultBSONDocParser parseDocument(final ByteBuffer buf) {
		return new DefaultBSONDocParser(buf);
	}

	public abstract BSONDocumentBuilder<T> newBuilder();

	protected void parse() {
		while (parseEntry());
		log.info("[" + startPos + "] Stopped parsing at " + pos);
		parsed = true;
	}

	protected boolean parseEntry() {
		final byte type = buf.get(pos.getAndIncrement());

		if (type == BSON.EOO) 
			return false;

		final int sz = buf.sizeCString(pos.get());
		final String name = buf.getCString(pos.getAndAdd(sz));

		log.info("[" + startPos + "] name: " + name + " type: " + type);
		
		switch (type) {
		case BSON.NULL:
		case BSON.UNDEF:
			b.putNull(name);
			break;
		case BSON.DOUBLE:
			b.putDouble(name, buf.getDouble(pos.getAndAdd(8)));
			break;
		case BSON.STRING:
			final String val = buf.getUTF8String(pos.get());
			b.putString(name, val);
			pos.getAndAdd(val.length() + 4 + 1);
			break;
		case BSON.DOCUMENT:
			log.info("//////// OBJECT");
			final ByteBuffer _subBuf = buf.slice();
			_subBuf.position(pos.get());
			final BSONReader<T> dP = newDocumentParser(_subBuf);
			final T doc = dP.result();
			log.info("SubDoc: " + doc + " Pos from " + pos + " to " + dP.lastPos());
			b.putDocument(name, doc);
			pos.getAndSet(dP.lastPos());
			break;
		case BSON.ARRAY:
			// TODO - Let user specify custom list builder !!!
			log.warning("**** I DONT KNOW HOW TO SLICE HERE YET !!!!");
			final Object list = parseArray();
			b.putList(name, list);
			break;
		case BSON.BINARY:
			// TODO - Break out and parse Binary
			break;
		case BSON.OBJECTID:
			// OIDs are stored as Big Endian
			b.putObjectID(name, buf.getIntBE(pos.getAndAdd(4)), buf.getIntBE(pos.getAndAdd(4)), buf.getIntBE(pos.getAndAdd(4)));
			break;
		case BSON.BOOLEAN:
			if (buf.get(pos.getAndIncrement()) == 0x01) 
				b.putBoolTrue(name);
			else 
				b.putBoolFalse(name);
			break;
		case BSON.UTC_DATETIME:
			final long tsp = buf.getLong(pos.getAndAdd(8));
			b.putDateTime(name, tsp);
			break;
		case BSON.REGEX:
			final int pSz = buf.sizeCString(pos.get());
			final String pattern = buf.getCString(pos.getAndAdd(pSz));
			final int oSz = buf.sizeCString(pos.get());
			final String options = buf.getCString(pos.getAndAdd(oSz));
			b.putRegex(name, pattern, options);
			break;
		case BSON.DBREF:
			// TODO - parse.. CString (NS) then OID
			throw new UnsupportedOperationException("DBRef not yet supported");
			// break;
		case BSON.JSCODE:
			final String code = buf.getUTF8String(pos.get());
			log.info("JSCode at '" + name + "' - " + code);
			b.putCode(name, code);
			pos.getAndAdd(code.length() + 4);
			break;
		case BSON.JSCODE_W_SCOPE:
			final String scopedCode = buf.getUTF8String(pos.get());
			log.warning("**** I DONT KNOW HOW TO SLICE HERE YET !!!!");
			final BSONReader<T> sP = newDocumentParser(buf.slice());
			final T scope = sP.result();
			b.putScopedCode(name, scopedCode, scope);
			pos.getAndAdd(scopedCode.length() + 4);
			break;
		case BSON.SYMBOL:
			final String sym = buf.getUTF8String(pos.get());
			b.putSymbol(name, sym);
			pos.getAndAdd(sym.length() + 4);
			break;
		case BSON.INT32:
			b.putInteger(name, buf.getInt(pos.getAndAdd(4)));
			break;
		case BSON.INT64:
			b.putLong(name, buf.getLong(pos.getAndAdd(8)));
			break;
		case BSON.TIMESTAMP:
			// Special BSON Timestamp for sharding, oplog, etc.
			final int inc = buf.getInt(pos.getAndAdd(4));
			final int time = buf.getInt(pos.getAndAdd(4));
			b.putTimestamp(name, time, inc);
			break;
		case BSON.MIN_KEY:
			b.putMinKey(name);
			break;
		case BSON.MAX_KEY:
			b.putMaxKey(name);
			break;
		default:
			log.warning("Unknown BSON Type '" + type + "'");
			throw new UnsupportedOperationException(
					"No support for decoding BSON type of byte '" + type + "'");
		}

		return true;
	}

	protected int lastPos() {
		return pos.get();
	}

	protected BSONList parseArray() {
		final BSONReader<BSONList> lP = new DefaultBSONArrayParser(buf.slice());
		return lP.result();
	}

	public T result() {
		if (!parsed)
			parse();

		return b.result();
	}

	public abstract BSONReader<T> newDocumentParser(ByteBuffer tBuf);

	protected BSONDocumentBuilder<T> b;
	protected final int len;
	protected final int startPos;
	protected final AtomicInteger pos;
	protected final BSONByteBuffer buf;
	protected boolean parsed = false;
	
	protected static final Logger log = Logger.getLogger("BSONReader");
}
