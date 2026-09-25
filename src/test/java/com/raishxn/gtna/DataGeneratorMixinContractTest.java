package com.raishxn.gtna;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/** Checks the compiled injector contract that previously crashed production clients. */
public final class DataGeneratorMixinContractTest {

    private DataGeneratorMixinContractTest() {}

    public static void main(String[] args) throws IOException {
        Set<String> targets = new HashSet<>();
        int[] require = { -1 };
        boolean[] remap = { true };
        String resource = "com/raishxn/gtna/mixin/kubejs/DataGeneratorMixin.class";
        try (var stream = DataGeneratorMixinContractTest.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) throw new AssertionError("missing compiled DataGeneratorMixin");
            new ClassReader(stream).accept(new ClassVisitor(Opcodes.ASM9) {

                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                 String signature, String[] exceptions) {
                    if (!name.equals("gtna$stopKubeJSBackgroundThread")) return null;
                    return new MethodVisitor(Opcodes.ASM9) {

                        @Override
                        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                            if (!descriptor.equals("Lorg/spongepowered/asm/mixin/injection/Inject;")) return null;
                            return new AnnotationVisitor(Opcodes.ASM9) {

                                @Override
                                public void visit(String name, Object value) {
                                    if (name.equals("require")) require[0] = (Integer) value;
                                    if (name.equals("remap")) remap[0] = (Boolean) value;
                                }

                                @Override
                                public AnnotationVisitor visitArray(String name) {
                                    if (!name.equals("method")) return null;
                                    return new AnnotationVisitor(Opcodes.ASM9) {

                                        @Override
                                        public void visit(String name, Object value) {
                                            targets.add((String) value);
                                        }
                                    };
                                }
                            };
                        }
                    };
                }
            }, ClassReader.SKIP_CODE);
        }
        if (!targets.equals(Set.of("run", "m_123917_")) || require[0] != 0 || remap[0]) {
            throw new AssertionError(
                    "DataGenerator injection must target dev and SRG names, be optional, and avoid refmap: " +
                            targets + ", require=" + require[0] + ", remap=" + remap[0]);
        }
    }
}
