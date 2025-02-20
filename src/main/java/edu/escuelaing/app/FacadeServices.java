package edu.escuelaing.app;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FacadeServices {
    private static FacadeServices instance;
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String BACKEND_URL = "http://localhost:45000/compreflex?comando=";

    private FacadeServices() {}

    public static FacadeServices getInstance() {
        if (instance == null) {
            instance = new FacadeServices();
        }
        return instance;
    }

    public String getReflectiveChatCommand(String comando) throws IOException {
        try{
            URL obj = new URL(BACKEND_URL + comando);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            //The following invocation perform the connection implicitly before getting the code
            int responseCode = con.getResponseCode();
            System.out.println("GET Response Code :: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuffer response = new StringBuffer();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                // print result
                System.out.println(response.toString());
                // Parseamos la respuesta a JSON antes de devolverla
                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                return jsonResponse.toString();
            } else {
                return createErrorJson("Error en la comunicación con el backend. Código de respuesta: " + responseCode);
            }
        } catch (IOException e) {
            return createErrorJson("Error al conectar con el backend: " + e.getMessage());
        }
    }

    private String createErrorJson(String message) {
        JsonObject errorResponse = new JsonObject();
        errorResponse.addProperty("error", message);
        return errorResponse.toString();
    }
}