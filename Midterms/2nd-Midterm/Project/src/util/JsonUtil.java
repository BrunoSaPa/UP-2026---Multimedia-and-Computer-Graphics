package util;


//class to help me read responses from the api without needing to pull in a full JSON library, since the responses are simple and I want to avoid extra dependencies, as we were told we could only use stuff seen in class :)
public final class JsonUtil {

    private JsonUtil() { }


    //Pulls the first string value for {@code "key": "value"} out of a raw JSON blob.  Returns {@code null} when the key is absent or the value is not a quoted string.

    public static String extractString(String json, String key) {
        if (json == null || key == null) return null;

        String needle = "\"" + key + "\"";
        int keyIdx = json.indexOf(needle);
        if (keyIdx < 0) return null;

        // skip past  "key" : "
        int colon = json.indexOf(':', keyIdx + needle.length());
        if (colon < 0) return null;

        int cursor = colon + 1;
        cursor = skipWhitespace(json, cursor);
        if (cursor >= json.length() || json.charAt(cursor) != '"') return null;

        int valueStart = cursor + 1;
        int valueEnd   = findClosingQuote(json, valueStart);
        if (valueEnd < 0) return null;

        return unescapeJson(json.substring(valueStart, valueEnd));
    }


    //looks for an error in the json and returns is message or null of none is found
    public static String extractErrorMessage(String json) {
        if (json == null) return null;
        int errorIdx = json.indexOf("\"error\"");
        if (errorIdx < 0) return null;
        return extractString(json.substring(errorIdx), "message");
    }


    public static String escapeJson(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static String unescapeJson(String value) {
        if (value == null) return null;
        return value
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n",  "\n")
                .replace("\\r",  "\r")
                .replace("\\t",  "\t")
                .replace("\\/",  "/");
    }


    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // HTTP 429 (rate-limit) and 5xx errors are worth retrying, since i was hittin quota limits before and failed the entire process
    public static boolean isRetryableStatus(int statusCode) {
        return statusCode == 429 || statusCode >= 500;
    }

    // sleeps with a small random to retry request
    public static void sleepWithBackoff(long backoffMs) throws InterruptedException {
        long jitter = (long) (Math.random() * 250);
        Thread.sleep(backoffMs + jitter);
    }


    //internal methods
    private static int skipWhitespace(String s, int from) {
        while (from < s.length() && Character.isWhitespace(s.charAt(from))) {
            from++;
        }
        return from;
    }

    private static int findClosingQuote(String json, int startAfterOpenQuote) {
        for (int i = startAfterOpenQuote; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\') { i++; continue; }
            if (c == '"')  return i;
        }
        return -1;
    }
}

