package net.evilmonkeylabs.mag7.bson;

import net.evilmonkeylabs.mag7.bson.doc.BSONDocumentBuilder;
import net.evilmonkeylabs.mag7.bson.doc.Document;
import java.nio.ByteBuffer;

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
// TODO - Make lazy?
public class DefaultBSONDocParser extends BSONReader<Document> {

	/**
	 * Expects the first arg to be a valid document
	 * 
	 * @param _buf
	 */
	DefaultBSONDocParser(final ByteBuffer _buf) {
		super(_buf);
		b = newBuilder();
	}

	@Override
	public BSONDocumentBuilder<Document> newBuilder() {
		return Document.newBuilder();
	}

	@Override
	public BSONReader<Document> newDocumentParser(ByteBuffer tBuf) {
		return new DefaultBSONDocParser(tBuf);
	}

}
