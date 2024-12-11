package pt.isec.pd.spring_boot.exemplo3.utils;

import java.io.Serializable;

public class HeartBeatMSG implements Serializable{
    public static final long serialVersionUID = 1010L;

    protected String port;
    protected String nameRmi;
    protected int dbVersion;
    protected boolean update;

    public HeartBeatMSG(String port, String nameRmi,int dbVersion,boolean update) {
        this.port = port;
        this.nameRmi = nameRmi;
        this.dbVersion = dbVersion;
        this.update = update;
    }

    public String getPort() {
        return port;
    }
    public String getNameRmi() {
        return nameRmi;
    }
    public int getDbVersion() {
        return dbVersion;
    }
    public boolean isUpdate() {
        return update;
    }
}

