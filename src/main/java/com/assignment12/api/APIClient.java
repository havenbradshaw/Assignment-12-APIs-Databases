package com.assignment12.api;

import com.google.gson.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class APIClient {
    private static final String API_URL = "https://restcountries.com/v3.1/all?fields=name,population,region";

    public List<Country> fetchAllCountries() throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new RuntimeException("API returned HTTP " + code);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            JsonElement root = JsonParser.parseReader(br);
            br.close();

            List<Country> result = new ArrayList<>();
            if (!root.isJsonArray()) return result;

            JsonArray arr = root.getAsJsonArray();
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject obj = el.getAsJsonObject();

                String name = "";
                try {
                    JsonObject nameObj = obj.getAsJsonObject("name");
                    if (nameObj != null && nameObj.has("common")) {
                        name = nameObj.get("common").getAsString();
                    }
                } catch (Exception ignore) {}

                long population = 0;
                try { population = obj.has("population") ? obj.get("population").getAsLong() : 0; } catch (Exception ignore) {}

                String region = "";
                try { region = obj.has("region") && !obj.get("region").isJsonNull() ? obj.get("region").getAsString() : ""; } catch (Exception ignore) {}

                if (name != null && !name.isEmpty()) {
                    result.add(new Country(name, population, region));
                }
            }

            return result;
        }
    }
}
