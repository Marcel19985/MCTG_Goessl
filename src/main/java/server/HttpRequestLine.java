package server;

//erste Zeile der Http Anfrage
public class HttpRequestLine {
    private String method;
    private String path;
    private String httpVersion;

    public HttpRequestLine(String method, String path, String httpVersion) {
        this.method = method;
        this.path = path;
        this.httpVersion = httpVersion;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public static HttpRequestLine parse(String requestLine) { //erstellt aus dem übergebenen String ein Objekt
        String[] parts = requestLine.split(" ");
        return new HttpRequestLine(parts[0], parts[1], parts[2]);
        /*
        parts[0]: Http Methode (GET, POST, PUT, DELETE)
        parts[1] = Pfad (z.B. "/users")
        parts[2] = Http Version
         */
    }
}
