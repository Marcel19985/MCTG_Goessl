package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpHeaders {
    private Map<String, String> headers = new HashMap<>();

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public int getContentLength() {
        String contentLength = headers.get("Content-Length");
        return contentLength != null ? Integer.parseInt(contentLength) : 0;
    }

    public static HttpHeaders parse(BufferedReader in) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                headers.addHeader(parts[0].trim(), parts[1].trim());
            }
        }
        return headers;
    }
}
