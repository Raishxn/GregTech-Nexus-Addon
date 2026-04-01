# :test_tube: API de Materiales
## Registrar Materiales Personalizados
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
