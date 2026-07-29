package com.example.aquapal.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.example.aquapal.waterDb.WaterUsage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CsvExporter {

    public static Intent buildShareIntent(Context context, List<WaterUsage> entries) throws IOException {
        File exportDir = new File(context.getCacheDir(), "exports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File csvFile = new File(exportDir, "aquapal_usage.csv");

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.append("Date,Category,Quantity (L),Description\n");
            for (WaterUsage usage : entries) {
                writer.append(sdf.format(usage.getDate())).append(",");
                writer.append(escapeCsv(usage.getCategory())).append(",");
                writer.append(String.valueOf(usage.getQuantity())).append(",");
                writer.append(escapeCsv(usage.getDescription())).append("\n");
            }
        }

        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", csvFile);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "AquaPal Water Usage Export");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return Intent.createChooser(shareIntent, "Export usage data");
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
