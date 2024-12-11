package pt.isec.pd.spring_boot.exemplo3.consumer;

import pt.isec.pd.spring_boot.exemplo3.models.UserInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Scanner;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class PdRestApiComSegurancaConsumer {

    public static String sendRequestAndShowResponse(String uri, String verb, String authorizationValue, String body) throws MalformedURLException, IOException {

        String responseBody = null;
        URL url = new URL(uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(verb);
        connection.setRequestProperty("Accept", "application/xml, */*");

        if(authorizationValue!=null) {
            connection.setRequestProperty("Authorization", authorizationValue);
        }

        if(body!=null){
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "Application/Json");
            connection.getOutputStream().write(body.getBytes());
        }

        connection.connect();

        int responseCode = connection.getResponseCode();
        System.out.println("Response code: " +  responseCode + " (" + connection.getResponseMessage() + ")");

        Scanner s;

        if(connection.getErrorStream()!=null) {
            s = new Scanner(connection.getErrorStream()).useDelimiter("\\A");
            responseBody = s.hasNext() ? s.next() : null;
        }

        try {
            s = new Scanner(connection.getInputStream()).useDelimiter("\\A");
            responseBody = s.hasNext() ? s.next() : null;
        } catch (IOException e){}

        connection.disconnect();

        System.out.println(verb + " " + uri + (body==null?"":" with body: "+body) + " ==> " + responseBody);
        System.out.println();

        return responseBody;
    }

    public static void main(String args[]) throws MalformedURLException, IOException {

        String helloUri = "http://localhost:8080/hello/fr?name=Jeanne";
        String helloUri2 = "http://localhost:8080/hello/gr?name=Jeanne";
        String loginUri = "http://localhost:8080/login";
        String loremUri = "http://localhost:8080/lorem";
        String isAdminUri = "http://localhost:8080/list/isAdmin";
        String submitCodeUri = "http://localhost:8080/list/submitCode";
        String registerUri = "http://localhost:8080/register";
        String addEventUri = "http://localhost:8080/list/addEvent";
        String deleteEventUri = "http://localhost:8080/list/deleteEvent";
        String getAllEventsUri= "http://localhost:8080/list/getAllEvents";
        String getAttendanceInEventUri= "http://localhost:8080/list/getAttendanceEvent";
        String getAttendanceClientUri= "http://localhost:8080/list/getAttendanceClient";
        String generateCodeUri= "http://localhost:8080/list/generateCode";

        System.out.println();
        String token=null;
        //Login como cliente normal
        //String credentials = Base64.getEncoder().encodeToString("jose@isec.pt:pass".getBytes());
        //Login como admin
        String credentials = Base64.getEncoder().encodeToString("admin@isec.pt:admin123".getBytes());
        token = sendRequestAndShowResponse(loginUri, "POST","basic "+ credentials, null); //Base64(admin:admin) YWRtaW46YWRtaW4=


        //Ve se é ou não admin
        //sendRequestAndShowResponse(isAdminUri, "GET", "bearer " + token, null);

        //permite adicionar(registar) clientes (sem estar logado)
        //sendRequestAndShowResponse(registerUri, "POST", null,  "{\"name\":\"jose2\",\"email\":\"jose2@isec.pt\",\"password\":\"pass2\",\"id\":\"213341\"}");
        //sendRequestAndShowResponse(registerUri, "POST", null ,  "{\"name\":\"tonico2\",\"email\":\"tni2co@isec.pt\",\"password\":\"a\",\"id\":\"1233241\"}");

        //permite submeter um codigo
        sendRequestAndShowResponse(submitCodeUri, "POST", "bearer " + token, "9987");

        //permite ver as presenças de um cliente
        //sendRequestAndShowResponse(getAttendanceClientUri, "POST", "bearer " + token, null);

        //permite criar um evento //TODO nao consigo mostar os eventos (para vrificar que esta correto), mas funciona
        //sendRequestAndShowResponse(addEventUri, "POST","bearer " + token  ,  "{\"name\":\"AulaTeste4\",\"place\":\"Sala1.3\",\"date\":\"2024-02-12\",\"startingHour\":\"10:00:00\",\"finishingHour\":\"12:00:00\"}");

        //permite apagar um evento
        //sendRequestAndShowResponse(deleteEventUri, "DELETE","bearer " + token  ,  "AAAA");

        //permite consultar as presenças registrada
        //sendRequestAndShowResponse(getAttendanceInEventUri, "POST", "bearer " + token, "AulaTeste");

        //permite consultar os eventos criados
        //sendRequestAndShowResponse(getAllEventsUri, "POST", "bearer " + token, null);

        // permite gerar um codigo de registo de repsenças
        //sendRequestAndShowResponse(generateCodeUri, "POST", "bearer " + token, "{\"eventName\":\"AulaTeste\",\"HoraFinal\":\"00:00:50\"}");


    }
}
