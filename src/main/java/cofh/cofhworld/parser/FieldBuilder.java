package cofh.cofhworld.parser;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.world.generator.WorldGen;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class FieldBuilder<T extends IBuilder<? extends WorldGen>> implements IBuilder<GeneratorFields>, IGeneratorFieldRegistry<T> {

	private Supplier<T> builderSupplier;

	private final ArrayList<Field<?, ?>> requiredFields = new ArrayList<>(), optionalFields = new ArrayList<>();

	public IGeneratorFieldRegistry<T> setBuilder(Supplier<T> builderSupplier) {

		this.builderSupplier = builderSupplier;
		return this;
	}

	public <V> IGeneratorFieldRegistry<T> addRequiredField(String name, Type<V> type, BiConsumer<T, V> parser, String... keys) {

		requiredFields.add(new Field<T, V>(name, null, null, keys));
		return addOptionalField(name, type, parser, keys);
	}

	public <V> IGeneratorFieldRegistry<T> addOptionalField(String name, Type<V> type, BiConsumer<T, V> parser, String... keys) {

		optionalFields.add(new Field<>(name, type, parser, keys));
		return this;
	}

	@Nonnull
	@Override
	public GeneratorFields build() {

		return new GeneratorFields(Objects.requireNonNull(builderSupplier), requiredFields.toArray(new Field[0]), optionalFields.toArray(new Field[0]));
	}
}
