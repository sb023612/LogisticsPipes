package logisticspipes.proxy.specialinventoryhandler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ISpecialInventoryHandler;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class QuantumChestHandler implements IInventoryUtil, ISpecialInventoryHandler {

	private static Class<?> GT_TileEntity_Quantumchest;
	private static Field mItemCount;
	private static Method getStoredItemData;

	private final TileEntity _tile;

	private QuantumChestHandler(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		_tile = tile;
	}

	public QuantumChestHandler() {
		_tile = null;
	}
	@Override
	public boolean init() {
		try {
			GT_TileEntity_Quantumchest = Class.forName("gregtechmod.common.tileentities.GT_TileEntity_Quantumchest");
			mItemCount = GT_TileEntity_Quantumchest.getDeclaredField("mItemCount");
			mItemCount.setAccessible(true);
			getStoredItemData = GT_TileEntity_Quantumchest.getDeclaredMethod("getStoredItemData", new Class[]{});
			return true;
		} catch(Exception e) {
			return false;
		}
	}

	@Override
	public boolean isType(TileEntity tile) {
		return GT_TileEntity_Quantumchest.isAssignableFrom(tile.getClass());
	}

	@Override
	public IInventoryUtil getUtilForTile(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		return new QuantumChestHandler(tile, hideOnePerStack, hideOne, cropStart, cropEnd);
	}


	@Override
	public int itemCount(ItemIdentifier itemIdent) {
		HashMap<ItemIdentifier, Integer> map = getItemsAndCount();
		if(map.containsKey(itemIdent)) {
			return map.get(itemIdent);
		}
		return 0;
	}

	@Override
	public HashMap<ItemIdentifier, Integer> getItemsAndCount() {
		HashMap<ItemIdentifier, Integer> map = new HashMap<ItemIdentifier, Integer>();
		ItemStack[] data = new ItemStack[]{};
		try {
			data = (ItemStack[]) getStoredItemData.invoke(_tile, new Object[]{});
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		ItemStack stack = ((IInventory)_tile).getStackInSlot(1);
		if(data.length < 1 || data[0] == null || data[0].itemID < 1) return map;
		if(stack == null || stack.itemID < 1) return map;
		ItemIdentifier dataIdent = ItemIdentifier.get(data[0]);
		ItemIdentifier stackIdent = ItemIdentifier.get(stack);
		if(dataIdent != stackIdent) {
			if(data[0].stackSize != 0) map.put(dataIdent, data[0].stackSize);
			if(stack.stackSize != 0) map.put(stackIdent, stack.stackSize);
		} else {
			map.put(dataIdent, data[0].stackSize + stack.stackSize - 1);
		}
		return map;
	}

	@Override
	public ItemStack getSingleItem(ItemIdentifier itemIdent) {
		ItemStack[] data = new ItemStack[]{};
		try {
			data = (ItemStack[]) getStoredItemData.invoke(_tile, new Object[]{});
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		if(data.length < 1 || data[0] == null || data[0].itemID < 1) return null;
		ItemStack stack = ((IInventory)_tile).getStackInSlot(1);
		if(stack == null || stack.itemID < 1) return null;
		ItemIdentifier dataIdent = ItemIdentifier.get(data[0]);
		ItemIdentifier stackIdent = ItemIdentifier.get(stack);
		if(stackIdent == itemIdent && stack.stackSize > 1) {
			stack.stackSize--;
			return stackIdent.makeNormalStack(1);
		}
		if(dataIdent == itemIdent && data[0].stackSize > 0) {
			try {
				mItemCount.set(_tile, data[0].stackSize - 1);
				return dataIdent.makeNormalStack(1);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		if(stackIdent == itemIdent && stack.stackSize > 0) {
			stack.stackSize--;
			return stackIdent.makeNormalStack(1);
		}
		return null;
	}

	@Override
	public ItemStack getMultipleItems(ItemIdentifier itemIdent, int count){
		if (itemCount(itemIdent) < count) return null;
		ItemStack stack = null;
		for (int i = 0; i < count; i++){
			if(stack == null){
				stack = getSingleItem(itemIdent);
			}
			else{
				stack.stackSize += getSingleItem(itemIdent).stackSize;
			}
		}
		return stack;
	}

	@Override
	public boolean containsItem(ItemIdentifier itemIdent) {
		ItemStack[] data = new ItemStack[]{};
		try {
			data = (ItemStack[]) getStoredItemData.invoke(_tile, new Object[]{});
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		if(data.length < 1 || data[0] == null || data[0].itemID < 1) return false;
		ItemIdentifier dataIdent = ItemIdentifier.get(data[0]);
		return itemIdent == dataIdent;
	}

	@Override
	public int roomForItem(ItemIdentifier itemIdent) {
		int result = Integer.MAX_VALUE - 128;
		ItemStack[] data = new ItemStack[]{};
		try {
			data = (ItemStack[]) getStoredItemData.invoke(_tile, new Object[]{});
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		if(data.length < 1 || data[0] == null || data[0].itemID < 1) return result;
		ItemStack stack = ((IInventory)_tile).getStackInSlot(1);
		if(stack == null || stack.itemID < 1) return result;
		ItemIdentifier dataIdent = ItemIdentifier.get(data[0]);
		ItemIdentifier stackIdent = ItemIdentifier.get(stack);
		if(itemIdent == dataIdent || itemIdent == stackIdent) {
			return result - (data[0].stackSize + stack.stackSize);
		} else {
			return 0;
		}
	}

	@Override
	public boolean hasRoomForItem(ItemIdentifier itemIdent) {
		return roomForItem(itemIdent) > 0;
	}
}
