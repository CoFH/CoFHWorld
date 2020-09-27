package cofh.cofhworld.parser;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.world.generator.WorldGen;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface IGeneratorFieldRegistry<T extends IBuilder<? extends WorldGen>> {

	IGeneratorFieldRegistry<T> setBuilder(Supplier<T> builderSupplier);

	<V> IGeneratorFieldRegistry<T> addRequiredField(String name, Type<V> type, BiConsumer<T, V> parser, String... keys);

	<V> IGeneratorFieldRegistry<T> addOptionalField(String name, Type<V> type, BiConsumer<T, V> parser, String... keys);
}
