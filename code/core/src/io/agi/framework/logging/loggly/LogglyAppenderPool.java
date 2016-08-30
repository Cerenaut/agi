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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;

public class LogglyAppenderPool extends Thread {

	protected static final Charset UTF_8 = Charset.forName("UTF-8");

	private final String event;
	private final String endpointUrl;
	private final Proxy proxy;
	private final String contentType;

	public LogglyAppenderPool(final String event, final String endpointUrl, final Proxy proxy,
			final String contentType) {

		this.event = event;
		this.endpointUrl = endpointUrl;
		this.proxy = proxy;
		this.contentType = contentType;
	}

	@Override
	public void run() {
		try {
			assert endpointUrl != null;
			final URL endpoint = new URL(endpointUrl);
			final HttpURLConnection connection;
			if (proxy == null) {
				connection = (HttpURLConnection) endpoint.openConnection();
			} else {
				connection = (HttpURLConnection) endpoint.openConnection(proxy);
			}

			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.addRequestProperty("Content-Type", contentType);
			connection.connect();
			sendAndClose(event, connection.getOutputStream());
			connection.disconnect();
			final int responseCode = connection.getResponseCode();
			if (responseCode != 200) {
				final String message = readResponseBody(connection.getInputStream());
				System.out.println("Loggly post failed (HTTP " + responseCode + ").  Response body:\n" + message);
			}
		} catch (final IOException e) {
			System.out.println("IOException while attempting to communicate with Loggly:\n " + e.getMessage());
		}
	}

	protected String readResponseBody(final InputStream input) throws IOException {
		try {
			final byte[] bytes = toBytes(input);
			return new String(bytes, UTF_8);
		} finally {
			input.close();
		}
	}

	protected byte[] toBytes(final InputStream is) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int count;
		final byte[] buf = new byte[512];

		while ((count = is.read(buf, 0, buf.length)) != -1) {
			baos.write(buf, 0, count);
		}
		baos.flush();

		return baos.toByteArray();
	}

	private void sendAndClose(final String event, final OutputStream output) throws IOException {
		try {
			output.write(event.getBytes(UTF_8));
		} finally {
			output.close();
		}
	}
}
