package cofh.cofhworld.parser;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.shape.Shape2D;
import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.variables.*;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.util.random.WeightedEnum;
import cofh.cofhworld.util.random.WeightedNBTTag;
import cofh.cofhworld.util.random.WeightedString;
import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 * @param <T> Type to be constructed
 */
public interface IBuilder<T> {

	/**
	 * Builds the object after all configuration has been set. This will use default values for any
	 * unspecified attributes for the object.
	 *
	 * @return the configured instance.
	 */
	@Nonnull
	T build();

	/**
	 * @param <V>
	 */
	class BuilderFields<V> {

		public final Supplier<? extends IBuilder<V>> builder;

		public final BuilderField<? super IBuilder<V>, ? super Object>[] requiredFields, fields;

		/**
		 *
		 * @param builder
		 * @param requiredFields
		 * @param fields
		 */
		public BuilderFields(Supplier<? extends IBuilder<V>> builder,
				BuilderField<? super IBuilder<V>, ? super Object>[] requiredFields,
				BuilderField<? super IBuilder<V>, ? super Object>[] fields) {

			this.builder = builder;

			this.requiredFields = requiredFields;
			this.fields = fields;
		}

		/**
		 *
		 * @param genObject
		 * @return
		 * @throws InvalidConfigurationException
		 */
		final public V parse(Config genObject) throws InvalidConfigurationException {

			return parse(genObject, null);
		}

		/**
		 *
		 * @param genObject
		 * @param onMissingField
		 * @return
		 * @throws InvalidConfigurationException
		 */
		public V parse(Config genObject, Consumer<String> onMissingField) throws InvalidConfigurationException {

			boolean missedFields = false;
			l: for (BuilderField<?, ?> field : requiredFields) {
				for (String key : field.keys) {
					if (genObject.hasPath(key)) {
						continue l;
					}
				}
				if (onMissingField != null) onMissingField.accept(field.name);
				missedFields = true;
			}
			if (missedFields) {
				throw new InvalidConfigurationException("Missing fields", genObject.origin());
			}

			IBuilder<V> builder = this.builder.get();

			for (BuilderField<? super IBuilder<V>, ? super Object> field : fields) {
				for (String key : field.keys) {
					if (genObject.hasPath(key)) {
						assert field.set != null;
						assert field.type != null;
						field.set.accept(builder, field.type.processValue.apply(genObject.getValue(key)));
						break;
					}
				}
			}

			return builder.build();
		}
	}

	/**
	 *
	 * @param <T>
	 */
	public interface IBuilderFieldRegistry<T, B extends IBuilder<T>> {

		/**
		 *
		 * @param builderSupplier
		 * @return
		 */
		IBuilderFieldRegistry<T, B> setConstructor(Supplier<B> builderSupplier);

		/**
		 *
		 * @param name
		 * @param type
		 * @param parser
		 * @param keys
		 * @param <V>
		 * @return
		 */
		<V> IBuilderFieldRegistry<T, B> addRequiredField(String name, Type<V> type, BiConsumer<B, V> parser, String... keys);

		/**
		 *
		 * @param name
		 * @param type
		 * @param parser
		 * @param keys
		 * @param <V>
		 * @return
		 */
		<V> IBuilderFieldRegistry<T, B> addOptionalField(String name, Type<V> type, BiConsumer<B, V> parser, String... keys);
	}

	/**
	 * @param <T>
	 * @param <B>
	 */
	class FieldBuilder<T, B extends IBuilder<T>> implements IBuilder<BuilderFields<T>>, IBuilderFieldRegistry<T, B> {

		private Supplier<B> supplier;

		private final ArrayList<BuilderField<B, ?>> requiredFields = new ArrayList<>(), optionalFields = new ArrayList<>();

		/**
		 *
		 * @param builderSupplier
		 * @return	this
		 */
		public FieldBuilder<T, B> setConstructor(Supplier<B> builderSupplier) {

			this.supplier = builderSupplier;
			return this;
		}

		/**
		 *
		 * @param name
		 * @param type
		 * @param parser
		 * @param keys
		 * @param <V>
		 * @return
		 */
		public <V> FieldBuilder<T, B> addRequiredField(String name, Type<V> type, BiConsumer<B, V> parser, String... keys) {

			requiredFields.add(new BuilderField<>(name, null, null, keys));
			return addOptionalField(name, type, parser, keys);
		}

		/**
		 *
		 * @param name
		 * @param type
		 * @param parser
		 * @param keys
		 * @param <V>
		 * @return
		 */
		public <V> FieldBuilder<T, B> addOptionalField(String name, Type<V> type, BiConsumer<B, V> parser, String... keys) {

			optionalFields.add(new BuilderField<>(name, Objects.requireNonNull(type), Objects.requireNonNull(parser), keys));
			return this;
		}

		@Nonnull
		@Override
		@SuppressWarnings("unchecked")
		public BuilderFields<T> build() {

			return new BuilderFields<>(Objects.requireNonNull(supplier), requiredFields.toArray(new BuilderField[0]), optionalFields.toArray(new BuilderField[0]));
		}
	}

