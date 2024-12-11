package pt.isec.pd.spring_boot.exemplo3.serverbackup;


import java.sql.*;

public class ManageDBBackup {
    String dbUrl;
    private Connection conn;

    public ManageDBBackup(String dbAddress, String dbName) {
        this.dbUrl = "jdbc:sqlite:" + dbAddress + "/" + dbName;
        //this.connect();
    }
    public void connect() {
        try {
            this.conn = DriverManager.getConnection(this.dbUrl);
            //System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            this.closeConnection();
        }
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
}
