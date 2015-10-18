package com.dm.lib.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class TinyHTTPServer {

	public final String TEST_CONTEXT = "/test";

	private final HttpServer httpServer;
	private final byte[] data;

	public TinyHTTPServer(int port, byte data[]) throws IOException {
		this.data = data;
		httpServer = HttpServer.create(new InetSocketAddress(port), 0);
		httpServer.createContext(TEST_CONTEXT, new HttpRequestHandler());
		httpServer.setExecutor(null);
	}

	public void start() {
		httpServer.start();
		System.out.println("TinyHTTPServer was started...");
	}

	public void stop() {
		httpServer.stop(0);
		System.out.println("TinyHTTPServer was stopped...");
	}

	public class HttpRequestHandler implements HttpHandler {

		private static final int HTTP_OK_STATUS = 200;

		public void handle(HttpExchange httpExchange) throws IOException {
			httpExchange.sendResponseHeaders(HTTP_OK_STATUS, data.length);
			OutputStream os = httpExchange.getResponseBody();
			os.write(data);
			os.close();
		}
	}


}
