package edu.escuelaing.app;

import java.io.*;
import java.lang.reflect.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.JsonObject;

public class ReflectiveChatGPT {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(45000);
            System.out.println("Reflective Chat listo para recibir en el puerto 45000...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String requestLine = in.readLine();
            if (requestLine == null || !requestLine.startsWith("GET")) {
                return;
            }

            System.out.println("Solicitud recibida: " + requestLine);

            String requestStringURI = requestLine.split(" ")[1];
            URI requestURI = new URI(requestStringURI);
            String query = requestURI.getQuery();

            JsonObject response;
            if (query != null && query.startsWith("comando=")) {
                String comando = query.split("=")[1];
                response = procesarComando(comando);
            } else {
                response = new JsonObject();
                response.addProperty("error", "Formato de comando inválido");
            }

            String jsonResponse = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: application/json\r\n" +
                    "\r\n" +
                    response.toString();

            out.println(jsonResponse);
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JsonObject procesarComando(String comando) {
        try {
            String reflectiveMethod = comando.split("\\(")[0];
            String params = comando.split("\\(")[1].split("\\)")[0];
            String[] paramsArray = params.split(",");

            Class<?> clazz = Class.forName(paramsArray[0]);
            String methodName = (paramsArray.length > 1) ? paramsArray[1] : "";

            switch (reflectiveMethod) {
                case "Class":
                    return getDeclaredFieldsAndMethods(clazz);
                case "invoke":
                    return invocarMetodo(clazz, methodName);
                case "unaryInvoke":
                    return invocarMetodoUnario(clazz, methodName, paramsArray[2], paramsArray[3]);
                case "binaryInvoke":
                    return invocarMetodoBinario(clazz, methodName, paramsArray[2], paramsArray[3], paramsArray[4], paramsArray[5]);
                default:
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("error", "Comando no reconocido");
                    return errorResponse;
            }
        } catch (Exception e) {
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Error al procesar el comando: " + e.getMessage());
            return errorResponse;
        }
    }

    private JsonObject getDeclaredFieldsAndMethods(Class<?> clazz) {
        JsonObject response = new JsonObject();
        response.addProperty("class", clazz.getName());

        List<String> methodsList = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            methodsList.add(method.getName());
        }
        response.addProperty("methods", methodsList.toString());

        List<String> fieldsList = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            fieldsList.add(field.getName());
        }
        response.addProperty("fields", fieldsList.toString());

        return response;
    }

    private JsonObject invocarMetodo(Class<?> clazz, String methodName) {
        try {
            Method method = clazz.getDeclaredMethod(methodName);
            Object result = method.invoke(null);
            JsonObject response = new JsonObject();
            response.addProperty("result", result.toString());
            return response;
        } catch (Exception e) {
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Error al invocar método sin parámetros: " + e.getMessage());
            return errorResponse;
        }
    }

    private JsonObject invocarMetodoUnario(Class<?> clazz, String methodName, String paramType, String paramValue) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, getPrimitiveClass(paramType));
            Object param = parseValue(paramType, paramValue);
            Object result = method.invoke(null, param);
            JsonObject response = new JsonObject();
            response.addProperty("result", result.toString());
            return response;
        } catch (Exception e) {
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Error al invocar método unario: " + e.getMessage());
            return errorResponse;
        }
    }

    private JsonObject invocarMetodoBinario(Class<?> clazz, String methodName, String paramType1, String paramValue1, String paramType2, String paramValue2) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, getPrimitiveClass(paramType1), getPrimitiveClass(paramType2));
            Object param1 = parseValue(paramType1, paramValue1);
            Object param2 = parseValue(paramType2, paramValue2);
            Object result = method.invoke(null, param1, param2);
            JsonObject response = new JsonObject();
            response.addProperty("result", result.toString());
            return response;
        } catch (Exception e) {
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Error al invocar método binario: " + e.getMessage());
            return errorResponse;
        }
    }

    private Class<?> getPrimitiveClass(String type) {
        switch (type) {
            case "int": return int.class;
            case "double": return double.class;
            case "String": return String.class;
            default: return null;
        }
    }

    private Object parseValue(String type, String value) {
        switch (type) {
            case "int": return Integer.parseInt(value);
            case "double": return Double.parseDouble(value);
            case "String": return value;
            default: return null;
        }
    }
}
