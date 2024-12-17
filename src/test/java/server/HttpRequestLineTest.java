package server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestLineTest {

    @Test
    void testParseValidRequestLine() {
        //gültige HTTP-Anfrage-Zeile:
        String requestLine = "GET /users/test HTTP/1.1";

        //Parse Anfrage-Zeile:
        HttpRequestLine parsedLine = HttpRequestLine.parse(requestLine);

        //Überprüfe Ergebnisse:
        assertEquals("GET", parsedLine.getMethod());
        assertEquals("/users/test", parsedLine.getPath());
        assertEquals("HTTP/1.1", parsedLine.getHttpVersion());
    }

    @Test
    void testGetMethod() {
        HttpRequestLine requestLine = new HttpRequestLine("PUT", "/update/data", "HTTP/1.1");
        assertEquals("PUT", requestLine.getMethod());
    }

    @Test
    void testGetPath() {
        HttpRequestLine requestLine = new HttpRequestLine("DELETE", "/delete/resource", "HTTP/2");
        assertEquals("/delete/resource", requestLine.getPath());
    }

    @Test
    void testGetHttpVersion() {
        HttpRequestLine requestLine = new HttpRequestLine("HEAD", "/check/status", "HTTP/1.1");
        assertEquals("HTTP/1.1", requestLine.getHttpVersion());
    }
}
