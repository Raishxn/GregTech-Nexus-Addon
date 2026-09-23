# Mapeamento de eras pelas quests do GTO (FTB Quests)

Fonte: modpack **GregTech Odyssey 0.6.0-dev1** (`.../config/ftbquests/quests/chapters/*.snbt`), capítulos por tier. Cada quest tem o item-alvo em `tasks[].item`. Este doc lista, por era, os itens **GTOCore/GTMThings** (o conteúdo que o GTNA precisa portar — o resto é GTCEu base).

Ordem das eras (order_index): stoneage → steam → ulv → lv → mv → hv → ev → iv → luv → zpm → uv → uhv → uev → uiv → uxv → opv.

> ⚠️ O capítulo lista **itens**, não só multiblocos: `gtocore:ulv_lathe` é um **singleblock** do GTOCore, e há ingots/dusts. Use os capítulos para saber **em que era** cada conteúdo entra; a lista de multiblocos a portar continua no `multiblock-port-manifest.md`.

## Como usar
1. Escolha a era (capítulo) em que estamos.
2. Filtre os itens `gtocore:`/`gtmthings:` que **não** têm `✅ GTNA`.
3. Confirme no manifesto/GTOCore se é **multibloco** (o alvo do GTNA) ou singleblock.

## Resumo: multiblocos GTOCore por era (confirmados no código)
- **Steam:** `steam_piston_hammer`, `steam_foundry`, `steam_pressor`, `steam_mixer`, `steam_separator`, `steam_bath`, `steam_ore_washer`, `steam_crusher`, `steam_cracker`, `large_coke_oven`, `item_vault`/`fluid_vault`/`general_vault`.
- **ULV:** a era é dominada por **singleblocks** do GTOCore (`ulv_assembler`, `ulv_lathe`, `ulv_wiremill`, `ulv_chemical_reactor`, `ulv_packer`, `ulv_fluid_solidifier`, `ulv_loom`, `ulv_electric_*`). Multiblocos: `primitive_distillation_tower` (✅ GTNA), `digital_miner`. → **nada de multibloco genuíno restante** (fecha com o `brick_kiln`, G-0060).
- **LV:** `liquefaction_furnace`, `lava_furnace`, `generator_array`, `tree_growth_simulator`, `thermal_power_pump`, `gas_compressor` (o resto do LV é GTCEu base + `large_steam_*` ✅).
- **MV:** `reaction_furnace`, `greenhouse`, `crystallization_chamber`, `component_assembler`, `processing_plant`.

## main-stoneage

- `gtocore:copper_coin` 🎯
- `gtocore:simple_crafting_terminal` 🎯
- `gtocore:plant_fiber` 🎯

## steam

- `gtocore:primitive_blast_furnace_hatch` 🎯
- `gtocore:steam_piston_hammer` 🎯
- `gtocore:steam_foundry` 🎯
- `gtocore:steam_pressor` 🎯
- `gtocore:ancient_reactor_core` 🎯
- `gtocore:pulsating_alloy_ingot` 🎯
- `gtocore:steam_mixer` 🎯
- `gtocore:raw_vacuum_tube` 🎯
- `gtocore:lp_steam_vacuum_pump` 🎯
- `gtocore:steam_separator` 🎯
- `gtocore:steam_bath` 🎯
- `gtocore:steam_ore_washer` 🎯
- `gtocore:steam_crusher` 🎯
- `gtocore:heater` 🎯
- `gtocore:boiler` 🎯
- `gtocore:leap_forward_one_blast_furnace` ✅ GTNA
- `gtocore:large_coke_oven` 🎯
- `gtocore:item_vault` 🎯
- `gtocore:fluid_vault` 🎯
- `gtocore:general_vault` 🎯
- `gtocore:vault_hatch` 🎯
- `gtocore:steam_fluid_input_hatch` 🎯
- `gtocore:steam_fluid_output_hatch` 🎯
- `gtocore:steam_vent_hatch` 🎯
- `gtocore:copper_coin` 🎯

## ulv

