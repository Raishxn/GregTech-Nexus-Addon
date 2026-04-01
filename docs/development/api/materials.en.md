# :test_tube: Materials API
## Registering Custom Materials
```java
public static Material MyMaterial = new Material.Builder(GTNACORE.id("my_material"))
    .ingot().fluid().dust()
    .color(0xRRGGBB)
    .iconSet(MaterialIconSet.METALLIC)
    .components(Iron, 3, Copper, 1)
    .blastTemp(1200, BlastProperty.GasTier.LOW)
    .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_GEAR)
    .buildAndRegister()
    .setFormula("Fe3Cu");
```

See the [GregTech Modding Skill](../../../.agent/skills/gregtech-modding/SKILL.md) for full reference.
