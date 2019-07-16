package i.gishreloaded.gishcode.hack.hacks;

import i.gishreloaded.gishcode.hack.Hack;
import i.gishreloaded.gishcode.hack.HackCategory;
import i.gishreloaded.gishcode.value.BooleanValue;
import i.gishreloaded.gishcode.value.Mode;
import i.gishreloaded.gishcode.value.ModeValue;

public class Teams extends Hack{

	public ModeValue mode;
	
	public Teams() {
		super("Teams", HackCategory.OTHER);
		
		this.mode = new ModeValue("Mode", new Mode("Base", false), new Mode("ArmorColor", false), new Mode("NameColor", true));
		
		this.addValue(mode);
	}

}
