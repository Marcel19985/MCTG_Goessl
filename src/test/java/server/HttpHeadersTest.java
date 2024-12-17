package server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class HttpHeadersTest {

    private HttpHeaders httpHeaders;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
    }

    @Test
    void testAddAndGetHeader() {
        //2 Header hinzufügen:
        httpHeaders.addHeader("Content-Type", "application/json");
        httpHeaders.addHeader("Authorization", "Bearer testToken");

        //Überprüfen, ob die 2 Header korrekt gespeichert wurden:
        assertEquals("application/json", httpHeaders.getHeader("Content-Type"));
        assertEquals("Bearer testToken", httpHeaders.getHeader("Authorization"));
    }

    @Test
    void testGetNonExistentHeader() { //Header abrufen, der nicht existiert
        assertNull(httpHeaders.getHeader("Non-Existent-Header"));
    }

    @Test
    void testGetContentLength() { //Content-Length-Header hinzufügen und prüfen
        httpHeaders.addHeader("Content-Length", "1234");

        assertEquals(1234, httpHeaders.getContentLength());
    }

    @Test
    void testParseHeaders() throws IOException {
        //HTTP-Header-Input:
        String rawHeaders = "Content-Type: application/json\r\n" +
                "Content-Length: 5678\r\n" +
                "Authorization: Bearer testToken\r\n" +
                "\r\n";

        BufferedReader reader = new BufferedReader(new StringReader(rawHeaders));

        //HttpHeaders parsen:
        HttpHeaders parsedHeaders = HttpHeaders.parse(reader);

        //Überprüfen, ob die Header korrekt geparst wurden:
        assertEquals("application/json", parsedHeaders.getHeader("Content-Type"));
        assertEquals("Bearer testToken", parsedHeaders.getHeader("Authorization"));
        assertEquals(5678, parsedHeaders.getContentLength());
    }
}
