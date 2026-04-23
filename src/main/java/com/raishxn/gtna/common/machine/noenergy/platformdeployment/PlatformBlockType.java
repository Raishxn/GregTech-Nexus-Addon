package com.raishxn.gtna.common.machine.noenergy.platformdeployment;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class PlatformBlockType {

    private PlatformBlockType() {}

    public record PlatformBlockStructure(
            String name,
            @Nullable String type,
            @Nullable String displayName,
            @Nullable String description,
            @Nullable String source,
            boolean preview,
            ResourceLocation resource,
            ResourceLocation blockMapping,
            int[] materials,
            List<PlatformSupport.Counted<ItemStack>> extraMaterials,
            int xSize,
            int ySize,
            int zSize) {

        public PlatformBlockStructure {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(resource, "resource");
            Objects.requireNonNull(blockMapping, "blockMapping");
            Objects.requireNonNull(materials, "materials");
            Objects.requireNonNull(extraMaterials, "extraMaterials");
            materials = Arrays.copyOf(materials, materials.length);
            extraMaterials = ImmutableList.copyOf(extraMaterials);
            if (xSize % 16 != 0) throw new IllegalArgumentException("X size must be multiple of 16");
            if (zSize % 16 != 0) throw new IllegalArgumentException("Z size must be multiple of 16");
        }

        public static Builder structure(String name) {
            return new Builder(name);
        }

        public static final class Builder {

            private final String name;
            private String type = "default";
            private String displayName;
            private String description;
            private String source;
            private boolean preview;
            private ResourceLocation resource;
            private ResourceLocation symbolMap;
            private final int[] materials = new int[] { 0, 0, 0 };
            private final List<PlatformSupport.Counted<ItemStack>> extraMaterials = new ArrayList<>();

            private Builder(String name) {
                this.name = name;
            }

            public Builder type(String type) {
                this.type = type;
                return this;
            }

            public Builder displayName(@Nullable String displayName) {
                this.displayName = displayName;
                return this;
            }

            public Builder description(@Nullable String description) {
                this.description = description;
                return this;
            }

            public Builder source(@Nullable String source) {
                this.source = source;
                return this;
            }

            public Builder preview(boolean preview) {
                this.preview = preview;
                return this;
            }

            public Builder resource(ResourceLocation resource) {
                this.resource = resource;
                return this;
            }

            public Builder symbolMap(ResourceLocation symbolMap) {
                this.symbolMap = symbolMap;
                return this;
            }

            public Builder materials(int index, int amount) {
                this.materials[index] = amount;
                return this;
            }

            public Builder extraMaterials(String itemId, int amount) {
                extraMaterials.add(new PlatformSupport.Counted<>(amount, PlatformSupport.itemStack(itemId)));
                return this;
            }

            public Builder extraMaterials(Item item, int amount) {
                extraMaterials.add(new PlatformSupport.Counted<>(amount, new ItemStack(item)));
                return this;
            }

            public Builder extraMaterials(ItemStack stack, int amount) {
                ItemStack copy = stack.copy();
                copy.setCount(1);
                extraMaterials.add(new PlatformSupport.Counted<>(amount, copy));
                return this;
            }

            public PlatformBlockStructure build() {
                Objects.requireNonNull(resource, "resource");
                Objects.requireNonNull(symbolMap, "symbolMap");

                int[] sizes;
                String resourcePath = "assets/" + resource.getNamespace() + "/" + resource.getPath();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        Objects.requireNonNull(PlatformBlockType.class.getClassLoader().getResourceAsStream(resourcePath),
                                "Missing resource: " + resourcePath)))) {
                    String line = reader.readLine();
                    if (line == null) {
                        throw new IOException("Empty structure file: " + resource);
                    }
                    line = line.trim();
                    if (!line.startsWith(".size(") || line.charAt(line.length() - 1) != ')') {
                        throw new IOException("Missing .size(...) definition in: " + resource);
                    }
                    String[] parts = line.substring(6, line.length() - 1).split(",");
                    sizes = new int[] {
                            Integer.parseInt(parts[0].trim()),
                            Integer.parseInt(parts[1].trim()),
                            Integer.parseInt(parts[2].trim())
                    };
                } catch (IOException exception) {
                    throw new IllegalStateException("Failed to read structure size for " + resource, exception);
                }

                return new PlatformBlockStructure(
                        name,
                        type,
                        displayName,
                        description,
                        source,
                        preview,
                        resource,
                        symbolMap,
                        materials,
                        extraMaterials,
                        sizes[0],
                        sizes[1],
                        sizes[2]);
            }
        }
    }

    public record PlatformPreset(
            String name,
            @Nullable String displayName,
            @Nullable String description,
            @Nullable String source,
            List<PlatformBlockStructure> structures) {

        public PlatformPreset {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(structures, "structures");
            if (structures.isEmpty()) {
                throw new IllegalArgumentException("structures must not be empty");
            }
            structures = ImmutableList.copyOf(structures);
        }

        public static PresetBuilder preset(String name) {
            return new PresetBuilder(name);
        }

        public static final class PresetBuilder {

            private final String name;
            private String displayName;
            private String description;
            private String source;
            private final List<PlatformBlockStructure> structures = new ArrayList<>();

            private PresetBuilder(String name) {
                this.name = name;
            }

            public PresetBuilder displayName(@Nullable String displayName) {
                this.displayName = displayName;
                return this;
            }

            public PresetBuilder description(@Nullable String description) {
                this.description = description;
                return this;
            }

            public PresetBuilder source(@Nullable String source) {
                this.source = source;
                return this;
            }

            public PresetBuilder addStructure(PlatformBlockStructure structure) {
                if (structure != null) {
                    structures.add(structure);
                }
                return this;
            }

            public PlatformPreset build() {
                return new PlatformPreset(name, displayName, description, source, structures);
            }
        }
    }
}
