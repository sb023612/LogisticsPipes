/** 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui;

import logisticspipes.interfaces.IGuiIDHandlerProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.pipe.FluidSupplierMode;
import logisticspipes.pipes.PipeItemsFluidSupplier;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.string.StringUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiFluidSupplierPipe extends GuiContainer implements IGuiIDHandlerProvider {
	private static final String PREFIX = "gui.fluidsupplier.";
	
	private PipeItemsFluidSupplier logic; 
	
	public GuiFluidSupplierPipe(IInventory playerInventory, IInventory dummyInventory, PipeItemsFluidSupplier logic) {
		super(null);
		
		
		DummyContainer dummy = new DummyContainer(playerInventory, dummyInventory);
		dummy.addNormalSlotsForPlayerInventory(18, 97);
		
		int xOffset = 72;
		int yOffset = 18;
		
		for (int row = 0; row < 3; row++){
			for (int column = 0; column < 3; column++){
				dummy.addDummySlot(column + row * 3, xOffset + column * 18, yOffset + row * 18);					
			}
		}
		this.inventorySlots = dummy; 

		this.logic = logic;
		xSize = 194;
		ySize = 186;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "TargetInv"), xSize / 2 - mc.fontRenderer.getStringWidth(StringUtil.translate(PREFIX + "TargetInv"))/2, 6, 0x404040);
		mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "Inventory"), 18, ySize - 102, 0x404040);
		mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "Partialrequests") + ":", xSize - 140, ySize - 112, 0x404040);
	}
	
	protected static final ResourceLocation SUPPLIER = new ResourceLocation("logisticspipes", "textures/gui/supplier.png");

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {	
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(SUPPLIER);
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
       buttonList.clear();
       buttonList.add(new GuiButton(0, width / 2 + 45, height / 2 - 25, 30, 20, logic.isRequestingPartials() ? StringUtil.translate(PREFIX + "Yes") : StringUtil.translate(PREFIX + "No")));

	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 0){
			logic.setRequestingPartials(!logic.isRequestingPartials());
			((GuiButton)buttonList.get(0)).displayString = logic.isRequestingPartials() ? StringUtil.translate(PREFIX + "Yes") : StringUtil.translate(PREFIX + "No");
			MainProxy.sendPacketToServer(PacketHandler.getPacket(FluidSupplierMode.class).setInteger((logic.isRequestingPartials() ? 1 : 0)).setPosX(logic.getX()).setPosY(logic.getY()).setPosZ(logic.getZ()));
		}
		super.actionPerformed(guibutton);
		
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_FluidSupplier_ID;
	}
}
