package net.anarchx.hungermechanics;

import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;

import org.apache.logging.log4j.LogManager;

import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
//import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.util.ResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.UseAction;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Supplier;

@Mod("hungermechanics")
public class HungermechanicsMod {
	public static final Logger LOGGER = LogManager.getLogger(HungermechanicsMod.class);
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation("hungermechanics", "hungermechanics"),
			() -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	public HungermechanicsModElements elements;
	private String sPenaltyString = null;
	private int hunger = 0;
	private final Config config = new Config();
	private float newExhaustion;
	public HungermechanicsMod() {
		elements = new HungermechanicsModElements();
		ModLoadingContext.get().registerConfig(Type.COMMON, this.config.getSpec());
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientLoad);
		MinecraftForge.EVENT_BUS.register(new HungermechanicsModFMLBusEvents(this));
		MinecraftForge.EVENT_BUS.addListener(this::onEntityTick);
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

	private void init(FMLCommonSetupEvent event) {
		elements.getElements().forEach(element -> element.init(event));
	}

	public void clientLoad(FMLClientSetupEvent event) {
		elements.getElements().forEach(element -> element.clientLoad(event));
	}

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
		event.getRegistry().registerAll(elements.getBlocks().stream().map(Supplier::get).toArray(Block[]::new));
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().registerAll(elements.getItems().stream().map(Supplier::get).toArray(Item[]::new));
	}

	@SubscribeEvent
	public void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
		event.getRegistry().registerAll(elements.getEntities().stream().map(Supplier::get).toArray(EntityType[]::new));
	}

	@SubscribeEvent
	public void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
		event.getRegistry().registerAll(elements.getEnchantments().stream().map(Supplier::get).toArray(Enchantment[]::new));
	}

	@SubscribeEvent
	public void registerSounds(RegistryEvent.Register<net.minecraft.util.SoundEvent> event) {
		elements.registerSounds(event);
	}
	private static class HungermechanicsModFMLBusEvents {
		private final HungermechanicsMod parent;
		HungermechanicsModFMLBusEvents(HungermechanicsMod parent) {
			this.parent = parent;
		}

		@SubscribeEvent
		public void serverLoad(FMLServerStartingEvent event) {
			this.parent.elements.getElements().forEach(element -> element.serverLoad(event));
		}
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
			if (player.isSprinting() && this.config.sprinting()) { //if sprinting
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
	
//	private void onAttackEntity(LivingKnockBackEvent event) {
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
    	if (event.getItem().getItem().getFoodProperties() != null ) {
    		if (event.getItem().getItem().getFoodProperties().isMeat()) { //if item eaten was meat
    			boolean rawfromconfig = Arrays.stream(this.config.rawmeat()).anyMatch(event.getItem().getItem().getRegistryName().toString()::equals);
    			if (rawfromconfig || event.getItem().getItem().getRegistryName().toString().contains("raw") == true) { //if item is in defined list or has RAW in name
    				if (1f - chance <= 0.6f) { //60% chance to get salmonela
    					event.getEntityLiving().addEffect(new EffectInstance(Effects.HUNGER, 300, 5));
    				}
    			}
    		}
    	}
    }
}
