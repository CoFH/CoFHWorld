package cofh.cofhworld.parser.variables;

import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.data.*;
import cofh.cofhworld.data.numbers.operation.BoundedProvider;
import cofh.cofhworld.data.numbers.operation.ConditionalProvider;
import cofh.cofhworld.data.numbers.operation.MathProvider;
import cofh.cofhworld.data.numbers.operation.UnaryMathProvider;
import cofh.cofhworld.data.numbers.random.SkellamRandomProvider;
import cofh.cofhworld.data.numbers.random.UniformRandomProvider;
import cofh.cofhworld.data.numbers.world.DirectionalScanner;
import cofh.cofhworld.data.numbers.world.WorldValueProvider;
import cofh.cofhworld.util.random.WeightedEnum;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import net.minecraft.util.Direction;

import static cofh.cofhworld.parser.variables.ConditionData.parseConditionValue;

public class NumberData {

	public static INumberProvider parseNumberValue(ConfigValue numberEntry) {

		switch (numberEntry.valueType()) {
			case NUMBER: {
				return new ConstantProvider(((Number) numberEntry.unwrapped()));
			}
			case OBJECT: {
				ConfigObject numberProps = (ConfigObject) numberEntry;
				Config numberObject = ((ConfigObject) numberEntry).toConfig();
				switch (numberProps.size()) {
					case 1: {
						if (numberProps.containsKey("value")) {
							// technically this allows "nesting" but really i just want to parse the value in one place. don't tell anyone.
							return parseNumberValue(numberObject.getValue("value"));
						} else if (numberProps.containsKey("variance")) {
							return new SkellamRandomProvider(parseNumberValue(numberObject.getValue("variance")));
						} else if (numberProps.containsKey("world-data")) {
							return WorldValueProvider.getProvider(numberObject.getString("world-data"));
						} else if (numberProps.containsKey("generator-data")) {
							return new DataProvider(numberObject.getString("generator-data"));
						}
						break;
					}
					case 2: {
						if (numberProps.containsKey("min") && numberProps.containsKey("max")) {
							return new UniformRandomProvider(parseNumberValue(numberObject.getValue("min")), parseNumberValue(numberObject.getValue("max")));
						} else if (numberProps.containsKey("generator-data") && numberProps.containsKey("default-value")) {
							return new DefaultedDataProvider(numberObject.getString("generator-data"), parseNumberValue(numberObject.getValue("default-value")));
						} else if (numberProps.containsKey("value")) {
							if (numberProps.containsKey("operation")) {
								return new UnaryMathProvider(parseNumberValue(numberObject.getValue("value")), numberObject.getString("operation"));
							} else if (numberProps.containsKey("cache")) {
								return new CacheProvider(numberObject.getString("cache"), parseNumberValue(numberObject.getValue("value")));
							} else if (numberProps.containsKey("store")) {
								return new StoreProvider(numberObject.getString("store"), parseNumberValue(numberObject.getValue("value")));
							}
						}
						break;
					}
					case 3: {
						INumberProvider a, b;
						if (numberProps.containsKey("operation") && numberProps.containsKey("value-a") && numberProps.containsKey("value-b")) {
							a = parseNumberValue(numberObject.getValue("value-a"));
							b = parseNumberValue(numberObject.getValue("value-b"));
							return new MathProvider(a, b, numberObject.getString("operation"));
						} else if (numberProps.containsKey("value") && numberProps.containsKey("min") && numberProps.containsKey("max")) {
							INumberProvider v = parseNumberValue(numberObject.getValue("value"));
							a = parseNumberValue(numberObject.getValue("min"));
							b = parseNumberValue(numberObject.getValue("max"));
							return new BoundedProvider(v, a, b);
						} else if (numberProps.containsKey("condition") && numberProps.containsKey("if-true") && numberProps.containsKey("if-false")) {
							a = parseNumberValue(numberObject.getValue("if-true"));
							b = parseNumberValue(numberObject.getValue("if-false"));
							return new ConditionalProvider(parseConditionValue(numberObject.getValue("condition")), a, b);
						} else if (numberProps.containsKey("condition") && numberProps.containsKey("limit") && numberProps.containsKey("direction")) {
							a = parseNumberValue(numberObject.getValue("limit"), 0, 256);
							WeightedEnum<Direction> dir = EnumData.parseEnumEntry(numberObject.getValue("direction"), Direction.class);
							if (dir == null) {
								throw new Error(String.format("Invalid direction provided at line %s", numberObject.getValue("direction").origin().lineNumber()));
							}
							return new DirectionalScanner(parseConditionValue(numberObject.getValue("condition")), dir.value, a);
						} else if (numberProps.containsKey("table") && numberProps.containsKey("lookup") && numberProps.containsKey("default")) {
							ConfigList list = numberObject.getList("table");
							INumberProvider[] table = new INumberProvider[list.size()];
							for (int i = 0, e = table.length; i < e; ++i) {
								table[i] = parseNumberValue(list.get(i));
							}
							INumberProvider def = parseNumberValue(numberObject.getValue("default"));
							return new TableProvider(table, parseNumberValue(numberObject.getValue("lookup")), def);
						}
						break;
					}
					case 4: {
						if (numberProps.containsKey("table") && numberProps.containsKey("lookup") &&
								numberProps.containsKey("less-than-value") && numberProps.containsKey("greater-than-value")) {
							ConfigList list = numberObject.getList("table");
							INumberProvider[] table = new INumberProvider[list.size()];
							for (int i = 0, e = table.length; i < e; ++i) {
								table[i] = parseNumberValue(list.get(i));
							}
							return new TableProvider(table, parseNumberValue(numberObject.getValue("lookup")),
									parseNumberValue(numberObject.getValue("less-than-value")), parseNumberValue(numberObject.getValue("greater-than-value")));
						}
						break;
					}
					default: {
						throw new Error(String.format("Too many properties on object at line %s", numberEntry.origin().lineNumber()));
					}
					case 0: {
						break;
					}
				}
				throw new Error(String.format("Unknown properties on object at line %s", numberEntry.origin().lineNumber()));
			}
			default:
				throw new Error(String.format("Unsupported data type at line %s", numberEntry.origin().lineNumber()));
		}
	}

	public static INumberProvider parseNumberValue(ConfigValue numberEntry, Number min, Number max) {

		return new BoundedProvider(parseNumberValue(numberEntry), new ConstantProvider(min), new ConstantProvider(max));
	}

}
