/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import buildcraft.api.transport.IPipeTile.PipeType;

import logisticspipes.asm.wrapper.LogisticsWrapperHandler;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.commands.LogisticsPipesCommand;
import logisticspipes.commands.chathelper.LPChatListener;
import logisticspipes.items.ItemDisk;
import logisticspipes.items.ItemHUDArmor;
import logisticspipes.items.ItemLogisticsPipe;
import logisticspipes.items.ItemModule;
import logisticspipes.items.ItemParts;
import logisticspipes.items.ItemPipeController;
import logisticspipes.items.ItemPipeSignCreator;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.items.LogisticsBrokenItem;
import logisticspipes.items.LogisticsFluidContainer;
import logisticspipes.items.LogisticsItem;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.items.LogisticsNetworkManager;
import logisticspipes.items.LogisticsSolidBlockItem;
import logisticspipes.items.RemoteOrderer;
import logisticspipes.log.RequestLogFormator;
import logisticspipes.logistics.LogisticsFluidManager;
import logisticspipes.logistics.LogisticsManager;
import logisticspipes.network.GuiHandler;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.PipeFluidBasic;
import logisticspipes.pipes.PipeFluidExtractor;
import logisticspipes.pipes.PipeFluidInsertion;
import logisticspipes.pipes.PipeFluidProvider;
import logisticspipes.pipes.PipeFluidRequestLogistics;
import logisticspipes.pipes.PipeFluidSatellite;
import logisticspipes.pipes.PipeFluidSupplierMk2;
import logisticspipes.pipes.PipeItemsApiaristAnalyser;
import logisticspipes.pipes.PipeItemsApiaristSink;
import logisticspipes.pipes.PipeItemsBasicLogistics;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsCraftingLogisticsMk2;
import logisticspipes.pipes.PipeItemsCraftingLogisticsMk3;
import logisticspipes.pipes.PipeItemsFirewall;
import logisticspipes.pipes.PipeItemsFluidSupplier;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.PipeItemsProviderLogisticsMk2;
import logisticspipes.pipes.PipeItemsRemoteOrdererLogistics;
import logisticspipes.pipes.PipeItemsRequestLogistics;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.pipes.PipeItemsSystemDestinationLogistics;
import logisticspipes.pipes.PipeItemsSystemEntranceLogistics;
import logisticspipes.pipes.PipeLogisticsChassiMk1;
import logisticspipes.pipes.PipeLogisticsChassiMk2;
import logisticspipes.pipes.PipeLogisticsChassiMk3;
import logisticspipes.pipes.PipeLogisticsChassiMk4;
import logisticspipes.pipes.PipeLogisticsChassiMk5;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.fluid.LogisticsFluidConnectorPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.ProxyManager;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.SpecialInventoryHandlerManager;
import logisticspipes.proxy.SpecialTankHandlerManager;
import logisticspipes.proxy.VersionNotSupportedException;
import logisticspipes.proxy.buildcraft.BuildCraftProxy;
import logisticspipes.proxy.forestry.ForestryProgressProvider;
import logisticspipes.proxy.ic2.IC2ProgressProvider;
import logisticspipes.proxy.progressprovider.MachineProgressProvider;
import logisticspipes.proxy.recipeproviders.AssemblyAdvancedWorkbench;
import logisticspipes.proxy.recipeproviders.AssemblyTable;
import logisticspipes.proxy.recipeproviders.AutoWorkbench;
import logisticspipes.proxy.recipeproviders.ImmibisCraftingTableMk2;
import logisticspipes.proxy.recipeproviders.LogisticsCraftingTable;
import logisticspipes.proxy.recipeproviders.RollingMachine;
import logisticspipes.proxy.recipeproviders.SolderingStation;
import logisticspipes.proxy.specialconnection.EnderIOHyperCubeConnection;
import logisticspipes.proxy.specialconnection.SpecialPipeConnection;
import logisticspipes.proxy.specialconnection.SpecialTileConnection;
import logisticspipes.proxy.specialconnection.TeleportPipes;
import logisticspipes.proxy.specialconnection.TesseractConnection;
import logisticspipes.proxy.specialtankhandler.SpecialTankHandler;
import logisticspipes.proxy.te.ThermalExpansionProgressProvider;
import logisticspipes.recipes.CraftingPermissionManager;
import logisticspipes.recipes.RecipeManager;
import logisticspipes.recipes.SolderingStationRecipes;
import logisticspipes.renderer.FluidContainerRenderer;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.renderer.LogisticsPipeBlockRenderer;
import logisticspipes.routing.RouterManager;
import logisticspipes.routing.ServerRouter;
import logisticspipes.routing.pathfinder.PipeInformaitonManager;
import logisticspipes.textures.Textures;
import logisticspipes.ticks.ClientPacketBufferHandlerThread;
import logisticspipes.ticks.DebugGuiTickHandler;
import logisticspipes.ticks.HudUpdateTick;
import logisticspipes.ticks.QueuedTasks;
import logisticspipes.ticks.RenderTickHandler;
import logisticspipes.ticks.RoutingTableUpdateThread;
import logisticspipes.ticks.ServerPacketBufferHandlerThread;
import logisticspipes.ticks.VersionChecker;
import logisticspipes.ticks.Watchdog;
import logisticspipes.ticks.WorldTickHandler;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.InventoryUtilFactory;
import logisticspipes.utils.RoutedItemHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.client.MinecraftForgeClient;
<<<<<<< HEAD
import net.minecraftforge.common.ForgeDirection;
=======
import net.minecraftforge.common.EnumHelper;
>>>>>>> mc16
import net.minecraftforge.common.MinecraftForge;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.TransportProxyClient;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLFingerprintViolationEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.FMLInjectionData;
import cpw.mods.fml.relauncher.Side;

