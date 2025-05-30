package Utils;

public class Normalize {
        public static String normalize(String word) {
            if (word == null || word.isEmpty()) {
                return word;
            }
            return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
        }


}
