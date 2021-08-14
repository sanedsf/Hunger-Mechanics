package net.anarchx.hungermechanics;

import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;

import org.apache.logging.log4j.LogManager;

import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.UseAction;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Mod("hungermechanics")
public class HungermechanicsMod {
	public static final Logger LOGGER = LogManager.getLogger(HungermechanicsMod.class);
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation("hungermechanics", "hungermechanics"),
			() -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	private String sPenaltyString = null;
	private int hunger = 0;
	private Config config = new Config();
	private float newExhaustion;
	private String[][] matrix;
	public HungermechanicsMod() {
		ModLoadingContext.get().registerConfig(Type.COMMON, this.config.getSpec());
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
		String[] data = this.config.customblocks();
	    matrix = new String[data.length][]; 
	    int r = 0;
	    for (String row : data) {
	        matrix[r++] = row.split(",");
	    }
		MinecraftForge.EVENT_BUS.addListener(this::onEntityTick);
//		MinecraftForge.EVENT_BUS.addListener(this::onFovEvent);
		MinecraftForge.EVENT_BUS.addListener(this::stopbreaking);
		MinecraftForge.EVENT_BUS.addListener(this::onLivingPlayer);
		MinecraftForge.EVENT_BUS.addListener(this::stopjump);
		MinecraftForge.EVENT_BUS.addListener(this::onAttackEntity);
		MinecraftForge.EVENT_BUS.addListener(this::doneeating);
		MinecraftForge.EVENT_BUS.addListener(this::itemInteract);
		MinecraftForge.EVENT_BUS.addListener(this::itemInteract2);
		MinecraftForge.EVENT_BUS.addListener(this::itemInteract3);
		MinecraftForge.EVENT_BUS.addListener(this::render);
	}
	private boolean render(RenderGameOverlayEvent.Text event) { //render hunger descriptor
		if (this.config.descriptor()) {
			Minecraft mc = Minecraft.getInstance();
			FontRenderer renderer = mc.font;
			if (hunger > 0) {
				sPenaltyString = this.config.hungerdescriptor()[hunger];
			}
			MatrixStack matrixstack = new MatrixStack();
			if (hunger != 0) {
				renderer.draw(matrixstack, sPenaltyString, (mc.getWindow().getGuiScaledWidth() / 2) + 91, mc.getWindow().getGuiScaledHeight() - 39, 0XFFFFFF);
			}
			return true;
		}
		return true;
	}

	private void stopbreaking(PlayerEvent.BreakSpeed event) { //reduce breaking speed based on hunger
		if (this.config.breaking()) {
			if (hunger == 4) { //dying
				event.setNewSpeed(0.125f); //1/8 of normal speed
			}
			else if (hunger == 3) { //starving
				event.setNewSpeed(0.25f); //1/4 speed
			}
			else if (hunger == 2) { //hungry
				event.setNewSpeed(0.50f); //1/2 speed
			}
		}
	}
	
	private void stopjump(LivingEvent.LivingJumpEvent event) { //cannot jump if low on hunger
		if (this.config.jumping()) {
			if (hunger == 4 && (event.getEntity() instanceof PlayerEntity)) {
				event.getEntity().lerpMotion(event.getEntity().getEntity().getDeltaMovement().x, event.getEntity().getEntity().getDeltaMovement().y - 0.25F, event.getEntity().getEntity().getDeltaMovement().z);
			}
		}
	}
	
