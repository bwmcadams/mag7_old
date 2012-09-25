package net.evilmonkeylabs.mag7.bson;

import net.evilmonkeylabs.mag7.bson.doc.BSONDocumentBuilder;
import net.evilmonkeylabs.mag7.bson.doc.Document;
import net.evilmonkeylabs.mag7.bson.io.BSONByteBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
// TODO - Make lazy?
public class DefaultBSONParser extends BSONReader<Document> {
    /**
     * Expects the first arg to be a valid document
     * @param _buf
     */
    DefaultBSONParser(final ByteBuffer _buf) {
        buf = new BSONByteBuffer(_buf);
        len = buf.getInt();
        b = newBuilder();
        parse();
    }

    protected void parse() {
        while (parseEntry()) {

        }
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




        }
    }

    public Document result() {
        return b.get();
    }

    @Override
    public BSONDocumentBuilder<Document> newBuilder() {
        return Document.newBuilder();
    }


    protected final BSONDocumentBuilder<Document> b;
    protected final int len;
    protected final BSONByteBuffer buf;
}
