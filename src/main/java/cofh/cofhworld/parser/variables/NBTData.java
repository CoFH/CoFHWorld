package cofh.cofhworld.parser.variables;

import cofh.cofhworld.util.random.WeightedNBTTag;
import com.typesafe.config.*;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.List;

import static cofh.cofhworld.CoFHWorld.log;

public class NBTData {

	public static boolean parseNBTList(ConfigValue entityEntry, List<WeightedNBTTag> list) {

		if (entityEntry.valueType() == ConfigValueType.LIST) {
			ConfigList blockList = (ConfigList) entityEntry;

			for (int i = 0, e = blockList.size(); i < e; i++) {
				WeightedNBTTag entry = parseNBTEntry(blockList.get(i));
				if (entry == null) {
					return false;
				}
				list.add(entry);
			}
		} else {
			WeightedNBTTag entry = parseNBTEntry(entityEntry);
			if (entry == null) {
				return false;
			}
			list.add(entry);
		}
		return true;
	}

	@Nullable
	public static WeightedNBTTag parseNBTEntry(ConfigValue nbtEntry) {

		int weight = 100;
		String json;
		ConfigOrigin jsonOrigin;
		switch (nbtEntry.valueType()) {
			case NULL:
				log.warn("Null NBT entry!");
				return null;
			case OBJECT:
				Config nbtObject = ((ConfigObject) nbtEntry).toConfig();
				ConfigValue data;
				if (nbtObject.hasPath("tag")) {
					data = nbtObject.getValue("tag");
				} else if (!nbtObject.hasPath("name")) {
					log.error("`name` or `tag` tag missing on object at line {}!", nbtEntry.origin().lineNumber());
					return null;
				} else {
					data = nbtObject.getValue("name");
				}
				if (data.valueType() != ConfigValueType.STRING) {
					log.error("Invalid NBT String on line {}!", data.origin().lineNumber());
					return null;
				}
				jsonOrigin = data.origin();
				json = String.valueOf(data.unwrapped());
				if (nbtObject.hasPath("weight")) {
					weight = nbtObject.getInt("weight");
				}
				break;
			case STRING:
				jsonOrigin = nbtEntry.origin();
				json = String.valueOf(nbtEntry.unwrapped());
				break;
			default:
				log.warn("Invalid NBT entry type at line {}", nbtEntry.origin().lineNumber());
				return null;
		}
		NBTTagCompound tag;
		try {
			tag = JsonToNBT.getTagFromJson(json);
		} catch (NBTException e) {
			log.error("Invalid NBT String at line {}!", jsonOrigin.lineNumber(), e);
			return null;
		}
		return new WeightedNBTTag(weight, tag);
	}

}
