package logisticspipes.network.guis.module.inpipe;

import java.io.IOException;

import logisticspipes.gui.GuiSupplierPipe;
import logisticspipes.modules.ModuleActiveSupplier;
import logisticspipes.modules.ModuleActiveSupplier.PatternMode;
import logisticspipes.modules.ModuleActiveSupplier.SupplyMode;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.utils.gui.DummyContainer;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;

@Accessors(chain=true)
public class ActiveSupplierSlot extends ModuleCoordinatesGuiProvider {
	
	@Getter
	@Setter
	private boolean patternUpgarde;
	
	@Getter
	@Setter
	private int[] slotArray;
	
	@Getter
	@Setter
	private boolean isLimit;
	
	@Getter
	@Setter
	private int mode;
	
	public ActiveSupplierSlot(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBoolean(patternUpgarde);
		data.writeIntegerArray(slotArray);
		data.writeBoolean(isLimit);
		data.writeInt(mode);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		patternUpgarde = data.readBoolean();
		slotArray = data.readIntegerArray();
		isLimit = data.readBoolean();
		mode = data.readInt();
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		ModuleActiveSupplier module = this.getLogisticsModule(player.getEntityWorld(), ModuleActiveSupplier.class);
		if(module == null) return null;
		module.setLimited(isLimit);
		if(patternUpgarde) {
			module.setPatternMode(PatternMode.values()[mode]);
		} else {
			module.setSupplyMode(SupplyMode.values()[mode]);
		}
		return new GuiSupplierPipe(player.inventory, module.getDummyInventory(), module, patternUpgarde, slotArray);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		ModuleActiveSupplier module = this.getLogisticsModule(player.getEntityWorld(), ModuleActiveSupplier.class);
		if(module == null) return null;
		DummyContainer dummy = new DummyContainer(player.inventory, module.getDummyInventory());
		dummy.addNormalSlotsForPlayerInventory(18, 97);
		
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 3; column++) {
				dummy.addDummySlot(column + row * 3, 72 + column * 18, 18 + row * 18);
			}
		}
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new ActiveSupplierSlot(getId());
	}
}
