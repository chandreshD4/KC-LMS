package com.kclms.app;

import android.os.Environment;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Locale;

public final class UniversalFileSearch {
    private UniversalFileSearch() {}

    public static ArrayList<File> search(String query, int limit) {
        ArrayList<File> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty() || limit < 1) {
            return results;
        }

        File root = Environment.getExternalStorageDirectory();
        if (root == null || !root.isDirectory()) return results;

        String needle = query.trim().toLowerCase(Locale.ROOT);
        ArrayDeque<File> pending = new ArrayDeque<>();
        pending.add(root);
        int visited = 0;
        final int maxVisited = 30000;

        while (!pending.isEmpty() && visited < maxVisited
                && results.size() < limit) {
            File dir = pending.removeFirst();
            visited++;

            File[] children = dir.listFiles();
            if (children == null) continue;

            for (File child : children) {
                String name = child.getName();
                if (name.toLowerCase(Locale.ROOT).contains(needle)) {
                    results.add(child);
                    if (results.size() >= limit) break;
                }

                if (child.isDirectory()
                        && !(dir.getName().equals("Android") && (name.equals("data") || name.equals("obb")))
                        && child.canRead()) {
                    pending.addLast(child);
                }
            }
        }
        return results;
    }
}
