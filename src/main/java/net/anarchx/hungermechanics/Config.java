package net.anarchx.hungermechanics;

//import java.lang.reflect.Array;
//import java.util.Arrays;
//import java.util.List;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class Config {
    
    private final ForgeConfigSpec spec;
    private final BooleanValue gravity;
    private final BooleanValue hunger;
    private final ConfigValue<Double> hungervalue;
    private final BooleanValue sprinting;
    private final ConfigValue<Double> sprintingvalue;
    private final BooleanValue breaking;
    private final BooleanValue jumping;
    private final BooleanValue salmonela;
    private final ConfigValue<String> rawmeat;
    private final BooleanValue sink;
    private final BooleanValue heavyarmor;
    private final BooleanValue attack;
    private final ConfigValue<Double> heavyarmorvalue;
	private final ConfigValue<String> armorinslot;
	private final BooleanValue descriptor;
	private final ConfigValue<String> hungerdescriptor;
	
    public Config() {
        
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        
        // General Configs
        builder.comment("Settings");
        builder.push("Hunger Mechanics");
        
        builder.comment("Enable the global gravity effect?");
        this.gravity = builder.define("gravity-effect", true);
        
        builder.comment("Will the player lose hunger every tick?");
        this.hunger = builder.define("hunger-depletion", true);
        
        builder.comment("How much hunger will the player lose every tick?");
        builder.comment("This value is added to the vanila rate. Do not set this too high. You have been warned.");
        this.hungervalue = builder.define("hunger-depletion-rate", 0.01);  //Vanilla sprinting is 0.1
        
        builder.comment("Will the player lose hunger while sprinting?");
        this.sprinting = builder.define("sprinting-depletion", true);
        
        builder.comment("How much hunger will the player lose while sprinting?");
        builder.comment("This value is added to the vanila rate. Do not set this too high. You have been warned.");
        this.sprintingvalue = builder.define("sprinting-depletion-rate", 0.02);  //Vanilla sprinting is 0.1
        
        builder.comment("Slow down the players block breaking speed based on how hungry they are?");
        this.breaking = builder.define("breaking", true);
        
        builder.comment("Having the HUNGER effect prevents the player from eating more food?");
        builder.comment("Foods considered INSTANT will work no matter what! This is intended.");
        this.salmonela  = builder.define("hungry", true);

        builder.comment("List of RAW foods that will be considered for giving the player the HUNGER effect. Seperated by commas.");
        builder.comment("example: minecraft:tropical_fish");
        this.rawmeat = builder.define("rawmeat", ("minecraft:tropical_fish").toString());
        
        builder.comment("Wearing Armor makes you sink to the bottom of the water.");
        this.sink = builder.define("sinking", true);
        
        builder.comment("Armors will consume more food while wearing?");
        this.heavyarmor = builder.define("heavyarmor", true);
        
        builder.comment("How much hunger will the armors deplete?");
        builder.comment("This value is added to the vanila rate. Do not set this too high. You have been warned.");
        this.heavyarmorvalue = builder.define("heavyarmor-depletion", 0.02);
        
        builder.comment("List of armors valid for sinking and heavyarmor categories.");
        builder.comment("Chestplates and Leggings ONLY! Using head or boots will NOT WORK!!!");
        builder.comment("Separated by commas! Use JEI to find the right values.");
        this.armorinslot = builder.define("sinking-armor", ("minecraft:iron_chestplate,minecraft:iron_leggings,minecraft:diamond_chestplate,minecraft:diamond_leggings").toString());
        
        builder.comment("Will the Hunger Descriptors be drawn on screen?");
        this.descriptor = builder.define("descriptor", true);
        
        builder.comment("Hunger Descriptions // Always needs to be 5 descriptors and the Invalid tag MUST be in the first position!");
        this.hungerdescriptor = builder.define("descriptor-hunger", ("Invalid, Peckish, Hungry, Starving, Dying").toString());
        
        builder.comment("If the player has the 'Starving' or 'Dying' stage then cannot jump anymore.");
        this.jumping  = builder.define("jumping", true);
        
        builder.comment("If the player has the 'Starving' or 'Dying' stage then does less damage.");
        builder.comment("This applies to weapons as well.");
        builder.comment("-50% damage when Starving -75% damage when Dying");
        this.attack  = builder.define("attack", true);
        
        builder.pop();
        this.spec = builder.build();
    }
    
    public ForgeConfigSpec getSpec () {
        
        return this.spec;
    }
    
    public boolean gravity () {
    	
    	return this.gravity.get();
    }
    
    public boolean breaking () {
    	
    	return this.breaking.get();
    }

	public boolean hunger() {
		return this.hunger.get();
	}
	
	public Double hungervalue() {
		return this.hungervalue.get();
	}
	
	public boolean sprinting() {
		return this.sprinting.get();
	}
	
	public Double sprintingvalue() {
		return this.sprintingvalue.get();
	}
	
    public boolean jumping() {
    	return this.jumping.get();
    }

    public boolean salmonela() {
    	return this.salmonela.get();
    }

    public String[] rawmeat() {
    	return this.rawmeat.get().split(",");
    }
    
    public boolean heavyarmor() {
    	return this.heavyarmor.get();
    }
    
    public Double heavyarmorvalue() {
		return this.heavyarmorvalue.get();
	}
    
    public boolean sink() {
    	return this.sink.get();
    }
    
    public boolean attack() {
    	return this.attack.get();
    }
    
    public String[] armorinslot() {
    	return this.armorinslot.get().split(",");
    }
    
    public boolean descriptor() {
    	return this.descriptor.get();
    }
    
    public String[] hungerdescriptor() {
    	return this.hungerdescriptor.get().split(",");
    }
}