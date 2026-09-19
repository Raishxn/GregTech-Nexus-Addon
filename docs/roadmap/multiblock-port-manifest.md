# Multiblock port manifest

This is the authoritative scope for the GTOCore and GTLAdditions port. Registry IDs are used instead of display names so every entry can be checked mechanically.

## Rules

- Target: GTCEu `7.5.3` on Minecraft `1.20.1`.
- Every recipe-processing controller is implemented on a GTNA multiple-recipes base and accepts the Thread Hatch where its structure permits it.
- Coil machines use `CoilWorkableElectricMultipleRecipesMachine` or a specialized subclass.
- Space machines may depend on Ad Astra and Stargate Journey.
- A machine is excluded as mana/arcane only when it actually consumes or transports Botania mana.
- Decorative blocks from another mod are replaced by GTNA blocks when that avoids an unrelated dependency.
- Existing GTNA controllers are extended or migrated; duplicate controllers are not registered.

## Not ported as new controllers

The following GCYM controllers already ship with GTCEu 7.5.3: `large_maceration_tower`, `large_chemical_bath`, `large_centrifuge`, `large_mixer`, `large_electrolyzer`, `large_electromagnet`, `large_packer`, `large_assembler`, `large_circuit_assembler`, `large_arc_smelter`, `large_engraving_laser`, `large_sifting_funnel`, `alloy_blast_smelter`, `large_autoclave`, `large_material_press`, `large_brewer`, `large_cutter`, `large_distillery`, `large_extractor`, `large_extruder`, `large_solidifier`, `large_wiremill`, `mega_blast_furnace`, `mega_vacuum_freezer`, `mega_alloy_blast_smelter`, `large_rock_crusher`, `large_bender`, `large_rolling`, `large_forming`, `large_arc_generator`, `large_laminator`, `large_laser_welder`, `large_crusher`.

These GTO IDs map to existing GTNA implementations and will be upgraded instead of duplicated: `annihilate_generator`, `eye_of_harmony`, `large_steam_bath`, `large_steam_centrifuge`, `large_steam_circuit_assembler`, `large_steam_crusher`, `large_steam_furnace`, `large_steam_mixer`, `large_steam_ore_washer`, `large_steam_solar_boiler`, `large_steam_storage_tank`, `large_steam_thermal_centrifuge`, `leap_forward_one_blast_furnace`, `me_storage`, `primitive_distillation_tower`.

These Botania-mana controllers are excluded: `mana_alloy_blast_smelter`, `base_mana_distributor`, `advanced_mana_distributor`, `mana_infuser`, `mana_condenser`, `elf_exchange`, `industrial_altar`, `mana_greenhouse`, `mana_garden`, `rune_engraving_chamber`, `large_alchemical_device`, `large_perfusion_device`, `the_primordial_reconstructor`, `resonance_flower`, `cosmic_celestial_spire_of_convergence`, `mana_beam_assembler`, `pulse_machine_maintenance_pedestal`.

## GTOCore candidates

### Research and computation

`supercomputing_center`, `data_center`, `primordial_scanning_station`, `analysis_and_research_center`, `synthetic_data_assembly_plant`.

### Generators

`magnetic_fluid_generator`, `chemical_energy_devourer`, `dyson_sphere_launch_silo`, `dyson_sphere_receiving_station`, `large_naquadah_reactor`, `advanced_hyper_reactor`, `hyper_reactor`, `generator_array`, `fuel_cell_generator`, `large_semi_fluid_generator`, `rocket_large_turbine`, `supercritical_steam_turbine`, `steam_mega_turbine`, `gas_mega_turbine`, `rocket_mega_turbine`, `supercritical_mega_steam_turbine`.

### Processing set A

