package cofh.cofhworld.parser.variables;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ConstantCondition;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.condition.data.CacheCondition;
import cofh.cofhworld.data.condition.data.DataCondition;
import cofh.cofhworld.data.condition.data.DefaultedDataCondition;
import cofh.cofhworld.data.condition.operation.BinaryCondition;
import cofh.cofhworld.data.condition.operation.ComparisonCondition;
import cofh.cofhworld.data.condition.operation.NotCondition;
import cofh.cofhworld.data.condition.random.RandomCondition;
import cofh.cofhworld.data.condition.world.MaterialCondition;
import cofh.cofhworld.data.condition.world.WorldValueCondition;
import cofh.cofhworld.data.numbers.INumberProvider;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;

import java.util.ArrayList;

import static cofh.cofhworld.parser.variables.NumberData.parseNumberValue;

public class ConditionData {

	public static ICondition parseConditionValue(ConfigValue conditionEntry) {

		switch (conditionEntry.valueType()) {
			case STRING:
				String value = String.valueOf(conditionEntry.unwrapped());
				if ("random".equalsIgnoreCase(value)) {
					return new RandomCondition();
				} else {
					return Boolean.parseBoolean(value) ? ConstantCondition.TRUE : ConstantCondition.FALSE;
				}
			case BOOLEAN:
				return ((Boolean) conditionEntry.unwrapped()) ? ConstantCondition.TRUE : ConstantCondition.FALSE;
			case OBJECT:
				ConfigObject condProps = (ConfigObject) conditionEntry;
				Config condObject = ((ConfigObject) conditionEntry).toConfig();
				switch (condProps.size()) {
					case 1:
						if (condProps.containsKey("value")) {
							// technically this allows "nesting" but really i just want to parse the string in one place. don't tell anyone.
							return parseConditionValue(condObject.getValue("value"));
						} else if (condProps.containsKey("random")) {
							// for technical completeness
							return new RandomCondition();
						} else if (condProps.containsKey("world-data")) {
							return WorldValueCondition.getCondition(condObject.getString("world-data"));
						} else if (condProps.containsKey("not")) {
							return new NotCondition(parseConditionValue(condObject.getValue("not")));
						} else if (condProps.containsKey("material")) {
							ArrayList<Material> material = new ArrayList<>();
							BlockData.parseMaterialList(condObject.getValue("material"), material);
							return new MaterialCondition(material);
						} else if (condProps.containsKey("generator-data")) {
							return new DataCondition(condObject.getString("generator-data"));
						}
						break;
					case 2:
						if (condProps.containsKey("generator-data")) {
							if (condProps.containsKey("check-property")) {
								return new DataCondition(condObject.getString("generator-data"), condObject.getBoolean("check-property"));
							} else if (condProps.containsKey("default-value")) {
								return new DefaultedDataCondition(condObject.getString("generator-data"), parseConditionValue(condObject.getValue("default-value")));
							}
						} else if (condProps.containsKey("cache") && condProps.containsKey("key")) {
							return new CacheCondition(condObject.getString("key"), parseConditionValue(condObject.getValue("cache")));
						}
						break;
					case 3:
						if (condProps.containsKey("value-a") && condProps.containsKey("value-b")) {
							if (condProps.containsKey("operation")) {
								ICondition a, b;
								a = parseConditionValue(condObject.getValue("value-a"));
								b = parseConditionValue(condObject.getValue("value-b"));
								return new BinaryCondition(a, b, condObject.getString("operation"));
							} else if (condProps.containsKey("comparison")) {
								INumberProvider a, b;
								a = parseNumberValue(condObject.getValue("value-a"));
								b = parseNumberValue(condObject.getValue("value-b"));
								return new ComparisonCondition(a, b, condObject.getString("comparison"));
							}
						}
						break;
					default:
						throw new Error(String.format("Too many properties on object at line %s", conditionEntry.origin().lineNumber()));
					case 0:
						break;
				}
				throw new Error(String.format("Unknown properties on object at line %s", conditionEntry.origin().lineNumber()));
			default:
				throw new Error(String.format("Unsupported data type at line %s", conditionEntry.origin().lineNumber()));
		}
	}
}
