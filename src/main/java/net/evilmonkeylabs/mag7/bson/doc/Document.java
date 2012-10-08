package net.evilmonkeylabs.mag7.bson.doc;

import java.util.HashMap;

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

public class Document extends HashMap<String, Object> {

	public static DocumentBuilder newBuilder() {
		return new DocumentBuilder();
	}

	static class DocumentBuilder extends BSONDocumentBuilder<Document> {

		DocumentBuilder() {
			doc = new Document();
		}

		@Override
		public void put(String key, Object value) {
			// TODO - type conversions here?
			doc.put(key, value);
		}

		@Override
		public Document result() {
			return doc;
		}

		private final Document doc;
	}

	private static final long serialVersionUID = -5744321137807600567L;
}
