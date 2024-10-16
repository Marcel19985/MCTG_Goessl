package handlers;

import server.HttpHeaders;
import server.HttpRequestLine;

import java.io.BufferedWriter;
import java.io.IOException;

public class PutRequestHandler { //Klasse hat bis jetzt noch keinen Nutzen außer Placeholder für API Endpoint

    public void handlePutRequest(HttpRequestLine requestLine, HttpHeaders headers, StringBuilder requestBody, BufferedWriter out) throws IOException {
        if (requestLine.getPath().startsWith("/deck")) {
            createResponseDoesNotExist(out);
        } else if (requestLine.getPath().startsWith("/users")) {
            createResponseDoesNotExist(out);
        }
    }

    public void createResponseDoesNotExist(BufferedWriter out) throws IOException {
        out.write("HTTP/1.1 501 Not Implemented\r\nContent-Type: text/plain\r\n\r\nThis method is not implemented yet.");
        out.flush();
    }

}
