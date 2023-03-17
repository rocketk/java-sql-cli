package io.github.rocketk.sqlcli;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author pengyu
 */
public class JsonUtil {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static <T> T unmarshal(String content, Class<T> clazz) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        return gson.fromJson(content, clazz);
    }

    public static String marshal(Object obj) {
        if (obj == null) {
            return null;
        }
        return gson.toJson(obj);
    }
}
