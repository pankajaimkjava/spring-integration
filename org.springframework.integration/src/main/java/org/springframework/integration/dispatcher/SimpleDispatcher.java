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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.integration.channel.DispatcherPolicy;
import org.springframework.integration.handler.MessageHandlerNotRunningException;
import org.springframework.integration.handler.MessageHandlerRejectedExecutionException;
import org.springframework.integration.message.Message;
import org.springframework.integration.message.MessageDeliveryException;
import org.springframework.integration.message.MessageTarget;

/**
 * Basic implementation of {@link MessageDispatcher}.
 * 
 * @author Mark Fisher
 */
public class SimpleDispatcher extends AbstractDispatcher {

	protected final DispatcherPolicy dispatcherPolicy;


	public SimpleDispatcher(DispatcherPolicy dispatcherPolicy) {
		this.dispatcherPolicy = (dispatcherPolicy != null) ? dispatcherPolicy : new DispatcherPolicy();
	}

	public boolean send(Message<?> message) {
		int attempts = 0;
		List<MessageTarget> targetList = new ArrayList<MessageTarget>(this.targets);
		while (attempts < this.dispatcherPolicy.getRejectionLimit()) {
			if (attempts > 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("target(s) rejected message after " + attempts +
							" attempt(s), will try again after 'retryInterval' of " +
							this.dispatcherPolicy.getRetryInterval() + " milliseconds");
				}
				try {
					Thread.sleep(this.dispatcherPolicy.getRetryInterval());
				}
				catch (InterruptedException iex) {
					Thread.currentThread().interrupt();
					return false;
				}
			}
			Iterator<MessageTarget> iter = targetList.iterator();
			if (!iter.hasNext()) {
				if (logger.isWarnEnabled()) {
					logger.warn("no active targets");
				}
				return false;
			}
			boolean rejected = false;
			while (iter.hasNext()) {
				MessageTarget target = iter.next();
				try {
					boolean sent = this.sendMessageToTarget(message, target);
					if (!this.dispatcherPolicy.isPublishSubscribe() && sent) {
						return true;
					}
					if (!sent && logger.isDebugEnabled()) {
						logger.debug("target rejected message, continuing with other targets if available");
					}
					iter.remove();
				}
				catch (MessageHandlerNotRunningException e) {
					if (logger.isDebugEnabled()) {
						logger.debug("target is not running, continuing with other targets if available", e);
					}
				}
				catch (MessageHandlerRejectedExecutionException e) {
					rejected = true;
					if (logger.isDebugEnabled()) {
						logger.debug("target '" + target + "' is busy, continuing with other targets if available", e);
					}
				}
			}
			if (!rejected) {
				return true;
			}
			attempts++;
		}
		if (this.dispatcherPolicy.getShouldFailOnRejectionLimit()) {
			throw new MessageDeliveryException(message, "Dispatcher reached rejection limit of "
					+ this.dispatcherPolicy.getRejectionLimit()
					+ ". Consider increasing the target's concurrency and/or "
					+ "the dispatcherPolicy's 'rejectionLimit'.");
		}
		return false;
	}

}
