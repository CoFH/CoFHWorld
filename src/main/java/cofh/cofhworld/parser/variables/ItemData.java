package cofh.cofhworld.parser.variables;

import cofh.cofhworld.util.Utils;
import cofh.cofhworld.util.WeightedRandomItemStack;
import com.typesafe.config.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;

import static cofh.cofhworld.CoFHWorld.log;

public class ItemData {

	public static boolean parseItemList(ConfigValue itemEntry, List<WeightedRandomItemStack> list) {

		if (itemEntry.valueType() == ConfigValueType.LIST) {
			ConfigList configList = (ConfigList) itemEntry;

			for (int i = 0, e = configList.size(); i < e; ++i) {
				WeightedRandomItemStack entry = parseItemEntry(configList.get(i));
				if (entry == null) {
					return false;
				}
				list.add(entry);
			}
		} else {
			WeightedRandomItemStack entry = parseItemEntry(itemEntry);
			if (entry == null) {
				return false;
			}
			list.add(entry);
		}
		return true;
	}

	public static WeightedRandomItemStack parseItemEntry(ConfigValue itemEntry) {

		if (itemEntry.valueType() == ConfigValueType.NULL) {
			return null;
		}
		int metadata = 0, stackSize = 1, chance = 100;
		ItemStack stack;

		if (itemEntry.valueType() != ConfigValueType.OBJECT) {
			// FIXME: we need to validate against null from the registry
			stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(String.valueOf(itemEntry.unwrapped()))), 1, metadata);
		} else {
			Config itemObject = ((ConfigObject) itemEntry).toConfig();

			if (itemObject.hasPath("metadata")) {
				metadata = itemObject.getInt("metadata");
			}
			if (itemObject.hasPath("count")) {
				stackSize = itemObject.getInt("count");
			} else if (itemObject.hasPath("stack-size")) {
				stackSize = itemObject.getInt("stack-size");
			} else if (itemObject.hasPath("amount")) {
				stackSize = itemObject.getInt("amount");
			}
			if (stackSize <= 0) {
				stackSize = 1;
			}
			if (itemObject.hasPath("weight")) {
				chance = itemObject.getInt("weight");
			}
			if (itemObject.hasPath("ore-name")) {
				String oreName = itemObject.getString("ore-name");
				if (!Utils.oreNameExists(oreName)) {
					log.error("Invalid ore name `{}` for item at line {}!", oreName, itemEntry.origin().lineNumber());
					return null;
				}
				ItemStack oreStack = OreDictionary.getOres(oreName, false).get(0);
				stack = Utils.cloneStack(oreStack, stackSize);
			} else {
				if (!itemObject.hasPath("name")) {
					log.error("Item entry missing valid name or ore name at line {}!", itemEntry.origin().lineNumber());
					return null;
				}
				stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemObject.getString("name"))), stackSize, metadata);
			}
			if (itemObject.hasPath("nbt")) {
				try {
					NBTTagCompound tagCompound = JsonToNBT.getTagFromJson(itemObject.getString("nbt"));

					stack.setTagCompound(tagCompound);
				} catch (NBTException t) {
					log.error("Item has invalid NBT data at line {}.", itemObject.getValue("nbt").origin().lineNumber(), t);
				}
			}
		}
		return new WeightedRandomItemStack(stack, chance);
	}

}
