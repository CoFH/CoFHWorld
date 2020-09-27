package cofh.cofhworld.parser;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.shape.Shape2D;
import cofh.cofhworld.parser.variables.*;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.util.random.WeightedEnum;
import cofh.cofhworld.util.random.WeightedNBTTag;
import cofh.cofhworld.util.random.WeightedString;
import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.ConfigValueType;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

public final class Field<T extends IBuilder<? extends WorldGen>, V> {

	final public String name;
	final public Type<V> type;
	final public BiConsumer<T, V> parser;
	final public String[] keys;

	public Field(String name, Type<V> type, BiConsumer<T, V> parser, String... keys) {

		this.name = name;
		this.type = type;

		this.parser = parser;
		this.keys = new String[keys == null ? 1 : keys.length + 1];
		{
			int i = 0;
			this.keys[i] = name;
			if (keys != null) {
				for (String key : keys)
					this.keys[++i] = key;
			}
		}
	}

	public static class Type<V> {
		public static final Type<List<WeightedBlock>> BLOCK_LIST = new Type<>(BlockData::parseBlockList);
		public static final Type<List<Material>> MATERIAL_LIST = new Type<>(BlockData::parseMaterialList);
		public static final Type<ICondition> CONDITION = new Type<>(ConditionData::parseConditionValue);
		public static final Type<INumberProvider> NUMBER = new Type<>(NumberData::parseNumberValue);
		public static final Type<WeightedString> STRING = new Type<>(StringData::parseStringEntry);
		public static final Type<List<WeightedString>> STRING_LIST = new Type<>(StringData::parseStringList);
		public static final Type<List<WeightedNBTTag>> STRUCTURE_LIST = new Type<>(StructreData::parseStructureList);
		public static final Type<Shape2D> SHAPE_2D = new Type<>(ShapeData::parse2DShapeEntry);

		public static final Type<List<WorldGen>> GENERATOR_LIST = new Type<>(GeneratorData::parseGenerators);

		public static final Type<Boolean> RAW_BOOLEAN = new Type<>(entry -> entry.valueType() == ConfigValueType.BOOLEAN ?
				(Boolean) entry.unwrapped() : Boolean.parseBoolean(String.valueOf(entry.unwrapped())));

		public final IGeneratorFunction<V> processType;

		private Type(IGeneratorFunction<V> processType) {

			this.processType = processType;
		}

		public static class Enum<V> extends Type<V> {

			private static final HashMap<Class<?>, Enum<?>> lists = new HashMap<>(), singles = new HashMap<>();

			public static <T extends java.lang.Enum<T>> Enum<List<WeightedEnum<T>>> ofList(final Class<T> values) {

				Enum<List<WeightedEnum<T>>> value = (Enum<List<WeightedEnum<T>>>) lists.get(values);
				if (value != null)
					return value;

				value = new Enum<>(entry -> EnumData.parseEnumList(entry, values));
				lists.put(values, value);
				return value;
			}

			public static <T extends java.lang.Enum<T>> Enum<WeightedEnum<T>> of(final Class<T> values) {

				Enum<WeightedEnum<T>> value = (Enum<WeightedEnum<T>>) singles.get(values);
				if (value != null)
					return value;

				value = new Enum<>(entry -> EnumData.parseEnumEntry(entry, values));
				singles.put(values, value);
				return value;
			}

			private Enum(IGeneratorFunction<V> processType) {

				super(processType);
			}
		}
	}
}
