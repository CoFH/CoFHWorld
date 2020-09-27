package cofh.cofhworld.parser.variables;

import cofh.cofhworld.util.random.WeightedString;
import com.typesafe.config.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static cofh.cofhworld.CoFHWorld.log;

public class StringData {

	public static List<WeightedString> parseStringList(ConfigValue stringEntry) {

		List<WeightedString> list = new ArrayList<>();
		if (!parseStringList(stringEntry, list))
			;

		return list;
	}

	public static boolean parseStringList(ConfigValue stringEntry, List<WeightedString> list) {

		if (stringEntry.valueType() == ConfigValueType.LIST) {
			ConfigList configList = (ConfigList) stringEntry;

			for (int i = 0, e = configList.size(); i < e; i++) {
				WeightedString entry = parseStringEntry(configList.get(i));
				if (entry == null) {
					return false;
				}
				list.add(entry);
			}
		} else {
			WeightedString entry = parseStringEntry(stringEntry);
			if (entry == null) {
				return false;
			}
			list.add(entry);
		}
		return true;
	}

	@Nullable
	public static WeightedString parseStringEntry(ConfigValue stringEntry) {

		int weight = 100;
		String value = null;
		switch (stringEntry.valueType()) {
			case LIST:
				log.warn("Lists are not supported for string values at line {}.", stringEntry.origin().lineNumber());
				return null;
			case NULL:
				log.warn("Null string entry at line {}", stringEntry.origin().lineNumber());
				return null;
			case OBJECT:
				Config stringObject = ((ConfigObject) stringEntry).toConfig();
				if (stringObject.hasPath("name")) {
					value = stringObject.getString("name");
				} else {
					log.warn("Value missing 'name' field at line {}", stringEntry.origin().lineNumber());
				}
				if (stringObject.hasPath("weight")) {
					weight = stringObject.getInt("weight");
				}
				break;
			case BOOLEAN:
			case NUMBER:
			case STRING:
				value = String.valueOf(stringEntry.unwrapped());
				break;
		}
		return new WeightedString(value, weight);
	}

}
