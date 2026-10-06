package com.raishxn.gtna;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The research display depends on two client mixins that no GameTest can run. This checks what can be
 * checked without a client: that the GTCEu and LDLib bytecode still has the exact shape the mixins aim at,
 * and that the mixins are written and registered as the code expects. A GTCEu or LDLib update that moves the
 * target fails here instead of crashing the client at startup.
 */
public final class ClientMixinContractTest {

    private static final String WIDGET = "com/gregtechceu/gtceu/integration/xei/widgets/GTRecipeWidget";
    private static final String EMI_RECIPE = "com/lowdragmc/lowdraglib/emi/ModularEmiRecipe";
    private static final String GT_EMI_RECIPE = "com/gregtechceu/gtceu/integration/emi/recipe/GTEmiRecipe";

    private ClientMixinContractTest() {}

    public static void main(String[] args) throws IOException {
        redirectTargetStillHasTwoConditionReads();
        redirectIsDeclaredOnTheSecondRead();
        emiHeightHookTargetsRealMethods();
        planetaryEmiScreenHookTargetsSelectedTabBeforeLayout();
        mixinsAreRegisteredAsClientMixins();
    }

    private static byte[] read(String internalName) throws IOException {
        try (InputStream stream = ClientMixinContractTest.class.getClassLoader()
                .getResourceAsStream(internalName + ".class")) {
            if (stream == null) throw new AssertionError("class not on the test classpath: " + internalName);
            return stream.readAllBytes();
        }
    }

