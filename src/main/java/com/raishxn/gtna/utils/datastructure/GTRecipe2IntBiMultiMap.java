package com.raishxn.gtna.utils.datastructure;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

public class GTRecipe2IntBiMultiMap {
    private final Object2ReferenceMap<GTRecipe, IntSet> keyToValues = new Object2ReferenceOpenHashMap();
    private final Int2ReferenceMap<ObjectSet<@NotNull GTRecipe>> valueToKeys = new Int2ReferenceOpenHashMap();

    public void put(@NotNull GTRecipe key, int value) {
        ((IntSet)this.keyToValues.computeIfAbsent(key, (k) -> new IntArraySet())).add(value);
        ((ObjectSet)this.valueToKeys.computeIfAbsent(value, (v) -> new ObjectArraySet())).add(key);
    }

    public IntSet getValues(GTRecipe key) {
        return (IntSet)this.keyToValues.getOrDefault(key, IntSets.emptySet());
    }

    public ObjectSet<GTRecipe> getKeys(int value) {
        return (ObjectSet)this.valueToKeys.getOrDefault(value, ObjectSets.emptySet());
    }

    public void remove(GTRecipe key, int value) {
        Optional.ofNullable((IntSet)this.keyToValues.get(key)).ifPresent((set) -> {
            set.remove(value);
            if (set.isEmpty()) {
                this.keyToValues.remove(key);
            }

        });
        Optional.ofNullable((ObjectSet)this.valueToKeys.get(value)).ifPresent((set) -> {
            set.remove(key);
            if (set.isEmpty()) {
                this.valueToKeys.remove(value);
            }

        });
    }

    public void removeByKey(GTRecipe key) {
        IntSet values = (IntSet)this.keyToValues.remove(key);
        if (values != null) {
            IntIterator var3 = values.iterator();

            while(var3.hasNext()) {
                int v = (Integer)var3.next();
                ObjectSet<GTRecipe> ks = (ObjectSet)this.valueToKeys.get(v);
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
        ObjectSet<GTRecipe> keys = (ObjectSet)this.valueToKeys.remove(value);
        if (keys != null) {
            ObjectIterator var3 = keys.iterator();

            while(var3.hasNext()) {
                GTRecipe k = (GTRecipe)var3.next();
                IntSet vs = (IntSet)this.keyToValues.get(k);
                if (vs != null) {
                    vs.remove(value);
                    if (vs.isEmpty()) {
                        this.keyToValues.remove(k);
                    }
                }
            }
        }

    }

    public ObjectSet<GTRecipe> keySet() {
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

    public void forEach(BiConsumer<GTRecipe, Integer> action) {
        ObjectIterator var2 = this.keyToValues.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<GTRecipe, IntSet> entry = (Map.Entry)var2.next();
            GTRecipe key = (GTRecipe)entry.getKey();
            IntIterator var5 = ((IntSet)entry.getValue()).iterator();

            while(var5.hasNext()) {
                int value = (Integer)var5.next();
                action.accept(key, value);
            }
        }

    }
}