@Mod(
		modid = "LogisticsPipes|Main",
		name = "Logistics Pipes",
		version = "${lp.version.full}",
		/* %------------CERTIFICATE-SUM-----------% */
		dependencies = "required-after:Forge@[9.10.1.850,);" +
				"required-after:BuildCraft|Core;" +
				"required-after:BuildCraft|Transport;" +
				"required-after:BuildCraft|Silicon;" +
				"after:IC2;" +
				"after:Forestry;" +
				"after:Thaumcraft;" +
				"after:CCTurtle;" +
				"after:ComputerCraft;" +
				"after:factorization;" +
				"after:GregTech_Addon;" +
				"after:AppliedEnergistics;" +
				"after:ThermalExpansion;" +
				"after:BetterStorage")
@NetworkMod(
		channels = {LogisticsPipes.LOGISTICS_PIPES_CHANNEL_NAME},
		packetHandler = PacketHandler.class,
		clientSideRequired = true)
public class LogisticsPipes {

	public LogisticsPipes() {
		LaunchClassLoader loader = (LaunchClassLoader)LogisticsPipes.class.getClassLoader();
		boolean found = false;
		for(IClassTransformer transformer:loader.getTransformers()) {
			if(transformer.getClass().getName().equals("logisticspipes.asm.LogisticsClassTransformer")) {
				found = true;
				break;
			}
		}
		if(!found) {
			throw new RuntimeException("LogisticsPipes could not find its class transformer. If you are running MC from an IDE make sure to copy the 'LogisticsPipes_dummy.jar' to your mods folder. If you are running MC normal please report this as a bug at 'https://github.com/RS485/LogisticsPipes/issues'.");
		}
		PacketHandler.intialize();
		NewGuiHandler.intialize();
	}
	
	@Instance("LogisticsPipes|Main")
	public static LogisticsPipes instance;
	
	//Network CHannel
	public static final String LOGISTICS_PIPES_CHANNEL_NAME = "BCLP"; // BCLP: Buildcraft-Logisticspipes

	//Log Requests
	public static boolean DisplayRequests;

	public static final boolean DEBUG = "%DEBUG%".equals("%" + "DEBUG" + "%") || "%DEBUG%".equals("true");
	public static final String MCVersion = "%MCVERSION%";
	public static final String VERSION = "%VERSION%:%DEBUG%";
	public static final boolean DEV_BUILD = VERSION.contains(".dev.") || DEBUG;
	public static boolean WATCHDOG = false;
	
	private boolean certificateError = false;

	// Logistics Pipes
	public static Item LogisticsBasicPipe;
	public static Item LogisticsRequestPipeMk1;
	public static Item LogisticsRequestPipeMk2;
	public static Item LogisticsProviderPipeMk1;
	public static Item LogisticsProviderPipeMk2;
	public static Item LogisticsCraftingPipeMk1;
	public static Item LogisticsCraftingPipeMk2;
	public static Item LogisticsCraftingPipeMk3;
	public static Item LogisticsSatellitePipe;
	public static Item LogisticsSupplierPipe;
	public static Item LogisticsChassisPipeMk1;
	public static Item LogisticsChassisPipeMk2;
	public static Item LogisticsChassisPipeMk3;
	public static Item LogisticsChassisPipeMk4;
	public static Item LogisticsChassisPipeMk5;
	public static Item LogisticsRemoteOrdererPipe;
	public static Item LogisticsInvSysConPipe;
	public static Item LogisticsEntrancePipe;
	public static Item LogisticsDestinationPipe;
	public static Item LogisticsFirewallPipe;
	public static Item logisticsRequestTable;
	
