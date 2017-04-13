package de.smarthistory.data;

/**
 * A util to convert julian dates.
 */
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class JulianDate {

    public static Date julianToDate(double jd) {

        double z, f, a, b, c, d, e, m, aux;
        Date date = new Date();
        jd += 0.5;
        z = Math.floor(jd);
        f = jd - z;

        if (z >= 2299161.0) {
            a = Math.floor((z - 1867216.25) / 36524.25);
            a = z + 1 + a - Math.floor(a / 4);
        } else {
            a = z;
        }

        b = a + 1524;
        c = Math.floor((b - 122.1) / 365.25);
        d = Math.floor(365.25 * c);
        e = Math.floor((b - d) / 30.6001);
        aux = b - d - Math.floor(30.6001 * e) + f;

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, (int) aux);
        aux = ((aux - calendar.get(Calendar.DAY_OF_MONTH)) * 24);
        calendar.set(Calendar.HOUR_OF_DAY, (int) aux);
        calendar.set(Calendar.MINUTE, (int) ((aux - calendar.get(Calendar.HOUR_OF_DAY)) * 60));

        if (e < 13.5) {
            m = e - 1;
        } else {
            m = e - 13;
        }
        // Se le resta uno al mes por el manejo de JAVA, donde los meses empiezan en 0.
        calendar.set(Calendar.MONTH, (int) m - 1);
        if (m > 2.5) {
            calendar.set(Calendar.YEAR, (int) (c - 4716));
        } else {
            calendar.set(Calendar.YEAR, (int) (c - 4715));
        }
        return calendar.getTime();
    }
}
