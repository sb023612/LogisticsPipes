/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.proxy.buildcraft;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import logisticspipes.LogisticsPipes;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.gates.ActionDisableLogistics;
import logisticspipes.proxy.buildcraft.gates.LogisticsTriggerProvider;
import logisticspipes.proxy.buildcraft.gates.TriggerCrafting;
import logisticspipes.proxy.buildcraft.gates.TriggerHasDestination;
import logisticspipes.proxy.buildcraft.gates.TriggerNeedsPower;
import logisticspipes.proxy.buildcraft.gates.TriggerSupplierFailed;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.tuples.LPPosition;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.CoreConstants;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.utils.Utils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.render.PipeRendererTESR;

public class BuildCraftProxy {
	
	public static ITrigger LogisticsFailedTrigger;
	public static ITrigger LogisticsCraftingTrigger;
	public static ITrigger LogisticsNeedPowerTrigger;
	public static ITrigger LogisticsHasDestinationTrigger;
	public static IAction LogisticsDisableAction;
	
	private Method canPipeConnect;
	
	public boolean checkPipesConnections(TileEntity from, TileEntity to, ForgeDirection way) {
		return checkPipesConnections(from, to, way, false);
	}
	
	//TODO generalise more for TE support
	public boolean checkPipesConnections(TileEntity from, TileEntity to, ForgeDirection way, boolean ignoreSystemDisconnection) {
		if(from instanceof TileGenericPipe && to instanceof TileGenericPipe && (((TileGenericPipe)from).pipe instanceof CoreRoutedPipe || ((TileGenericPipe)to).pipe instanceof CoreRoutedPipe)) {
			if(((TileGenericPipe)from).pipe instanceof CoreRoutedPipe) {
				if (!((CoreRoutedPipe)((TileGenericPipe)from).pipe).canPipeConnect(to, way, ignoreSystemDisconnection)) {
					return false;
				}
			} else {
				((CoreRoutedPipe)((TileGenericPipe) to).pipe).globalIgnoreConnectionDisconnection = true;
				if (!canPipeConnect((TileGenericPipe) from, to, way)) {
					((CoreRoutedPipe)((TileGenericPipe) to).pipe).globalIgnoreConnectionDisconnection = false;
					return false;
				}
				((CoreRoutedPipe)((TileGenericPipe) to).pipe).globalIgnoreConnectionDisconnection = false;
			}
			if(((TileGenericPipe)to).pipe instanceof CoreRoutedPipe) {
				if (!((CoreRoutedPipe)((TileGenericPipe) to).pipe).canPipeConnect(from, way.getOpposite(), ignoreSystemDisconnection)) {
					return false;
				}
			} else {
				((CoreRoutedPipe)((TileGenericPipe) from).pipe).globalIgnoreConnectionDisconnection = true;
				if (!canPipeConnect((TileGenericPipe) to, from, way.getOpposite())) {
					((CoreRoutedPipe)((TileGenericPipe) from).pipe).globalIgnoreConnectionDisconnection = false;
					return false;
				}
				((CoreRoutedPipe)((TileGenericPipe) from).pipe).globalIgnoreConnectionDisconnection = false;
			}
			return true;
		} else if(from instanceof TileGenericPipe && ((TileGenericPipe)from).pipe instanceof CoreRoutedPipe) {
			if (!((CoreRoutedPipe)((TileGenericPipe)from).pipe).canPipeConnect(to, way, ignoreSystemDisconnection)) {
				return false;
			}
			return true;
		} else if(to instanceof TileGenericPipe && ((TileGenericPipe)to).pipe instanceof CoreRoutedPipe) {
			if (!((CoreRoutedPipe)((TileGenericPipe) to).pipe).canPipeConnect(from, way.getOpposite(), ignoreSystemDisconnection)) {
				return false;
			}
			return true;
		} else {
			return Utils.checkPipesConnections(from, to);
		}
	}

	public boolean initProxyAndCheckVersion() {
		try {
			canPipeConnect = TileGenericPipe.class.getDeclaredMethod("canPipeConnect", new Class[]{TileEntity.class, ForgeDirection.class});
			canPipeConnect.setAccessible(true);
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			throw new UnsupportedOperationException("You seem to have an outdated Buildcraft version. Please update to the newest BuildCraft version to run LogisticsPipes.");
		}
	}