    /** {@code GTRecipeWidget.setRecipeWidget} must read {@code GTRecipe.conditions} exactly twice. */
    private static void redirectTargetStillHasTwoConditionReads() throws IOException {
        int[] reads = { 0 };
        boolean[] found = { false };
        new ClassReader(read(WIDGET)).accept(new ClassVisitor(Opcodes.ASM9) {

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                             String[] exceptions) {
                if (!name.equals("setRecipeWidget") || !descriptor.equals("()V")) return null;
                found[0] = true;
                return new MethodVisitor(Opcodes.ASM9) {

                    @Override
                    public void visitFieldInsn(int opcode, String owner, String field, String fieldDescriptor) {
                        if (opcode == Opcodes.GETFIELD && owner.equals("com/gregtechceu/gtceu/api/recipe/GTRecipe") &&
                                field.equals("conditions") && fieldDescriptor.equals("Ljava/util/List;")) {
                            reads[0]++;
                        }
                    }
                };
            }
        }, 0);
        check(found[0], "GTRecipeWidget.setRecipeWidget()V no longer exists");
        check(reads[0] == 2, "GTRecipeWidget.setRecipeWidget reads GTRecipe.conditions " + reads[0] +
                " times; GTRecipeWidgetMixin redirects ordinal 1 of exactly 2");
    }

    /** Our redirect must say {@code ordinal = 1} on that field, in that method. */
    private static void redirectIsDeclaredOnTheSecondRead() throws IOException {
        String[] method = { null };
        String[] target = { null };
        int[] ordinal = { -1 };
        new ClassReader(read("com/raishxn/gtna/mixin/client/GTRecipeWidgetMixin"))
                .accept(new ClassVisitor(Opcodes.ASM9) {

                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                                     String[] exceptions) {
                        if (!name.equals("gtna$listResearchConditions")) return null;
                        return new MethodVisitor(Opcodes.ASM9) {

                            @Override
                            public AnnotationVisitor visitAnnotation(String annotation, boolean visible) {
                                if (!annotation.equals("Lorg/spongepowered/asm/mixin/injection/Redirect;")) return null;
                                return new AnnotationVisitor(Opcodes.ASM9) {

                                    @Override
                                    public AnnotationVisitor visitArray(String name) {
                                        if (!name.equals("method")) return null;
                                        return new AnnotationVisitor(Opcodes.ASM9) {

                                            @Override
                                            public void visit(String n, Object value) {
                                                method[0] = (String) value;
                                            }
                                        };
                                    }

                                    @Override
                                    public AnnotationVisitor visitAnnotation(String name, String d) {
                                        if (!name.equals("at")) return null;
                                        return new AnnotationVisitor(Opcodes.ASM9) {

                                            @Override
                                            public void visit(String key, Object value) {
                                                if (key.equals("target")) target[0] = (String) value;
                                                if (key.equals("ordinal")) ordinal[0] = (Integer) value;
                                            }
                                        };
                                    }
                                };
                            }
                        };
                    }
                }, ClassReader.SKIP_CODE);
        check("setRecipeWidget".equals(method[0]), "the redirect must target setRecipeWidget, was " + method[0]);
        check("Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;conditions:Ljava/util/List;".equals(target[0]),
                "the redirect must target GTRecipe.conditions, was " + target[0]);
        check(ordinal[0] == 1, "the redirect must use ordinal 1, was " + ordinal[0]);
    }

    /** The EMI height hook needs {@code getDisplayHeight()I} on LDLib's class and {@code getId} on GTCEu's. */
    private static void emiHeightHookTargetsRealMethods() throws IOException {
        check(declares(EMI_RECIPE, "getDisplayHeight", "()I"), "ModularEmiRecipe.getDisplayHeight()I is gone");
        check(declares(GT_EMI_RECIPE, "getId", "()Lnet/minecraft/resources/ResourceLocation;"),
                "GTEmiRecipe.getId() is gone; ModularEmiRecipeMixin reads the recipe id with it");
        String mixin = new String(read("com/raishxn/gtna/mixin/client/ModularEmiRecipeMixin"),
                StandardCharsets.ISO_8859_1);
        check(mixin.contains("com.lowdragmc.lowdraglib.emi.ModularEmiRecipe"), "mixin target string changed");
        check(mixin.contains("com.gregtechceu.gtceu.integration.emi.recipe.GTEmiRecipe"),
                "the class name the mixin compares against changed");
        check(mixin.contains("getDisplayHeight"), "the injector no longer names getDisplayHeight");
    }

    private static void planetaryEmiScreenHookTargetsSelectedTabBeforeLayout() throws IOException {
        String screen = "dev/emi/emi/screen/RecipeScreen";
        check(declares(screen, "setPage", "(III)V"), "EMI RecipeScreen.setPage target is gone");
        check(declares("dev/emi/emi/screen/RecipeTab", "getPageCount", "()I"), "EMI RecipeTab page count is gone");
        check(declares("dev/emi/emi/screen/RecipeTab", "bakePages", "(I)V"), "EMI RecipeTab pagination is gone");
        boolean[] selectedBeforeCount = { false };
        new ClassReader(read(screen)).accept(new ClassVisitor(Opcodes.ASM9) {

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                             String[] exceptions) {
                if (!name.equals("setPage")) return null;
                return new MethodVisitor(Opcodes.ASM9) {

                    private boolean tabAssigned;
                    private boolean firstCount = true;

                    @Override
                    public void visitFieldInsn(int opcode, String owner, String field, String desc) {
                        if (opcode == Opcodes.PUTFIELD && owner.equals(screen) && field.equals("tab"))
                            tabAssigned = true;
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String method, String desc, boolean iface) {
                        if (owner.equals("dev/emi/emi/screen/RecipeTab") && method.equals("getPageCount") &&
                                firstCount) {
                            selectedBeforeCount[0] = tabAssigned;
                            firstCount = false;
                        }
                    }
                };
            }
        }, 0);
        check(selectedBeforeCount[0], "EOH sizing must run after the selected tab is assigned");
        String mixin = new String(read("com/raishxn/gtna/mixin/client/EyeOfHarmonyEmiScreenMixin"),
                StandardCharsets.ISO_8859_1);
        check(mixin.contains("Lorg/spongepowered/asm/mixin/Pseudo;"), "optional EMI hook must be @Pseudo");
        check(mixin.contains("eye_of_harmony"), "sizing hook must retain the EOH category filter");
    }

    private static boolean declares(String internalName, String method, String descriptor) throws IOException {
        List<String> methods = new ArrayList<>();
        new ClassReader(read(internalName)).accept(new ClassVisitor(Opcodes.ASM9) {

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                                             String[] exceptions) {
                methods.add(name + desc);
                return null;
            }
        }, ClassReader.SKIP_CODE);
        return methods.contains(method + descriptor);
    }

    private static void mixinsAreRegisteredAsClientMixins() throws IOException {
        String json;
        try (InputStream stream = ClientMixinContractTest.class.getClassLoader()
                .getResourceAsStream("gtna.mixins.json")) {
            check(stream != null, "gtna.mixins.json is missing");
            json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        int clientStart = json.indexOf("\"client\"");
        check(clientStart > 0, "gtna.mixins.json has no client section");
        String client = json.substring(clientStart, json.indexOf(']', clientStart));
        Set<String> required = new HashSet<>(Set.of("client.GTRecipeWidgetMixin", "client.ModularEmiRecipeMixin",
                "client.EyeOfHarmonyEmiScreenMixin"));
        required.removeIf(client::contains);
        check(required.isEmpty(), "not registered in the client mixin list: " + required);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError("ClientMixinContractTest: " + message);
        // Keep the output readable when the run passes.
    }
}