- `gtocore:ulv_semi_fluid` 🎯
- `gtocore:ulv_thermal_generator` 🎯
- `gtocore:ulv_assembler` 🎯
- `gtocore:ulv_wind_mill_turbine` 🎯
- `gtocore:steam_cracker` 🎯
- `gtocore:primitive_distillation_tower` ✅ GTNA
- `gtocore:heat_hatch` 🎯
- `gtocore:ulv_chemical_reactor` 🎯
- `gtocore:ulv_fluid_solidifier` 🎯
- `gtocore:air_vent` 🎯
- `gtocore:ulv_packer` 🎯
- `gtocore:alchemy_cauldron` 🎯
- `gtocore:coal_slurry_bucket` 🎯
- `gtocore:water_gas_bucket` 🎯
- `gtocore:heat_interface` 🎯
- `gtocore:ulv_lathe` 🎯
- `gtocore:ulv_wiremill` 🎯
- `gtocore:hp_steam_vacuum_pump` 🎯
- `gtocore:ulv_electric_pump` 🎯
- `gtocore:ulv_conveyor_module` 🎯
- `gtocore:ulv_fluid_regulator` 🎯
- `gtocore:ulv_robot_arm` 🎯
- `gtocore:ulv_electric_motor` 🎯
- `gtocore:ulv_electric_piston` 🎯
- `gtocore:ulv_loom` 🎯
- `gtocore:pulsating_alloy_superconductor_base` 🎯
- `gtocore:pulsating_alloy_single_wire` 🎯
- `gtocore:infinite_intake_hatch` 🎯
- `gtocore:digital_miner` 🎯
- `gtocore:heater` 🎯
- `gtocore:cooler` 🎯
- `gtocore:electric_heater` 🎯
- `gtocore:mana_heater` 🎯
- `gtocore:normal_heat_pipe` 🎯
- `gtocore:heat_detector_cover` 🎯

## lv

- `gtocore:lv_semi_fluid` 🎯
- `gtocore:lv_thermal_generator` 🎯
- `gtocore:lv_wind_mill_turbine` 🎯
- `gtocore:liquefaction_furnace` 🎯
- `gtocore:lv_laminator` 🎯
- `gtocore:dark_steel_ingot` 🎯
- `gtocore:precision_steam_mechanism` 🎯
- `gtocore:large_steam_circuit_assembler` ✅ GTNA
- `gtocore:slaughterhouse` 🎯
- `gtocore:large_steam_storage_tank` ✅ GTNA
- `gtocore:conductive_alloy_ingot` 🎯
- `gtocore:lv_laser_welder` 🎯
- `gtocore:electric_heater` 🎯
- `gtocore:phenolic_resin_bucket` 🎯
- `gtocore:lava_furnace` 🎯
- `gtocore:generator_array` 🎯
- `gtocore:tree_growth_simulator` 🎯
- `gtocore:lv_energy_input_hatch_16a` 🎯
- `gtocore:steel_motor_enclosure` 🎯
- `gtocore:steel_piston_housing` 🎯
- `gtocore:steel_pump_barrel` 🎯
- `gtocore:steel_emitter_base` 🎯
- `gtocore:lv_rolling` 🎯
- `gtocore:lv_cluster` 🎯
- `gtocore:steel_sensor_casing` 🎯
- `gtmthings:wireless_item_transfer_cover` 🎯
- `gtmthings:wireless_fluid_transfer_cover` 🎯
- `gtocore:lv_unpacker` 🎯
- `gtocore:lv_loom` 🎯
- `gtocore:steel_field_generator_casing` 🎯
- `gtocore:steam_pump` 🎯
- `gtocore:thermal_power_pump` 🎯
- `gtocore:lv_accelerate_hatch` 🎯
- `gtocore:lv_power_amplifiers` 🎯
- `gtocore:large_steam_centrifuge` ✅ GTNA
- `gtocore:large_steam_thermal_centrifuge` ✅ GTNA
- `gtocore:large_steam_ore_washer` ✅ GTNA
- `gtocore:large_steam_bath` ✅ GTNA
- `gtocore:large_steam_furnace` ✅ GTNA
- `gtocore:large_steam_mixer` ✅ GTNA
- `gtocore:large_steam_crusher` ✅ GTNA
- `gtocore:large_steam_macerator` 🎯
- `gtocore:large_steam_solar_boiler` ✅ GTNA
- `gtocore:gas_compressor` 🎯
- `gtocore:manganese_phosphide_superconductor_base` 🎯
- `gtocore:mica_insulator_foil` 🎯
- `gtocore:mica_based_sheet` 🎯
- `gtocore:mica_based_pulp` 🎯
- `gtocore:speed_upgrade_module` 🎯
- `gtocore:energy_upgrade_module` 🎯
- `gtocore:wireless_charger` 🎯
- `gtocore:wireless_charger_cover` 🎯

