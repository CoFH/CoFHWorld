//package cofh.cofhworld.parser.variables;
//
//import cofh.cofhworld.util.Utils;
//import cofh.cofhworld.util.random.WeightedItemStack;
//import cofh.cofhworld.util.random.WeightedString;
//import com.typesafe.config.*;
//import net.minecraft.item.ItemStack;
//import net.minecraft.nbt.JsonToNBT;
//import net.minecraft.nbt.NBTException;
//import net.minecraft.nbt.NBTTagCompound;
//import net.minecraft.util.ResourceLocation;
//import net.minecraftforge.fml.common.registry.ForgeRegistries;
//import net.minecraftforge.oredict.OreDictionary;
//
//import javax.annotation.Nullable;
//import java.util.List;
//import java.util.Objects;
//
//import static cofh.cofhworld.CoFHWorld.log;
//
//public class ItemData {
//
//	public static boolean parseItemList(ConfigValue itemEntry, List<WeightedItemStack> list) {
//
//		if (itemEntry.valueType() == ConfigValueType.LIST) {
//			ConfigList configList = (ConfigList) itemEntry;
//
//			for (int i = 0, e = configList.size(); i < e; ++i) {
//				WeightedItemStack entry = parseItemEntry(configList.get(i));
//				if (entry == null) {
//					return false;
//				}
//				list.add(entry);
//			}
//		} else {
//			WeightedItemStack entry = parseItemEntry(itemEntry);
//			if (entry == null) {
//				return false;
//			}
//			list.add(entry);
//		}
//		return true;
//	}
//
//	@Nullable
//	public static WeightedItemStack parseItemEntry(ConfigValue itemEntry) {
//
//		if (itemEntry.valueType() == ConfigValueType.NULL) {
//			return null;
//		}
//		int metadata = 0, stackSize = 1, chance = 100;
//		ItemStack stack;
//
//		if (itemEntry.valueType() != ConfigValueType.OBJECT) {
//			stack = parseItem(itemEntry, stackSize, metadata);
//		} else {
//			Config itemObject = ((ConfigObject) itemEntry).toConfig();
//
//			if (itemObject.hasPath("metadata")) {
//				metadata = itemObject.getInt("metadata");
//			}
//			if (itemObject.hasPath("count")) {
//				stackSize = itemObject.getInt("count");
//			} else if (itemObject.hasPath("stack-size")) {
//				stackSize = itemObject.getInt("stack-size");
//			} else if (itemObject.hasPath("amount")) {
//				stackSize = itemObject.getInt("amount");
//			}
//			if (stackSize <= 0) {
//				stackSize = 1;
//			}
//			if (itemObject.hasPath("weight")) {
//				chance = itemObject.getInt("weight");
//			}
//			if (itemObject.hasPath("ore-name")) {
//				String oreName = itemObject.getString("ore-name");
//				if (!Utils.oreNameExists(oreName)) {
//					log.error("Invalid ore-name `{}` for item at line {}!", oreName, itemEntry.origin().lineNumber());
//					return null;
//				}
//				ItemStack oreStack = OreDictionary.getOres(oreName, false).get(0);
//				stack = Utils.cloneStack(oreStack, stackSize);
//			} else {
//				if (!itemObject.hasPath("name")) {
//					log.error("Item entry missing valid name or ore-name at line {}!", itemEntry.origin().lineNumber());
//					return null;
//				}
//				stack = parseItem(itemObject.getValue("name"), stackSize, metadata);
//			}
//			if (stack != null && itemObject.hasPath("nbt")) {
//				try {
//					NBTTagCompound tagCompound = JsonToNBT.getTagFromJson(itemObject.getString("nbt"));
//
//					stack.setTagCompound(tagCompound);
//				} catch (NBTException t) {
//					log.error("Item has invalid NBT data at line {}.", itemObject.getValue("nbt").origin().lineNumber(), t);
//				}
//			}
//		}
//		return stack == null ? null : new WeightedItemStack(stack, chance);
//	}
//
//	@Nullable
//	private static ItemStack parseItem(ConfigValue itemName, int stackSize, int metadata) {
//
//		WeightedString name = StringData.parseStringEntry(itemName);
//		if (name != null) {
//			ResourceLocation loc = new ResourceLocation(name.value);
//			if (ForgeRegistries.ITEMS.containsKey(loc)) {
//				return new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(loc), () -> "Null item registered for `" + loc.toString() + '`'), stackSize, metadata);
//			} else {
//				log.error("No item registered for name `{}` on line {}", loc.toString(), itemName.origin().lineNumber());
//			}
//		}
//		return null;
//	}
//
//}
