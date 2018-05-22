package cofh.cofhworld.parser.variables;

import cofh.cofhworld.data.numbers.*;
import cofh.cofhworld.data.numbers.operation.BoundedProvider;
import cofh.cofhworld.data.numbers.operation.MathProvider;
import cofh.cofhworld.data.numbers.random.SkellamRandomProvider;
import cofh.cofhworld.data.numbers.random.UniformRandomProvider;
import cofh.cofhworld.data.numbers.world.WorldValueProvider;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;

public class NumberData {

	public static INumberProvider parseNumberValue(ConfigValue numberEntry) {

		switch (numberEntry.valueType()) {
		case NUMBER:
			return new ConstantProvider(((Number) numberEntry.unwrapped()));
		case OBJECT:
			ConfigObject numberProps = (ConfigObject) numberEntry;
			Config numberObject = ((ConfigObject) numberEntry).toConfig();
			switch (numberProps.size()) {
			case 1:
				if (numberProps.containsKey("value")) {
					return new ConstantProvider(numberObject.getNumber("value"));
				} else if (numberProps.containsKey("variance")) {
					return new SkellamRandomProvider(parseNumberValue(numberObject.getValue("variance")));
				} else if (numberProps.containsKey("world-data")) {
					return new WorldValueProvider(numberObject.getString("world-data"));
				}
				break;
			case 2:
				if (numberProps.containsKey("min") && numberProps.containsKey("max")) {
					return new UniformRandomProvider(parseNumberValue(numberObject.getValue("min")), parseNumberValue(numberObject.getValue("max")));
				}
				break;
			case 3:
				INumberProvider a, b;
				if (numberProps.containsKey("operation") &&
						numberProps.containsKey("value-a") &&
						numberProps.containsKey("value-b")) {
					a = parseNumberValue(numberObject.getValue("value-a"));
					b = parseNumberValue(numberObject.getValue("value-b"));
					return new MathProvider(a, b, numberObject.getString("operation"));
				} else if (numberProps.containsKey("value") &&
						numberProps.containsKey("min") &&
						numberProps.containsKey("max")) {
					INumberProvider v = parseNumberValue(numberObject.getValue("value"));
					a = parseNumberValue(numberObject.getValue("min"));
					b = parseNumberValue(numberObject.getValue("max"));
					return new BoundedProvider(v, a, b);
				}
				break;
			default:
				throw new Error(String.format("Too many properties on object at line %s", numberEntry.origin().lineNumber()));
			case 0:
				break;
			}
			throw new Error(String.format("Unknown properties on object at line %s", numberEntry.origin().lineNumber()));
		default:
			throw new Error(String.format("Unsupported data type at line %s", numberEntry.origin().lineNumber()));
		}
	}

	public static INumberProvider parseNumberValue(ConfigValue numberEntry, Number min, Number max) {

		return new BoundedProvider(parseNumberValue(numberEntry), new ConstantProvider(min), new ConstantProvider(max));
	}

}
