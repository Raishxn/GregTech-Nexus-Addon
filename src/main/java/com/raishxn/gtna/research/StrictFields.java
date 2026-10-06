package com.raishxn.gtna.research;

import com.mojang.serialization.*;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Optional JSON fields that still fail when present but invalid. The stock {@code optionalFieldOf}
 * swallows the error and uses the default, which would let a node with a broken grant load without
 * its gate.
 */
final class StrictFields {

    private StrictFields() {}

    static <T> MapCodec<T> optional(String name, Codec<T> codec, T fallback) {
        return new MapCodec<>() {

            @Override
            public <O> Stream<O> keys(DynamicOps<O> ops) {
                return Stream.of(ops.createString(name));
            }

            @Override
            public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input) {
                O value = input.get(name);
                return value == null ? DataResult.success(fallback) : codec.parse(ops, value);
            }

            @Override
            public <O> RecordBuilder<O> encode(T value, DynamicOps<O> ops, RecordBuilder<O> prefix) {
                return prefix.add(name, codec.encodeStart(ops, value));
            }
        };
    }

    static <T> MapCodec<Optional<T>> optional(String name, Codec<T> codec) {
        return new MapCodec<>() {

            @Override
            public <O> Stream<O> keys(DynamicOps<O> ops) {
                return Stream.of(ops.createString(name));
            }

            @Override
            public <O> DataResult<Optional<T>> decode(DynamicOps<O> ops, MapLike<O> input) {
                O value = input.get(name);
                return value == null ? DataResult.success(Optional.empty()) :
                        codec.parse(ops, value).map(Optional::of);
            }

            @Override
            public <O> RecordBuilder<O> encode(Optional<T> value, DynamicOps<O> ops, RecordBuilder<O> prefix) {
                return value.isPresent() ? prefix.add(name, codec.encodeStart(ops, value.get())) : prefix;
            }
        };
    }
}
