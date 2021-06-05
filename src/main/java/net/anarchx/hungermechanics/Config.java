package net.anarchx.hungermechanics;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class Config {
    
    private final ForgeConfigSpec spec;
//    private final ConfigValue<Boolean> gravity;
    private final ConfigValue<String> movementspeed;
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
	private final ConfigValue<String> customblocks;
	
    public Config() {
        
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        
        // General Configs
        builder.comment("Settings");
        builder.push("Hunger Mechanics");
        builder.comment("Will the player lose hunger every tick?");
        this.hunger = builder.define("hunger-depletion", true);
        
        builder.comment("How much hunger will the player lose every tick?"
        		+ "\nThis value is ADDED to the vanila rate. Do not set this too high. You have been warned.");
        this.hungervalue = builder.define("hunger-depletion-rate", 0.01);  //Vanilla sprinting is 0.1
        
        builder.comment("Will the player lose hunger while sprinting?");
        this.sprinting = builder.define("sprinting-depletion", true);
        
        builder.comment("How much hunger will the player lose while sprinting?"
        		+ "\nThis value is ADDED to the vanila rate. Do not set this too high. You have been warned.");
        this.sprintingvalue = builder.define("sprinting-depletion-rate", 0.02);  //Vanilla sprinting is 0.1
        
        builder.comment("Will the Hunger Descriptors be drawn on screen?");
        this.descriptor = builder.define("descriptor", true);
        
        builder.comment("Hunger Descriptions"
        		+ "\nALWAYS MUST BE 5 descriptors and the Invalid tag MUST BE in the first position!!!"
        		+ "\nSeperated by semi-colon!");
        this.hungerdescriptor = builder.define("descriptor-hunger", ("Invalid;Peckish;Hungry;Starving;Dying").toString());
        
        builder.comment("If the player has the 'Starving' or 'Dying' stage then cannot jump anymore.");
        this.jumping  = builder.define("jumping", true);
        
        builder.comment("If the player has the 'Starving' or 'Dying' stage then does less damage."
        		+ "\nThis applies to weapons as well."
        		+ "\n-50% damage when Starving -75% damage when Dying");
        this.attack  = builder.define("attack", true);
        
        builder.comment("Slow down the players block breaking speed based on how hungry they are?");
        this.breaking = builder.define("breaking", true); 
        
        builder.push("Gravity Effect");
//        builder.comment("Enable the global gravity effect?\nThis only disables the slow moving effect from working.");
//        this.gravity = builder.define("gravity-effect", false);
//        
        builder.comment("Movement speed REDUCTION of player base speed and gravity based on the formula below."
        		+ "\nSetting negative values will instead increase the speed by that percentage."
        		+ "\n50% reduction = Base player speed * (100-50)/100"
        		+ "\nValues in order: Base speed, SlowPlants, SlowBlocks I, SlowBlocks II"
        		+ "\n0;40;10;30"
        		+ "\nSeperated by semi-colon!");
        this.movementspeed = builder.define("movement-reduction", ("0;40;10;30").toString());

        builder.comment("Blocks in this list will have custom movement REDUCTION PERCENTAGE using the movement-reduction formula."
        		+ "\nSetting negative values will instead increase the speed by that percentage."
        		+ "\nMod:Blockname, Speed Reduction Percentage;"
        		+ "\nminecraft:air,20;minecraft:gravel,-15;minecraft:path,-15;"
        		+ "\nSeperated by semi-colon! Use JEI to find the right values.");
        this.customblocks = builder.define("customblocks", ("minecraft:air,20;minecraft:gravel,-15;minecraft:path,-15;").toString());

        builder.pop();
        
        builder.push("Salmonela");
        builder.comment("Having the HUNGER effect prevents the player from eating more food?"
        		+ "\nFoods considered INSTANT will work no matter what! This is intended.");
        this.salmonela  = builder.define("hungry", true);

        builder.comment("List of RAW foods that will be considered for giving the player the HUNGER effect."
        		+ "\nexample: minecraft:tropical_fish"
        		+ "\nSeperated by semi-colon! Use JEI to find the right values.");
        this.rawmeat = builder.define("rawmeat", ("minecraft:tropical_fish;").toString());
        builder.pop();
        
        builder.push("Heavy Armor");
        builder.comment("Armors will consume more food while wearing?");
        this.heavyarmor = builder.define("heavyarmor", true);
        
        builder.comment("How much hunger will the armors deplete?"
        		+ "\nThis value is ADDED to the vanila rate. Do not set this too high. You have been warned.");
        this.heavyarmorvalue = builder.define("heavyarmor-depletion", 0.02);
        
        builder.comment("List of armors valid for sinking and heavyarmor categories."
        		+ "\nChestplates and Leggings ONLY! Using head or boots will NOT WORK!!!"
        		+ "\nSeperated by semi-colon.! Use JEI to find the right values.");
        this.armorinslot = builder.define("sinking-armor", ("minecraft:iron_chestplate;minecraft:iron_leggings;minecraft:diamond_chestplate;minecraft:diamond_leggings").toString());
        
        builder.comment("Wearing Armor makes you sink to the bottom of the water.");
        this.sink = builder.define("sinking", true);
        
        builder.pop();
        builder.pop();
        this.spec = builder.build();
    }
    
    public ForgeConfigSpec getSpec () {
        
        return this.spec;
    }
    
//    public boolean gravity() {
//    	return this.gravity.get();
//    }
    
    public boolean breaking() {
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
    	return this.rawmeat.get().split(";");
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
    	return this.armorinslot.get().split(";");
    }
    
    public boolean descriptor() {
    	return this.descriptor.get();
    }
    
    public String[] hungerdescriptor() {
    	return this.hungerdescriptor.get().split(";");
    }
    
    public String[] movementspeed() {
    	return this.movementspeed.get().split(";");
    }
    
    public String[] customblocks() {
        return this.customblocks.get().split(";");
    }
}