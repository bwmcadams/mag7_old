package net.evilmonkeylabs.mag7.bson;

import net.evilmonkeylabs.mag7.bson.doc.BSONDocumentBuilder;
import net.evilmonkeylabs.mag7.bson.doc.BSONList;
import net.evilmonkeylabs.mag7.bson.doc.Document;
import net.evilmonkeylabs.mag7.bson.io.BSONByteBuffer;
import net.evilmonkeylabs.mag7.bson.types.BSONTimestamp;
import net.evilmonkeylabs.mag7.bson.types.Code;
import net.evilmonkeylabs.mag7.bson.types.CodeWScope;
import net.evilmonkeylabs.mag7.bson.types.ObjectID;

import java.nio.ByteBuffer;
import java.util.Date;

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
        len = buf.getInt();
    }

    public DefaultBSONDocParser parseDocument(final ByteBuffer buf) {
        return new DefaultBSONDocParser(buf);
    }

    public abstract BSONDocumentBuilder<T> newBuilder();

    protected void parse() {
        while (parseEntry());
        parsed = true;
    }

    protected boolean parseEntry() {
        final byte type = buf.get();

        if (type == BSON.EOO)
            return false;

        final String name = buf.getCString();

        switch (type) {
            case BSON.NULL:
            case BSON.UNDEF:
                b.put(name, null);
                break;
            case BSON.DOUBLE:
                b.put(name, buf.getDouble());
                break;
            case BSON.STRING:
                b.put(name, buf.getUTF8String());
                break;
            case BSON.DOCUMENT:
                final BSONReader<T> dP = newDocumentParser(buf.slice());
                final T doc = dP.result();
                b.put(name, doc);
                break;
            case BSON.ARRAY:
                //  TODO - Let user specify custom list builder !!!
                final Object list = parseArray();
                b.put(name, list);
                break;
            case BSON.BINARY:
                // TODO - Break out and parse Binary
                break;
            case BSON.OBJECTID:
                // OIDs are stored as Big Endian
                // TODO - ObjectID Implementation
                b.put(name, new ObjectID( buf.getIntBE(), buf.getIntBE(), buf.getIntBE() ));
                break;
            case BSON.BOOLEAN:
                switch (buf.get()) {
                    case 0x01:
                        b.put(name, true);
                    case 0x00:
                    default: // fall through
                        b.put(name, false);
                        break;
                }
                break;
            case BSON.UTC_DATETIME:
                // TODO - Custom dates
                long tsp = buf.getLong();
                if (!useCustomDate)
                    b.put(name, new Date(tsp));
                break;
            case BSON.REGEX:
                // TODO - Regex Parsing
                final String pattern = buf.getCString();
                final String options = buf.getCString();
                break;
            case BSON.DBREF:
                // TODO - parse.. CString (NS) then OID
                throw new UnsupportedOperationException("DBRef not yet supported");
                //break;
            case BSON.JSCODE:
                b.put(name, new Code(buf.getUTF8String()));
                break;
            case BSON.JSCODE_W_SCOPE:
                final String code = buf.getUTF8String();
                final BSONReader<T> sP = newDocumentParser(buf.slice());
                final T scope = sP.result();
                b.put(name, new CodeWScope<T>(code, scope));
                break;
            case BSON.SYMBOL:
                // TODO - Should we use something other than String for symbols in java?
                b.put(name, buf.getUTF8String());
                // TODO - Need custom hooks for Scala, Clojure
                break;
            case BSON.INT32:
                b.put(name, buf.getInt());
                break;
            case BSON.INT64:
                b.put(name, buf.getLong());
                break;
            case BSON.TIMESTAMP:
                // Special BSON Timestamp for sharding, oplog, etc.
                final int inc = buf.getInt();
                final int time = buf.getInt();
                b.put(name, new BSONTimestamp(time, inc));
                break;
            case BSON.MIN_KEY:
                b.put(name, BSON.MinKey.getInstance());
                break;
            case BSON.MAX_KEY:
                b.put(name, BSON.MaxKey.getInstance());
                break;
            default:
                throw new UnsupportedOperationException("No support for decoding BSON type of byte '" + type + "'");
        }

        return true;
    }

    protected BSONList parseArray() {
        final BSONReader<BSONList> lP = new DefaultBSONArrayParser(buf.slice());
        return lP.result();
    }

    public T result() {
        if (!parsed)
            parse();

        return b.get();
    }

    public abstract BSONReader<T> newDocumentParser(ByteBuffer tBuf);

    protected BSONDocumentBuilder<T> b;
    protected final int len;
    protected final BSONByteBuffer buf;
    protected boolean useCustomDate = false;
    protected boolean parsed = false;
}
