package cofh.cofhworld.parser.variables;

import cofh.cofhworld.util.random.WeightedNBTTag;
import com.typesafe.config.*;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

import static cofh.cofhworld.CoFHWorld.log;

public class EntityData {

	public static boolean parseEntityList(ConfigValue entityEntry, List<WeightedNBTTag> list) {

		if (entityEntry.valueType() == ConfigValueType.LIST) {
			ConfigList blockList = (ConfigList) entityEntry;

			for (int i = 0, e = blockList.size(); i < e; i++) {
				WeightedNBTTag entry = parseEntityEntry(blockList.get(i));
				if (entry == null) {
					return false;
				}
				list.add(entry);
			}
		} else {
			WeightedNBTTag entry = parseEntityEntry(entityEntry);
			if (entry == null) {
				return false;
			}
			list.add(entry);
		}
		return true;
	}

	public static WeightedNBTTag parseEntityEntry(ConfigValue entityEntry) {

		switch (entityEntry.valueType()) {
			case NULL:
				log.warn("Null entity entry!");
				return null;
			case OBJECT:
				Config entityObject = ((ConfigObject) entityEntry).toConfig();
				NBTTagCompound data;
				if (entityObject.hasPath("spawner-tag")) {
					try {
						data = JsonToNBT.getTagFromJson(entityObject.getString("spawner-tag"));
					} catch (NBTException e) {
						log.error("Invalid `spawner-tag` value at line {}!", entityObject.getValue("spawner-tag").origin().lineNumber(), e);
						return null;
					}
				} else if (!entityObject.hasPath("entity")) {
					log.error("`entity` or `spawner-tag` tag missing on object at line {}!", entityEntry.origin().lineNumber());
					return null;
				} else {
					data = new NBTTagCompound();
					String type = entityObject.getString("entity");
					data.setString("EntityId", type);
				}
				int weight = entityObject.hasPath("weight") ? entityObject.getInt("weight") : 100;
				return new WeightedNBTTag(weight, data);
			case STRING:
				String type = (String) entityEntry.unwrapped();
				if (type == null) {
					log.error("Invalid entity entry!"); // probably invalid execution path?
					return null;
				}
				NBTTagCompound tag = new NBTTagCompound();
				tag.setString("EntityId", type);
				return new WeightedNBTTag(100, tag);
			default:
				log.warn("Invalid entity entry type at line {}", entityEntry.origin().lineNumber());
				return null;
		}
	}

}
