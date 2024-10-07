package server;

//Klassen für Netzwerkkommunikation:
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(10001)) {  //erstellt ServerSocket, der auf Port 10001 hört; Klasse ServerSocket hat einen Konstruktur, der den Port 10001 (reservierter Port, sollte verfügbar sein) öffnet
            //Block wird nur ausgeführt wenn Bedingung in try true ist:
            System.out.println("Server started on port 10001, waiting for connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept(); //Sobald Client Verbunden ist, wird Socket Objekt zurück gegeben
                HttpServer.handleClient(clientSocket); // Ruft handleClient in HttpServer Klasse auf
            }

        } catch (Exception e) {
            e.printStackTrace();  //bei Exception wird Fehler ausgegeben
        }
    }
}
//Interface kommt später noch eine gute Gelegenheit
//Beim Registrierung, Anmeldung etc. könnte man aus dem Objekt ein JSON erstellen und wieder an den CLient zurückgeben