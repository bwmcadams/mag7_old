package net.evilmonkeylabs.mag7.bson;

import net.evilmonkeylabs.mag7.bson.doc.BSONDocumentBuilder;
import net.evilmonkeylabs.mag7.bson.doc.BSONList;
import net.evilmonkeylabs.mag7.bson.io.BSONByteBuffer;
import net.evilmonkeylabs.mag7.bson.io.BSONException;

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
			log.info("/// OBJECT");
			final int _subL = buf.getInt(pos.get());
			final ByteBuffer _subBuf = buf.slice();
			_subBuf.position(pos.get());
			final BSONReader<T> dP = newDocumentParser(_subBuf);
			final T doc = dP.result();
			b.putDocument(name, doc);
			pos.getAndAdd(_subL);
			break;
		case BSON.ARRAY:
			// TODO - Let user specify custom list builder !!!
			log.info("/// ARRAY");
			final int _lstL = buf.getInt(pos.get());
			b.putList(name, parseArray());
			pos.getAndAdd(_lstL);
			break;
		case BSON.BINARY:
			log.info("/// Binary at : " + pos.get());
			final int _binL = buf.getInt(pos.getAndAdd(4));
			final byte _sT = buf.get(pos.getAndIncrement());
			log.info("/// Binary SubType: " + _sT + " of length " + _binL);
			// TODO - can we make this more efficient?
			final byte[] _bin = new byte[_binL];
			ByteBuffer bytes;
			
			if (_sT == BSON.BINARY_OLD) {
				// Old format had an extra length header; parse out before passing to a simple "got Binary" method
				bytes = buf.get(_bin, pos.addAndGet(4), 0, _binL - 4);
			} else {
				bytes = buf.get(_bin, pos.get(), 0, _binL);
			}
			
			pos.getAndAdd(_binL);
			
			if (_sT == BSON.BINARY_UUID) {
				if (_binL != 16)
					throw new BSONException("Invalid UUID Length in Binary. Expected 16, got " + _binL);
				b.putUUID(name, bytes, true);
			} else if (_sT == BSON.BINARY_UUID_OLD) {  
				if (_binL != 16)
					throw new BSONException("Invalid UUID Length in Binary. Expected 16, got " + _binL);
				b.putUUID(name, bytes, false);
			} else if (_sT == BSON.BINARY_MD5) {
				if (_binL != 16)
					throw new BSONException("Invalid MD5 Length in Binary. Expected 16, got " + _binL);
				b.putMD5(name, bytes);
			} else {
				b.putBinary(name, bytes,_sT);
			}
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
			pos.getAndAdd(code.length() + 4 + 1);
			break;
		case BSON.JSCODE_W_SCOPE:
			pos.getAndAdd(4); // we don't really need the length of the whole code scoped block, so skip it
			final String scopedCode = buf.getUTF8String(pos.get());
			pos.getAndAdd(scopedCode.length() + 4 + 1);
			final int _scpL = buf.getInt(pos.get());
			final ByteBuffer _scpBuf = buf.slice();
			_scpBuf.position(pos.get());
			final BSONReader<T> sP = newDocumentParser(_scpBuf);
			final T scope = sP.result();
			b.putScopedCode(name, scopedCode, scope);
			pos.getAndAdd(_scpL);
			break;
		case BSON.SYMBOL:
			final String sym = buf.getUTF8String(pos.get());
			b.putSymbol(name, sym);
			pos.getAndAdd(sym.length() + 4 + 1);
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


	public T result() {
		if (!parsed)
			parse();

		return b.result();
	}

	/**
	 * Because we can't be terribly flexible with generics (such as abstract types)
	 * in Java, and a List Parser is technically a Doc parser, there is no 
	 * declared type for an embedded list. 
	 * 
	 * For now ,this is a bit hacky but override this to parse your list out however you want
	 * and return an Object.  
	 * 
	 * TODO - Make a more elegant solution.
	 * 
	 * @return
	 */
	protected Object parseArray() {
		final ByteBuffer _subLst = buf.slice();
		_subLst.position(pos.get());
		final BSONReader<BSONList> lP = new DefaultBSONArrayParser(_subLst);
		return lP.result();
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
