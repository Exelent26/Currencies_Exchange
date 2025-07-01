package main.util;

import java.util.HashMap;

public class RequestBodyParser {
    public static HashMap<String, String> parametersPairCreatorFromBody(String body) {
        HashMap<String, String> result = new HashMap<>();

        String[] pairs = body.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            String key = idx > 0 ? pair.substring(0, idx) : pair;
            String value = idx > 0 ? pair.substring(idx + 1) : "";
            result.put(key, value);
        }
        return result;
    }
}
