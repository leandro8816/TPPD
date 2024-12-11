package pt.isec.pd.spring_boot.exemplo3.utils;

import java.sql.Date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

public class Utils {
    private static final Scanner scanner = new Scanner(System.in);

    public static String promptUserInput(String prompt) {
        System.out.print(prompt);
        return scanner.next();
    }

    public static int promptUserInputInt(String prompt) {
        System.out.print(prompt);
        return scanner.nextInt();
    }

    public static java.sql.Date stringToDate(String dataString) {
        // Define o formato da data esperado
        SimpleDateFormat formatoData = new SimpleDateFormat("yyyy-MM-dd");

        try {
            // Converte a string para um objeto java.util.Date
            java.util.Date dataUtil = formatoData.parse(dataString);

            // Converte o objeto java.util.Date para java.sql.Date
            return new java.sql.Date(dataUtil.getTime());
        } catch (ParseException e) {
            // Trata a exceção se o formato da data for inválido
            System.err.println("Erro ao converter a data: " + e.getMessage());
            return null;
        }
    }

    public static java.sql.Time stringToTime(String horaString) {
        // Define o formato da hora esperado
        SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

        try {
            // Converte a string para um objeto java.util.Date
            java.util.Date horaUtil = formatoHora.parse(horaString);

            // Converte o objeto java.util.Date para java.sql.Time
            return new java.sql.Time(horaUtil.getTime());
        } catch (ParseException e) {
            // Trata a exceção se o formato da hora for inválido
            System.err.println("Erro ao converter a hora: " + e.getMessage());
            return null;
        }
    }

    public static java.sql.Time calcularHoraFutura(int minutos) {
        // Obtém a hora atual
        Calendar cal = Calendar.getInstance();
        java.util.Date horaAtual = cal.getTime();

        // Adiciona os minutos fornecidos ao tempo atual
        cal.add(Calendar.MINUTE, minutos);

        // Obtém a nova hora após a adição dos minutos
        java.util.Date horaFutura = cal.getTime();

        // Converte a hora futura para java.sql.Time
        return new java.sql.Time(horaFutura.getTime());
    }

    public static boolean isValidDateFormat(String input) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(input);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean isValidTimeFormat(String input){

    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        timeFormat.setLenient(false);
        try

    {
        timeFormat.parse(input);
        return true;
    } catch(
    ParseException e)

    {
        return false;
    }
}
}