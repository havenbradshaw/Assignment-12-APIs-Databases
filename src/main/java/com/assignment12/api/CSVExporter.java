package com.assignment12.api;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVExporter {
    public static void export(File outFile, List<Country> countries) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))) {
            bw.write("Name,Population,Region\n");
            for (Country c : countries) {
                String line = String.format("%s,%d,%s\n",
                        escape(c.getName()), c.getPopulation(), escape(c.getRegion()));
                bw.write(line);
            }
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        String out = s.replace("\"", "\"\"");
        if (out.contains(",") || out.contains("\n") || out.contains("\"")) {
            return "\"" + out + "\"";
        }
        return out;
    }
}
