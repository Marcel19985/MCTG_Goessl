package server;

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

    public static HttpRequestLine parse(String requestLine) {
        String[] parts = requestLine.split(" ");
        return new HttpRequestLine(parts[0], parts[1], parts[2]);
    }
}
