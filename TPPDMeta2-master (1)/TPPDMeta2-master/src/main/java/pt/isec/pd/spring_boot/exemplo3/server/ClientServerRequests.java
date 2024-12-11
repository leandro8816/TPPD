package pt.isec.pd.spring_boot.exemplo3.server;

public interface ClientServerRequests {
    //Comandos para clientes e administradores
    public static final String REGISTER = "REGISTER";
    public static final String LOGIN = "LOGIN";
    public static final String CHECK_ATTENDANCE_CLI = "CHECK_ATTENDANCE_CLI";
    //Comandos para clientes
    public static final String EDIT = "EDIT";
    public static final String CHECK_CODE = "CHECK_CODE";

    //Comandos para administradores
    public static final String CREATE_EVENT = "CREATE_EVENT";
    public static final String EDIT_EVENT_DATA = "EDIT_EVENT_DATA";
    public static final String DELETE_EVENT = "DELETE_EVENT";
    public static final String CHECK_EVENTS = "CHECK_EVENTS";
    public static final String GENERATE_CODE = "GENERATE_CODE";
    public static final String CHECK_ATTENDANCE = "CHECK_ATTENDANCE";
    public static final String CHECK_ATTENDANCE_BY_USER = "CHECK_ATTENDANCE_BY_USER";
    public static final String OBTAIN_CSV_F1 = "OBTAIN_CSV_F1";
    public static final String OBTAIN_CSV_F2 = "OBTAIN_CSV_F2";
    public static final String DELETE_ATTENDANCE = "DELETE_ATTENDANCE";
    public static final String INSERT_ATTENDANCE = "INSERT_ATTENDANCE";
    public static final String UPDATE = "UPDATE";
    public static final String LOGOUT = "LOGOUT";

}
