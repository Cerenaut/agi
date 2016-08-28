/*
 * Copyright (c) 2016.
 *
 * This file is part of Project AGI. <http://agi.io>
 *
 * Project AGI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Project AGI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Project AGI.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.agi.framework.logging.loggly;

import java.io.Serializable;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;


@Plugin(name = "LogglyAppender", category = "Core", elementType = "appender", printObject = true)
public class Log4jLogglyAppender extends AbstractAppender {

	String endpointUrl;
	LogglyThreadPool logglyThreadPool = new LogglyThreadPool(50);;

	public Log4jLogglyAppender(final String name, final Filter filter, final Layout<? extends Serializable> layout,
			final String endpointUrl, final boolean ignoreExceptions) {
		super(name, filter, layout, ignoreExceptions);
		this.endpointUrl = endpointUrl;
	}

	@Override
	public void append(final LogEvent event) {
		final String msg = new String(getLayout().toByteArray(event));
		final String contentType = getLayout().getContentType();
		final LogglyAppenderPool task = new LogglyAppenderPool(msg, endpointUrl, null, contentType);
		logglyThreadPool.addTask(task);

	}

	public void setEndpointUrl(final String endpointUrl) {
		this.endpointUrl = endpointUrl;
	}

	@PluginFactory
	public static Log4jLogglyAppender createAppender(@PluginAttribute("name") final String name,
			@PluginElement("Layout") Layout<? extends Serializable> layout,
			@PluginElement("Filter") final Filter filter, @PluginAttribute("endpointUrl") final String endpointUrl) {
		if (name == null) {
			LOGGER.error("No name provided for LogglyAppender");
			return null;
		}
		if (layout == null) {
			layout = PatternLayout.createDefaultLayout();
		}
		return new Log4jLogglyAppender(name, filter, layout, endpointUrl, true);
	}
}
