package org.openjfx.service;

import org.openjfx.dto.ElementType;
import org.openjfx.dto.LoadedElement;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoadFromCsvService {

    private static final String DELIMITER = ";";

    private LoadFromCsvService() { }

    public static List<LoadedElement> load(ElementType type) throws IOException {
        List<LoadedElement> commands = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(type.getFileName()))) {
            String line;
            // skip first line
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(DELIMITER);
                commands.add(new LoadedElement(Integer.parseInt(values[0]), values[1], Integer.parseInt(values[2]), values[3], values[4], values[5]));
            }
        }
        return commands;
    }

}
