package logisticspipes.proxy.buildcraft.bc60.gates;

import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.proxy.buildcraft.bc60.gates.wrapperclasses.PipeWrapper;
import logisticspipes.textures.provider.LPActionTriggerIconProvider;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.transport.IPipeTrigger;
import buildcraft.transport.Pipe;

public class TriggerCrafting extends LPTrigger implements IPipeTrigger {

	public TriggerCrafting() {
		super("LogisticsPipes:trigger.isCrafting");
	}

	@Override
	public boolean isTriggerActive(Pipe pipe, ITriggerParameter parameter) {
		if(pipe instanceof PipeWrapper) {
			if (!(((PipeWrapper)pipe).tile.pipe instanceof PipeItemsCraftingLogistics)) return false;
			return ((PipeItemsCraftingLogistics)((PipeWrapper)pipe).tile.pipe).getLogisticsModule().waitingForCraft;
		}
		return false;
	}

	@Override
	public int getIconIndex() {
		return LPActionTriggerIconProvider.triggerCraftingIconIndex;
	}

	@Override
	public String getDescription() {
		return "Pipe Waiting for Crafting";
	}

	@Override
	public boolean requiresParameter() {
		return false;
	}

}