## mv

- `gtocore:alumina_ceramic_flake` 🎯
- `gtocore:alumina_ceramic_dust` 🎯
- `gtocore:reaction_furnace` 🎯
- `gtocore:electronic_grade_silicon_dust` 🎯
- `gtocore:coolant_liquid_bucket` 🎯
- `gtocore:processing_plant` 🎯
- `gtocore:crystallization_chamber` 🎯
- `gtocore:silicon_crystal_seed` 🎯
- `gtocore:greenhouse` 🎯
- `gtocore:component_assembler` 🎯
- `gtocore:alumina_ceramic_rough_blank` 🎯
- `gtocore:alumina_ceramic_block` 🎯
- `gtocore:alumina_ceramic_brick` 🎯
- `gtocore:aluminium_hydroxide_dust` 🎯
- `gtocore:mv_semi_fluid` 🎯
- `gtocore:mv_thermal_generator` 🎯
- `gtocore:mv_wind_mill_turbine` 🎯
- `gtocore:mv_cluster` 🎯
- `gtocore:mv_laminator` 🎯
- `gtocore:mv_rolling` 🎯
- `gtocore:mv_loom` 🎯
- `gtocore:mv_arc_generator` 🎯
- `gtocore:energetic_alloy_ingot` 🎯
- `gtocore:vacuum_interface` 🎯
- `gtocore:energetic_photovoltaic_power_station` 🎯
- `gtocore:virtual_item_supply_machine` 🎯
- `gtocore:mv_programmablec_hatch` 🎯
- `gtmthings:programmable_cover` 🎯

## hv

- `gtocore:desulfurizer` 🎯
- `gtocore:fishing_ground` 🎯
- `gtocore:polymerization_reactor` 🎯
- `gtocore:void_transporter` 🎯
- `gtocore:drone_control_center` 🎯
- `gtocore:hv_drone_hatch` 🎯
- `gtocore:planet_scan_satellite` 🎯
- `gtocore:satellite_control_center` 🎯
- `gtocore:three_dimensional_printer` 🎯
- `gtocore:vibrant_alloy_ingot` 🎯
- `gtocore:planet_data_chip` 🎯
- `gtocore:virtual_item_supply_machine` 🎯
- `gtocore:mv_programmablec_hatch` 🎯
- `gtocore:evaporation_plant` 🎯
- `gtocore:nether_reactor_core` 🎯
- `gtocore:pulsating_photovoltaic_power_station` 🎯
- `gtocore:recycler` 🎯
- `gtocore:integral_framework_mv` 🎯
- `gtocore:integral_framework_hv` 🎯
- `gtocore:large_steam_input_hatch` 🎯
- `gtmthings:programmable_cover` 🎯

## ev