`digital_miner`, `evaporation_plant`, `plasma_condenser`, `rare_earth_centrifugal`, `comprehensive_tombarthite_processing_facility`, `sps_crafting`, `advanced_sps_crafting`, `matter_fabricator`, `void_fluid_drilling_rig`, `void_miner`, `large_void_miner`, `chemical_plant`, `decay_hastener`, `recycler`, `mass_fabricator`, `advanced_mass_fabricator`, `precision_assembler`, `fishing_ground`, `lava_furnace`, `large_gas_collector`, `engraving_laser_plant`, `magnetic_confinement_dimensionality_shock_device`, `quantum_force_transformer`, `chemical_complex`, `integrated_ore_processor`, `dragon_egg_copier`, `large_cracker`, `steam_piston_hammer`, `large_steam_forge_hammer`, `steam_pressor`, `steam_foundry`, `large_steam_macerator`, `steam_mixer`, `steam_separator`, `steam_bath`, `steam_ore_washer`, `petrochemical_plant`, `large_pyrolyse_oven`, `mega_wiremill`, `superconducting_magnetic_presser`, `heavy_rolling`, `phase_change_cube`, `particle_stream_matrix_filling_machine`, `disassembly`, `element_copying`, `atomic_energy_excitation_plant`, `industrial_flotation_cell`, `vacuum_drying_furnace`, `molten_core`, `electric_implosion_compressor`, `stellar_forge`, `component_assembly_line`, `advanced_integrated_ore_processor`, `integrated_assembler`.

### Processing set B

`digestion_tank`, `wood_distillation`, `desulfurizer`, `liquefaction_furnace`, `reaction_furnace`, `mega_brewer`, `fuel_refining_complex`, `microorganism_master`, `lightning_rod`, `magnetic_energy_reaction_furnace`, `high_energy_laser_lathe`, `neutronium_wire_cutting`, `nano_phagocytosis_plant`, `road_of_heaven`, `mega_bath_tank`, `mega_vacuum_drying_furnace`, `molecular_oscillation_dehydrator`, `extreme_compressor`, `water_purification_plant`, `clarifier_purification_unit`, `ozonation_purification_unit`, `flocculation_purification_unit`, `ph_neutralization_purification_unit`, `extreme_temperature_fluctuation_purification_unit`, `high_energy_laser_purification_unit`, `residual_decontaminant_degasser_purification_unit`, `absolute_baryonic_perfection_purification_unit`.

### Processing set C

`steam_cracker`, `steam_crusher`, `molecular_transformer`, `extreme_electric_furnace`, `high_temperature_reaction_hub`, `component_assembler`, `three_dimensional_printer`, `gravity_bending_device`, `hand_of_arachne`, `cracker_hub`, `chemical_vapor_deposition`, `physical_vapor_deposition`, `plasma_centrifuge`, `plasma_extraction`, `biochemical_reaction`, `biochemical_extraction`, `ore_extraction_module`, `polymer_twisting_module`, `bioengineering_module`, `nanites_integrated_processing_center`, `planet_core_drilling`, `advanced_infinite_driller`, `energy_injector`, `neutron_vortex`, `neutron_compressor`, `nanites_circuit_assembly_factory`, `precision_assembly_center`, `thermal_power_pump`.

### Processing set D

`greenhouse`, `space_probe_surface_reception`, `hyperdimensional_plasma_fusion_core`, `circuit_assembly_line`, `assembler_module`, `resource_collection_module`, `block_conversion_room`, `large_block_conversion_room`, `pcb_factory`, `blaze_blast_furnace`, `cold_ice_freezer`, `bedrock_drilling_rig`, `nyarlathoteps_tentacle`, `processing_plant`, `nano_forge`, `isa_mill`, `neutron_activator`, `heat_exchanger`, `infinity_fluid_drilling_rig`, `advanced_assembly_line`, `fission_reactor`, `space_elevator`, `slaughterhouse`, `luv_kuangbiao_one_giant_nuclear_fusion_reactor`, `incubator`, `large_incubator`, `dissolving_tank`, `god_forge`.

### Ultra-late processing

`dimensional_focus_engraving_array`, `star_ultimate_material_forge_factory`, `super_blast_smelter`, `super_particle_collider`, `compound_extreme_cooling_unit`, `compound_distillation_fractionator`, `superconducting_electromagnetism`, `crystal_builder`, `holy_separator`, `field_extruder_factory`, `swarm_core`.

### Industry, storage and infrastructure

