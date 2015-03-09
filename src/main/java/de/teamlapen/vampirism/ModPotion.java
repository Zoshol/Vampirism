package de.teamlapen.vampirism;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import de.teamlapen.vampirism.util.Logger;
import net.minecraft.potion.Potion;

public class ModPotion extends Potion{
	public static Potion sunscreen;
	
	public ModPotion(int id, boolean full_effectiv, int color) {
		super(id, full_effectiv, color);
	}
	
	public Potion setIconIndex(int par1, int par2)
	{
		super.setIconIndex(par1, par2);
		return this;
	}

	private static void increasePotionArraySize(){
		Potion[] potionTypes = null;

	    for (Field f : Potion.class.getDeclaredFields()) {
	        f.setAccessible(true);
	        try {
	            if (f.getName().equals("potionTypes") || f.getName().equals("field_76425_a")) {
	                Field modfield = Field.class.getDeclaredField("modifiers");
	                modfield.setAccessible(true);
	                modfield.setInt(f, f.getModifiers() & ~Modifier.FINAL);

	                potionTypes = (Potion[])f.get(null);
	                final Potion[] newPotionTypes = new Potion[256];
	                System.arraycopy(potionTypes, 0, newPotionTypes, 0, potionTypes.length);
	                f.set(null, newPotionTypes);
	            }
	        } catch (Exception e) {
	        	Logger.e("ModPotion","COULDN'T INCREASE POTION ARRAY SIZE",e);
	        }
	    }

	}
	
	public static void init(){
		increasePotionArraySize();
		sunscreen=new ModPotion(40,false,345345).setIconIndex(0, 0).setPotionName("potion.vampirism:sunscreen");
	}
}