- `gtocore:water_purification_plant` 🎯
- `gtocore:isostatic_press` 🎯
- `gtocore:sintering_furnace` 🎯
- `gtocore:barium_titanate_ceramic_flake` 🎯
- `gtocore:chemical_vapor_deposition` 🎯
- `gtocore:clarifier_purification_unit` 🎯
- `gtocore:filtered_water_bucket` 🎯
- `gtocore:large_gas_collector` 🎯
- `gtocore:void_miner` 🎯
- `gtocore:dense_hydrazine_fuel_mixture_bucket` 🎯
- `gtocore:rocket_fuel_cn3h7o3_bucket` 🎯
- `gtocore:rocket_fuel_rp_1_bucket` 🎯
- `gtocore:vibrant_photovoltaic_power_station` 🎯
- `gtocore:rocket_large_turbine` 🎯
- `gtocore:steam_mega_turbine` 🎯
- `gtocore:gas_mega_turbine` 🎯
- `gtocore:ev_rocket_engine` 🎯
- `gtocore:integral_framework_ev` 🎯
- `gtocore:heavy_duty_plate_2` 🎯
- `gtocore:large_greenhouse` 🎯

## iv

- `gtocore:yttrium_oxide_dust` 🎯
- `gtocore:ozonation_purification_unit` 🎯
- `gtocore:ozone_water_bucket` 🎯
- `gtocore:rocket_fuel_h8n4c2o4_bucket` 🎯
- `gtocore:blaze_blast_furnace` 🎯
- `gtocore:drilling_control_center` 🎯
- `gtocore:fission_reactor` 🎯
- `gtocore:heat_exchanger` 🎯
- `gtocore:auto_configuration_maintenance_hatch` 🎯
- `gtocore:nuclear_waste` 🎯
- `gtocore:chemical_plant` 🎯
- `gtocore:iv_processing_array` 🎯
- `gtocore:rocket_mega_turbine` 🎯
- `gtocore:dissolving_tank` 🎯
- `gtocore:digestion_tank` 🎯
- `gtocore:molecular_transformer` 🎯
- `gtocore:hastelloy_n_75_ingot` 🎯
- `gtocore:tungsten_tetraboride_ceramics_flake` 🎯
- `gtocore:cold_ice_freezer` 🎯
- `gtocore:integral_framework_iv` 🎯
- `gtocore:advanced_tesseract_generator` 🎯
- `gtocore:tesseract_target_marker` 🎯
- `gtocore:catalyst_hatch` 🎯
- `gtocore:advanced_catalyst_hatch` 🎯
- `gtocore:infinite_water_hatch` 🎯
- `gtocore:wireless_dimension_repeater` 🎯
- `gtocore:iv_wireless_input_hatch_256a` 🎯
- `gtocore:electroplating_bath` 🎯
- `gtocore:high_pressure_steam_input_hatch` 🎯
- `gtocore:red_mud_bucket` 🎯
- `gtocore:fiber_extruder` 🎯
- `gtocore:thermo_press` 🎯

## luv

- `gtocore:neutron_activator` 🎯
- `gtocore:flocculation_purification_unit` 🎯
- `gtocore:flocculent_water_bucket` 🎯
- `gtocore:energy_injector` 🎯
- `gtocore:precision_assembler` 🎯
- `gtocore:magnetic_fluid_generator` 🎯
- `gtocore:luv_kuangbiao_one_giant_nuclear_fusion_reactor` 🎯
- `gtocore:integral_framework_luv` 🎯
- `gtocore:isa_mill` 🎯
- `gtocore:industrial_flotation_cell` 🎯
- `gtocore:vacuum_drying_furnace` 🎯
- `gtocore:silica_ceramic_flake` 🎯
- `gtocore:large_void_miner` 🎯
- `gtocore:polyurethaneresin_bucket` 🎯
- `gtocore:silicon_nitride_ceramic_flake` 🎯
- `gtocore:magneto_resonatic_dust` 🎯
- `gtocore:imprinted_resonatic_circuit_board` 🎯
- `gtocore:large_crystallization_chamber` 🎯
- `gtocore:circuit_compound_dust` 🎯
- `gtocore:simple_fiber_optic_rough` 🎯
- `gtocore:drawing_tower` 🎯
- `gtocore:block_conversion_room` 🎯
- `gtocore:fall_king_ingot` 🎯
- `gtocore:supercritical_steam_turbine` 🎯
- `gtocore:supercritical_mega_steam_turbine` 🎯
- `gtocore:supercritical_steam_input_hatch` 🎯
- `gtocore:large_cracker` 🎯
- `gtocore:virtual_coin_miner` 🎯

