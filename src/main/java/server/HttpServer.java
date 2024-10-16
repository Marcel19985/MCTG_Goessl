package server;
import handlers.*; //um an requestHandler weiterzuleiten

//für Kommunikation vom und zum Client:
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.Socket;
import java.sql.SQLException;


public class HttpServer {

    //Instanzen der RequestHandler erstellen:
    private final GetRequestHandler getRequestHandler = new GetRequestHandler();
    private final PostRequestHandler postRequestHandler = new PostRequestHandler();
    private final PutRequestHandler putRequestHandler = new PutRequestHandler();
    private final DeleteRequestHandler deleteRequestHandler = new DeleteRequestHandler();

    public void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

            //Parse request line:
            String firstLine = in.readLine();
            HttpRequestLine requestLine = HttpRequestLine.parse(firstLine); //neues Objekt

            //Parse headers:
            HttpHeaders headers = HttpHeaders.parse(in); //neues Objekt

            //RequestBody auslesen:
            int contentLength = headers.getContentLength();
            StringBuilder requestBody = new StringBuilder(); //ist effizeint für String- operationen
            if (contentLength > 0) {
                char[] bodyChars = new char[contentLength];
                in.read(bodyChars, 0, contentLength);
                requestBody.append(bodyChars);
            }

            //API Endpoints:
            if ("POST".equals(requestLine.getMethod())) {
                postRequestHandler.handlePostRequest(requestLine, headers, requestBody, out);
            } else if ("GET".equals(requestLine.getMethod())) {
                getRequestHandler.handleGetRequest(requestLine, headers, requestBody, out);
            } else if ("PUT".equals(requestLine.getMethod())) {
                putRequestHandler.handlePutRequest(requestLine, headers, requestBody, out);
            } else if ("DELETE".equals(requestLine.getMethod())) {
                deleteRequestHandler.handleDeleteRequest(requestLine, headers, requestBody, out);
            } else {
                out.write("HTTP/1.1 405 Method Not Allowed\r\nContent-Type: text/plain\r\n\r\nMethod not allowed.");
                out.flush();
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }


}