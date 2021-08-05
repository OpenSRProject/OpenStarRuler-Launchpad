package com.dalolorn.sr2modmanager.adapter;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Recommendation {
    public final List<String> history = new ArrayList<>();

    private Recommendation() {}

    @NotNull private static Recommendation instance = new Recommendation();
    private static final int HISTORY_SIZE_LIMIT = 5;

    public static boolean load() throws IOException {
        var file = new File("history.json");
        if (!file.exists()) {
            getInstance().save();
            return false;
        } else {
            try (var reader = new FileReader(file)) {
                instance = new Gson().fromJson(reader, Recommendation.class);
            }
        }
        return true;
    }

    @NotNull
    public static Recommendation getInstance() {
        return instance;
    }

    public void save() throws IOException {
        var file = new File("history.json");
        if (!file.exists()) {
            file.createNewFile();
        }

        try (var writer = new FileWriter(file, false)) {
            writer.write(new Gson().toJson(this));
        }
    }

    /**
     * Adds an item to the saved list (if distinct), removes last item if list length above SIZE_LIMIT
     *
     * @param url the url to be added
     */
    public void addItem(String url) throws IOException {
        // Removing it so it gets added to the start of the list again
        history.remove(url);

        history.add(0, url);
        while (history.size() > HISTORY_SIZE_LIMIT) {
            history.remove(HISTORY_SIZE_LIMIT);
        }
		save();

    }

    public List<String> getRecommendationList() {
        return history;
    }
}