## zpm

- `gtocore:preparation_petri_dish` 🎯
- `gtocore:biochemical_extraction` 🎯
- `gtocore:biochemical_reaction` 🎯
- `gtocore:fuel_refining_complex` 🎯
- `gtocore:mega_alloy_blast_smelter` 🎯
- `gtocore:stellar_energy_rocket_fuel_bucket` 🎯
- `gtocore:sterile_cleaning_maintenance_hatch` 🎯
- `gtocore:bacterial_growth_medium_bucket` 🎯
- `gtocore:eschericia_coli_dust` 🎯
- `gtocore:bifidobacterium_breve_dust` 🎯
- `gtocore:brevibacterium_flavium_dust` 🎯
- `gtocore:streptococcus_pyogenes_dust` 🎯
- `gtocore:rapidly_replicating_animal_cells_bucket` 🎯
- `gtocore:pluripotency_induction_gene_therapy_fluid_bucket` 🎯
- `gtocore:large_incubator` 🎯
- `gtocore:radiation_hatch` 🎯
- `gtocore:hydroxyapatite_ceramic_flake` 🎯
- `gtocore:glacio_spirit` 🎯
- `gtocore:algae_farm` 🎯
- `gtocore:blue_algae` 🎯
- `gtocore:gold_algae` 🎯
- `gtocore:brown_algae` 🎯
- `gtocore:green_algae` 🎯
- `gtocore:red_algae` 🎯
- `gtocore:nutrient_distillation_bucket` 🎯
- `gtocore:cloud_seed_concentrated_bucket` 🎯
- `gtocore:fire_water_bucket` 🎯
- `gtocore:vapor_of_levity_bucket` 🎯
- `gtocore:advanced_assembly_line` 🎯
- `gtocore:petrochemical_plant` 🎯
- `gtocore:super_particle_collider` 🎯
- `gtocore:integral_framework_zpm` 🎯
- `gtocore:lafium_ingot` 🎯
- `gtocore:cupriavidus_necator_dust` 🎯
- `gtocore:sterilized_petri_dish` 🎯
- `gtocore:modular_configuration_maintenance_hatch` 🎯
- `gtocore:wood_distillation` 🎯
- `gtocore:fast_neutron_breeder_reactor` 🎯
- `gtocore:light_emitting_charged_suspicious_waste_dust` 🎯
- `gtocore:etrium_dust` 🎯

## uv

