package logisticspipes.pipes.basic;

import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import logisticspipes.blocks.powertile.LogisticsPowerProviderTileEntity;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.subproxies.ILPBCPowerProxy;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.WorldUtil;
import logisticspipes.utils.tuples.Pair;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class PowerSupplierHandler {
	private final static float INTERNAL_BC_BUFFER_MAX = 1000;
	private final static float INTERNAL_RF_BUFFER_MAX = 10000;
	private final static float INTERNAL_IC2_BUFFER_MAX = 2048 * 4;
	private WeakHashMap<TileEntity, ILPBCPowerProxy> bcPowerTileCache = new WeakHashMap<TileEntity, ILPBCPowerProxy>();
	
	private final CoreRoutedPipe pipe;

	private float internal_BC_Buffer = 0F;
	private float internal_RF_Buffer = 0F;
	private float internal_IC2_Buffer = 0F;
	
	public PowerSupplierHandler(CoreRoutedPipe pipe) {
		this.pipe = pipe;
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		if(internal_BC_Buffer > 0)
			nbttagcompound.setFloat("bufferMJ", internal_BC_Buffer);
		if(internal_RF_Buffer > 0)
			nbttagcompound.setFloat("bufferRF", internal_RF_Buffer);
		if(internal_IC2_Buffer > 0)
			nbttagcompound.setFloat("bufferEU", internal_IC2_Buffer);
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		internal_BC_Buffer = nbttagcompound.getFloat("bufferMJ");
		internal_RF_Buffer = nbttagcompound.getFloat("bufferRF");
		internal_IC2_Buffer = nbttagcompound.getFloat("bufferEU");
	}

	public void update() {
		if(SimpleServiceLocator.buildCraftProxy.isActive() && this.pipe.getUpgradeManager().hasBCPowerSupplierUpgrade()) {
			if(pipe.isNthTick(20 * 60 * 10)) {
				bcPowerTileCache.clear();
			}
			//Use Buffer
			WorldUtil worldUtil = new WorldUtil(this.pipe.getWorld(), this.pipe.getX(), this.pipe.getY(), this.pipe.getZ());
			LinkedList<AdjacentTile> adjacent = worldUtil.getAdjacentTileEntities(false);
			float globalNeed = 0;
			double[] need = new double[adjacent.size()];
			int i=0;
			for(AdjacentTile adTile:adjacent) {
				if(this.pipe.canPipeConnect(adTile.tile, adTile.orientation)) {
					ILPBCPowerProxy receptor = null;
					if(!bcPowerTileCache.containsKey(adTile.tile)) {
						receptor = SimpleServiceLocator.buildCraftProxy.getPowerReceiver(adTile.tile, adTile.orientation);
						bcPowerTileCache.put(adTile.tile, receptor);
					} else {
						receptor = bcPowerTileCache.get(adTile.tile);
					}
					if(receptor != null) {
						globalNeed += need[i] = Math.min(receptor.getMaxEnergyReceived(), receptor.getMaxEnergyStored() - receptor.getEnergyStored());
					}
				}
				i++;
			}
			if(globalNeed != 0 && !Float.isNaN(globalNeed)) {
				float fullfillable = Math.min(1, internal_BC_Buffer / globalNeed);
				i = 0;
				for(AdjacentTile adTile:adjacent) {
					if(this.pipe.canPipeConnect(adTile.tile, adTile.orientation)) {
						ILPBCPowerProxy receptor = SimpleServiceLocator.buildCraftProxy.getPowerReceiver(adTile.tile, adTile.orientation);
						if(receptor != null) {
							if(internal_BC_Buffer + 1 < need[i] * fullfillable) return;
							double used = receptor.receiveEnergy(need[i] * fullfillable, adTile.orientation);
							if(used > 0) {
								//MainProxy.sendPacketToAllWatchingChunk(this.pipe.getX(), this.pipe.getZ(), MainProxy.getDimensionForWorld(this.pipe.getWorld()), PacketHandler.getPacket(PowerPacketLaser.class).setColor(LogisticsPowerProviderTileEntity.BC_COLOR).setPos(this.pipe.getLPPosition()).setRenderBall(true).setDir(adTile.orientation).setLength(0.5F));
								((LogisticsTileGenericPipe)pipe.container).addLaser(adTile.orientation, 0.5F, LogisticsPowerProviderTileEntity.BC_COLOR, false, true);
								internal_BC_Buffer -= used;
							}
							if(internal_BC_Buffer < 0) {
								internal_BC_Buffer = 0;
								return;
							}
						}
					}
					i++;
				}
			}
			//Rerequest Buffer
			List<Pair<ISubSystemPowerProvider, List<IFilter>>> provider = this.pipe.getRouter().getSubSystemPowerProvider();
			float available = 0;
			outer:
			for(Pair<ISubSystemPowerProvider, List<IFilter>> pair : provider) {
				for(IFilter filter:pair.getValue2()) {
					if(filter.blockPower()) continue outer;
				}
				if(pair.getValue1().usePaused()) continue;
				if(!pair.getValue1().getBrand().equals("MJ")) continue;
				available += pair.getValue1().getPowerLevel();
			}
			if(available > 0) {
				float neededPower = INTERNAL_BC_BUFFER_MAX - internal_BC_Buffer;
				if(neededPower > 0) {
					if(this.pipe.useEnergy((int) (neededPower / 1000), false)) {
						outer:
						for(Pair<ISubSystemPowerProvider, List<IFilter>> pair : provider) {
							for(IFilter filter:pair.getValue2()) {
								if(filter.blockPower()) continue outer;
							}
							if(pair.getValue1().usePaused()) continue;
							if(!pair.getValue1().getBrand().equals("MJ")) continue;
							float requestamount = neededPower * (pair.getValue1().getPowerLevel() / available);
							pair.getValue1().requestPower(this.pipe.getRouterId(), requestamount);
						}
					}
				}
			}
		} else if(!bcPowerTileCache.isEmpty()) {
			bcPowerTileCache.clear();
		}
		if(SimpleServiceLocator.thermalExpansionProxy.isTE() && this.pipe.getUpgradeManager().hasRFPowerSupplierUpgrade()) {
			//Use Buffer
			WorldUtil worldUtil = new WorldUtil(this.pipe.getWorld(), this.pipe.getX(), this.pipe.getY(), this.pipe.getZ());
			LinkedList<AdjacentTile> adjacent = worldUtil.getAdjacentTileEntities(false);
			float globalNeed = 0;
			float[] need = new float[adjacent.size()];
			int i=0;
			for(AdjacentTile adTile:adjacent) {
				if(SimpleServiceLocator.thermalExpansionProxy.isEnergyHandler(adTile.tile) && this.pipe.canPipeConnect(adTile.tile, adTile.orientation) && SimpleServiceLocator.thermalExpansionProxy.canConnectEnergy(adTile.tile, adTile.orientation.getOpposite())) {
					globalNeed += need[i] = SimpleServiceLocator.thermalExpansionProxy.getMaxEnergyStored(adTile.tile, adTile.orientation.getOpposite()) - SimpleServiceLocator.thermalExpansionProxy.getEnergyStored(adTile.tile, adTile.orientation.getOpposite());
				}
				i++;
			}
			if(globalNeed != 0 && !Float.isNaN(globalNeed)) {
				float fullfillable = Math.min(1, internal_RF_Buffer / globalNeed);
				i = 0;
				for(AdjacentTile adTile:adjacent) {
					if(SimpleServiceLocator.thermalExpansionProxy.isEnergyHandler(adTile.tile) && this.pipe.canPipeConnect(adTile.tile, adTile.orientation) && SimpleServiceLocator.thermalExpansionProxy.canConnectEnergy(adTile.tile, adTile.orientation.getOpposite())) {
						if(internal_RF_Buffer + 1 < need[i] * fullfillable) return;
						int used = SimpleServiceLocator.thermalExpansionProxy.receiveEnergy(adTile.tile, adTile.orientation.getOpposite(), (int) (need[i] * fullfillable), false);
						if(used > 0) {
							//MainProxy.sendPacketToAllWatchingChunk(this.pipe.getX(), this.pipe.getZ(), MainProxy.getDimensionForWorld(this.pipe.getWorld()), PacketHandler.getPacket(PowerPacketLaser.class).setColor(LogisticsPowerProviderTileEntity.RF_COLOR).setPos(this.pipe.getLPPosition()).setRenderBall(true).setDir(adTile.orientation).setLength(0.5F));
							((LogisticsTileGenericPipe)pipe.container).addLaser(adTile.orientation, 0.5F, LogisticsPowerProviderTileEntity.RF_COLOR, false, true);
							internal_RF_Buffer -= used;
						}
						if(internal_RF_Buffer < 0) {
							internal_RF_Buffer = 0;
							return;
						}
					}
					i++;
				}
			}
			//Rerequest Buffer
			List<Pair<ISubSystemPowerProvider, List<IFilter>>> provider = this.pipe.getRouter().getSubSystemPowerProvider();
			float available = 0;
			outer:
			for(Pair<ISubSystemPowerProvider, List<IFilter>> pair : provider) {
				for(IFilter filter:pair.getValue2()) {
					if(filter.blockPower()) continue outer;
				}
				if(pair.getValue1().usePaused()) continue;
				if(!pair.getValue1().getBrand().equals("RF")) continue;
				available += pair.getValue1().getPowerLevel();
			}
			if(available > 0) {
				float neededPower = INTERNAL_RF_BUFFER_MAX - internal_RF_Buffer;
				if(neededPower > 0) {
					if(this.pipe.useEnergy((int) (neededPower / 100), false)) {
						outer:
						for(Pair<ISubSystemPowerProvider, List<IFilter>> pair : provider) {
							for(IFilter filter:pair.getValue2()) {
								if(filter.blockPower()) continue outer;
							}
							if(pair.getValue1().usePaused()) continue;
							if(!pair.getValue1().getBrand().equals("RF")) continue;
							float requestamount = neededPower * (pair.getValue1().getPowerLevel() / available);
							pair.getValue1().requestPower(this.pipe.getRouterId(), requestamount);
						}
					}
				}
			}
		}
		if(SimpleServiceLocator.IC2Proxy.hasIC2() && this.pipe.getUpgradeManager().getIC2PowerLevel() > 0) {
			//Use Buffer
			WorldUtil worldUtil = new WorldUtil(this.pipe.getWorld(), this.pipe.getX(), this.pipe.getY(), this.pipe.getZ());
			LinkedList<AdjacentTile> adjacent = worldUtil.getAdjacentTileEntities(false);
			float globalNeed = 0;
			float[] need = new float[adjacent.size()];
			int i=0;
			for(AdjacentTile adTile:adjacent) {
				if(SimpleServiceLocator.IC2Proxy.isEnergySink(adTile.tile) && this.pipe.canPipeConnect(adTile.tile, adTile.orientation) && SimpleServiceLocator.IC2Proxy.acceptsEnergyFrom(adTile.tile, this.pipe.container, adTile.orientation.getOpposite())) {
					globalNeed += need[i] = (float) SimpleServiceLocator.IC2Proxy.demandedEnergyUnits(adTile.tile);
				}
				i++;
			}
			if(globalNeed != 0 && !Float.isNaN(globalNeed)) {
				float fullfillable = Math.min(1, internal_IC2_Buffer / globalNeed);
				i = 0;
				for(AdjacentTile adTile:adjacent) {
					if(SimpleServiceLocator.IC2Proxy.isEnergySink(adTile.tile) && this.pipe.canPipeConnect(adTile.tile, adTile.orientation) && SimpleServiceLocator.IC2Proxy.acceptsEnergyFrom(adTile.tile, this.pipe.container ,adTile.orientation.getOpposite())) {
						if(internal_IC2_Buffer + 1 < need[i] * fullfillable) return;
						double toUse = Math.min(this.pipe.getUpgradeManager().getIC2PowerLevel(), need[i] * fullfillable);
						double unUsed = SimpleServiceLocator.IC2Proxy.injectEnergyUnits(adTile.tile, adTile.orientation.getOpposite(), toUse);
						double used = toUse - unUsed;
						if(used > 0) {
							//MainProxy.sendPacketToAllWatchingChunk(this.pipe.getX(), this.pipe.getZ(), MainProxy.getDimensionForWorld(this.pipe.getWorld()), PacketHandler.getPacket(PowerPacketLaser.class).setColor(LogisticsPowerProviderTileEntity.IC2_COLOR).setPos(this.pipe.getLPPosition()).setRenderBall(true).setDir(adTile.orientation).setLength(0.5F));
							((LogisticsTileGenericPipe)pipe.container).addLaser(adTile.orientation, 0.5F, LogisticsPowerProviderTileEntity.IC2_COLOR, false, true);
							internal_IC2_Buffer -= used;
						}
						if(internal_IC2_Buffer < 0) {
							internal_IC2_Buffer = 0;
							return;
						}
					}
					i++;
				}
			}
			//Rerequest Buffer
			List<Pair<ISubSystemPowerProvider, List<IFilter>>> provider = this.pipe.getRouter().getSubSystemPowerProvider();
			float available = 0;
			outer:
			for(Pair<ISubSystemPowerProvider, List<IFilter>> pair : provider) {
				for(IFilter filter:pair.getValue2()) {
					if(filter.blockPower()) continue outer;
				}
				if(pair.getValue1().usePaused()) continue;
				if(!pair.getValue1().getBrand().equals("EU")) continue;
				available += pair.getValue1().getPowerLevel();
			}
			if(available > 0) {
				float neededPower = INTERNAL_IC2_BUFFER_MAX - internal_IC2_Buffer;
				if(neededPower > 0) {
					if(this.pipe.useEnergy((int) (neededPower / 10000), false)) {
						outer:
						for(Pair<ISubSystemPowerProvider, List<IFilter>> pair : provider) {
							for(IFilter filter:pair.getValue2()) {
								if(filter.blockPower()) continue outer;
							}
							if(pair.getValue1().usePaused()) continue;
							if(!pair.getValue1().getBrand().equals("EU")) continue;
							float requestamount = neededPower * (pair.getValue1().getPowerLevel() / available);
							pair.getValue1().requestPower(this.pipe.getRouterId(), requestamount);
						}
					}
				}
			}
		}
	}

	public void addBCPower(float toSend) {
		internal_BC_Buffer += toSend;
	}

	public void addRFPower(float toSend) {
		internal_RF_Buffer += toSend;
	}

	public void addIC2Power(float toSend) {
		internal_IC2_Buffer += toSend;
	}
}