	// Logistics Apiarist's Pipes
	public static Item LogisticsApiaristAnalyzerPipe;
	public static Item LogisticsApiaristSinkPipe;
	
	// Logistics Fluid Pipes
	public static Item LogisticsFluidBasicPipe;
	public static Item LogisticsFluidRequestPipe;
	public static Item LogisticsFluidProviderPipe;
	public static Item LogisticsFluidSatellitePipe;
	public static Item LogisticsFluidSupplierPipeMk1;
	public static Item LogisticsFluidSupplierPipeMk2;
	public static Item LogisticsFluidConnectorPipe;
	public static Item LogisticsFluidInsertionPipe;
	public static Item LogisticsFluidExtractorPipe;

	// Logistics Modules/Upgrades
	public static ItemModule ModuleItem;
	public static ItemUpgrade UpgradeItem;
	
	// Miscellaneous Items
	public static Item LogisticsNetworkMonitior;
	public static Item LogisticsRemoteOrderer;
	public static Item LogisticsCraftingSignCreator;
	public static ItemDisk LogisticsItemDisk;
	public static Item LogisticsItemCard;
	public static ItemHUDArmor LogisticsHUDArmor;
	public static Item LogisticsParts;
	public static Item LogisticsUpgradeManager;
	public static Item LogisticsFluidContainer;
	public static Item LogisticsBrokenItem;
	public static Item LogisticsPipeControllerItem;
	
	public static List<Item> pipelist = new ArrayList<Item>();
	
	// Logistics Blocks
	public static Block LogisticsSolidBlock;
	public static Block LogisticsPipeBlock;

	public static Textures textures = new Textures();
	
	public static final String logisticsTileGenericPipeMapping = "logisticspipes.pipes.basic.LogisticsTileGenericPipe";
	
	public static CreativeTabLP LPCreativeTab = new CreativeTabLP();
	public static PipeType LogisticsPipeType;
	
	public static Logger log;
	public static Logger requestLog;
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		String BCVersion = null;
		try {
			Field versionField = buildcraft.core.Version.class.getDeclaredField("VERSION");
			BCVersion = (String) versionField.get(null);
		} catch(Exception e) {
			e.printStackTrace();
		}
		String expectedBCVersion = "4.2.2";
		if(BCVersion != null) {
			if(!BCVersion.equals("@VERSION@") && !BCVersion.contains(expectedBCVersion)) {
				throw new VersionNotSupportedException("BC", BCVersion, expectedBCVersion, "");
			}
		} else {
			log.info("Couldn't check the BC Version.");
		}
		
		RouterManager manager = new RouterManager();
		SimpleServiceLocator.setRouterManager(manager);
		SimpleServiceLocator.setDirectConnectionManager(manager);
		SimpleServiceLocator.setSecurityStationManager(manager);
		SimpleServiceLocator.setLogisticsManager(new LogisticsManager());
		SimpleServiceLocator.setInventoryUtilFactory(new InventoryUtilFactory());
		SimpleServiceLocator.setSpecialConnectionHandler(new SpecialPipeConnection());
		SimpleServiceLocator.setSpecialConnectionHandler(new SpecialTileConnection());
		SimpleServiceLocator.setLogisticsFluidManager(new LogisticsFluidManager());
		SimpleServiceLocator.setSpecialTankHandler(new SpecialTankHandler());
		SimpleServiceLocator.setCraftingPermissionManager(new CraftingPermissionManager());
		SimpleServiceLocator.setMachineProgressProvider(new MachineProgressProvider());
		SimpleServiceLocator.setRoutedItemHelper(new RoutedItemHelper());
		
