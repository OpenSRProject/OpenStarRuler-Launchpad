package com.dalolorn.sr2modmanager.adapter;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Recommendation {
    public List<String> history = new ArrayList<>();

    private Recommendation() {}

    private static Recommendation instance = new Recommendation();
    private static final int HISTORY_SIZE_LIMIT = 5;
    // I guess this could be in a file....
    private static final List<String> RECOMMENDATIONS = Arrays.asList(
            "https://github.com/DaloLorn/Rising-Stars",
            "https://github.com/sol-oriens/Shores-of-Infinity",
            "https://github.com/Skeletonxf/star-ruler-2-mod-ce",
            "https://github.com/Vandaria/SR2-Lost-Sector",
            "https://github.com/sol-oriens/SR2-Community-Patch"
    );

    public static boolean load() throws IOException {
        File file = new File("history.json");
        if (!file.exists()) {
            getInstance().save();
            return false;
        } else {
            try (FileReader reader = new FileReader(file)) {
                instance = new Gson().fromJson(reader, Recommendation.class);
            }
        }
        return true;
    }

    public static Recommendation getInstance() {
        return instance;
    }

    public void save() throws IOException {
        File file = new File("history.json");
        if (!file.exists()) {
            file.createNewFile();
        }

        try (FileWriter writer = new FileWriter(file, false)) {
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
        List<String> res = new ArrayList<>(history);
        RECOMMENDATIONS.forEach(r -> {
            if (!history.contains(r)) {
                res.add(r);
            }
        });
        return res;
    }
}
