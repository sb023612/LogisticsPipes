package logisticspipes.proxy.interfaces;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IIronChestProxy {

	public boolean isIronChest(TileEntity tile);

	@SideOnly(Side.CLIENT)
	public boolean isChestGui(GuiScreen gui);
}
