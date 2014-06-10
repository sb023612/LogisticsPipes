package logisticspipes.proxy.interfaces;

import net.minecraft.client.model.ModelSign;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IBetterSignProxy {

	@SideOnly(Side.CLIENT)
	public void hideSignSticks(ModelSign model);
}
