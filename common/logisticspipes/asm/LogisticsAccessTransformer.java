package logisticspipes.asm;

import java.io.IOException;

import cpw.mods.fml.common.asm.transformers.AccessTransformer;
import cpw.mods.fml.relauncher.FMLInjectionData;

public class LogisticsAccessTransformer extends AccessTransformer {

	public LogisticsAccessTransformer() throws IOException {
		//CHECKSTYLE:OFF
		//Difference between formater and checkstyle
		super("lp_at_" + FMLInjectionData.data()[4]/*MCVersion*/+ ".cfg");
		//CHECKSTYLE:ON
	}
}
