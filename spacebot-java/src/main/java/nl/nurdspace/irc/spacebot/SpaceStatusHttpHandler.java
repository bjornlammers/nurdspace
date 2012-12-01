package nl.nurdspace.irc.spacebot;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public final class SpaceStatusHttpHandler implements HttpHandler {
	
	@Override
	public void handle(final HttpExchange exchange) throws IOException {
		String response = SpaceStatus.getInstance().asJSON().toJSONString();
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.toString().getBytes());
        exchange.close();
	}
}
