package net.anarchx.hungermechanics;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@HungermechanicsModElements.ModElement.Tag
public class HungerMechanicsGravityOnPotionActiveTickProcedure extends HungermechanicsModElements.ModElement {
	public HungerMechanicsGravityOnPotionActiveTickProcedure(HungermechanicsModElements instance) {
		super(instance, 2);
		MinecraftForge.EVENT_BUS.register(this);
	}

	public static void executeProcedure(Map<String, Object> dependencies) {
	}
	
	private final Config config = new Config();
	
	private Material[] mtslow1 = {
			Material.CLAY,
			Material.LEAVES,
			Material.CLOTH_DECORATION,
			Material.DIRT,
			Material.CORAL,
			Material.GRASS,
			Material.SPONGE,
			Material.WOOL,
			Material.AIR,
			Material.CLOTH_DECORATION,
			Material.STRUCTURAL_AIR};
	
	private Material[] mtslow2 = {
			Material.SAND,
			Material.SNOW,
			Material.TOP_SNOW};
	
	private Material[] plants = { 
			Material.PLANT,
			Material.WATER_PLANT,
			Material.REPLACEABLE_PLANT,
			Material.REPLACEABLE_WATER_PLANT,
			Material.REPLACEABLE_FIREPROOF_PLANT,
			Material.CACTUS,
			Material.VEGETABLE};
	
	private List<Material> slow1 = Arrays.asList(mtslow1);
	private List<Material> slow2 = Arrays.asList(mtslow2);
	private List<Material> slowplants = Arrays.asList(plants);
	
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			Entity entity = event.player;
			World world = entity.level;
			double i = entity.position().x;
			double j = entity.position().y;
			double k = entity.position().z;
			Map<String, Object> dependencies = new HashMap<>();
			dependencies.put("x", i);
			dependencies.put("y", j);
			dependencies.put("z", k);
			dependencies.put("world", world);
			dependencies.put("entity", entity);
			dependencies.put("event", event);
			HungerMechanicsGravityOnPotionActiveTickProcedure.executeProcedure(dependencies);
			BlockPos posBelow = entity.blockPosition().below();
			BlockState blockStateBelow = entity.level.getBlockState(posBelow);
			LivingEntity e = event.player;
			if (config.gravity()) {
				if (!e.hasEffect(HungerMechanicsGravity.potion)) {
					if (slowplants.contains(entity.level.getBlockState(entity.blockPosition()).getMaterial())) {
						e.addEffect(new EffectInstance(HungerMechanicsGravity.potion, 2, 2, false,false,false));
					}
					else {
						if(slow1.contains(blockStateBelow.getMaterial())) {
							e.addEffect(new EffectInstance(HungerMechanicsGravity.potion, 2, 0, false,false,false));
						}
						else if (slow2.contains(blockStateBelow.getMaterial())) {
							e.addEffect(new EffectInstance(HungerMechanicsGravity.potion, 2, 1, false,false,false));
						}
						else {
							return;
						}
					}
				}
			}
		}
	}
}
