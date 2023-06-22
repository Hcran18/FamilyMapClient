package ApplicationLogic;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import Request.LoginRequest;
import Request.RegisterRequest;
import Result.ClearResult;
import Result.EventResult;
import Result.LoginResult;
import Result.PersonResult;
import Result.RegisterResult;

public class ServerProxy {
    String serverHost;
    String serverPort;

    public ServerProxy(String serverHost, String serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public RegisterResult register(RegisterRequest request) {
        System.out.println("trying to connect to the server");
        try {
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/user/register");

            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod("POST");

            http.setDoOutput(true);

            http.connect();

            System.out.println("connected to server");

            Gson gson = new Gson();
            String reqData = gson.toJson(request);

            OutputStream reqBody = http.getOutputStream();

            writeString(reqData, reqBody);

            reqBody.close();

            System.out.println("Response from server received");

            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream respBody = http.getInputStream();

                String respData = readString(respBody);

                RegisterResult result = gson.fromJson(respData, RegisterResult.class);

                return result;
            }
            else {
                System.out.println(http.getResponseMessage());

                InputStream respBody = http.getErrorStream();

                String respData = readString(respBody);

                RegisterResult result = gson.fromJson(respData, RegisterResult.class);

                return result;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public LoginResult login(LoginRequest request) {
        try {
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/user/login");

            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod("POST");

            http.setDoOutput(true);

            http.connect();

            Gson gson = new Gson();
            String reqData = gson.toJson(request);

            OutputStream reqBody = http.getOutputStream();

            writeString(reqData, reqBody);

            reqBody.close();

            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream respBody = http.getInputStream();

                String respData = readString(respBody);

                LoginResult result = gson.fromJson(respData, LoginResult.class);

                return result;
            }
            else {
                System.out.println(http.getResponseMessage());

                InputStream respBody = http.getErrorStream();

                String respData = readString(respBody);

                LoginResult result = gson.fromJson(respData, LoginResult.class);

                return result;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PersonResult getPeople(String authtoken) {
        System.out.println("Attempting to connect to getPeople");
        try {
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/person");

            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod("GET");

            http.setDoOutput(false);

            System.out.println("Authtoken to be sent: " + authtoken);

            http.addRequestProperty("Authorization", authtoken);

            http.connect();

            System.out.println("Connected to getPeople");
            System.out.println("Response code in get people: " + http.getResponseCode());

            Gson gson = new Gson();
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream respBody = http.getInputStream();

                String respData = readString(respBody);

                PersonResult result = gson.fromJson(respData, PersonResult.class);

                System.out.println("People got");

                return result;
            }
            else {
                System.out.println("Unable to get people");

                InputStream respBody = http.getErrorStream();

                String respData = readString(respBody);

                PersonResult result = gson.fromJson(respData, PersonResult.class);

                return result;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public EventResult getEvents(String authtoken) {
        try {
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/event");

            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod("GET");

            http.setDoOutput(false);

            http.addRequestProperty("Authorization", authtoken);

            http.connect();

            Gson gson = new Gson();
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream respBody = http.getInputStream();

                String respData = readString(respBody);

                EventResult result = gson.fromJson(respData, EventResult.class);

                return result;
            }
            else {
                InputStream respBody = http.getErrorStream();

                String respData = readString(respBody);

                EventResult result = gson.fromJson(respData, EventResult.class);

                return result;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //For test cases
    public ClearResult clear() {
        try {
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/clear");

            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod("POST");

            http.setDoOutput(false);

            http.connect();

            Gson gson = new Gson();
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream respBody = http.getInputStream();

                String respData = readString(respBody);

                ClearResult result = gson.fromJson(respData, ClearResult.class);

                return result;
            }
            else {
                InputStream respBody = http.getErrorStream();

                String respData = readString(respBody);

                ClearResult result = gson.fromJson(respData, ClearResult.class);

                return result;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String readString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader sr = new InputStreamReader(is);
        char[] buf = new char[1024];
        int len;
        while ((len = sr.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }
        return sb.toString();
    }

    private void writeString(String str, OutputStream os) throws IOException {
        OutputStreamWriter sw = new OutputStreamWriter(os);
        sw.write(str);
        sw.flush();
    }
}
