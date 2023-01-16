package me.basiqueevangelist.dynreg.util;

import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public final class TopSort {
    private TopSort() {

    }

    public static <T> List<T> topSort(Iterable<T> entries, Function<T, Iterable<T>> entryToDependents, MutableBoolean hasCycle) {
        List<T> visited = new ArrayList<>();
        List<T> visiting = new ArrayList<>();
        List<T> ordered = new ArrayList<>();

        for (var entry : entries) {
            if (!(visited.contains(entry) || dfs(entry, visited, visiting, ordered, entryToDependents))) {
                hasCycle.setTrue();
            }

        }

        Collections.reverse(ordered);

        return ordered;
    }

    private static <T> boolean dfs(T entry, List<T> visited, List<T> visiting, List<T> ordered, Function<T, Iterable<T>> entryToDependents) {
        if (visited.contains(entry)) return true;
        else if (visiting.contains(entry)) return false;

        visiting.add(entry);

        for (T dependent : entryToDependents.apply(entry)) {
            if (!dfs(dependent, visited, visiting, ordered, entryToDependents))
                return false;
        }

        visiting.remove(entry);
        visited.add(entry);
        ordered.add(entry);
        return true;
    }
}
