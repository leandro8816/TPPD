package pt.isec.pd.spring_boot.exemplo3.server;



import pt.isec.pd.spring_boot.exemplo3.client.ClientInfo;
import pt.isec.pd.spring_boot.exemplo3.utils.CodigoRegisto;
import pt.isec.pd.spring_boot.exemplo3.models.Event;
import pt.isec.pd.spring_boot.exemplo3.utils.Utils;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ManageDB {
    String dbUrl;
    private Connection conn;
    HeartBeatSender heartBeatSender;

    public ManageDB(String dbAddress, String dbName,HeartBeatSender heartBeatSender) {
        this.dbUrl = "jdbc:sqlite:" + dbAddress + "/" + dbName;
        this.heartBeatSender = heartBeatSender;
        this.connect();
        this.createTable();
        inserirVersao("pd",0);
        insertUtl("admin",0,"admin@isec.pt","admin123");
        System.out.println("VERSAo " +obterVersaoPorNome("pd"));
        displayAllCodigosRegisto();
        getAllUsers();
        displayAllEvents();
    }
    public ManageDB() {
        this.dbUrl = "jdbc:sqlite:" + "src" + "/" + "database";
        this.connect();
        this.createTable();
        heartBeatSender = new HeartBeatSender();
        inserirVersao("pd",0);
        insertUtl("admin",0,"admin@isec.pt","admin123");
        System.out.println("VERSAo " +obterVersaoPorNome("pd"));
        displayAllCodigosRegisto();
        getAllUsers();
        insertEvent("AAAA","ca", Utils.stringToDate( "1000-09-09"),Utils.stringToTime("12:00:00"),Utils.stringToTime("14:00:00"));
    }

    public String getAllUsers() {
        String selectQuery = "SELECT * FROM Utilizador";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(selectQuery);
            StringBuilder results = new StringBuilder();
            while (rs.next()) {
                results.append("UTILIZADOR '" + rs.getString("Nome") + "' --> Nome: " +
                        rs.getString("Numero_Estudante") +
                        ", Email: " + rs.getString("Email") + ", Pass_word: " + rs.getString("Pass_word") + "\n");
            }
            System.out.println(results);
            return results.toString();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return "";
        }
    }

    public MSG checkPass(String userEmail, String password) {

        String selectQuery = "SELECT Pass_word FROM Utilizador WHERE Email = ?";

        try {
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            pstmt.setString(1, userEmail);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                if (!Objects.equals(rs.getString("Pass_word"), password)) {
                    return MSG.WRONG_PASSWORD;
                } else {
                    return MSG.RIGHT_PASSWORD;
                }
            } else {
                return MSG.NO_REGISTERED_USER;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public void clearDb() {
        String deleteQuery = "DELETE * FROM users";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(deleteQuery);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void connect() {
        try {
            this.conn = DriverManager.getConnection(this.dbUrl);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            this.closeConnection();
        }
    }

    private void createTable() {
        String createQueryUtilizador =
                "CREATE TABLE IF NOT EXISTS Utilizador (" +
                        "Nome VARCHAR(255) NOT NULL ," +
                        "Numero_Estudante INT NOT NULL," +
                        "Email VARCHAR(255) NOT NULL PRIMARY KEY," +
                        "Pass_word VARCHAR(255) NOT NULL" +
                        ");";

        String createQueryEventos =
                "CREATE TABLE IF NOT EXISTS Evento (" +
                        "Nome_Evento VARCHAR(255) NOT NULL PRIMARY KEY," +
                        "Local VARCHAR(255) NOT NULL," +
                        "Data DATE NOT NULL," +
                        "Hora_Inicio TIME NOT NULL," +
                        "Hora_Fim TIME NOT NULL" +
                        ");";

        String createQueryCodigosRegisto =
                "CREATE TABLE IF NOT EXISTS Codigos_Registo ("
                        + "Codigo INT NOT NULL PRIMARY KEY,"
                        + "Hora_Final TIME NOT NULL,"
                        + "Nome_Evento VARCHAR(255),"
                        + "FOREIGN KEY (Nome_Evento) REFERENCES Evento(Nome_Evento)"
                        + ")";

        String createQueryAssiste =
                "CREATE TABLE IF NOT EXISTS Assiste (\n" +
                        "ID_Assiste INT NOT NULL PRIMARY KEY ,\n" +
                        "Email_Utilizador VARCHAR(255) NOT NULL,\n" +
                        "Nome_Evento VARCHAR(255) NOT NULL,\n" +
                        "FOREIGN KEY (Email_Utilizador) REFERENCES Utilizador(Email),\n" +
                        "FOREIGN KEY (Nome_Evento) REFERENCES Evento(Nome_Evento)\n" +
                        ");";


        String createQueryVersion =
                "CREATE TABLE IF NOT EXISTS Versao (\n" +
                        "    Nome_Aplicacao VARCHAR(255) PRIMARY KEY,\n" +
                        "    Numero_Versao INT NOT NULL\n" +
                        ");";
        try {
            Statement stmt = this.conn.createStatement();
            stmt.execute(createQueryEventos);
            stmt.execute(createQueryCodigosRegisto);
            stmt.execute(createQueryUtilizador);
            stmt.execute(createQueryAssiste);
            stmt.execute(createQueryVersion);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean insertUtl(String nome, int numeroEstudante, String email, String palavraChave) {
        if (!utilizadorExists(email)) {
            String insertQuery = "INSERT INTO Utilizador (Nome, Numero_Estudante, Email, Pass_word) VALUES (?, ?, ?, ?)";

            try {
                PreparedStatement pstmt = conn.prepareStatement(insertQuery);
                pstmt.setString(1, nome);
                pstmt.setInt(2, numeroEstudante);
                pstmt.setString(3, email);
                pstmt.setString(4, palavraChave);
                pstmt.executeUpdate();
                System.out.println("Utilizador inserido com sucesso!");
                incrementarVersao("pd");
                return true;
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } else {
            //System.out.println("Utilizador com o email fornecido já existe. Inserção cancelada.");
        }
        return false;
    }



    public void displayAllCodigosRegisto() {
        String selectQuery = "SELECT * FROM Codigos_Registo";

        try {
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int codigo = rs.getInt("Codigo");
                String nomeEvento = rs.getString("Nome_Evento");
                // Assuming Hora_Final is a TIME data type
                java.sql.Time horaFinal = rs.getTime("Hora_Final");

                System.out.println("Código: " + codigo);
                System.out.println("Nome do Evento: " + nomeEvento);
                System.out.println("Hora Final: " + horaFinal);
                System.out.println("---------------");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao mostrar códigos de registro: " + e.getMessage());
        }
    }

    public void editCodigoRegisto(int codigo, Time novaHoraFinal, String novoNomeEvento) {
        if (codeExists(codigo)) {
            String updateQuery = "UPDATE Codigos_Registo SET Hora_Final = ?, Nome_Evento = ? WHERE Codigo = ?";

            try {
                PreparedStatement pstmt = conn.prepareStatement(updateQuery);
                pstmt.setTime(1, novaHoraFinal);
                pstmt.setString(2, novoNomeEvento);
                pstmt.setInt(3, codigo);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Informações do código de registro atualizadas com sucesso!");
                } else {
                    System.out.println("Nenhum código de registro atualizado. Verifique o código fornecido.");
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Código de registro não encontrado. Edição cancelada.");
        }
    }

    private int generateUniqueRandomCode() {

        int randomCode = generateRandomCode();


        while (codeExists(randomCode)) {
            randomCode = generateRandomCode();
        }

        return randomCode;
    }

    private int generateRandomCode() {

        return (int) (Math.random() * 9000) + 1000;
    }

    public boolean codeExists(int code) {
        String selectQuery = "SELECT COUNT(*) AS count FROM Codigos_Registo WHERE Codigo = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            pstmt.setInt(1, code);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                return count > 0;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }



    public boolean codeExistsInTime(int code) {
        String selectQuery = "SELECT COUNT(*) AS count, Hora_Final FROM Codigos_Registo WHERE Codigo = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            pstmt.setInt(1, code);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                Time horaFinal = rs.getTime("Hora_Final");

                if (count > 0 && isCurrentTimeBefore(horaFinal)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    private boolean isCurrentTimeBefore(Time horaFinal) {
        LocalTime currentTime = LocalTime.now();
        LocalTime finalTime = horaFinal.toLocalTime();

        return currentTime.isBefore(finalTime);
    }

    public String insertCodigoRegisto(CodigoRegisto codigoRegisto) {
        int existingCode = getCodeByEventName(codigoRegisto.getEventName());

        if (existingCode != -1) {

            return updateCodigoRegisto(existingCode, generateUniqueRandomCode(), codigoRegisto.getHoraFinal(), codigoRegisto.getEventName());
        } else {

            int randomCode = generateUniqueRandomCode();
            String insertQuery = "INSERT INTO Codigos_Registo (Codigo, Hora_Final, Nome_Evento) VALUES (?, ?, ?)";

            try {
                PreparedStatement pstmt = conn.prepareStatement(insertQuery);
                pstmt.setInt(1, randomCode);
                pstmt.setTime(2, codigoRegisto.getHoraFinal());
                pstmt.setString(3, codigoRegisto.getEventName());

                pstmt.executeUpdate();
                incrementarVersao("pd");
                return ("Código de registro <"+ randomCode +"> inserido com sucesso!");

            } catch (SQLException e) {
                System.out.println("Erro ao inserir código de registro: " + e.getMessage());
            }
        }
        return null;
    }

    private String updateCodigoRegisto(int codigoAtual, int novoCodigo, Time novaHoraFinal, String novoNomeEvento) {
        if (codeExists(codigoAtual)) {
            String updateQuery = "UPDATE Codigos_Registo SET Codigo = ?, Hora_Final = ?, Nome_Evento = ? WHERE Codigo = ?";

            try {
                PreparedStatement pstmt = conn.prepareStatement(updateQuery);
                pstmt.setInt(1, novoCodigo);
                pstmt.setTime(2, novaHoraFinal);
                pstmt.setString(3, novoNomeEvento);
                pstmt.setInt(4, codigoAtual);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    incrementarVersao("pd");
                    return ("Código de registro atualizado com sucesso!");
                } else {
                    return ("Nenhum código de registro atualizado. Verifique o código fornecido.");
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } else {
           return ("Código de registro não encontrado. Atualização cancelada.");
        }
        return  null;
    }

    private int getCodeByEventName(String eventName) {
        String selectQuery = "SELECT Codigo FROM Codigos_Registo WHERE Nome_Evento = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            pstmt.setString(1, eventName);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("Codigo");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    public String getEventNameByCode(int code) {
        String selectQuery = "SELECT Nome_Evento FROM Codigos_Registo WHERE Codigo = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            pstmt.setInt(1, code);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("Nome_Evento");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private boolean utilizadorExists(String userEmail) {
        String selectQuery = "SELECT COUNT(*) AS count FROM Utilizador WHERE Email = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            pstmt.setString(1, userEmail);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                return count > 0;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    private boolean eventoExists(String eventName) {
        String selectQuery = "SELECT COUNT(*) AS count FROM Evento WHERE Nome_Evento = ?";

        try (PreparedStatement preparedStatement = conn.prepareStatement(selectQuery)) {
            preparedStatement.setString(1, eventName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar a existência do evento: " + e.getMessage());
        }

        return false;
    }

    private boolean codigoRegistoExists(int codigo) {
        String selectQuery = "SELECT COUNT(*) AS count FROM Codigos_Registo WHERE Codigo = ?";

        try (PreparedStatement preparedStatement = conn.prepareStatement(selectQuery)) {
            preparedStatement.setInt(1, codigo);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar a existência do código de registro: " + e.getMessage());
        }

        return false;
    }

    public void closeConnection() {
        try {
            if (this.conn != null) {
                this.conn.close();
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public boolean editUtilizador(String email, String novoNome, int novoNumeroEstudante, String novaPalavraChave) {
        if (utilizadorExists(email)) {
            String updateQuery = "UPDATE Utilizador SET Nome = ?, Numero_Estudante = ?, Pass_word = ? WHERE Email = ?";

            try {
                PreparedStatement pstmt = conn.prepareStatement(updateQuery);
                pstmt.setString(1, novoNome);
                pstmt.setInt(2, novoNumeroEstudante);
                pstmt.setString(3, novaPalavraChave);
                pstmt.setString(4, email);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    incrementarVersao("pd");
                    System.out.println("Informações do utilizador atualizadas com sucesso!");
                    return true;
                } else {
                    System.out.println("Nenhum utilizador atualizado. Verifique o email fornecido.");
                    return false;
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Utilizador com o email fornecido não encontrado. Edição cancelada.");
        }
        return false;
    }

    public void editEmail(String emailAtual, String novoEmail) {
        if (utilizadorExists(emailAtual)) {
            if (!utilizadorExists(novoEmail)) {
                String updateQuery = "UPDATE Utilizador SET Email = ? WHERE Email = ?";

                try {
                    PreparedStatement pstmt = conn.prepareStatement(updateQuery);
                    pstmt.setString(1, novoEmail);
                    pstmt.setString(2, emailAtual);

                    int rowsAffected = pstmt.executeUpdate();

                    if (rowsAffected > 0) {
                        incrementarVersao("pd");
                        System.out.println("Email do utilizador atualizado com sucesso!");
                    } else {
                        System.out.println("Nenhum utilizador atualizado. Verifique o email fornecido.");
                    }
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            } else {
                System.out.println("O novo email já pertence a outro utilizador. Edição cancelada.");
            }
        } else {
            System.out.println("Utilizador com o email fornecido não encontrado. Edição cancelada.");
        }
    }

    public void fillClient(ClientInfo clientInfo) {
        if (utilizadorExists(clientInfo.getEmail())) {
            String selectQuery = "SELECT Nome, Numero_Estudante FROM Utilizador WHERE Email = ?";
            try {
                PreparedStatement pstmt = conn.prepareStatement(selectQuery);
                pstmt.setString(1, clientInfo.getEmail());

                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    String nome = rs.getString("Nome");
                    int numeroEstudante = rs.getInt("Numero_Estudante");

                    clientInfo.setName(nome);
                    clientInfo.setStudentNumber(numeroEstudante);

                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public boolean insertEvent(String nomeEvento, String local, Date data, Time horaInicio, Time horaFim) {
        if(eventoExists(nomeEvento)){
            return false;
        }
        String insertQuery = "INSERT INTO Evento (Nome_Evento, Local, Data, Hora_Inicio, Hora_Fim) VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);
            pstmt.setString(1, nomeEvento);
            pstmt.setString(2, local);
            pstmt.setDate(3, data);
            pstmt.setTime(4, horaInicio);
            pstmt.setTime(5, horaFim);

            pstmt.executeUpdate();
            incrementarVersao("pd");
            System.out.println("Evento inserido com sucesso!");
            displayAllEvents();
            return true;
        } catch (SQLException e) {
            System.out.println("Erro ao inserir evento: " + e.getMessage());
        }
        return false;
    }

    public String displayAllEvents() {
        String selectQuery = "SELECT * FROM Evento";
        StringBuilder sb = new StringBuilder();
        try {
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String nomeEvento = rs.getString("Nome_Evento");
                String local = rs.getString("Local");
                Date data = rs.getDate("Data");
                Time horaInicio = rs.getTime("Hora_Inicio");
                Time horaFim = rs.getTime("Hora_Fim");

                sb.append("Evento: ").append(nomeEvento)
                        .append(", Local: ").append(local)
                        .append(", Data: ").append(data)
                        .append(", Hora de Início: ").append(horaInicio)
                        .append(", Hora de Fim: ").append(horaFim)
                        .append("\n---------------\n");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao mostrar eventos: " + e.getMessage());
        }
        return sb.toString();
    }

    public boolean existeAlguemAssistindo(String nomeEvento) {
        String selectQuery = "SELECT COUNT(*) AS count FROM Assiste WHERE Nome_Evento = ?";

        try {
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            pstmt.setString(1, nomeEvento);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                return count > 0;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar se alguém está a assistir ao evento: " + e.getMessage());
        }
        return false;
    }

    public String editEvent(String nomeEvento, String novoLocal, Date novaData, Time novaHoraInicio, Time novaHoraFim) {

        if (eventoExists(nomeEvento)) {
            if(existeAlguemAssistindo(nomeEvento)){
                return ("Erro existem presencas neste evento!");
            }
            StringBuilder updateQuery = new StringBuilder("UPDATE Evento SET ");


            List<Object> params = new ArrayList<>();

            if (novoLocal != null) {
                updateQuery.append("Local = ?, ");
                params.add(novoLocal);
            }

            if (novaData != null) {
                updateQuery.append("Data = ?, ");
                params.add(novaData);
            }

            if (novaHoraInicio != null) {
                updateQuery.append("Hora_Inicio = ?, ");
                params.add(novaHoraInicio);
            }

            if (novaHoraFim != null) {
                updateQuery.append("Hora_Fim = ?, ");
                params.add(novaHoraFim);
            }


            updateQuery.setLength(updateQuery.length() - 2);

            updateQuery.append(" WHERE Nome_Evento = ?");

            try {
                PreparedStatement pstmt = conn.prepareStatement(updateQuery.toString());

                // Configurar os parâmetros
                for (int i = 0; i < params.size(); i++) {
                    pstmt.setObject(i + 1, params.get(i));
                }

                pstmt.setString(params.size() + 1, nomeEvento);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    incrementarVersao("pd");
                    return ("Informações do evento atualizadas com sucesso!");
                } else {
                    return ("Nenhum evento atualizado. Verifique o nome fornecido.");
                }
            } catch (SQLException e) {
                System.out.println("Erro ao editar o evento: " + e.getMessage());
            }
        } else {
            return ("Evento com o nome fornecido não encontrado. Edição cancelada.");
        }
        return null;
    }

    public String deleteEvent(String nomeEvento) {
        if (eventoExists(nomeEvento)) {
            String deleteQuery = "DELETE FROM Evento WHERE Nome_Evento = ?";

            try {
                PreparedStatement pstmt = conn.prepareStatement(deleteQuery);
                pstmt.setString(1, nomeEvento);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    incrementarVersao("pd");
                    return ("Evento removido com sucesso!");
                } else {
                    return ("Nenhum evento removido. Verifique o nome fornecido.");
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } else {
            return ("Evento com o nome fornecido não encontrado. Remoção cancelada.");
        }
        return null;
    }
    public boolean deleteEventBool(String nomeEvento) {
        if (eventoExists(nomeEvento)) {
            String deleteQuery = "DELETE FROM Evento WHERE Nome_Evento = ?";

            try {
                PreparedStatement pstmt = conn.prepareStatement(deleteQuery);
                pstmt.setString(1, nomeEvento);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    incrementarVersao("pd");
                    return true;
                } else {
                    return false;
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } else {
            return false;
        }
        return false;
    }
    //assiste

    public String obterInfoUtilizadoresPorEvento(String nomeEvento) {
        String selectQuery = "SELECT U.* FROM Utilizador U " +
                "JOIN Assiste A ON U.Email = A.Email_Utilizador " +
                "WHERE A.Nome_Evento = ?";
        StringBuilder resultado = new StringBuilder();

        try {
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            pstmt.setString(1, nomeEvento);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String nome = rs.getString("Nome");
                String numeroEstudante = rs.getString("Numero_Estudante");
                String email = rs.getString("Email");

                resultado.append("Nome: ").append(nome).append("\n");
                resultado.append("Número de Estudante: ").append(numeroEstudante).append("\n");
                resultado.append("Email: ").append(email).append("\n");
                resultado.append("---------------").append("\n");
            }
        } catch (SQLException e) {
            resultado.append("Erro ao obter informações dos utilizadores por evento: ").append(e.getMessage());
        }

        return resultado.toString();
    }
    public String deleteAssiste(String emailUtilizador, String nomeEvento) {
        String deleteQuery = "DELETE FROM Assiste WHERE Email_Utilizador = ? AND Nome_Evento = ?";

        try {
            PreparedStatement pstmt = conn.prepareStatement(deleteQuery);
            pstmt.setString(1, emailUtilizador);
            pstmt.setString(2, nomeEvento);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                incrementarVersao("pd");
                return ("Registros da presenca deletados com sucesso!");
            } else {
                return ("Nenhum registro deletado. Verifique o email e o nome do evento fornecidos.");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao deletar registros da tabela Assiste: " + e.getMessage());
        }
        return null;
    }


    public String insertAssiste(String emailUtilizador, String nomeEvento) {
        String insertQueryAssiste =
                "INSERT INTO Assiste (ID_Assiste, Email_Utilizador, Nome_Evento) VALUES (?, ?, ?)";
        try {
            PreparedStatement pstmt = conn.prepareStatement(insertQueryAssiste);

            pstmt.setInt(1, generateRandomCode());
            pstmt.setString(2, emailUtilizador);
            pstmt.setString(3, nomeEvento);

            pstmt.executeUpdate();
            incrementarVersao("pd");
            return  ("Presenca inserido com sucesso!");

        } catch (SQLException e) {
            System.out.println("Error inserting data into Assiste table: " + e.getMessage());
        }
        return null;
    }

    public List<Event> getConsultasByUtilizador(String emailUtilizador) {
        String selectQuery = "SELECT Nome_Evento FROM Assiste WHERE Email_Utilizador = ?";

        //StringBuilder resultado = new StringBuilder();
        List<Event> events = new ArrayList<>();
        try {
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            pstmt.setString(1, emailUtilizador);

            ResultSet rs = pstmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("Nenhum evento encontrado para o utilizador com email: " + emailUtilizador);
            } else {
                //resultado.append("Eventos do utilizador com email ").append(emailUtilizador).append(":\n");
                while (rs.next()) {
                    String nomeEvento = rs.getString("Nome_Evento");
                    events.add(getEventByName(nomeEvento));
                    System.out.println("Encontrou: " + nomeEvento);
                    //resultado.append(nomeEvento).append("\n");
                }
            }
        } catch (SQLException e) {
            //resultado.append("Erro ao obter consultas de eventos: ").append(e.getMessage());
        }

        return events;
    }

    public Event getEventByName(String eventName) {
        String selectQuery = "SELECT * FROM Evento WHERE Nome_Evento = ?";

        try {
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            pstmt.setString(1, eventName);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("Nome_Evento");
                String place = rs.getString("Local");
                Date date = rs.getDate("Data");
                Time startingHour = rs.getTime("Hora_Inicio");
                Time finishingHour = rs.getTime("Hora_Fim");

                return new Event(name, place, date, startingHour, finishingHour);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao obter informações do evento: " + e.getMessage());
        }

        return null;
    }

    public void displayAllAssiste() {
        String selectQuery = "SELECT * FROM Assiste";

        try {
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int idAssiste = rs.getInt("ID_Assiste");
                String emailUtilizador = rs.getString("Email_Utilizador");
                String nomeEvento = rs.getString("Nome_Evento");

                System.out.println("ID_Assiste: " + idAssiste);
                System.out.println("Email_Utilizador: " + emailUtilizador);
                System.out.println("Nome_Evento: " + nomeEvento);
                System.out.println("---------------");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao mostrar registros da tabela Assiste: " + e.getMessage());
        }
    }

    public int getStudentNumberByEmail(String email) {
        String selectQuery = "SELECT Numero_Estudante FROM Utilizador WHERE Email = ?";

        try {
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            pstmt.setString(1, email);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("Numero_Estudante");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao obter número de estudante por e-mail: " + e.getMessage());
        }

        return -1;
    }

    public String getNameByEmail(String email) {
        String selectQuery = "SELECT Nome FROM Utilizador WHERE Email = ?";

        try {
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            pstmt.setString(1, email);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("Nome");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao obter nome por e-mail: " + e.getMessage());
        }

        return null;
    }

    public boolean existeVersaoPeloNome(String nomeVersao) {
        String selectQuery = "SELECT COUNT(*) AS count FROM Versao WHERE Nome_Aplicacao = ?";

        try {
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            pstmt.setString(1, nomeVersao);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");

                return count > 0;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar se existe alguma versão pelo nome: " + e.getMessage());
        }
        return false;
    }

    public void inserirVersao(String nomeAplicacao, int numeroVersao) {

        if(existeVersaoPeloNome(nomeAplicacao)){
            return ;
        }

        String insertQuery = "INSERT INTO Versao (Nome_Aplicacao, Numero_Versao) VALUES (?, ?)";

        try {
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);
            pstmt.setString(1, nomeAplicacao);
            pstmt.setInt(2, numeroVersao);

            pstmt.executeUpdate();

            System.out.println("Versão inserida com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao inserir versão: " + e.getMessage());
        }
    }

    public void incrementarVersao(String nomeAplicacao) {
        String selectQuery = "SELECT Numero_Versao FROM Versao WHERE Nome_Aplicacao = ?";
        String updateQuery = "UPDATE Versao SET Numero_Versao = ? WHERE Nome_Aplicacao = ?";

        try {
            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
            selectStmt.setString(1, nomeAplicacao);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                int versaoAtual = rs.getInt("Numero_Versao");

                int novaVersao = versaoAtual + 1;

                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setInt(1, novaVersao);
                updateStmt.setString(2, nomeAplicacao);
                updateStmt.executeUpdate();

                System.out.println("Versão incrementada com sucesso! Nova versão: " + novaVersao);
                heartBeatSender.sendHeartBeat(novaVersao,true);
            } else {
                System.out.println("Aplicação não encontrada na tabela de versões.");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao incrementar versão: " + e.getMessage());
        }
    }

    public int obterVersaoPorNome(String nomeAplicacao) {
        String selectQuery = "SELECT Numero_Versao FROM Versao WHERE Nome_Aplicacao = ?";

        try {
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            pstmt.setString(1, nomeAplicacao);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("Numero_Versao");
            } else {
                System.out.println("Aplicação não encontrada na tabela de versões.");
                return -1;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao obter versão por nome: " + e.getMessage());
            return -1;
        }
    }

    public String get3cli(String emailUtilizador) {
        String selectQuery = "SELECT E.Nome_Evento, E.Local, E.Data FROM Assiste A " +
                "JOIN Evento E ON A.Nome_Evento = E.Nome_Evento " +
                "WHERE A.Email_Utilizador = ?";

        StringBuilder resultado = new StringBuilder();
        try {
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            pstmt.setString(1, emailUtilizador);

            ResultSet rs = pstmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                resultado.append("Nenhum evento encontrado para o utilizador com email: ").append(emailUtilizador);
            } else {
                resultado.append("Eventos do utilizador com email ").append(emailUtilizador).append(":\n");
                while (rs.next()) {
                    String nomeEvento = rs.getString("Nome_Evento");
                    String local = rs.getString("Local");
                    Date data = rs.getDate("Data");

                    resultado.append("Nome: ").append(nomeEvento).append(", Local: ").append(local).append(", Data: ").append(data).append("\n");
                }
            }
        } catch (SQLException e) {
            resultado.append("Erro ao obter consultas de eventos: ").append(e.getMessage());
        }

        return resultado.toString();
    }
}
