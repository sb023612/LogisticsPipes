/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui.modules;

import logisticspipes.modules.ModuleItemSink;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.module.ItemSinkDefaultPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiStringHandlerButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiItemSink extends ModuleBaseGui {

	private final ModuleItemSink _itemSink;
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
       //Default item toggle:
       buttonList.clear();
       buttonList.add(new GuiStringHandlerButton(0, width / 2 + 50, height / 2 - 34, 30, 20, new GuiStringHandlerButton.StringHandler(){
		@Override
		public String getContent() {
			return _itemSink.isDefaultRoute() ? "Yes" : "No";
		}}));
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		switch(guibutton.id)
		{
			case 0:
				_itemSink.setDefaultRoute(!_itemSink.isDefaultRoute());
				MainProxy.sendPacketToServer(PacketHandler.getPacket(ItemSinkDefaultPacket.class).setDefault(_itemSink.isDefaultRoute()).setModulePos(_itemSink));
				break;
		}
		
	}
	
	public GuiItemSink(IInventory playerInventory, ModuleItemSink itemSink) {
		super(null, itemSink);
		_itemSink = itemSink;
		DummyContainer dummy = new DummyContainer(playerInventory, _itemSink.getFilterInventory());
		dummy.addNormalSlotsForPlayerInventory(8, 60);

		//Pipe slots
	    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
	    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
	    }
	    
	    this.inventorySlots = dummy;
		xSize = 175;
		ySize = 142;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		mc.fontRenderer.drawString(_itemSink.getFilterInventory().getInventoryName(), 8, 6, 0x404040);
		mc.fontRenderer.drawString("Inventory", 8, ySize - 92, 0x404040);
		mc.fontRenderer.drawString("Default route:", 65, 45, 0x404040);
	}
	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/itemsink.png");
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}
}
