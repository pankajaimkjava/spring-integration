/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.eai.message;

/**
 * A simple Message implementation that encapsulates
 * a single Object as its payload.
 *
 * @author Mark Fisher
 */
public class DocumentMessage implements Message {

	private MessageHeader header;

	private Object payload;


	public DocumentMessage(Object id, Object payload) {
		this.header = new MessageHeader(id);
		this.payload = payload;
	}


	public MessageHeader getHeader() {
		return this.header;
	}

	public Object getPayload() {
		return this.payload;
	}

}
