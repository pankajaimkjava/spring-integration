/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.integration.dispatcher;

import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.message.Message;
import org.springframework.integration.message.MessageTarget;

/**
 * A broadcasting dispatcher implementation. It makes a best effort to
 * send the message to each of its targets. If it fails to send to any
 * one target, it will log a warn-level message but continue to send
 * to the other targets.
 * 
 * @author Mark Fisher
 */
public class BroadcastingDispatcher extends AbstractDispatcher {

	public boolean send(final Message<?> message) {
		for (final MessageTarget target : this.targets) {
			TaskExecutor executor = this.getTaskExecutor();
			if (executor != null) {
				executor.execute(new Runnable() {
					public void run() {
						sendMessageToTarget(message, target);
					}
				});
			}
			else {
				this.sendMessageToTarget(message, target);
			}
		}
		return true;
	}

}
