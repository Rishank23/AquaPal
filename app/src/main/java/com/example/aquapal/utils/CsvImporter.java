package com.example.aquapal.utils;

import android.content.ContentResolver;
import android.net.Uri;

import com.example.aquapal.waterDb.WaterUsage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CsvImporter {

    public static class Result {
        public final List<WaterUsage> imported;
        public final int skipped;

        Result(List<WaterUsage> imported, int skipped) {
            this.imported = imported;
            this.skipped = skipped;
        }
    }

    public static Result parse(ContentResolver resolver, Uri uri) throws IOException {
        List<WaterUsage> imported = new ArrayList<>();
        int skipped = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        try (InputStream input = resolver.openInputStream(uri)) {
            if (input == null) throw new IOException("Could not open file");

            BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    if (line.toLowerCase(Locale.ROOT).startsWith("date,")) continue;
                }
                if (line.trim().isEmpty()) continue;

                List<String> fields = splitCsvLine(line);
                if (fields.size() < 4) {
                    skipped++;
                    continue;
                }

                try {
                    Date date = sdf.parse(fields.get(0).trim());
                    String category = fields.get(1).trim();
                    float quantity = Float.parseFloat(fields.get(2).trim());
                    String description = fields.get(3).trim();

                    if (date == null || quantity <= 0 || category.isEmpty()) {
                        skipped++;
                        continue;
                    }

                    imported.add(new WaterUsage(quantity, category, description, date));
                } catch (ParseException | NumberFormatException e) {
                    skipped++;
                }
            }
        }

        return new Result(imported, skipped);
    }

    private static List<String> splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString());
        return fields;
    }
}