	private Material[] mtslow1 = {
			Material.CLAY,
			Material.LEAVES,
			Material.CLOTH_DECORATION,
			Material.DIRT,
			Material.CORAL,
			Material.GRASS,
			Material.SPONGE,
			Material.WOOL,
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
	
	private void onEntityTick (TickEvent.PlayerTickEvent event) { // lose hunger every time the entity ticks (based on the activity)
        // Even when you are doing nothing, your food levels are still going down slowly
        // Values are based off of vanilla behavior. https://minecraft.gamepedia.com/Hunger
        if ((event.player instanceof PlayerEntity) && event.phase == TickEvent.Phase.START && this.config.hunger()) {
        	newExhaustion = this.config.hungervalue().floatValue();
        	event.player.causeFoodExhaustion(newExhaustion);
        }
        if ((event.player instanceof PlayerEntity) && event.phase == TickEvent.Phase.END && this.config.hunger()) {
        	if (event.player.getFoodData().getFoodLevel() <= 2 ) {
        		hunger = 4;
        	}
        	else if (event.player.getFoodData().getFoodLevel() <= 5){
        		hunger=3;
        	}
        	else if (event.player.getFoodData().getFoodLevel() <= 7){
        		hunger=2;
        	}
        	else if (event.player.getFoodData().getFoodLevel() <= 10){
        		hunger=1;
        	}
        	else {
        		hunger = 0;
        	}
        }
        if (this.config.gravity()){
        	if (event.phase == TickEvent.Phase.END) {
        		Entity entity = event.player;
        		BlockPos posBelow = entity.blockPosition().below();
        		BlockState blockStateBelow = entity.level.getBlockState(posBelow);
        		boolean found = false;
        		int percent = 0;
        		PlayerEntity ent = (PlayerEntity) event.player;
        		ModifiableAttributeInstance attribute = ent.getAttribute(Attributes.MOVEMENT_SPEED);
        		for (int row = 0; row < matrix.length; row++) {
        			if(matrix[row][0].contains(blockStateBelow.getBlock().getRegistryName().toString())) {
        				found = true;
        				percent=row;
        				break;
        			}else {
        				found = false;
        			}
        		}
        		if (found) {
        			attribute.setBaseValue(0.1D*((100D-Double.valueOf(matrix[percent][1]))/100D));
        		}
        		else {
        			if (slowplants.contains(entity.level.getBlockState(entity.blockPosition()).getMaterial())) {
        				attribute.setBaseValue(0.1D*((100D-Double.valueOf(this.config.movementspeed()[1])))/100D);//50%
        			}
        			else {
        				if(slow1.contains(blockStateBelow.getMaterial())) {
        					attribute.setBaseValue(0.1D*((100D-Double.valueOf(this.config.movementspeed()[2])))/100D);//10%
        				}
        				else if (slow2.contains(blockStateBelow.getMaterial())) {
        					attribute.setBaseValue(0.1D*((100D-Double.valueOf(this.config.movementspeed()[3])))/100D);//35%
        				}
        				else {
        					attribute.setBaseValue(0.1D*((100D-Double.valueOf(this.config.movementspeed()[0])))/100D);//0%
        					return;
        				}
        			}
        		}
        	}
        }
	}
	
	private void onLivingPlayer(LivingEvent.LivingUpdateEvent event) { // sink the player
		if (event.getEntity() instanceof PlayerEntity) { // only get the player entity
			PlayerEntity player = (PlayerEntity) event.getEntity();
			boolean test = Arrays.stream(this.config.armorinslot()).anyMatch(player.inventory.armor.get(2).getItem().getRegistryName().toString()::equals);
			boolean test2 = Arrays.stream(this.config.armorinslot()).anyMatch(player.inventory.armor.get(1).getItem().getRegistryName().toString()::equals);
			if (this.config.sink()) {
				if ( test || test2) {
					// check the armor the player is using (chest or legs)
					// if the player is wearing ANY armor piece that makes you sink then set the player
					// motion downwards, -0.03F is slowing you down just enough
					if (player.isInWaterOrBubble()) {
						player.lerpMotion(player.getEntity().getDeltaMovement().x, player.getEntity().getDeltaMovement().y - 0.03F, player.getEntity().getDeltaMovement().z);
						player.setSprinting(false);
					}
				}
			}
			if (this.config.heavyarmor()) {
				if ( test || test2) {
					newExhaustion = this.config.heavyarmorvalue().floatValue();
					player.causeFoodExhaustion(newExhaustion);
				}
			}
			if (player.isSprinting() && this.config.sprinting()) { //if player sprinting
				newExhaustion = this.config.sprintingvalue().floatValue();
				player.causeFoodExhaustion(newExhaustion);
			}
		}
	}
	
	private void onAttackEntity(LivingHurtEvent event) { //when an entity gets damaged
		if (event.getSource().getEntity() instanceof PlayerEntity && this.config.attack()) { //only if player attacking
			if (hunger == 4) { //dying
				event.setAmount(event.getAmount() * 0.25f);
			}
			else if (hunger == 3) { //starving
				event.setAmount(event.getAmount() * 0.50f);
			}
		}
	}
	
//	private void onKnockbackEntity(LivingKnockBackEvent event) {
//		if (event.attacker.getEntity() instanceof PlayerEntity && this.config.attack()) { //only if player attacking
//			if (hunger == 4) { //dying
//				event.setStrength(0);
//			}
//		}
//	}
	
    private void itemInteract(PlayerInteractEvent.RightClickItem event) { //when eating
    	if (this.config.salmonela()) { // if salmonela is on
    		if (event.getItemStack().getUseAnimation() == UseAction.EAT) { //if the action is eating
    			if (event.getEntity() instanceof PlayerEntity && event.getPlayer().hasEffect(Effects.HUNGER)) {
    				//if entity is instance of player and the event is on client side and the player has the hunger effect
    				event.setCanceled(true);
    			}
    		}
    	}
    }
    
    private void itemInteract2(PlayerInteractEvent.EntityInteract event) { //when eating looking at entity
    	if (this.config.salmonela()) { // if salmonela is on
    		if (event.getItemStack().getUseAnimation() == UseAction.EAT) { //if the action is eating
    			if (event.getEntity() instanceof PlayerEntity && event.getPlayer().hasEffect(Effects.HUNGER)) {
    				//if entity is instance of player and the event is on client side and the player has the hunger effect
    				event.setCanceled(true);
    			}
    		}
    	}
    }
    
    private void itemInteract3(PlayerInteractEvent.RightClickBlock event) { //when eating looking at block
    	if (this.config.salmonela()) { // if salmonela is on
    		if (event.getItemStack().getUseAnimation() == UseAction.EAT) { //if the action is eating
    			if (event.getEntity() instanceof PlayerEntity && event.getPlayer().hasEffect(Effects.HUNGER)) {
    				//if entity is instance of player and the event is on client side and the player has the hunger effect
    				event.setCanceled(true);
    			}
    		}
    	}
    }
    
    private void doneeating(LivingEntityUseItemEvent.Finish event) { //when you finish eating something
    	float chance = new Random().nextFloat();
    	//if (event.getItem().getItem().getFoodProperties() != null ) { why is this here anyway, it doesnt matter if it has properties or not
    		//if (event.getItem().getItem().getFoodProperties().isMeat()) { //if item eaten was meat
    			boolean rawfromconfig = Arrays.stream(this.config.rawmeat()).anyMatch(event.getItem().getItem().getRegistryName().toString()::equals);
    			if (rawfromconfig || event.getItem().getItem().getRegistryName().toString().contains("raw") == this.config.salmonelaraw()) { //if item is in defined list or has RAW in name
    				if (1f - chance <= 0.6f) { //60% chance to get salmonela
    					event.getEntityLiving().addEffect(new EffectInstance(Effects.HUNGER, 300, 5));
    				}
    			//}
    		//}
    	}
    }
}
