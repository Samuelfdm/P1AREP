package edu.escuelaing.app;

import java.net.*;
import java.io.*;

public class FacadeWeb {
    public static void main(String[] args) throws IOException, URISyntaxException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(36000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        boolean running = true;

        while (running) {
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine;
            boolean isFirstLine = true;
            String resourcePath = "";

            while ((inputLine = in.readLine()) != null) {
                if (isFirstLine) {
                    resourcePath = inputLine.split(" ")[1];
                    System.out.println("resourcePath: " + resourcePath);
                    isFirstLine = false;
                }
                System.out.println("Received: " + inputLine);
                if (inputLine.isEmpty()) {
                    break;
                }
            }

            URI requestURI = new URI(resourcePath);

            if (requestURI.getPath().startsWith("/cliente")) {
                outputLine = getClientResponse();
            } else if (requestURI.getPath().startsWith("/consulta")) {
                outputLine = getConsultResponse(requestURI);
            } else {
                outputLine = getErrorPage();
            }

            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    private static String getConsultResponse(URI requestURI) throws IOException {
        String query = requestURI.getQuery();
        if (query == null || !query.startsWith("comando=")) {
            return getErrorJson("Parámetro 'comando' inválido o no presente.");
        }

        String comando = query.split("=")[1];
        String response = FacadeServices.getInstance().getReflectiveChatCommand(comando);

        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n" +
                "\r\n" +
                response;
    }

    public static String getClientResponse() {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "\r\n" +
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                " <head>\n" +
                " <title>Reflective ChatGPT</title>\n" +
                " <meta charset=\"UTF-8\">\n" +
                " <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                " </head>\n" +
                " <body>\n" +
                " <h1>Form with GET</h1>\n" +
                " <form action=\"/consulta\">\n" +
                " <label for=\"comando\">Comando:</label><br>\n" +
                " <input type=\"text\" id=\"comando\" name=\"comando\" placeholder=\"Ej: Class(java.lang.Math)\"><br><br>\n" +
                " <input type=\"button\" value=\"Submit\" onclick=\"loadGetMsg()\">\n" +
                " </form>\n" +
                " <div id=\"getrespmsg\"></div>\n" +
                " <script>\n" +
                " function loadGetMsg() {\n" +
                " let comando = document.getElementById(\"comando\").value;\n" +
                " const xhttp = new XMLHttpRequest();\n" +
                " xhttp.onload = function() {\n" +
                " document.getElementById(\"getrespmsg\").innerHTML =\n" +
                " this.responseText;\n" +
                " }\n" +
                " xhttp.open(\"GET\", \"/consulta?comando=\"+comando);\n" +
                " xhttp.send();\n" +
                " }\n" +
                " </script>\n" +
                " </body>\n" +
                "</html>";
    }

    private static String getErrorPage() {
        return "HTTP/1.1 404 Not Found\n" +
                "Content-Type: text/html\n" +
                "\n" +
                "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<title>Not Found</title>\n" +
                "</head>" +
                "<body>" +
                "<h1>404 Not Found</h1>" +
                "</body>" +
                "</html>";
    }

    private static String getErrorJson(String message) {
        return "HTTP/1.1 400 Bad Request\r\n" +
                "Content-Type: application/json\r\n" +
                "\r\n" +
                "{ \"error\": \"" + message + "\" }";
    }
}