- `gtocore:amprosium_ingot` 🎯
- `gtocore:gravity_hatch` 🎯
- `gtocore:super_blast_smelter` 🎯
- `gtocore:infinity_fluid_drilling_rig` 🎯
- `gtocore:space_elevator` 🎯
- `gtocore:assembler_module` 🎯
- `gtocore:resource_collection_module` 🎯
- `gtocore:space_drone_mk1` 🎯
- `gtocore:tcetieseaweedextract` 🎯
- `gtocore:unknowwater_bucket` 🎯
- `gtocore:bioware_boule` 🎯
- `gtocore:biological_cells` 🎯
- `gtocore:alien_algae_ore` 🎯
- `gtocore:nanotube_spool` 🎯
- `gtocore:bioware_processing_core` 🎯
- `gtocore:bioware_printed_circuit_board` 🎯
- `gtocore:smd_capacitor_bioware` 🎯
- `gtocore:smd_diode_bioware` 🎯
- `gtocore:smd_resistor_bioware` 🎯
- `gtocore:smd_transistor_bioware` 🎯
- `gtocore:smd_inductor_bioware` 🎯
- `gtocore:polyimide_bucket` 🎯
- `gtocore:bioware_processor` 🎯
- `gtocore:bioware_assembly` 🎯
- `gtocore:bioware_computer` 🎯
- `gtocore:bioware_mainframe` 🎯
- `gtocore:circuit_assembly_line` 🎯
- `gtocore:precision_circuit_assembly_robot_mk1` 🎯
- `gtocore:abyssalalloy_ingot` 🎯
- `gtocore:abyssalalloy_coil_block` 🎯
- `gtocore:plasma_condenser` 🎯
- `gtocore:tellurate_ceramics_flake` 🎯
- `gtocore:uv_accelerate_hatch` 🎯
- `gtocore:uv_thread_hatch` 🎯
- `gtocore:uv_overclock_hatch` 🎯
- `gtocore:biohmediumsterilized_bucket` 🎯
- `gtocore:bioware_chip` 🎯
- `gtocore:extreme_temperature_water_bucket` 🎯
- `gtocore:extreme_temperature_fluctuation_purification_unit` 🎯
- `gtocore:ph_neutralization_purification_unit` 🎯
- `gtocore:ph_neutral_water_bucket` 🎯
- `gtocore:polyetheretherketone_bucket` 🎯
- `gtocore:mithril_ingot` 🎯
- `gtocore:space_elevator_power_module_1` 🎯
- `gtocore:integral_framework_uv` 🎯
- `gtocore:superconducting_electromagnetism` 🎯
- `gtocore:mutated_living_solder_bucket` 🎯
- `gtocore:wetware_soc` 🎯
- `gtocore:supercomputing_center` 🎯
- `gtocore:cerebrum` 🎯
- `gtocore:chemical_complex` 🎯
- `gtocore:orichalcum_ingot` 🎯
- `gtocore:nanites_integrated_processing_center` 🎯
- `gtocore:piranha_solution_bucket` 🎯
- `gtocore:bioengineering_module` 🎯
- `gtocore:ore_extraction_module` 🎯
- `gtocore:polymer_twisting_module` 🎯
- `gtocore:carbon_nanites` 🎯
- `gtocore:nano_forge` 🎯
- `gtocore:gemini_containment_system` 🎯
- `gtocore:pcb_factory` 🎯
- `gtocore:cycloparaphenylene_bucket` 🎯

## uhv

- `gtocore:energetic_netherite_dust` 🎯
- `gtocore:uhv_fusion_reactor` 🎯
- `gtocore:lemurite_bucket` 🎯
- `gtocore:quantanium_ingot` 🎯
- `gtocore:smd_capacitor_optical` 🎯
- `gtocore:smd_diode_optical` 🎯
- `gtocore:smd_resistor_optical` 🎯
- `gtocore:smd_inductor_optical` 🎯
- `gtocore:smd_transistor_optical` 🎯
- `gtocore:non_linear_optical_lens` 🎯
- `gtocore:low_frequency_laser` 🎯
- `gtocore:medium_frequency_laser` 🎯
- `gtocore:high_frequency_laser` 🎯
- `gtocore:periodically_poled_lithium_niobate_boule` 🎯
- `gtocore:optical_processing_core` 🎯
- `gtocore:engraving_laser_plant` 🎯
- `gtocore:rutherfordium_amprosium_wafer` 🎯
- `gtocore:raw_photon_carrying_wafer` 🎯
- `gtocore:glowstone_nanites` 🎯
- `gtocore:optical_wafer` 🎯
- `gtocore:simple_optical_soc` 🎯
- `gtocore:electric_equilibrium_water_bucket` 🎯
- `gtocore:high_energy_laser_purification_unit` 🎯
- `gtocore:optical_printed_circuit_board` 🎯
- `gtocore:kevlar_plate` 🎯
- `gtocore:thulium_hexaboride_ceramics_flake` 🎯
- `gtocore:fullerene_dust` 🎯
- `gtocore:optical_processor` 🎯
- `gtocore:optical_assembly` 🎯
- `gtocore:optical_computer` 🎯
- `gtocore:optical_mainframe` 🎯
- `gtocore:matter_fabricator` 🎯
- `gtocore:titansteel_ingot` 🎯
- `gtocore:titansteel_coil_block` 🎯
- `gtocore:uu_amplifier_bucket` 🎯
- `gtocore:space_drone_mk2` 🎯
- `gtocore:enderite_ingot` 🎯
- `gtocore:nm_wafer` 🎯
- `gtocore:stellar_forge` 🎯
- `gtocore:hexanitrohexaaxaisowurtzitane_dust` 🎯
- `gtocore:naquadria_charge` 🎯
- `gtocore:enderium_ingot` 🎯
- `gtocore:ender_crystal` 🎯
- `gtocore:integral_framework_uhv` 🎯
- `gtocore:quicksilver_bucket` 🎯
- `gtocore:space_drone_mk3` 🎯
- `gtocore:component_assembly_line` 🎯
- `gtocore:ingot_field_shape` 🎯
- `gtocore:orichalcum_nanites` 🎯
- `gtocore:liquidcrystalkevlar_bucket` 🎯
- `gtocore:rutherfordium_amprosium_boule` 🎯
- `gtocore:rare_earth_centrifugal` 🎯
- `gtocore:transcending_matter_bucket` 🎯
- `gtocore:large_algae_farm` 🎯
- `gtocore:algae_access_hatch` 🎯
- `gtocore:bose_einstein_cooling_container` 🎯

