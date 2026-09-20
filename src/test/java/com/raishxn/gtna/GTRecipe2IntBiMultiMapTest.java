package com.raishxn.gtna;

import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;

/**
 * Validates the dual-map bookkeeping of {@code GTRecipe2IntBiMultiMap} (recipe -> int values +
 * int values -> recipes): multi-value/multi-key growth, pair-wise removal, orphan-set cleanup
 * on {@code remove}/{@code removeByKey}/{@code removeByValue}, size/forEach accounting and the
 * cross-map invariant under a randomized op sequence checked against a reference model.
 *
 * <p>
 * The production class is keyed on {@code GTRecipe}, which needs a Minecraft bootstrap to
 * construct. Following the {@link ModeIdMatcherTest} precedent, this test exercises a
 * byte-for-byte mirror of the algorithm (GTRecipe -> {@link RecipeKey}), where RecipeKey
 * replicates GTRecipe's equality: identity by recipe id ({@code equals}/
 * {@code hashCode} on {@code this.id}). Keep the {@link BiMultiMap} body in sync with
 * {@code GTRecipe2IntBiMultiMap} — the point is to lock the cross-map consistency rules so a
 * refactor cannot leave one map pointing at entries the other dropped. GTLCore-style:
 * {@code main()} + asserts, no JUnit.
 */
public final class GTRecipe2IntBiMultiMapTest {

    private GTRecipe2IntBiMultiMapTest() {}

    public static void main(String[] args) {
        putAndGetBasics();
        multipleValuesAndKeys();
        removeSinglePair();
        removeCleansOrphansBothDirections();
        removeByKeyAndByValue();
        rePutAfterRemoval();
        forEachCoversEveryPair();
        clearResetsEverything();
        fuzzAgainstReferenceModel();
        System.out.println("[GTRecipe2IntBiMultiMapTest] all cases passed");
    }

    private static void putAndGetBasics() {
        RecipeKey r1 = new RecipeKey("gtceu:algorithm_processor");
        BiMultiMap map = new BiMultiMap();
        map.put(r1, 1);
        check(map.getValues(r1).size() == 1 && map.getValues(r1).contains(1), "put maps key to value");
        check(map.getKeys(1).size() == 1 && map.getKeys(1).contains(r1), "inverse maps value to key");
        check(map.size() == 1, "size counts pairs");
        check(map.getValues(new RecipeKey("gtceu:mainframe")).isEmpty(), "unknown key -> empty values");
        check(map.getKeys(99).isEmpty(), "unknown value -> empty keys");
        // A second key equal (same id) to an existing one must hit the same entry, matching
        // GTRecipe's id-based equality.
        map.put(new RecipeKey("gtceu:algorithm_processor"), 1);
        check(map.size() == 1, "re-put of an equal key/id pair does not duplicate");
    }

    private static void multipleValuesAndKeys() {
        RecipeKey r1 = new RecipeKey("r1");
        RecipeKey r2 = new RecipeKey("r2");
        BiMultiMap map = new BiMultiMap();
        map.put(r1, 1);
        map.put(r1, 2);
        map.put(r2, 2);
        map.put(r2, 3);
        check(map.size() == 4, "size counts all four pairs");
        check(map.getValues(r1).size() == 2, "key holds two values");
        check(map.getKeys(2).size() == 2, "value holds two keys");
        check(map.keySet().size() == 2, "keySet has both keys");
        check(map.valueSet().size() == 3, "valueSet has values 1, 2, 3");
    }

    private static void removeSinglePair() {
        RecipeKey r1 = new RecipeKey("r1");
        RecipeKey r2 = new RecipeKey("r2");
        BiMultiMap map = new BiMultiMap();
        map.put(r1, 1);
        map.put(r1, 2);
        map.put(r2, 1);
        map.remove(r1, 1);
        check(map.size() == 2, "only the targeted pair is removed");
        check(map.getValues(r1).size() == 1 && map.getValues(r1).contains(2), "key keeps its other value");
        check(map.getKeys(1).size() == 1 && map.getKeys(1).contains(r2), "value keeps its other key");
    }