	/**
	 * Representation of a field on an {@link IBuilder} so that it may be assigned and validated by factory code
	 *
	 * @param <T>	The {@link IBuilder} class that this field represents.
	 * @param <V>	The type of the value for this field, as assignable through {@link Type}.
	 */
	final class BuilderField<T extends IBuilder<?>, V> {

		/**
		 * The name of this field
		 */
		@Nonnull
		final public String name;
		/**
		 * The {@link Type} of this field.
		 *
		 * <b>May</b> be {@code null} if this field object is merely informational.
		 */
		@Nullable
		final public Type<V> type;
		/**
		 * The method that will assign this field's value.
		 *
		 * <b>May</b> be {@code null} if this field object is merely informational.
		 */
		@Nullable
		final public BiConsumer<T, V> set;
		/**
		 * A list of keys that can represent this field in data. Will <b>always</b> contain at least 1 item, {@code name}.
		 */
		@Nonnull
		final public String[] keys;

		/**
		 * Construct a representation of a field on an {@link IBuilder} so that it may be assigned and validated by factory code
		 *
		 * @param name			The {@code name} of this field.
		 * @param type			The {@link Type} of this field. <b>May</b> be null if this field will only be for information.
		 * @param parser		The method that will assign this field's value. <b>May</b> be null if this field will only be for information.
		 * @param extraNames	Alias names that may represent this field. <b>May</b> be null or empty if this field does not have any aliases.
		 */
		public BuilderField(String name, @Nullable Type<V> type, @Nullable BiConsumer<T, V> parser, @Nullable String... extraNames) {

			this.name = name;
			this.type = type;

			this.set = parser;
			if (extraNames == null || extraNames.length == 0) {
				keys = new String[] { name };
			} else {
				this.keys = new String[extraNames.length + 1];
				System.arraycopy(extraNames, 0, keys, 1, extraNames.length);
				this.keys[0] = name;
			}
		}

		/**
		 * @param <V>
		 */
		public static class Type<V> {

			/**
			 *
			 * @param <R>
			 */
			@FunctionalInterface
			private static interface ITypeParserFunction<R> {

				R apply(ConfigValue t) throws InvalidConfigurationException;
			}

			/**
			 *
			 */
			public static final Type<List<WeightedBlock>> BLOCK_LIST = new Type<>(BlockData::parseBlockList);
			/**
			 *
			 */
			public static final Type<List<Material>> MATERIAL_LIST = new Type<>(BlockData::parseMaterialList);
			/**
			 *
			 */
			public static final Type<ICondition> CONDITION = new Type<>(ConditionData::parseConditionValue);
			/**
			 *
			 */
			public static final Type<INumberProvider> NUMBER = new Type<>(NumberData::parseNumberValue);
			/**
			 *
			 */
			public static final Type<WeightedString> STRING = new Type<>(StringData::parseStringEntry);
			/**
			 *
			 */
			public static final Type<List<WeightedString>> STRING_LIST = new Type<>(StringData::parseStringList);
			/**
			 *
			 */
			public static final Type<List<WeightedNBTTag>> STRUCTURE_LIST = new Type<>(StructreData::parseStructureList);
			/**
			 *
			 */
			public static final Type<Shape2D> SHAPE_2D = new Type<>(ShapeData::parse2DShapeEntry);

			/**
			 *
			 */
			public static final Type<List<WorldGen>> GENERATOR_LIST = new Type<>(GeneratorData::parseGenerators);

			/**
			 *
			 */
			public static final Type<Boolean> RAW_BOOLEAN = new Type<>(entry -> entry.valueType() == ConfigValueType.BOOLEAN ?
					(Boolean) entry.unwrapped() : Boolean.parseBoolean(String.valueOf(entry.unwrapped())));

			public final ITypeParserFunction<V> processValue;

			private Type(ITypeParserFunction<V> processType) {

				this.processValue = processType;
			}

			/**
			 * @param <V>
			 */
			public static class Enum<V> extends Type<V> {

				private static final HashMap<Class<?>, Enum<?>> lists = new HashMap<>(), singles = new HashMap<>();

				/**
				 * @param values
				 * @param <T>
				 *
				 * @return
				 */
				@SuppressWarnings("unchecked")
				public static <T extends java.lang.Enum<T>> Enum<List<WeightedEnum<T>>> ofList(final Class<T> values) {

					Enum<List<WeightedEnum<T>>> value = (Enum<List<WeightedEnum<T>>>) lists.get(values);
					if (value != null)
						return value;

					value = new Enum<>(entry -> EnumData.parseEnumList(entry, values));
					lists.put(values, value);
					return value;
				}

				/**
				 * @param values
				 * @param <T>
				 *
				 * @return
				 */
				@SuppressWarnings("unchecked")
				public static <T extends java.lang.Enum<T>> Enum<WeightedEnum<T>> of(final Class<T> values) {

					Enum<WeightedEnum<T>> value = (Enum<WeightedEnum<T>>) singles.get(values);
					if (value != null)
						return value;

					value = new Enum<>(entry -> EnumData.parseEnumEntry(entry, values));
					singles.put(values, value);
					return value;
				}

				private Enum(ITypeParserFunction<V> processType) {

					super(processType);
				}
			}
		}
	}

}