		if(event.getSide().isClient()) {
			//SimpleServiceLocator.buildCraftProxy.registerLocalization();
		}
		NetworkRegistry.instance().registerGuiHandler(LogisticsPipes.instance, new GuiHandler());
		if(event.getSide().equals(Side.CLIENT)) {
			TickRegistry.registerTickHandler(new RenderTickHandler(), Side.CLIENT);
		}
		TickRegistry.registerTickHandler(new WorldTickHandler(), Side.SERVER);
		TickRegistry.registerTickHandler(new WorldTickHandler(), Side.CLIENT);
		TickRegistry.registerTickHandler(new QueuedTasks(), Side.SERVER);
		if(event.getSide() == Side.CLIENT) {
			SimpleServiceLocator.setClientPacketBufferHandlerThread(new ClientPacketBufferHandlerThread());
		}
		SimpleServiceLocator.setServerPacketBufferHandlerThread(new ServerPacketBufferHandlerThread());	
		for(int i=0; i<Configs.MULTI_THREAD_NUMBER; i++) {
			new RoutingTableUpdateThread(i);
		}
		LogisticsEventListener eventListener = new LogisticsEventListener();
		MinecraftForge.EVENT_BUS.register(eventListener);
		GameRegistry.registerPlayerTracker(eventListener);
		NetworkRegistry.instance().registerConnectionHandler(eventListener);
		NetworkRegistry.instance().registerChatListener(new LPChatListener());
		textures.registerBlockIcons(null);
		
		SimpleServiceLocator.buildCraftProxy.initProxyAndCheckVersion();

		if(event.getSide().equals(Side.CLIENT)) {
			TickRegistry.registerTickHandler(DebugGuiTickHandler.instance(), Side.CLIENT);
		}
		TickRegistry.registerTickHandler(DebugGuiTickHandler.instance(), Side.SERVER);
		
		LogisticsPipeType = EnumHelper.addEnum(PipeType.class, "LOGISTICS", new Class<?>[]{}, new Object[]{});
//		FMLInterModComms.sendMessage("Waila", "register", this.getClass()
//		 .getPackage().getName()
//		 + ".waila.WailaRegister.register");
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		Configs.load();
		log = evt.getModLog();
		requestLog = Logger.getLogger("LogisticsPipes|Request");
		requestLog.setUseParentHandlers(false);
		try {
			File logPath = new File((File) FMLInjectionData.data()[6], "LogisticsPipes-Request.log");
			FileHandler fileHandler = new FileHandler(logPath.getPath(), true);
			fileHandler.setFormatter(new RequestLogFormator());
			fileHandler.setLevel(Level.ALL);
			requestLog.addHandler(fileHandler);
		} catch (Exception e) {}
		if(DEBUG) {
			log.setLevel(Level.ALL);
		}
		if(certificateError) {
			log.severe("Certificate not correct");
			log.severe("This in not a LogisticsPipes version from RS485.");
		}
		if (DEV_BUILD) {
			log.fine("You are using a dev version.");
			log.fine("While the dev versions contain cutting edge features, they may also contain more bugs.");
			log.fine("Please report any you find to https://github.com/RS485/LogisticsPipes-Dev/issues");
		}
		SimpleServiceLocator.setPipeInformationManager(new PipeInformaitonManager());
		SimpleServiceLocator.setBuildCraftProxy(new BuildCraftProxy());
		SimpleServiceLocator.buildCraftProxy.registerPipeInformationProvider();

		if (Configs.EASTER_EGGS) {
			Calendar calendar = Calendar.getInstance();
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int month = calendar.get(Calendar.MONTH);
			if (month == Calendar.OCTOBER && day == 1) { //GUIpsp's birthday.
				Item.slimeBall.setTextureName("logisticspipes:eastereggs/guipsp");
			}
		}
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		
		boolean isClient = event.getSide() == Side.CLIENT;
		
		ProxyManager.load();
		SpecialInventoryHandlerManager.load();
		SpecialTankHandlerManager.load();

		SimpleServiceLocator.specialpipeconnection.registerHandler(new TeleportPipes());
		SimpleServiceLocator.specialtileconnection.registerHandler(new TesseractConnection());
		SimpleServiceLocator.specialtileconnection.registerHandler(new EnderIOHyperCubeConnection());
		
		Object renderer = null;
		if(isClient) {
			renderer = new FluidContainerRenderer();
		}
		
		LogisticsNetworkMonitior = new LogisticsNetworkManager(Configs.LOGISTICSNETWORKMONITOR_ID);
		LogisticsNetworkMonitior.setUnlocalizedName("networkMonitorItem");
		
		LogisticsItemCard = new LogisticsItemCard(Configs.ITEM_CARD_ID);
		LogisticsItemCard.setUnlocalizedName("logisticsItemCard");
		if(isClient) {
			MinecraftForgeClient.registerItemRenderer(LogisticsItemCard.itemID, (FluidContainerRenderer)renderer);
		}
		
		LogisticsRemoteOrderer = new RemoteOrderer(Configs.LOGISTICSREMOTEORDERER_ID);
		LogisticsRemoteOrderer.setUnlocalizedName("remoteOrdererItem");

