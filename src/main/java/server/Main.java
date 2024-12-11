package server;

//Klassen für Netzwerkkommunikation:
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService; //Thread Pool
import java.util.concurrent.Executors; //Thread Pool

public class Main {
    public static void main(String[] args) {

        ExecutorService threadPool = Executors.newFixedThreadPool(10); //Threadpool mit 10 Threads

        try (ServerSocket serverSocket = new ServerSocket(10001)) {  //erstellt ServerSocket, der auf Port 10001 hört; Klasse ServerSocket hat einen Konstruktur, der den Port 10001 (reservierter Port, sollte verfügbar sein) öffnet
            //Block wird nur ausgeführt wenn Bedingung in try true ist:
            System.out.println("Server started on port 10001, waiting for connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept(); //Sobald Client Verbunden ist, wird Socket Objekt zurück gegeben
                threadPool.submit(() -> {
                    try {
                        HttpServer httpServer = new HttpServer();
                        httpServer.handleClient(clientSocket); //ruft handleClient auf
                    } catch (Exception e) {
                        e.printStackTrace(); //Exception
                    } finally {
                        try {
                            clientSocket.close(); //ClientSocket schließen
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();  //bei Exception wird Fehler ausgegeben
        } finally {
            threadPool.shutdown(); // Schließt den Thread Pool beim Beenden des Programms
        }
    }
}