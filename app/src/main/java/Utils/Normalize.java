package Utils;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Normalize {
        public static String normalize(String word) {
            if (word == null || word.isEmpty()) {
                return word;
            }
            return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
        }
    public static String formatTimestampToFrenchText(Timestamp timestamp, String utcOffset) {
        if (timestamp == null) return "";

        Date date = timestamp.toDate();

        // On utilise le fuseau local pour formater correctement l'heure
        // Mais on affichera le texte UTC passé en paramètre
        TimeZone tz = TimeZone.getDefault();  // ou un autre fixe si besoin

        SimpleDateFormat jourFormat = new SimpleDateFormat("EEEE", Locale.FRENCH);
        SimpleDateFormat jourDuMois = new SimpleDateFormat("d", Locale.FRENCH);
        SimpleDateFormat moisFormat = new SimpleDateFormat("MMMM", Locale.FRENCH);
        SimpleDateFormat heureFormat = new SimpleDateFormat("HH'h'mm", Locale.FRENCH);

        jourFormat.setTimeZone(tz);
        jourDuMois.setTimeZone(tz);
        moisFormat.setTimeZone(tz);
        heureFormat.setTimeZone(tz);

        String jour = jourFormat.format(date);
        String dateStr = Character.toUpperCase(jour.charAt(0)) + jour.substring(1)
                + " " + jourDuMois.format(date)
                + " " + moisFormat.format(date)
                + " à " + heureFormat.format(date);

        if (utcOffset != null && !utcOffset.isEmpty()) {
            dateStr += " (" + utcOffset + ")";
        }

        return dateStr;
    }

}