		ItemPipeSignCreator.registerPipeSignTypes();
		LogisticsCraftingSignCreator = new ItemPipeSignCreator(Configs.LOGISTICSCRAFTINGSIGNCREATOR_ID);
		LogisticsCraftingSignCreator.setUnlocalizedName("ItemPipeSignCreator");
		
		int renderIndex;
		if(isClient) {
			renderIndex = RenderingRegistry.addNewArmourRendererPrefix("LogisticsHUD");
		} else {
			renderIndex = 0;
		}
		LogisticsHUDArmor = new ItemHUDArmor(Configs.ITEM_HUD_ID, renderIndex);
		LogisticsHUDArmor.setUnlocalizedName("logisticsHUDGlasses");
		
		LogisticsParts = new ItemParts(Configs.ITEM_PARTS_ID);
		LogisticsParts.setUnlocalizedName("logisticsParts");
		
		SimpleServiceLocator.buildCraftProxy.registerTrigger();
		
		ModuleItem = new ItemModule(Configs.ITEM_MODULE_ID);
		ModuleItem.setUnlocalizedName("itemModule");
		ModuleItem.loadModules();
		
		LogisticsItemDisk = new ItemDisk(Configs.ITEM_DISK_ID);
		LogisticsItemDisk.setUnlocalizedName("itemDisk");

		UpgradeItem = new ItemUpgrade(Configs.ITEM_UPGRADE_ID);
		UpgradeItem.setUnlocalizedName("itemUpgrade");
		UpgradeItem.loadUpgrades();
		
		//TODO make it visible in creative search
		LogisticsUpgradeManager = new LogisticsItem(Configs.ITEM_UPGRADE_MANAGER_ID);
		LogisticsUpgradeManager.setUnlocalizedName("upgradeManagerItem");
		
		LogisticsFluidContainer = new LogisticsFluidContainer(Configs.ITEM_LIQUID_CONTAINER_ID);
		LogisticsFluidContainer.setUnlocalizedName("logisticsFluidContainer");
		if(isClient) {
			MinecraftForgeClient.registerItemRenderer(LogisticsFluidContainer.itemID, (FluidContainerRenderer)renderer);
		}
		
		LogisticsBrokenItem = new LogisticsBrokenItem(Configs.ITEM_BROKEN_ID);
		LogisticsBrokenItem.setUnlocalizedName("brokenItem");

		LogisticsPipeControllerItem = new ItemPipeController(Configs.ITEM_PIPE_CONTROLLER_ID);
		LogisticsPipeControllerItem.setUnlocalizedName("pipeController");

		//Blocks
		LogisticsSolidBlock = new LogisticsSolidBlock(Configs.LOGISTICS_SOLID_BLOCK_ID);
		GameRegistry.registerBlock(LogisticsSolidBlock, LogisticsSolidBlockItem.class, null);
		LogisticsSolidBlock.setUnlocalizedName("logisticsSolidBlock");

		LogisticsPipeBlock = new LogisticsBlockGenericPipe(Configs.LOGISTICS_PIPE_BLOCK_ID);
		GameRegistry.registerBlock(LogisticsPipeBlock, ItemBlock.class, null);
		LogisticsPipeBlock.setUnlocalizedName("logisticsPipeBlock");

		registerPipes(event.getSide());
		
		SimpleServiceLocator.IC2Proxy.addCraftingRecipes();
		SimpleServiceLocator.forestryProxy.addCraftingRecipes();
		SimpleServiceLocator.thaumCraftProxy.addCraftingRecipes();
		SimpleServiceLocator.ccProxy.addCraftingRecipes();
		SimpleServiceLocator.thermalExpansionProxy.addCraftingRecipes();

		SimpleServiceLocator.addCraftingRecipeProvider(LogisticsWrapperHandler.getWrappedRecipeProvider("BuildCraft|Factory", "AutoWorkbench", AutoWorkbench.class));
		SimpleServiceLocator.addCraftingRecipeProvider(LogisticsWrapperHandler.getWrappedRecipeProvider("BuildCraft|Silicon", "AssemblyAdvancedWorkbench", AssemblyAdvancedWorkbench.class));
		SimpleServiceLocator.addCraftingRecipeProvider(LogisticsWrapperHandler.getWrappedRecipeProvider("BuildCraft|Silicon", "AssemblyTable", AssemblyTable.class));
		SimpleServiceLocator.addCraftingRecipeProvider(LogisticsWrapperHandler.getWrappedRecipeProvider("Railcraft", "RollingMachine", RollingMachine.class));
		SimpleServiceLocator.addCraftingRecipeProvider(LogisticsWrapperHandler.getWrappedRecipeProvider("Tubestuff", "ImmibisCraftingTableMk2", ImmibisCraftingTableMk2.class));
		SimpleServiceLocator.addCraftingRecipeProvider(new SolderingStation());
		SimpleServiceLocator.addCraftingRecipeProvider(new LogisticsCraftingTable());
		
