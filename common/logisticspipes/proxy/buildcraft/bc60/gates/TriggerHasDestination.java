package logisticspipes.proxy.buildcraft.bc60.gates;

import java.util.ArrayList;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.bc60.gates.wrapperclasses.PipeWrapper;
import logisticspipes.textures.provider.LPActionTriggerIconProvider;
import logisticspipes.utils.item.ItemIdentifier;
import net.minecraft.item.ItemStack;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.transport.IPipeTrigger;
import buildcraft.transport.Pipe;

public class TriggerHasDestination extends LPTrigger implements IPipeTrigger {

	public TriggerHasDestination() {
		super("LogisticsPipes:trigger.hasDestination");
	}

	@Override
	public int getIconIndex() {
		return LPActionTriggerIconProvider.triggerHasDestinationIconIndex;
	}
	
	@Override
	public boolean hasParameter() {
		return true;
	}
	
	@Override
	public String getDescription() {
		return "Item has destination";
	}

	@Override
	public boolean isTriggerActive(Pipe pipe, ITriggerParameter parameter) {
		if(pipe instanceof PipeWrapper) {
			if (((PipeWrapper)pipe).tile.pipe instanceof CoreRoutedPipe) {
				if (parameter != null && parameter.getItemStack() != null) {
					ItemStack item = parameter.getItemStack();
					if (SimpleServiceLocator.logisticsManager.hasDestination(ItemIdentifier.get(item), false, ((CoreRoutedPipe) ((PipeWrapper)pipe).tile.pipe).getRouter().getSimpleID(), new ArrayList<Integer>()) != null) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean requiresParameter() {
		return false;
	}
}
