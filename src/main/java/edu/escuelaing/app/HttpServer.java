package edu.escuelaing.app;

import java.net.*;
import java.io.*;

public class HttpServer {
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
                //outputLine = getClientResponse(requestURI);
                outputLine = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: text/html\r\n"
                        + "\r\n"
                        + "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        " <head>\n" +
                        " <title>Form Example</title>\n" +
                        " <meta charset=\"UTF-8\">\n" +
                        " <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                        " </head>\n" +
                        " <body>\n" +
                        " <h1>Form with GET</h1>\n" +
                        " <form action=\"/hello\">\n" +
                        " <label for=\"name\">Name:</label><br>\n" +
                        " <input type=\"text\" id=\"name\" name=\"name\" value=\"John\"><br><br>\n" +
                        " <input type=\"button\" value=\"Submit\" onclick=\"loadGetMsg()\">\n" +
                        " </form>\n" +
                        " <div id=\"getrespmsg\"></div>\n" +
                        " <script>\n" +
                        " function loadGetMsg() {\n" +
                        " let nameVar = document.getElementById(\"name\").value;\n" +
                        " const xhttp = new XMLHttpRequest();\n" +
                        " xhttp.onload = function() {\n" +
                        " document.getElementById(\"getrespmsg\").innerHTML =\n" +
                        " this.responseText;\n" +
                        " }\n" +
                        " xhttp.open(\"GET\", \"/consulta?name=\"+nameVar);\n" +
                        " xhttp.send();\n" +
                        " }\n" +
                        " </script>\n" +
                        " <h1>Form with POST</h1>\n" +
                        " <form action=\"/hellopost\">\n" +
                        " <label for=\"postname\">Name:</label><br>\n" +
                        " <input type=\"text\" id=\"postname\" name=\"name\" value=\"John\"><br><br>\n" +
                        " <input type=\"button\" value=\"Submit\" onclick=\"loadPostMsg(postname)\">\n" +
                        " </form>\n" +
                        "\n" +
                        " <div id=\"postrespmsg\"></div>\n" +
                        "\n" +
                        " <script>\n" +
                        " function loadPostMsg(name){\n" +
                        " let url = \"/consulta?name=\" + name.value;\n" +
                        " fetch (url, {method: 'POST'})\n" +
                        " .then(x => x.text())\n" +
                        " .then(y => document.getElementById(\"postrespmsg\").innerHTML = y);\n" +
                        " }\n" +
                        " </script>\n" +
                        " </body>\n" +
                        "</html>";
            } else {
                outputLine = "HTTP/1.1 404 Not Found\n" +
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

            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }
}