	public boolean canPipeConnect(TileGenericPipe tile, TileEntity with, ForgeDirection side) {
		try {
			return (Boolean) canPipeConnect.invoke(tile, with, side);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void dropItems(World world, IInventory inventory, int x, int y, int z) {
		InvUtils.dropItems(world, inventory, x, y, z);
	}

	public void dropItems(World world, ItemStack stack, int x, int y, int z) {
		InvUtils.dropItems(world, stack, x, y, z);
	}

	public void registerTrigger() {
		ActionManager.registerTriggerProvider(new LogisticsTriggerProvider());
		
		/* Triggers */
		LogisticsFailedTrigger = new TriggerSupplierFailed(700);
		LogisticsNeedPowerTrigger = new TriggerNeedsPower(701);
		LogisticsCraftingTrigger = new TriggerCrafting(702);
		LogisticsHasDestinationTrigger = new TriggerHasDestination(703);
		
		/* Actions */
		LogisticsDisableAction = new ActionDisableLogistics(700);
	}

	/**
	 * Registers a new logistics pipe with buildcraft. The buildcraft implementation does not allow for a new item
	 * implementation (only the block)
	 *
	 * @param key   buildcraft key for the pipe
	 * @param clas  Class name of the pipe block
	 * @return the pipe
	 */
	@SuppressWarnings("unchecked")
	public static void registerPipe(int key, Class<? extends Pipe<?>> clas) {
		Map<Integer, Class<? extends Pipe>> pipes = null;
		try {
			pipes = BlockGenericPipe.pipes;
		} catch(NoSuchFieldError e) {
			try {
				pipes = (Map<Integer, Class<? extends Pipe>>) BlockGenericPipe.class.getDeclaredField("pipes").get(null);
			} catch (Exception e2) {
				return;
			}
		}
		pipes.put(key, clas);
		return;
	}

	//IToolWrench interaction
	public boolean isWrenchEquipped(EntityPlayer entityplayer) {
		return (entityplayer.getCurrentEquippedItem() != null) && (entityplayer.getCurrentEquippedItem().getItem() instanceof IToolWrench);
	}

	public boolean canWrench(EntityPlayer entityplayer, int x, int y, int z) {
		if ((entityplayer.getCurrentEquippedItem() != null) && (entityplayer.getCurrentEquippedItem().getItem() instanceof IToolWrench))
			return ((IToolWrench)entityplayer.getCurrentEquippedItem().getItem()).canWrench(entityplayer, x, y, z);
		return false;
	}

	public void wrenchUsed(EntityPlayer entityplayer, int x, int y, int z) {
		if ((entityplayer.getCurrentEquippedItem() != null) && (entityplayer.getCurrentEquippedItem().getItem() instanceof IToolWrench))
			((IToolWrench)entityplayer.getCurrentEquippedItem().getItem()).wrenchUsed(entityplayer, x, y, z);
	}


	public boolean isUpgradeManagerEquipped(EntityPlayer entityplayer) {
		return entityplayer != null && entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().itemID == LogisticsPipes.LogisticsUpgradeManager.itemID;
	}
	
	public void resetItemRotation(PipeRendererTESR renderer) {
		try {
			Field f = PipeRendererTESR.class.getDeclaredField("dummyEntityItem");
			f.setAccessible(true);
			EntityItem item = (EntityItem) f.get(renderer);
			item.hoverStart = 0;
		} catch(NoSuchFieldException e) {
			e.printStackTrace();
		} catch(SecurityException e) {
			e.printStackTrace();
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
		} catch(IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void registerPipeInformationProvider() {
		SimpleServiceLocator.pipeInformaitonManager.registerProvider(TileGenericPipe.class, BCPipeInformationProvider.class);
	}
	
	public boolean insertIntoBuildcraftPipe(TileEntity tile, LPTravelingItem item) {
		if(tile instanceof TileGenericPipe) {
			TileGenericPipe pipe = (TileGenericPipe)tile;
			if(BlockGenericPipe.isValid(pipe.pipe) && pipe.pipe.transport instanceof PipeTransportItems) {
				TravelingItem bcItem = null;
				if(item instanceof LPTravelingItemServer) {
					LPRoutedBCTravelingItem lpBCItem = new LPRoutedBCTravelingItem();
					lpBCItem.setRoutingInformation(((LPTravelingItemServer)item).getInfo());
					lpBCItem.saveToExtraNBTData();
					bcItem = lpBCItem;
				} else {
					bcItem = new TravelingItem();
				}
				LPPosition p = new LPPosition(tile.xCoord + 0.5F, tile.yCoord + CoreConstants.PIPE_MIN_POS, tile.zCoord + 0.5F);
				if(item.output.getOpposite() == ForgeDirection.DOWN) {
					p.moveForward(item.output.getOpposite(), 0.24F);
				} else if(item.output.getOpposite() == ForgeDirection.UP) {
					p.moveForward(item.output.getOpposite(), 0.74F);
				} else {
					p.moveForward(item.output.getOpposite(), 0.49F);
				}
				bcItem.setPosition(p.getXD(), p.getYD(), p.getZD());
				bcItem.setSpeed(item.getSpeed());
				if(item.getItemIdentifierStack() != null) {
					bcItem.setItemStack(item.getItemIdentifierStack().makeNormalStack());
				}
				((PipeTransportItems)pipe.pipe.transport).injectItem(bcItem, item.output);
				return true;
			}
		}
		return false;
	}
}