## uev

- `gtocore:field_extruder_factory` 🎯
- `gtocore:lhc` 🎯
- `gtocore:nyarlathoteps_tentacle` 🎯
- `gtocore:exotic_mainframe` 🎯
- `gtocore:cosmic_mainframe` 🎯
- `gtocore:supracausal_mainframe` 🎯
- `gtocore:suprachronal_circuit_max` 🎯
- `gtocore:quantum_force_transformer` 🎯
- `gtocore:eye_of_harmony` ✅ GTNA
- `gtocore:uev_fusion_reactor` 🎯
- `gtocore:sps_crafting` 🎯
- `gtocore:bedrock_drilling_rig` 🎯
- `gtocore:mass_fabricator` 🎯
- `gtocore:space_probe_surface_reception` 🎯
- `gtocore:dyson_sphere_launch_silo` 🎯
- `gtocore:magnetic_confinement_dimensionality_shock_device` 🎯
- `gtocore:atomic_energy_excitation_plant` 🎯
- `gtocore:star_ultimate_material_forge_factory` 🎯
- `gtocore:hyperdimensional_plasma_fusion_core` 🎯
- `gtocore:dragon_egg_copier` 🎯
- `gtocore:neutron_compressor` 🎯
- `gtocore:max_neutron_compressor` 🎯
- `gtocore:infinity_singularity` 🎯
- `gtocore:annihilate_generator` ✅ GTNA
- `gtocore:comprehensive_tombarthite_processing_facility` 🎯
- `gtocore:advanced_integrated_ore_processor` ✅ GTNA
- `gtocore:data_center` 🎯
- `gtocore:road_of_heaven` 🎯
- `gtocore:element_copying` 🎯
- `gtocore:suprachronal_mainframe_complex` 🎯
- `gtocore:create_ultimate_battery` 🎯
- `gtocore:chaotic_energy_core` 🎯
- `gtmthings:creative_fluid_input_hatch` 🎯
- `gtmthings:creative_item_input_bus` 🎯
- `gtmthings:creative_laser_hatch` 🎯
- `gtocore:all_fluids_cell` 🎯
- `gtocore:degassed_water_bucket` 🎯


---

## GTNL e TST: era-quests (marcadores)

Além do GTO, o **GTNL** (BetterQuesting) tem **um quest por era** com a máquina representativa como
ícone: *Your LV Age*, *Your HV Age*, *Your EV Age*, *Your IV Age*, *Your LuV Age*, *Your ZPM Age*,
*Your UV Age*, *Your UHV Age*, *Your UEV Age* (lang `betterquesting.quest.<id>.name`; os quests não
têm descrição, só o ícone `gregtech:gt.blockmachines:<meta>`). Também tem as quest lines de era steam
`Tier 0.75 - Superheated` e `Tier 0.999... - Supercritical`. O **TST** segue o mesmo padrão (a config
habilita a aba de quests), mas os dados de quest **não estão no repo** do TST — usamos o GTO como mapa
principal e o GTNL como confirmação das eras steam.