`large_coke_oven`, `crystallization_chamber`, `large_crystallization_chamber`, `algae_farm`, `polymerization_reactor`, `satellite_control_center`, `electric_cooking`, `tree_growth_simulator`, `large_greenhouse`, `ancient_reactor_core`, `nether_reactor_core`, `void_transporter`, `boss_summoner`, `drilling_control_center`, `drone_control_center`, `wireless_energy_substation`, `wireless_dimension_repeater`, `sintering_furnace`, `isostatic_press`, `drawing_tower`, `rocket_assembler`, `gas_compressor`, `super_molecular_assembler`, `rarity_forge`, `me_energy_substation`, `wireless_charger`, `integrated_vapor_deposition_system`, `multiblock_crate`, `item_vault`, `fluid_vault`, `general_vault`, `me_cpu`, `iv_processing_array`, `luv_processing_array`.

### Advanced industry

`lhc`, `encapsulator_execution_module`, `processing_encapsulator`, `giant_flotation_tank`, `entropy_flux_engine`, `transliminal_oasis`, `neutron_forging_anvil`, `dissolution_core`, `fast_neutron_breeder_reactor`, `neutron_irradiation_chamber`, `life_forge`, `gemini_containment_system`, `kerr_newman_homogenizer`, `mantle_crusher`, `giant_sintering_array`, `mega_se_mining`, `mega_se_assembler`, `mega_se_assembly_line`, `smart_siftering_hub`, `electroplating_bath`, `giant_electrochemical_workstation`, `atomizing_condenser`, `thermo_press`, `fiber_extruder`, `brick_kiln`, `pigment_mixer`, `large_algae_farm`, `virtual_coin_miner`, `carving_center`.

### Space systems

`space_station`, `large_expandable_space_station_core_module`, `space_station_extension_module`, `space_station_docking_module`, `space_station_transparent_docking_module`, `space_station_environmental_maintenance_module`, `orbital_smelting_facility`, `orbital_fine_materials_factory`, `orbital_nanoprecision_processing_chamber`, `space_drone_dock`, `space_station_energy_conversion_module`, `space_elevator_connector_module`, `space_bio_research_module`, `planetary_gas_collector`.

## GTLAdditions candidates

`nexus_satellite_factory_mk1`, `nexus_satellite_factory_mk2`, `nexus_satellite_factory_mk3`, `nexus_satellite_factory_mk4`, `lucid_etchdreamer`, `atomic_transmutation_core`, `subatomic_transmutatioon_core`, `astral_convergence_nexus`, `nebula_reaper`, `arcanic_astrograph`, `arcane_cache_vault`, `space_scaling_instrument`, `draconic_collapse_core`, `titan_crip_earthbore`, `biological_simulation_laboratory`, `dimensionally_transcendent_chemical_plant`, `quantum_syphon_matrix`, `fuxi_bagua_heaven_forging_furnace`, `antientropy_condensation_center`, `taixu_turbid_array`, `planetary_ionisation_convergence_tower`, `inferno_cleft_smelting_vault`, `skeleton_shift_rift_engine`, `time_space_distorter`, `apocalyptic_torsion_quantum_matrix`, `forge_of_the_antichrist`, `recursive_reverse_array`, `catalytic_cascade_array`, `magnetorheological_convergence_core`, `spacetime_stasis_device`, `supratemporal_boosting_engine`, `heliofusion_exoticizer`, `helioflare_power_forge`, `heliofluix_melting_core`, `heliothermal_plasma_fabricator`, `heliophase_leyline_crystallizer`, `heart_of_the_universe`, `light_hunter_space_station`, `dimension_focus_infinity_crafting_array`, `primordial_evolution_nexus`, `biosphere_iii`, `garden_of_hermes`, `space_elevator_mkii`, `space_infinity_integrated_ore_processor`, `macro_atomic_resonant_fragment_stripper`.

## Delivery order

1. Runtime foundation, Thread Hatch correctness, Pattern Buffer modes and coiled base.
2. Existing GTNA controller migration and regression tests.
3. Low/mid-tier GTO processing and generators.
4. GTO infrastructure, research and advanced processing.
5. GTO space systems.
6. GTLAdditions blocks, recipe types and early/mid candidates.
7. GTLAdditions space and ultra-late candidates.
8. Models, translations, recipes, EMI/Jade integration and full runtime audit.
