package i.gishreloaded.gishcode.hack.hacks;

import java.awt.Color;

import org.lwjgl.input.Keyboard;

import i.gishreloaded.gishcode.Main;
import i.gishreloaded.gishcode.hack.Hack;
import i.gishreloaded.gishcode.hack.HackCategory;
import i.gishreloaded.gishcode.utils.system.Wrapper;
import i.gishreloaded.gishcode.utils.visual.ColorUtils;
import i.gishreloaded.gishcode.utils.visual.RenderUtils;
import i.gishreloaded.gishcode.value.BooleanValue;
import i.gishreloaded.gishcode.value.Mode;
import i.gishreloaded.gishcode.value.ModeValue;
import i.gishreloaded.gishcode.value.NumberValue;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Text;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class ClickGui extends Hack{

	public static BooleanValue rainbow;
	public static BooleanValue shadow;
	
	public static NumberValue red;
	public static NumberValue green;
	public static NumberValue blue;
	public static NumberValue alpha;
	
	public static int color;
	
	public ClickGui() {
		super("ClickGui", HackCategory.VISUAL);
		this.setKey(Keyboard.KEY_RSHIFT);
		this.setShow(false);
		
		this.shadow = new BooleanValue("Shadow", true);
		this.rainbow = new BooleanValue("Rainbow", true);
		this.red = new NumberValue("Red", 170D, 0D, 255D);
		this.green = new NumberValue("Green", 170D, 0D, 255D);
		this.blue = new NumberValue("Blue", 170D, 0D, 255D);
		this.alpha = new NumberValue("Alpha", 170D, 0D, 255D);
		
		this.addValue(shadow, rainbow, red, green, blue, alpha);
		this.setColor();
	}
	
	 public static int getColor() {
		 return rainbow.getValue() ? ColorUtils.rainbow().getRGB() : color;
	 }
	
	 public static void setColor() {
		if(rainbow.getValue()) {
			color = ColorUtils.rainbow().getRGB();
		}
		else
		{
			color = ColorUtils.color(red.getValue().intValue(),
					green.getValue().intValue(),
					blue.getValue().intValue(),
					alpha.getValue().intValue());
		}
	}
	
	@Override
	public void onEnable() {
		Wrapper.INSTANCE.mc().displayGuiScreen(Main.hackManager.getGui());
		super.onEnable();
	}
	
	@Override
	public void onClientTick(ClientTickEvent event) {
		this.setColor();
		super.onClientTick(event);
	}
	
	@Override
	public void onRenderGameOverlay(Text event) {
		if(shadow.getValue()) {
			ScaledResolution sr = new ScaledResolution(Wrapper.INSTANCE.mc());
			RenderUtils.drawRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), ColorUtils.color(0.0F, 0.0F, 0.0F, 0.5F));
		}
		super.onRenderGameOverlay(event);
	}

}
