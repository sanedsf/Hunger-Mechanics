
package net.anarchx.hungermechanics;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effect;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;

@HungermechanicsModElements.ModElement.Tag
public class HungerMechanicsGravity extends HungermechanicsModElements.ModElement {
	@ObjectHolder("hungermechanics:gravity")
	public static final Effect potion = null;
	public HungerMechanicsGravity(HungermechanicsModElements instance) {
		super(instance, 2);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@SubscribeEvent
	public void registerEffect(RegistryEvent.Register<Effect> event) {
		event.getRegistry().register(new EffectCustom());
	}
	public static class EffectCustom extends Effect {
		private final ResourceLocation potionIcon; //not used, why?
		public EffectCustom() {
			super(EffectType.HARMFUL, -11513776);
			addAttributeModifier(Attributes.MOVEMENT_SPEED, "91AEAA56-376B-4498-935B-2F7F68070635", (double)-0.125F, AttributeModifier.Operation.MULTIPLY_TOTAL);
			setRegistryName("gravity");
			potionIcon = new ResourceLocation("minecraft:textures/mob_effect/slowness.png"); //doesn't work
		}

		@Override
		public ITextComponent getDisplayName() {
			return new TranslationTextComponent("Gravity");
		}

		@Override
		public boolean isBeneficial() {
			return false;
		}

		@Override
		public boolean isInstantenous() {
			return false;
		}

		@Override
		public boolean shouldRenderInvText(EffectInstance effect) {
			return false;
		}

		@Override
		public boolean shouldRender(EffectInstance effect) {
			return false;
		}

		@Override
		public boolean shouldRenderHUD(EffectInstance effect) {
			return false;
		}
	}
}
