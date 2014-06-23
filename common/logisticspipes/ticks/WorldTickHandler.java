package logisticspipes.ticks;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.LinkedList;

import logisticspipes.LogisticsPipes;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.item.ItemIdentifier;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.core.ITileBufferHolder;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class WorldTickHandler implements ITickHandler {
	
	public static LinkedList<TileGenericPipe> clientPipesToReplace = new LinkedList<TileGenericPipe>();
	public static LinkedList<TileGenericPipe> serverPipesToReplace = new LinkedList<TileGenericPipe>();
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		LinkedList<TileGenericPipe> localList;
		if(type.contains(TickType.CLIENT)) {
			MainProxy.proxy.tickClient();
			localList = clientPipesToReplace;
		} else if(type.contains(TickType.SERVER)) {
			MainProxy.proxy.tickServer();
			localList = serverPipesToReplace;
		} else {
			System.out.println("not client, not server ... what is " + type);
			return;
		}
		while(localList.size() > 0) {
			//try {
				TileGenericPipe tile = localList.get(0);
				int x = tile.xCoord;
				int y = tile.yCoord;
				int z = tile.zCoord;
				World world = tile.worldObj;

				System.out.println((world.isRemote?"Client":"Server") + " replacing BC tile/block with LogisticsPipes tile/block at " + x + "," + y + "," + z);

				//TE or its chunk might've gone away while we weren't looking
				TileEntity tilecheck = world.getBlockTileEntity(x, y, z);
				if(tilecheck != tile) {
					System.out.println("Tile changed under us, aborting");
					localList.remove(0);
					continue;
				}

				//ugly magic here.
				TileGenericPipe newTile;
				//set tile.pipe to null, so tile.invalidate() won't call pipe.invalidate()
				Pipe savedpipe = tile.pipe;
				tile.pipe = null;
				if(tile instanceof LogisticsTileGenericPipe) {
					//we're already the right TE, re-use it
					newTile = tile;
				} else {
					//copy all data from the BC TE to a LP TE
					newTile = new LogisticsTileGenericPipe();
					for(Field field:tile.getClass().getDeclaredFields()) {
						try {
							field.setAccessible(true);
							field.set(newTile, field.get(tile));
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}
				}
				//switch out the block with a LogisticsBlockLogisticsPipe, calls invalidate on the old tile, breakBlock on the BlockGenericPipe and creates a new LogisticsTileGenericPipe we don't really need in the process
				world.setBlock(x, y, z, LogisticsPipes.LogisticsPipeBlock.blockID);
				//validate the new/saved TE
				newTile.validate();
				//restore the pipe
				newTile.pipe = savedpipe;
				//now swap the newly created TE out with the one containing the data of the previous TE
				world.setBlockTileEntity(x, y, z, newTile);
				//great so far, we still have to tell any items in the pipe about their shiny new tile.
				if(newTile.pipe != null) {
					newTile.pipe.setTile(newTile);
					if(newTile.pipe.transport instanceof PipeTransportItems) {
						for(TravelingItem entity:((PipeTransportItems)newTile.pipe.transport).items) {
							entity.setContainer(newTile);
						}
					}
				}

				//getTile creates the TileCache as needed.
				for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
					TileEntity tileSide = newTile.getTile(o);

					if (tileSide instanceof ITileBufferHolder) {
						((ITileBufferHolder) tileSide).blockCreated(o, LogisticsPipes.LogisticsPipeBlock.blockID, newTile);
					}
				}
				//newTile.scheduleNeighborChange();
			/*} catch (IllegalAccessException e) {
				e.printStackTrace();
			}*/
			localList.remove(0);
		}
		ItemIdentifier.tick();
		FluidIdentifier.initFromForge(true);
		if(type.contains(TickType.SERVER)) {
			HudUpdateTick.tick();
			SimpleServiceLocator.craftingPermissionManager.tick();
			if(LogisticsPipes.WATCHDOG) {
				Watchdog.tickServer();
			}
		} else {
			if(LogisticsPipes.WATCHDOG) {
				Watchdog.tickClient();
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT, TickType.SERVER);
	}

	@Override
	public String getLabel() {
		return "LogisticsPipes WorldTick";
	}
}