    private static void removeCleansOrphansBothDirections() {
        RecipeKey r1 = new RecipeKey("r1");
        BiMultiMap map = new BiMultiMap();
        map.put(r1, 1);
        map.remove(r1, 1);
        check(map.size() == 0, "size drops to zero");
        check(map.keySet().isEmpty(), "key entry removed from keyToValues when its set empties");
        check(map.valueSet().isEmpty(), "value entry removed from valueToKeys when its set empties");
        check(map.getValues(r1).isEmpty(), "removed key -> empty values, not null");
        check(map.getKeys(1).isEmpty(), "removed value -> empty keys, not null");
    }

    private static void removeByKeyAndByValue() {
        RecipeKey r1 = new RecipeKey("r1");
        RecipeKey r2 = new RecipeKey("r2");
        BiMultiMap map = new BiMultiMap();
        map.put(r1, 1);
        map.put(r1, 2);
        map.put(r2, 1);
        map.put(r2, 3);

        BiMultiMap byKey = new BiMultiMap();
        byKey.put(r1, 1);
        byKey.put(r1, 2);
        byKey.put(r2, 1);
        byKey.put(r2, 3);
        byKey.removeByKey(r1);
        check(byKey.size() == 2, "removeByKey drops every pair of the key");
        check(byKey.getKeys(1).size() == 1 && byKey.getKeys(1).contains(r2), "value 1 still maps to r2");
        check(byKey.valueSet().size() == 2 && byKey.valueSet().contains(1) && byKey.valueSet().contains(3),
                "value 2 vanished with its only key, values 1 and 3 remain");
        check(byKey.getValues(r1).isEmpty(), "removed key has no values left");

        map.removeByValue(1);
        check(map.size() == 2, "removeByValue drops every pair of the value");
        check(map.getValues(r1).size() == 1 && map.getValues(r1).contains(2), "r1 keeps value 2");
        check(map.getKeys(1).isEmpty(), "value 1 fully removed");
        check(map.keySet().size() == 2, "both keys still present");
    }

    private static void rePutAfterRemoval() {
        RecipeKey r1 = new RecipeKey("r1");
        BiMultiMap map = new BiMultiMap();
        map.put(r1, 1);
        map.removeByKey(r1);
        map.put(r1, 1);
        check(map.size() == 1 && map.getValues(r1).contains(1) && map.getKeys(1).contains(r1),
                "re-put after removeByKey rebuilds both directions");
        map.removeByValue(1);
        map.put(r1, 1);
        check(map.size() == 1 && map.getKeys(1).contains(r1),
                "re-put after removeByValue rebuilds both directions");
    }

    private static void forEachCoversEveryPair() {
        RecipeKey r1 = new RecipeKey("r1");
        RecipeKey r2 = new RecipeKey("r2");
        BiMultiMap map = new BiMultiMap();
        map.put(r1, 1);
        map.put(r1, 2);
        map.put(r2, 2);
        Map<String, Integer> seen = new HashMap<>();
        map.forEach((key, value) -> seen.merge(key.id + "->" + value, 1, Integer::sum));
        check(seen.size() == 3, "forEach visits every distinct pair");
        check(seen.values().stream().allMatch(count -> count == 1), "forEach visits each pair exactly once");
        check(seen.containsKey("r1->1") && seen.containsKey("r1->2") && seen.containsKey("r2->2"),
                "forEach covers the expected pairs");
    }

    private static void clearResetsEverything() {
        RecipeKey r1 = new RecipeKey("r1");
        BiMultiMap map = new BiMultiMap();
        map.put(r1, 1);
        map.put(r1, 2);
        map.clear();
        check(map.size() == 0 && map.keySet().isEmpty() && map.valueSet().isEmpty(),
                "clear empties both maps");
        check(map.getValues(r1).isEmpty() && map.getKeys(1).isEmpty(), "lookups after clear are empty");
    }

