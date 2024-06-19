package org.codefromheaven.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class JsonUtils {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy");
    private static final ObjectMapper mapper = new ObjectMapper();;

    static {
        mapper.setDateFormat(SDF);
    }

    private JsonUtils() {
    }

    public static String serialize(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot serialize object!", e);
        }
    }

    public static <T> T deserialize(File file, Class<T> tClass) throws FileNotFoundException {
        try {
            return mapper.readValue(file, tClass);
        } catch (IOException e) {
            throw new FileNotFoundException("Cannot find object!");
        }
    }

}
