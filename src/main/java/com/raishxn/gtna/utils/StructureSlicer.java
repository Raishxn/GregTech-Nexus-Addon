//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.raishxn.gtna.utils;

import java.util.ArrayList;
import java.util.List;

public class StructureSlicer {
    public static List<String[][]> sliceAndInsert(String[][] input, int n, int m, int q, int k, int l) {
        if (input != null && input.length != 0) {
            List<String[][]> results = new ArrayList();

            for(int i = k; i <= l; ++i) {
                String[][] result = processForInsertCount(input, n, m, q, i);
                results.add(result);
            }

            return results;
        } else {
            throw new IllegalArgumentException("Input array cannot be null or empty");
        }
    }

    private static String[][] processForInsertCount(String[][] input, int n, int m, int q, int insertCount) {
        List<String[]> processedRows = new ArrayList();

        for(String[] row : input) {
            if (row != null && row.length != 0) {
                String[] frontSlice = sliceArray(row, 0, n);
                String[] backSlice = sliceArray(row, m - 1, row.length);
                String extraString = q > 0 && q <= row.length ? row[q - 1] : "";
                String[] combinedRow = combineWithInserts(frontSlice, backSlice, extraString, insertCount);
                processedRows.add(combinedRow);
            } else {
                processedRows.add(new String[0]);
            }
        }

        return (String[][])processedRows.toArray(new String[0][]);
    }

    private static String[] sliceArray(String[] array, int start, int end) {
        if (start < 0) {
            start = 0;
        }

        if (end > array.length) {
            end = array.length;
        }

        if (start >= end) {
            return new String[0];
        } else {
            String[] slice = new String[end - start];
            System.arraycopy(array, start, slice, 0, end - start);
            return slice;
        }
    }

    private static String[] combineWithInserts(String[] front, String[] back, String extra, int insertCount) {
        int totalLength = front.length + insertCount + back.length;
        String[] combined = new String[totalLength];
        int index = 0;

        for(String s : front) {
            combined[index++] = s;
        }

        for(int i = 0; i < insertCount; ++i) {
            combined[index++] = extra;
        }

        for(String s : back) {
            combined[index++] = s;
        }

        return combined;
    }
}
