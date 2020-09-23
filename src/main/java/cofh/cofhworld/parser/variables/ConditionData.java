package cofh.cofhworld.parser.variables;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ConstantCondition;
import cofh.cofhworld.data.condition.ICondition;
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
					return new ConstantCondition(Boolean.parseBoolean(value));
				}
			case BOOLEAN:
				return new ConstantCondition((Boolean) conditionEntry.unwrapped());
			case OBJECT:
				ConfigObject conditionProps = (ConfigObject) conditionEntry;
				Config conditionObject = ((ConfigObject) conditionEntry).toConfig();
				switch (conditionProps.size()) {
					case 1:
						if (conditionProps.containsKey("value")) {
							// technically this allows "nesting" but really i just want to parse the string in one place. don't tell anyone.
							return parseConditionValue(conditionObject.getValue("value"));
						} else if (conditionProps.containsKey("random")) {
							// for technical completeness
							return new RandomCondition();
						} else if (conditionProps.containsKey("world-data")) {
							return new WorldValueCondition(conditionObject.getString("world-data"));
						} else if (conditionProps.containsKey("not")) {
							return new NotCondition(parseConditionValue(conditionObject.getValue("not")));
						} else if (conditionProps.containsKey("material")) {
							ArrayList<Material> material = new ArrayList<>();
							BlockData.parseMaterialList(conditionObject.getValue("material"), material);
							return new MaterialCondition(material);
						}
						break;
					case 3:
						if (conditionProps.containsKey("value-a") && conditionProps.containsKey("value-b")) {
							if (conditionProps.containsKey("operation")) {
								ICondition a, b;
								a = parseConditionValue(conditionObject.getValue("value-a"));
								b = parseConditionValue(conditionObject.getValue("value-b"));
								return new BinaryCondition(a, b, conditionObject.getString("operation"));
							} else if (conditionProps.containsKey("comparison")) {
								INumberProvider a, b;
								a = parseNumberValue(conditionObject.getValue("value-a"));
								b = parseNumberValue(conditionObject.getValue("value-b"));
								return new ComparisonCondition(a, b, conditionObject.getString("comparison"));
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