		SimpleServiceLocator.machineProgressProvider.registerProgressProvider(LogisticsWrapperHandler.getWrappedProgressProvider("Forestry", "Generic", ForestryProgressProvider.class));
		SimpleServiceLocator.machineProgressProvider.registerProgressProvider(LogisticsWrapperHandler.getWrappedProgressProvider("ThermalExpansion", "Generic", ThermalExpansionProgressProvider.class));
		SimpleServiceLocator.machineProgressProvider.registerProgressProvider(LogisticsWrapperHandler.getWrappedProgressProvider("IC2", "Generic", IC2ProgressProvider.class));
		
		SolderingStationRecipes.loadRecipe();
		
		MainProxy.proxy.registerTileEntities();

		RecipeManager.loadRecipes();
		
		//Registering special particles
		MainProxy.proxy.registerParticles();
		
		//init Modular Powersuits modules
		SimpleServiceLocator.mpsProxy.initModules();
		
		//init Fluids
		FluidIdentifier.initFromForge(false);

		if (!FMLCommonHandler.instance().getModName().contains("MCPC") && ((Configs.WATCHDOG_CLIENT && isClient) || Configs.WATCHDOG_SERVER)) {
			new Watchdog(isClient);
			WATCHDOG = true;
		}
		new VersionChecker();
	}
	
	@EventHandler
	public void cleanup(FMLServerStoppingEvent event) {
		SimpleServiceLocator.routerManager.serverStopClean();
		QueuedTasks.clearAllTasks();
		HudUpdateTick.clearUpdateFlags();
		PipeItemsSatelliteLogistics.cleanup();
		PipeFluidSatellite.cleanup();
		ServerRouter.cleanup();
		if(event.getSide().equals(Side.CLIENT)) {
			LogisticsHUDRenderer.instance().clear();
		}
	}
	
	@EventHandler
	public void registerCommands(FMLServerStartingEvent event) {
		event.registerServerCommand(new LogisticsPipesCommand());
	}
	
	@EventHandler
	public void certificateWarning(FMLFingerprintViolationEvent warning) {
		if(!DEBUG) {
			System.out.println("[LogisticsPipes|Certificate] Certificate not correct");
			System.out.println("[LogisticsPipes|Certificate] Expected: " + warning.expectedFingerprint);
			System.out.println("[LogisticsPipes|Certificate] File: " + warning.source.getAbsolutePath());
			System.out.println("[LogisticsPipes|Certificate] This in not a LogisticsPipes version from RS485.");
			certificateError = true;
		}
	}
	
	public void registerPipes(Side side) {
		LogisticsPipes.LogisticsBasicPipe = createPipe(Configs.LOGISTICSPIPE_BASIC_ID, PipeItemsBasicLogistics.class, "Basic Logistics Pipe", side);
		LogisticsPipes.LogisticsRequestPipeMk1 = createPipe(Configs.LOGISTICSPIPE_REQUEST_ID, PipeItemsRequestLogistics.class, "Request Logistics Pipe", side);
		LogisticsPipes.LogisticsProviderPipeMk1 = createPipe(Configs.LOGISTICSPIPE_PROVIDER_ID, PipeItemsProviderLogistics.class, "Provider Logistics Pipe", side);
		LogisticsPipes.LogisticsCraftingPipeMk1 = createPipe(Configs.LOGISTICSPIPE_CRAFTING_ID, PipeItemsCraftingLogistics.class, "Crafting Logistics Pipe", side);
		LogisticsPipes.LogisticsSatellitePipe = createPipe(Configs.LOGISTICSPIPE_SATELLITE_ID, PipeItemsSatelliteLogistics.class, "Satellite Logistics Pipe", side);
		LogisticsPipes.LogisticsSupplierPipe = createPipe(Configs.LOGISTICSPIPE_SUPPLIER_ID, PipeItemsSupplierLogistics.class, "Supplier Logistics Pipe", side);
		LogisticsPipes.LogisticsChassisPipeMk1 = createPipe(Configs.LOGISTICSPIPE_CHASSI1_ID, PipeLogisticsChassiMk1.class, "Logistics Chassi Mk1", side);
		LogisticsPipes.LogisticsChassisPipeMk2 = createPipe(Configs.LOGISTICSPIPE_CHASSI2_ID, PipeLogisticsChassiMk2.class, "Logistics Chassi Mk2", side);
		LogisticsPipes.LogisticsChassisPipeMk3 = createPipe(Configs.LOGISTICSPIPE_CHASSI3_ID, PipeLogisticsChassiMk3.class, "Logistics Chassi Mk3", side);
		LogisticsPipes.LogisticsChassisPipeMk4 = createPipe(Configs.LOGISTICSPIPE_CHASSI4_ID, PipeLogisticsChassiMk4.class, "Logistics Chassi Mk4", side);
		LogisticsPipes.LogisticsChassisPipeMk5 = createPipe(Configs.LOGISTICSPIPE_CHASSI5_ID, PipeLogisticsChassiMk5.class, "Logistics Chassi Mk5", side);
		LogisticsPipes.LogisticsCraftingPipeMk2 = createPipe(Configs.LOGISTICSPIPE_CRAFTING_MK2_ID, PipeItemsCraftingLogisticsMk2.class, "Crafting Logistics Pipe MK2", side);
		LogisticsPipes.LogisticsRequestPipeMk2 = createPipe(Configs.LOGISTICSPIPE_REQUEST_MK2_ID, PipeItemsRequestLogisticsMk2.class, "Request Logistics Pipe MK2", side);
		LogisticsPipes.LogisticsRemoteOrdererPipe = createPipe(Configs.LOGISTICSPIPE_REMOTE_ORDERER_ID, PipeItemsRemoteOrdererLogistics.class, "Remote Orderer Pipe", side);
		LogisticsPipes.LogisticsProviderPipeMk2 = createPipe(Configs.LOGISTICSPIPE_PROVIDER_MK2_ID, PipeItemsProviderLogisticsMk2.class, "Provider Logistics Pipe MK2", side);
		LogisticsPipes.LogisticsApiaristAnalyzerPipe = createPipe(Configs.LOGISTICSPIPE_APIARIST_ANALYSER_ID, PipeItemsApiaristAnalyser.class, "Apiarist Logistics Analyser Pipe", side);
		LogisticsPipes.LogisticsApiaristSinkPipe = createPipe(Configs.LOGISTICSPIPE_APIARIST_SINK_ID, PipeItemsApiaristSink.class, "Apiarist Logistics Analyser Pipe", side);
		LogisticsPipes.LogisticsInvSysConPipe = createPipe(Configs.LOGISTICSPIPE_INVSYSCON_ID, PipeItemsInvSysConnector.class, "Logistics Inventory System Connector", side);
		LogisticsPipes.LogisticsEntrancePipe = createPipe(Configs.LOGISTICSPIPE_ENTRANCE_ID, PipeItemsSystemEntranceLogistics.class, "Logistics System Entrance Pipe", side);
		LogisticsPipes.LogisticsDestinationPipe = createPipe(Configs.LOGISTICSPIPE_DESTINATION_ID, PipeItemsSystemDestinationLogistics.class, "Logistics System Destination Pipe", side);
		LogisticsPipes.LogisticsCraftingPipeMk3 = createPipe(Configs.LOGISTICSPIPE_CRAFTING_MK3_ID, PipeItemsCraftingLogisticsMk3.class, "Crafting Logistics Pipe MK3", side);
		LogisticsPipes.LogisticsFirewallPipe = createPipe(Configs.LOGISTICSPIPE_FIREWALL_ID, PipeItemsFirewall.class, "Firewall Logistics Pipe", side);
		
		LogisticsPipes.LogisticsFluidSupplierPipeMk1 = createPipe(Configs.LOGISTICSPIPE_LIQUIDSUPPLIER_ID, PipeItemsFluidSupplier.class, "Fluid Supplier Logistics Pipe", side);
		
		LogisticsPipes.LogisticsFluidBasicPipe = createPipe(Configs.LOGISTICSPIPE_LIQUID_BASIC, PipeFluidBasic.class, "Basic Logistics Fluid Pipe", side);
		LogisticsPipes.LogisticsFluidInsertionPipe = createPipe(Configs.LOGISTICSPIPE_LIQUID_INSERTION, PipeFluidInsertion.class, "Logistics Fluid Insertion Pipe", side);
		LogisticsPipes.LogisticsFluidProviderPipe = createPipe(Configs.LOGISTICSPIPE_LIQUID_PROVIDER, PipeFluidProvider.class, "Logistics Fluid Provider Pipe", side);
		LogisticsPipes.LogisticsFluidRequestPipe = createPipe(Configs.LOGISTICSPIPE_LIQUID_REQUEST, PipeFluidRequestLogistics.class, "Logistics Fluid Request Pipe", side);
		LogisticsPipes.LogisticsFluidExtractorPipe = createPipe(Configs.LOGISTICSPIPE_LIQUID_EXTRACTOR, PipeFluidExtractor.class, "Logistics Fluid Extractor Pipe", side);
		LogisticsPipes.LogisticsFluidSatellitePipe = createPipe(Configs.LOGISTICSPIPE_LIQUID_SATELLITE, PipeFluidSatellite.class, "Logistics Fluid Satellite Pipe", side);
		LogisticsPipes.LogisticsFluidSupplierPipeMk2 = createPipe(Configs.LOGISTICSPIPE_LIQUID_SUPPLIER_MK2, PipeFluidSupplierMk2.class, "Logistics Fluid Supplier Pipe Mk2", side);
		
		LogisticsPipes.logisticsRequestTable = createPipe(Configs.LOGISTICSPIPE_REQUEST_TABLE_ID, PipeBlockRequestTable.class, "Request Table", side);
		
		// Registered as BC pipe
		//TODO remove in future version
		LogisticsPipes.LogisticsFluidConnectorPipe = createBCPipe(Configs.LOGISTICSPIPE_LIQUID_CONNECTOR, LogisticsFluidConnectorPipe.class, "Logistics Fluid Connector Pipe", side);
	}
	
	private static ItemLogisticsPipe registerPipe(int key, Class<? extends CoreRoutedPipe> clas) {
		ItemLogisticsPipe item = LogisticsBlockGenericPipe.registerPipe(key, clas);
		SimpleServiceLocator.buildCraftProxy.registerPipe(item.itemID, clas);
		return item;
	}
	
	private Item createPipe(int defaultID, Class<? extends CoreRoutedPipe> clas, String descr, Side side) {
		ItemLogisticsPipe res = registerPipe(defaultID, clas);
		res.setCreativeTab(LogisticsPipes.LPCreativeTab);
		res.setUnlocalizedName(clas.getSimpleName());
		CoreRoutedPipe pipe = LogisticsBlockGenericPipe.createPipe(res.itemID);
		if(pipe instanceof CoreRoutedPipe) {
			res.setPipeIconIndex(((CoreRoutedPipe)pipe).getTextureType(ForgeDirection.UNKNOWN).normal);
		}
		
		if(side.isClient()) {
			if(pipe instanceof PipeBlockRequestTable) {
				MinecraftForgeClient.registerItemRenderer(res.itemID, new LogisticsPipeBlockRenderer());
			} else {
				MinecraftForgeClient.registerItemRenderer(res.itemID, TransportProxyClient.pipeItemRenderer);
			}
		}
		if(defaultID != Configs.LOGISTICSPIPE_BASIC_ID && defaultID != Configs.LOGISTICSPIPE_LIQUID_CONNECTOR) {
			registerShapelessResetRecipe(res, 0, LogisticsPipes.LogisticsBasicPipe, 0);
		}
		pipelist.add(res);
		return res;
	}
	
	private Item createBCPipe(int defaultID, Class<? extends Pipe<?>> clas, String descr, Side side) {
		ItemPipe res = BlockGenericPipe.registerPipe(defaultID, clas);
		res.setCreativeTab(LogisticsPipes.LPCreativeTab);
		res.setUnlocalizedName(clas.getSimpleName());
		CoreRoutedPipe pipe = LogisticsBlockGenericPipe.createPipe(res.itemID);
		if(pipe instanceof CoreRoutedPipe) {
			res.setPipeIconIndex(((CoreRoutedPipe)pipe).getTextureType(ForgeDirection.UNKNOWN).normal);
		}
		
		if(side.isClient()) {
			if(pipe instanceof PipeBlockRequestTable) {
				MinecraftForgeClient.registerItemRenderer(res.itemID, new LogisticsPipeBlockRenderer());
			} else {
				MinecraftForgeClient.registerItemRenderer(res.itemID, TransportProxyClient.pipeItemRenderer);
			}
		}
		pipelist.add(res);
		return res;
	}
	
	protected void registerShapelessResetRecipe(Item fromItem, int fromData, Item toItem, int toData) {
		for(int j = 1; j < 10; j++) {
			Object[] obj = new Object[j];
			for(int k = 0; k < j; k++) {
				obj[k] = new ItemStack(fromItem, 1, toData);
			}
			CraftingManager.getInstance().addShapelessRecipe(new ItemStack(toItem, j, fromData), obj);
		}
	}
}
