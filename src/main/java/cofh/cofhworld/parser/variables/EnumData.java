package cofh.cofhworld.parser.variables;

import cofh.cofhworld.util.random.WeightedEnum;
import com.typesafe.config.*;

import java.util.List;

import static cofh.cofhworld.CoFHWorld.log;

public class EnumData {

	public static <T extends Enum<T>> boolean parseEnumList(ConfigValue enumEntry, List<WeightedEnum<T>> list, Class<T> values) {

		if (enumEntry.valueType() == ConfigValueType.LIST) {
			ConfigList configList = (ConfigList) enumEntry;

			for (int i = 0, e = configList.size(); i < e; i++) {
				WeightedEnum<T> entry = parseEnumEntry(configList.get(i), values);
				if (entry == null) {
					return false;
				}
				list.add(entry);
			}
		} else {
			WeightedEnum<T> entry = parseEnumEntry(enumEntry, values);
			if (entry == null) {
				return false;
			}
			list.add(entry);
		}
		return true;
	}

	public static <T extends Enum<T>> WeightedEnum<T> parseEnumEntry(ConfigValue enumEntry, Class<T> values) {

		int weight = 100;
		String type = null;
		switch (enumEntry.valueType()) {
			case LIST:
				log.warn("Lists are not supported for enum values at line {}.", enumEntry.origin().lineNumber());
				return null;
			case NULL:
				log.warn("Null enum entry at line {}", enumEntry.origin().lineNumber());
				return null;
			case OBJECT:
				Config enumObject = ((ConfigObject) enumEntry).toConfig();
				if (enumObject.hasPath("name")) {
					type = enumObject.getString("name");
				} else {
					log.warn("Value missing 'name' field at line {}", enumEntry.origin().lineNumber());
				}
				if (enumObject.hasPath("weight")) {
					weight = enumObject.getInt("weight");
				}
				break;
			case STRING:
				type = String.valueOf(enumEntry.unwrapped());
				break;
			default:
				log.warn("Invalid type for enum at line {}", enumEntry.origin().lineNumber());
				return null;
		}
		try {
			T v = Enum.valueOf(values, type);
			return new WeightedEnum<T>(v, weight);
		} catch (IllegalArgumentException e) {
			log.error("Invalid enum entry {} on line {}", type, enumEntry.origin().lineNumber());
		}
		return null;
	}

}
