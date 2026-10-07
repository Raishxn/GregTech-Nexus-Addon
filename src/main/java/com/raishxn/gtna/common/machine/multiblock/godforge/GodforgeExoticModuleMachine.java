package com.raishxn.gtna.common.machine.multiblock.godforge;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import com.raishxn.gtna.api.capability.WirelessEnergyManager;
import com.raishxn.gtna.api.machine.feature.godforge.GodforgeModuleStats.Type;
import com.raishxn.gtna.api.machine.feature.godforge.GodforgeOverclock;
import com.raishxn.gtna.common.data.GTNAMaterials;
import com.raishxn.gtna.common.data.material.GodforgeMaterials;
import com.raishxn.gtna.utils.datastructure.Int128;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Heliofusion Exoticizer, from GTNH {@code MTEExoticModule}: draws a random set of up to seven plasmas, hands out the
 * raw materials, waits for exactly those plasma amounts in its input hatches and then makes Quark Gluon Plasma (or
 * Magmatter in Magmatter mode) for each parallel, paying the energy from the wireless network.
 */
public class GodforgeExoticModuleMachine extends GodforgeModuleMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            GodforgeExoticModuleMachine.class, GodforgeModuleMachine.MANAGED_FIELD_HOLDER);

    public static final int NUMBER_OF_INPUTS = 7;
    public static final int RECIPE_DURATION = 200;

    /** GTNH {@code plasmaGTMaterialList} with its weights; materials missing in GTCEu are skipped. */
    private static final Map<String, Integer> QGP_WEIGHTS = new LinkedHashMap<>();
    /** GTNH {@code exoticModuleMagmatterItemMap} (all weights equal). */
    private static final List<String> MAGMATTER_MATERIALS = List.of("neutronium", "flerovium", "infinity",
            "tritanium");

    static {
        String[] names = { "aluminium", "americium", "antimony", "argon", "arsenic", "barium", "beryllium", "caesium",
                "calcium", "cadmium", "carbon", "cerium", "chlorine", "cobalt", "copper", "deuterium", "dysprosium",
                "erbium", "europium", "fluorine", "gadolinium", "gallium", "gold", "helium", "holmium", "hydrogen",
                "indium", "iron", "lanthanum", "lithium", "lutetium", "magnesium", "manganese", "molybdenum",
                "neodymium", "nickel", "niobium", "nitrogen", "palladium", "phosphorus", "potassium", "praseodymium",
                "promethium", "radon", "rubidium", "samarium", "silicon", "silver", "sodium", "strontium", "sulfur",
                "tantalum", "tellurium", "terbium", "thulium", "tin", "titanium", "tritium", "tungsten", "uranium_235",
                "uranium_238", "vanadium", "ytterbium", "yttrium", "zinc", "zirconium", "thorium", "germanium",
                "thallium", "ruthenium", "rhenium", "rhodium", "hafnium", "curium", "iodine", "mercury" };
        int[] weights = { 6000, 10000, 6000, 6000, 6000, 6000, 6000, 6000, 10000, 6000, 6000, 6000, 6000, 6000, 6000,
                6000, 2000, 2000, 6000, 6000, 2000, 6000, 6000, 10000, 6000, 10000, 6000, 10000, 6000, 6000, 6000,
                6000, 6000, 6000, 6000, 10000, 10000, 10000, 6000, 6000, 6000, 6000, 2000, 10000, 2000, 6000, 6000,
                10000, 6000, 2000, 10000, 6000, 2000, 1000, 6000, 10000, 10000, 6000, 6000, 6000, 6000, 6000, 2000,
                6000, 6000, 6000, 6000, 2000, 2000, 6000, 2000, 6000, 6000, 10000, 6000, 6000 };
        for (int i = 0; i < names.length; i++) QGP_WEIGHTS.put(names[i], weights[i]);
    }

    /** One required plasma: fluid registry ID and exact amount in mB. */
    public record Requirement(ResourceLocation fluid, long amount) {}

    @Persisted
    @DescSynced
    private boolean magmatterMode;
    @DescSynced
    private int progress;
    @DescSynced
    private int duration;

    private final List<Requirement> requirements = new ArrayList<>();
    private boolean running;
    private long outputAmount;
    private final RandomSource random = RandomSource.create();
    @Nullable
    private TickableSubscription exoticTick;

    public GodforgeExoticModuleMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, Type.EXOTIC, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public boolean isMagmatterMode() {
        return magmatterMode;
    }

    /** Switching mode is only possible between recipes, as in the original GUI. */
    public void setMagmatterMode(boolean enabled) {
        if (running) return;
        magmatterMode = enabled;
        godforgeStats().magmatterMode = enabled;
        requirements.clear();
        markDirty();
    }

    /** Screwdriver toggles Magmatter mode once the forge has unlocked it (Effortless Existence upgrade). */
    @Override
    protected net.minecraft.world.InteractionResult onScrewdriverClick(net.minecraft.world.entity.player.Player player,
                                                                       net.minecraft.world.InteractionHand hand,
                                                                       net.minecraft.core.Direction gridSide,
                                                                       net.minecraft.world.phys.BlockHitResult hit) {
        if (isRemote()) return net.minecraft.world.InteractionResult.SUCCESS;
        if (!godforgeStats().magmatterCapable && !magmatterMode) {
            player.sendSystemMessage(Component.translatable("gtna.godforge.exotic.mode.locked"));
            return net.minecraft.world.InteractionResult.CONSUME;
        }
        setMagmatterMode(!magmatterMode);
        player.sendSystemMessage(Component.translatable(magmatterMode ? "gtna.godforge.exotic.mode.magmatter" :
                "gtna.godforge.exotic.mode.qgp"));
        return net.minecraft.world.InteractionResult.CONSUME;
    }

    /** GTNH magmatter mode button (locked without Effortless Existence). */
    @Override
    public void attachConfigurators(com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel panel) {
        super.attachConfigurators(panel);
        panel.attachConfigurators(new com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton.Toggle(
                new com.lowdragmc.lowdraglib.gui.texture.TextTexture("QGP"),
                new com.lowdragmc.lowdraglib.gui.texture.TextTexture("MM"),
                () -> magmatterMode, (click, pressed) -> {
                    if (godforgeStats().magmatterCapable || magmatterMode) setMagmatterMode(pressed);
                }).setTooltipsSupplier(pressed -> List.of(Component.translatable(pressed ?
                        "gtna.godforge.button.magmattermode.tooltip.02" :
                        godforgeStats().magmatterCapable ? "gtna.godforge.button.magmattermode.tooltip.01" :
                                "gtna.godforge.button.magmattermode.tooltip.03"))));
        panel.attachConfigurators(new com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton.Toggle(
                com.raishxn.gtna.common.machine.multiblock.godforge.gui.GodforgeGui.OVERLAY_REFRESH,
                com.raishxn.gtna.common.machine.multiblock.godforge.gui.GodforgeGui.OVERLAY_REFRESH,
                () -> false, (click, pressed) -> refreshRecipe())
                .setTooltipsSupplier(pressed -> List.of(
                        Component.translatable("gtna.godforge.button.reciperefresh.tooltip"),
                        Component.translatable("gtna.godforge.button.refreshtimer.tooltip").append(" " +
                                refreshCooldownSeconds() + " ").append(Component.translatable(
                                        "gtna.godforge.button.seconds")))));
    }

    /** {@code RECIPE_REFRESH_LIMIT}: a drawn recipe can be replaced once per minute. */
    public static final int RECIPE_REFRESH_LIMIT = 60 * 20;
    @Persisted
    private long lastRefresh;

    /** {@code refreshRecipe}: draws a new input set when the module is idle and the cooldown has passed. */
    public boolean refreshRecipe() {
        if (running || getLevel() == null) return false;
        long now = getLevel().getGameTime();
        if (now - lastRefresh <= RECIPE_REFRESH_LIMIT) return false;
        generate();
        lastRefresh = now;
        return true;
    }

    public long refreshCooldownSeconds() {
        if (getLevel() == null) return 0;
        return Math.max(0, (RECIPE_REFRESH_LIMIT - (getLevel().getGameTime() - lastRefresh)) / 20);
    }

    public List<Requirement> requirements() {
        return List.copyOf(requirements);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        godforgeStats().magmatterMode = magmatterMode;
        exoticTick = subscribeServerTick(exoticTick, this::exoticTick);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        if (exoticTick != null) {
            exoticTick.unsubscribe();
            exoticTick = null;
        }
    }

    private void exoticTick() {
        if (!isFormed() || !(getLevel() instanceof ServerLevel level)) return;
        if (running) {
            if (++progress >= duration) finish();
            return;
        }
        if (!isConnected() || getOffsetTimer() % 20 != 0) return;
        if (requirements.isEmpty()) {
            generate();
            return;
        }
        if (!inputsMatch()) return;
        long parallel = actualParallel();
        GodforgeOverclock calc = new GodforgeOverclock();
        calc.recipeEUt = GTValues.VA[GTValues.MAX];
        calc.duration = RECIPE_DURATION;
        calc.machineVoltage = Math.min(godforgeStats().processingVoltage, Long.MAX_VALUE);
        calc.machineAmperage = Integer.MAX_VALUE;
        calc.durationModifier = godforgeStats().speedBonus;
        calc.eutModifier = godforgeStats().energyDiscount;
        calc.durationDecreasePerOC = godforgeStats().overclockTimeFactor;
        calc.calculate();
        BigInteger energy = BigInteger.valueOf(calc.consumption()).multiply(BigInteger.valueOf(
                calc.calculatedDuration())).multiply(BigInteger.valueOf(parallel));
        java.util.UUID owner = networkOwner();
        if (owner == null || !WirelessEnergyManager.consumeDirectEnergy(level, owner, Int128.fromBigInteger(energy),
                GlobalPos.of(level.dimension(), getPos()), "godforge_exotic_module")) {
            return;
        }
        consumeInputs();
        godforgeStats().powerTally = godforgeStats().powerTally.add(energy);
        godforgeStats().recipeTally += parallel;
        outputAmount = (magmatterMode ? 576L : 1000L) * parallel;
        duration = calc.calculatedDuration();
        progress = 0;
        running = true;
        markDirty();
    }

    /** Draws a new plasma set and hands out the raw materials, as {@code generateQuarkGluonRecipe}. */
    private void generate() {
        requirements.clear();
        if (magmatterMode) {
            List<Material> candidates = new ArrayList<>();
            for (String name : MAGMATTER_MATERIALS) {
                Material material = GodforgeMaterials.material(name);
                if (material != null && plasma(material) != null && material.hasProperty(PropertyKey.DUST))
                    candidates.add(material);
            }
            if (candidates.isEmpty()) return;
            Material item = candidates.get(random.nextInt(candidates.size()));
            int time = randomInRange(1, 50);
            int space = randomInRange(51, 100);
            requirements.add(new Requirement(id(plasma(item)), 144L * (space - time)));
            requirements.add(new Requirement(id(GTNAMaterials.Time.getFluid()), time));
            requirements.add(new Requirement(id(GTNAMaterials.Space.getFluid()), space));
            eject(List.of(ChemicalHelper.get(TagPrefix.dust, item)),
                    List.of(new FluidStack(GTNAMaterials.Time.getFluid(), time),
                            new FluidStack(GTNAMaterials.Space.getFluid(), space)));
        } else {
            Map<Material, Integer> items = new LinkedHashMap<>();
            Map<Material, Integer> fluids = new LinkedHashMap<>();
            QGP_WEIGHTS.forEach((name, weight) -> {
                Material material = GodforgeMaterials.material(name);
                if (material == null || plasma(material) == null) return;
                if (material.hasProperty(PropertyKey.DUST)) items.put(material, weight);
                else if (material.getFluid() != null) fluids.put(material, weight);
            });
            int numberOfFluids = Math.min(randomInRange(0, NUMBER_OF_INPUTS), fluids.size());
            int numberOfItems = Math.min(NUMBER_OF_INPUTS - numberOfFluids, items.size());
            List<ItemStack> raw = new ArrayList<>();
            List<FluidStack> rawFluids = new ArrayList<>();
            for (Material material : pick(items, numberOfItems)) {
                int count = randomInRange(1, 7);
                raw.add(ChemicalHelper.get(TagPrefix.dust, material, count));
                requirements.add(new Requirement(id(plasma(material)), 144L * 9 * count));
            }
            for (Material material : pick(fluids, numberOfFluids)) {
                int amount = randomInRange(1, 64);
                rawFluids.add(new FluidStack(material.getFluid(), amount));
                requirements.add(new Requirement(id(plasma(material)), 1000L * amount));
            }
            eject(raw, rawFluids);
        }
        markDirty();
    }

    private int randomInRange(int min, int max) {
        return (int) (random.nextDouble() * (max - min)) + min;
    }

    /** Weighted draw without duplicates, as {@code getRandomItemInputs}. */
    private List<Material> pick(Map<Material, Integer> weights, int count) {
        List<Map.Entry<Material, Integer>> entries = new ArrayList<>(weights.entrySet());
        int cumulative = 0;
        List<Integer> bounds = new ArrayList<>();
        for (var entry : entries) {
            cumulative += entry.getValue();
            bounds.add(cumulative);
        }
        List<Material> picked = new ArrayList<>();
        int guard = 0;
        while (picked.size() < count && guard++ < 10_000) {
            int roll = randomInRange(1, cumulative);
            for (int j = 0; j < bounds.size(); j++) {
                if (roll <= bounds.get(j)) {
                    Material material = entries.get(j).getKey();
                    if (!picked.contains(material)) picked.add(material);
                    break;
                }
            }
        }
        return picked;
    }

    @Nullable
    private static Fluid plasma(Material material) {
        return material.hasProperty(PropertyKey.FLUID) ? material.getFluid(FluidStorageKeys.PLASMA) : null;
    }

    private static ResourceLocation id(Fluid fluid) {
        return BuiltInRegistries.FLUID.getKey(fluid);
    }

    private List<NotifiableFluidTank> tanks(IO io) {
        List<NotifiableFluidTank> result = new ArrayList<>();
        for (var part : getParts()) {
            for (var list : part.getRecipeHandlers()) {
                for (var handler : list.getHandlersFlat()) {
                    if (handler instanceof NotifiableFluidTank tank && tank.handlerIO == io) result.add(tank);
                }
            }
        }
        return result;
    }

    private List<NotifiableItemStackHandler> buses(IO io) {
        List<NotifiableItemStackHandler> result = new ArrayList<>();
        for (var part : getParts()) {
            for (var list : part.getRecipeHandlers()) {
                for (var handler : list.getHandlersFlat()) {
                    if (handler instanceof NotifiableItemStackHandler items && items.handlerIO == io)
                        result.add(items);
                }
            }
        }
        return result;
    }

    private void eject(List<ItemStack> items, List<FluidStack> fluids) {
        for (ItemStack stack : items) {
            ItemStack left = stack.copy();
            for (var bus : buses(IO.OUT)) {
                for (int slot = 0; slot < bus.storage.getSlots() && !left.isEmpty(); slot++) {
                    left = bus.storage.insertItem(slot, left, false);
                }
            }
        }
        for (FluidStack stack : fluids) {
            int left = stack.getAmount();
            for (var tank : tanks(IO.OUT)) {
                for (var storage : tank.getStorages()) {
                    if (left <= 0) break;
                    left -= storage.fill(new FluidStack(stack.getFluid(), left), FluidAction.EXECUTE);
                }
            }
        }
    }

    /** Exactly the drawn amounts must be present, as in GTNH ({@code waiting_for_inputs} otherwise). */
    private boolean inputsMatch() {
        for (Requirement requirement : requirements) {
            Fluid fluid = BuiltInRegistries.FLUID.get(requirement.fluid());
            long stored = 0;
            for (var tank : tanks(IO.IN)) {
                for (var storage : tank.getStorages()) {
                    if (storage.getFluid().getFluid() == fluid) stored += storage.getFluidAmount();
                }
            }
            if (stored != requirement.amount()) return false;
        }
        return !requirements.isEmpty();
    }

    private void consumeInputs() {
        for (Requirement requirement : requirements) {
            Fluid fluid = BuiltInRegistries.FLUID.get(requirement.fluid());
            long left = requirement.amount();
            for (var tank : tanks(IO.IN)) {
                for (var storage : tank.getStorages()) {
                    if (left <= 0) break;
                    if (storage.getFluid().getFluid() != fluid) continue;
                    left -= storage.drain((int) Math.min(Integer.MAX_VALUE, left), FluidAction.EXECUTE).getAmount();
                }
            }
        }
        requirements.clear();
    }

    private void finish() {
        Fluid product = magmatterMode ? GTNAMaterials.MagMatter.getFluid() : GTNAMaterials.QuarkGluonPlasma.getFluid();
        long left = outputAmount;
        for (var tank : tanks(IO.OUT)) {
            for (var storage : tank.getStorages()) {
                if (left <= 0) break;
                left -= storage.fill(new FluidStack(product, (int) Math.min(Integer.MAX_VALUE, left)),
                        FluidAction.EXECUTE);
            }
        }
        running = false;
        progress = 0;
        duration = 0;
        outputAmount = 0;
        markDirty();
    }

    @Override
    public void addDisplayText(List<Component> text) {
        super.addDisplayText(text);
        text.add(Component.translatable(magmatterMode ? "gtna.godforge.exotic.mode.magmatter" :
                "gtna.godforge.exotic.mode.qgp"));
        for (Requirement requirement : requirements) {
            Fluid fluid = BuiltInRegistries.FLUID.get(requirement.fluid());
            text.add(Component.translatable("gtna.godforge.exotic.requires",
                    new FluidStack(fluid, 1).getDisplayName(), requirement.amount()));
        }
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        CompoundTag exotic = new CompoundTag();
        ListTag list = new ListTag();
        for (Requirement requirement : requirements) {
            CompoundTag entry = new CompoundTag();
            entry.putString("fluid", requirement.fluid().toString());
            entry.putLong("amount", requirement.amount());
            list.add(entry);
        }
        exotic.put("requirements", list);
        exotic.putBoolean("running", running);
        exotic.putInt("progress", progress);
        exotic.putInt("duration", duration);
        exotic.putLong("outputAmount", outputAmount);
        tag.put("exotic", exotic);
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        CompoundTag exotic = tag.getCompound("exotic");
        requirements.clear();
        ListTag list = exotic.getList("requirements", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            ResourceLocation fluid = ResourceLocation.tryParse(entry.getString("fluid"));
            if (fluid != null) requirements.add(new Requirement(fluid, entry.getLong("amount")));
        }
        running = exotic.getBoolean("running");
        progress = exotic.getInt("progress");
        duration = exotic.getInt("duration");
        outputAmount = exotic.getLong("outputAmount");
        godforgeStats().magmatterMode = magmatterMode;
    }
}
