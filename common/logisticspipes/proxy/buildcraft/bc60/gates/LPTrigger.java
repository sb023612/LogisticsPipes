package logisticspipes.proxy.buildcraft.bc60.gates;


import logisticspipes.textures.Textures;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.TriggerParameter;

public abstract class LPTrigger implements ITrigger {
	protected final String uniqueTag;

	public LPTrigger(String uniqueTag) {
		this.uniqueTag = uniqueTag;
		ActionManager.registerTrigger(this);
	}

	@Override
	public String getUniqueTag() {
		return uniqueTag;
	}
	
    public abstract int getIconIndex();

	@Override
	public boolean hasParameter() {
		return false;
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public final ITriggerParameter createParameter() {
		return new TriggerParameter();
	}

	@Override
	public IIcon getIcon() {
		return Textures.LPactionIconProvider.getIcon(getIconIndex());
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		Textures.LPactionIconProvider.registerIcons(iconRegister);	
	}

	@Override
	public buildcraft.api.gates.ITrigger rotateLeft() {
		return this;
	}
}
