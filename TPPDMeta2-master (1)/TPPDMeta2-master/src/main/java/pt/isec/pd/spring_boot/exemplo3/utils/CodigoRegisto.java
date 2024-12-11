package pt.isec.pd.spring_boot.exemplo3.utils;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

public class CodigoRegisto implements Serializable {
    private Time HoraFinal;
    private String eventName;

    public CodigoRegisto(Time HoraFinal, String eventName) {
        this.HoraFinal = HoraFinal;
        this.eventName = eventName;
    }


    public Time getHoraFinal() {
        return HoraFinal;
    }

    public String getEventName() {
        return eventName;
    }
}
