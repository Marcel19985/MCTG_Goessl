package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpHeaders {
    private Map<String, String> headers = new HashMap<>(); //headers werden als Map gespeichert: key value pairs

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getHeader(String key) { //mit key kann value des headers abgefragt werden
        return headers.get(key);
    }

    public int getContentLength() {
        String contentLength = headers.get("Content-Length");
        return contentLength != null ? Integer.parseInt(contentLength) : 0; //wenn contentLength != 0 -> return length -> ansonsten 0
    }

    public static HttpHeaders parse(BufferedReader in) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        String line;
        while (!(line = in.readLine()).isEmpty()) { //parse nicht leere Zeilen (leere Zeile trennt headers vom body):
            String[] parts = line.split(":", 2);
            if (parts.length == 2) { //stellt sicher, dass Zeile in 2 Teile geteilt wurde
                headers.addHeader(parts[0].trim(), parts[1].trim()); //parts[0] = header Name; parts[1] = Wert des headers; trim() entfernt Leerzeiochen
            }
        }
        return headers; //gib Objekt HttpHeaders zurück
    }
}