    /**
     * Randomized put/remove/removeByKey/removeByValue sequence against a reference model, with
     * the cross-map invariant asserted after every step: a pair present in one direction must be
     * present in the other, sizes must match the model, and no orphan entries may survive.
     */
    private static void fuzzAgainstReferenceModel() {
        Random rng = new Random(0x5EEDL);
        List<RecipeKey> keys = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            keys.add(new RecipeKey("recipe_" + i));
        }
        BiMultiMap map = new BiMultiMap();
        Map<String, TreeSet<Integer>> model = new HashMap<>();
        Map<Integer, HashSet<String>> inverse = new HashMap<>();

        for (int step = 0; step < 5000; step++) {
            RecipeKey key = keys.get(rng.nextInt(keys.size()));
            int value = rng.nextInt(6);
            int op = rng.nextInt(10);
            if (op < 5) {
                map.put(key, value);
                model.computeIfAbsent(key.id, k -> new TreeSet<>()).add(value);
                inverse.computeIfAbsent(value, v -> new HashSet<>()).add(key.id);
            } else if (op < 7) {
                map.remove(key, value);
                removeFromModel(model, inverse, key.id, value);
            } else if (op == 7) {
                map.removeByKey(key);
                TreeSet<Integer> values = model.remove(key.id);
                if (values != null) {
                    values.forEach(v -> removeFromInverse(inverse, key.id, v));
                }
            } else if (op == 8) {
                map.removeByValue(value);
                HashSet<String> owners = inverse.remove(value);
                if (owners != null) {
                    owners.forEach(id -> removeFromKey(model, id, value));
                }
            }
            assertConsistent(map, model, inverse, step);
        }
    }

    private static void removeFromModel(Map<String, TreeSet<Integer>> model, Map<Integer, HashSet<String>> inverse,
                                        String id, int value) {
        removeFromKey(model, id, value);
        removeFromInverse(inverse, id, value);
    }

    private static void removeFromKey(Map<String, TreeSet<Integer>> model, String id, int value) {
        TreeSet<Integer> values = model.get(id);
        if (values != null) {
            values.remove(value);
            if (values.isEmpty()) {
                model.remove(id);
            }
        }
    }

    private static void removeFromInverse(Map<Integer, HashSet<String>> inverse, String id, int value) {
        HashSet<String> owners = inverse.get(value);
        if (owners != null) {
            owners.remove(id);
            if (owners.isEmpty()) {
                inverse.remove(value);
            }
        }
    }

    private static void assertConsistent(BiMultiMap map, Map<String, TreeSet<Integer>> model,
                                         Map<Integer, HashSet<String>> inverse, int step) {
        int modelSize = model.values().stream().mapToInt(TreeSet::size).sum();
        check(map.size() == modelSize, "fuzz step " + step + ": size matches model");
        check(map.keySet().size() == model.size(), "fuzz step " + step + ": key count matches model");
        check(map.valueSet().size() == inverse.size(), "fuzz step " + step + ": value count matches model");
        for (RecipeKey key : map.keySet()) {
            TreeSet<Integer> expected = model.get(key.id);
            check(expected != null && expected.size() == map.getValues(key).size(),
                    "fuzz step " + step + ": key " + key.id + " value set matches model");
            for (int value : map.getValues(key)) {
                check(inverse.get(value) != null && inverse.get(value).contains(key.id),
                        "fuzz step " + step + ": pair " + key.id + "->" + value + " missing from inverse");
            }
        }
        for (int value : map.valueSet()) {
            HashSet<String> owners = inverse.get(value);
            check(owners != null && owners.size() == map.getKeys(value).size(),
                    "fuzz step " + step + ": value " + value + " key set matches model");
            for (RecipeKey key : map.getKeys(value)) {
                check(model.get(key.id) != null && model.get(key.id).contains(value),
                        "fuzz step " + step + ": pair " + key.id + "->" + value + " missing from keyToValues");
            }
        }
        int[] visited = { 0 };
        map.forEach((key, value) -> visited[0]++);
        check(visited[0] == modelSize, "fuzz step " + step + ": forEach visits exactly size() pairs");
    }

    // ---- byte-for-byte mirror of GTRecipe2IntBiMultiMap (GTRecipe -> RecipeKey). Keep in sync! ----

    private static final class BiMultiMap {

        private final Object2ReferenceMap<RecipeKey, IntSet> keyToValues = new Object2ReferenceOpenHashMap<>();
        private final Int2ReferenceMap<ObjectSet<RecipeKey>> valueToKeys = new Int2ReferenceOpenHashMap<>();

        public void put(RecipeKey key, int value) {
            ((IntSet) this.keyToValues.computeIfAbsent(key, (k) -> new IntArraySet())).add(value);
            ((ObjectSet) this.valueToKeys.computeIfAbsent(value, (v) -> new ObjectArraySet())).add(key);
        }

        public IntSet getValues(RecipeKey key) {
            return (IntSet) this.keyToValues.getOrDefault(key, IntSets.emptySet());
        }

        public ObjectSet<RecipeKey> getKeys(int value) {
            return (ObjectSet) this.valueToKeys.getOrDefault(value, ObjectSets.emptySet());
        }

        public void remove(RecipeKey key, int value) {
            Optional.ofNullable((IntSet) this.keyToValues.get(key)).ifPresent((set) -> {
                set.remove(value);
                if (set.isEmpty()) {
                    this.keyToValues.remove(key);
                }

            });
            Optional.ofNullable((ObjectSet) this.valueToKeys.get(value)).ifPresent((set) -> {
                set.remove(key);
                if (set.isEmpty()) {
                    this.valueToKeys.remove(value);
                }

            });
        }

        public void removeByKey(RecipeKey key) {
            IntSet values = (IntSet) this.keyToValues.remove(key);
            if (values != null) {
                IntIterator var3 = values.iterator();

                while (var3.hasNext()) {
                    int v = (Integer) var3.next();
                    ObjectSet<RecipeKey> ks = (ObjectSet) this.valueToKeys.get(v);
                    if (ks != null) {
                        ks.remove(key);
                        if (ks.isEmpty()) {
                            this.valueToKeys.remove(v);
                        }
                    }
                }
            }
        }

        public void removeByValue(int value) {
            ObjectSet<RecipeKey> keys = (ObjectSet) this.valueToKeys.remove(value);
            if (keys != null) {
                ObjectIterator var3 = keys.iterator();

                while (var3.hasNext()) {
                    RecipeKey k = (RecipeKey) var3.next();
                    IntSet vs = (IntSet) this.keyToValues.get(k);
                    if (vs != null) {
                        vs.remove(value);
                        if (vs.isEmpty()) {
                            this.keyToValues.remove(k);
                        }
                    }
                }
            }
        }

        public ObjectSet<RecipeKey> keySet() {
            return ObjectSets.unmodifiable(this.keyToValues.keySet());
        }

        public IntSet valueSet() {
            return IntSets.unmodifiable(this.valueToKeys.keySet());
        }

        public void clear() {
            this.keyToValues.clear();
            this.valueToKeys.clear();
        }

        public int size() {
            return this.keyToValues.values().stream().mapToInt(Set::size).sum();
        }

        public void forEach(BiConsumer<RecipeKey, Integer> action) {
            ObjectIterator var2 = this.keyToValues.entrySet().iterator();

            while (var2.hasNext()) {
                Map.Entry<RecipeKey, IntSet> entry = (Map.Entry) var2.next();
                RecipeKey key = (RecipeKey) entry.getKey();
                IntIterator var5 = ((IntSet) entry.getValue()).iterator();

                while (var5.hasNext()) {
                    int value = (Integer) var5.next();
                    action.accept(key, value);
                }
            }
        }
    }

    private static final class RecipeKey {

        private final String id;

        private RecipeKey(String id) {
            this.id = id;
        }

        // Mirrors GTRecipe.equals/hashCode: identity by recipe id.
        @Override
        public boolean equals(Object obj) {
            return obj instanceof RecipeKey other && this.id.equals(other.id);
        }

        @Override
        public int hashCode() {
            return this.id.hashCode();
        }

        @Override
        public String toString() {
            return this.id;
        }
    }

    // ---- helpers ----

    private static int caseNum = 0;

    private static void check(boolean condition, String name) {
        caseNum++;
        if (!condition) {
            throw new AssertionError("GTRecipe2IntBiMultiMapTest case #" + caseNum + " '" + name + "' failed");
        }
    }
}
