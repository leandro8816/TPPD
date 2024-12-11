package pt.isec.pd.spring_boot.exemplo3.consumer;

import pt.isec.pd.spring_boot.exemplo3.models.Event;
import pt.isec.pd.spring_boot.exemplo3.utils.LoginResult;
import pt.isec.pd.spring_boot.exemplo3.utils.Utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Scanner;

public class RestClient {


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
        //metodos
    public static boolean register(String registerUri,String name, int studentNumber, String email, String password) throws IOException {

        sendRequestAndShowResponse(registerUri, "POST", null ,   "{\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"id\":\"" + studentNumber + "\"}");
        return true;
    }

    public static LoginResult login(String loginUri , String email, String password) throws IOException {
        LoginResult lg = new LoginResult();
        String credentials = Base64.getEncoder().encodeToString((email + ":" + password).getBytes());
        String token = sendRequestAndShowResponse(loginUri, "POST","basic "+ credentials, null);
        if (token != null) {
            lg.setToken(token);
            lg.setSuccess(true);
        } else {
            lg.setSuccess(false);
        }
        return lg;
    }

    public static String isAdmin(String isAdminUri, String token) throws IOException {
        return sendRequestAndShowResponse(isAdminUri, "GET", "bearer " + token, null);
    }

    public static boolean submitCode(String submitCodeUri, String code,String token) throws IOException {
        String result = sendRequestAndShowResponse(submitCodeUri, "POST", "bearer " + token, code);
        if(result.equals("Codigo não encontrado"))
            return false;
        return true;

    }

    public static String checkAttendanceClient(String getAttendanceClientUri, String token) throws IOException {
        return sendRequestAndShowResponse(getAttendanceClientUri, "POST", "bearer " + token, null);
    }

    public static void createEvent(String addEventUri, String token, Event event) throws IOException {
        sendRequestAndShowResponse(addEventUri, "POST","bearer " + token  ,  "{\"name\":\""+event.getName() +"\",\"place\":\""+ event.getPlace()+"\",\"date\":\""+event.getDate().toString()+"\",\"startingHour\":\"" +event.getStartingHour().toString() + "\",\"finishingHour\":\""+event.getFinishingHour().toString()+"\"}");
    }

    public static void deleteEvent(String deleteEventUri, String token, String nome) throws IOException {
        sendRequestAndShowResponse(deleteEventUri, "DELETE","bearer " + token  ,  nome);
    }

    public static String checkEvents(String getAllEventsUri, String token) throws IOException {
        return sendRequestAndShowResponse(getAllEventsUri, "POST", "bearer " + token, null);

    }

    public static void generateCode(String generateCodeUri, String token, String evento, int duracao) throws IOException {
        sendRequestAndShowResponse(generateCodeUri, "POST", "bearer " + token, "{\"eventName\":\""+evento+"\",\"HoraFinal\":\"00:00:"+duracao+"\"}");
    }

    public static String checkAttendance(String getAttendanceInEventUri, String token, String evento) throws IOException {
        return sendRequestAndShowResponse(getAttendanceInEventUri, "POST", "bearer " + token, "AulaTeste");
    }
}
