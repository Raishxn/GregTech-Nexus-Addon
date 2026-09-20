package com.raishxn.gtna;

import com.raishxn.gtna.utils.StructureSlicer;

import java.util.List;

/**
 * Locks the aisle-stretching semantics of {@code StructureSlicer.sliceAndInsert}: for every
 * insert count {@code i} in {@code [k, l]} it rebuilds each row as
 * {@code row[0, n) + i × row[q-1] + row[m-1, len)} — front slice, then {@code i} copies of the
 * 1-based column {@code q} (empty string when {@code q} is out of range), then the back slice.
 * The class is decompile-derived, so this test keeps a future refactor (e.g. the Fase 3 aisle
 * extraction) from silently changing slice/insert behavior. GTLCore-style: {@code main()} +
 * asserts, no JUnit.
 */
public final class StructureSlicerTest {

    private StructureSlicerTest() {}

    public static void main(String[] args) {
        identityAtZeroInserts();
        insertsDuplicateChosenColumn();
        insertCountRangeInclusive();
        qIsOneBasedAndClamped();
        sliceClamping();
        nullAndEmptyRowsPassThrough();
        multiRowIndependence();
        invalidInputThrows();
        System.out.println("[StructureSlicerTest] all cases passed");
    }

    private static void identityAtZeroInserts() {
        List<String[][]> results = StructureSlicer.sliceAndInsert(input("ABCDEFGHIJ"), 4, 5, 5, 0, 0);
        check(results.size() == 1, "zero-insert produces one result");
        check(join(results.get(0)[0]).equals("ABCDEFGHIJ"), "front[0,n) + 0 inserts + back[m-1,len) == original");
    }

    private static void insertsDuplicateChosenColumn() {
        List<String[][]> results = StructureSlicer.sliceAndInsert(input("ABCDEFGHIJ"), 4, 5, 5, 0, 2);
        check(results.size() == 3, "insert range [0,2] yields three results");
        check(join(results.get(0)[0]).equals("ABCDEFGHIJ"), "i=0 keeps original");
        check(join(results.get(1)[0]).equals("ABCD" + "E" + "EFGHIJ"), "i=1 inserts one copy of column q");
        check(join(results.get(2)[0]).equals("ABCD" + "EE" + "EFGHIJ"), "i=2 inserts two copies of column q");
    }

    private static void insertCountRangeInclusive() {
        List<String[][]> results = StructureSlicer.sliceAndInsert(input("ABCDEFGHIJ"), 4, 5, 5, 2, 5);
        check(results.size() == 4, "insert range [2,5] is inclusive on both ends");
        for (int i = 0; i < results.size(); i++) {
            check(results.get(i)[0].length == 10 + 2 + i, "result " + i + " has 10 + " + (2 + i) + " columns");
        }
    }

    private static void qIsOneBasedAndClamped() {
        // q is 1-based: q=1 -> first column.
        String[][] r = StructureSlicer.sliceAndInsert(input("ABCDEFGHIJ"), 1, 2, 1, 1, 1).get(0);
        check(join(r[0]).equals("A" + "A" + "BCDEFGHIJ"), "q=1 duplicates the first column");
        // q = row length -> last column.
        r = StructureSlicer.sliceAndInsert(input("ABCDEFGHIJ"), 4, 5, 10, 1, 1).get(0);
        check(join(r[0]).equals("ABCD" + "J" + "EFGHIJ"), "q=len duplicates the last column");
        // Out-of-range q inserts empty strings instead of throwing (0 would index q-1 = -1).
        r = StructureSlicer.sliceAndInsert(input("ABCDEFGHIJ"), 4, 5, 0, 1, 1).get(0);
        check(r[0].length == 11 && r[0][4].isEmpty(), "q=0 inserts empty string without throwing");
        r = StructureSlicer.sliceAndInsert(input("ABCDEFGHIJ"), 4, 5, 11, 1, 1).get(0);
        check(r[0].length == 11 && r[0][4].isEmpty(), "q=len+1 inserts empty string without throwing");
    }

    private static void sliceClamping() {
        // n=0 -> empty front slice; the inserted column follows nothing.
        String[][] r = StructureSlicer.sliceAndInsert(input("ABCDEFGHIJ"), 0, 5, 5, 1, 1).get(0);
        check(join(r[0]).equals("E" + "EFGHIJ"), "n=0 gives empty front slice");
        // n beyond the row clamps to the full row as front.
        r = StructureSlicer.sliceAndInsert(input("ABCDEFGHIJ"), 99, 5, 5, 1, 1).get(0);
        check(join(r[0]).equals("ABCDEFGHIJ" + "E" + "EFGHIJ"), "n beyond length clamps to full front");
        // m=1 -> back slice starts at index 0 (start clamp), duplicating the whole row.
        r = StructureSlicer.sliceAndInsert(input("ABCDEFGHIJ"), 4, 1, 5, 1, 1).get(0);
        check(join(r[0]).equals("ABCD" + "E" + "ABCDEFGHIJ"), "m=1 gives full-row back slice");
        // m beyond the row -> empty back slice.
        r = StructureSlicer.sliceAndInsert(input("ABCDEFGHIJ"), 4, 11, 5, 1, 1).get(0);
        check(join(r[0]).equals("ABCD" + "E"), "m beyond length gives empty back slice");
    }

    private static void nullAndEmptyRowsPassThrough() {
        String[][] in = new String[][] { null, new String[0], input("ABC")[0] };
        List<String[][]> results = StructureSlicer.sliceAndInsert(in, 1, 2, 1, 2, 2);
        check(results.size() == 1, "one result per insert count");
        check(results.get(0)[0].length == 0, "null row becomes an empty row");
        check(results.get(0)[1].length == 0, "empty row stays empty");
        check(results.get(0)[2].length == 3 + 2, "non-empty row still stretched by its own length");
    }

    private static void multiRowIndependence() {
        List<String[][]> results = StructureSlicer.sliceAndInsert(input("ABCDEFGHIJ", "ABC"), 1, 1, 2, 1, 1);
        check(results.size() == 1, "one result for the single insert count");
        // Row 0: m=1 clamps the back slice to the whole row.
        check(join(results.get(0)[0]).equals("A" + "B" + "ABCDEFGHIJ"), "long row stretched independently");
        // Row 1 (len 3): q=2 is valid, m=1 clamps to whole row.
        check(join(results.get(0)[1]).equals("A" + "B" + "ABC"), "short row stretched independently");
    }

    private static void invalidInputThrows() {
        expectIllegalArgument(() -> StructureSlicer.sliceAndInsert(null, 4, 5, 5, 0, 0),
                "null input throws IllegalArgumentException");
        expectIllegalArgument(() -> StructureSlicer.sliceAndInsert(new String[0][], 4, 5, 5, 0, 0),
                "empty input throws IllegalArgumentException");
    }

    // ---- helpers ----

    private static int caseNum = 0;

    private static void check(boolean condition, String name) {
        caseNum++;
        if (!condition) {
            throw new AssertionError("StructureSlicerTest case #" + caseNum + " '" + name + "' failed");
        }
    }

    private static String[][] input(String... rows) {
        String[][] structure = new String[rows.length][];
        for (int i = 0; i < rows.length; i++) {
            structure[i] = rows[i].chars().mapToObj(c -> String.valueOf((char) c)).toArray(String[]::new);
        }
        return structure;
    }

    private static String join(String[] row) {
        return String.join("", row);
    }

    private static void expectIllegalArgument(Runnable action, String name) {
        caseNum++;
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("StructureSlicerTest case #" + caseNum + " '" + name + "' did not throw");
    }
}
