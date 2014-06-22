package logisticspipes.pipes.basic;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class LogisticsBlockLogisticsPipe extends LogisticsBlockGenericPipe {

	public LogisticsBlockLogisticsPipe(int i) {
		super(i);
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new LogisticsTileGenericPipe();
	}
}
