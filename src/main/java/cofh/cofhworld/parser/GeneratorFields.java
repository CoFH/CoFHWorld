package cofh.cofhworld.parser;

import cofh.cofhworld.world.generator.WorldGen;

import java.util.function.Supplier;

public class GeneratorFields {

	public final Supplier<? extends IBuilder<? extends WorldGen>> builder;

	public final Field<? super IBuilder<? extends WorldGen>, ? super Object>[] requiredFields, fields;

	public GeneratorFields(Supplier<? extends IBuilder<? extends WorldGen>> builder,
			Field<? super IBuilder<? extends WorldGen>, ? super Object>[] requiredFields,
			Field<? super IBuilder<? extends WorldGen>, ? super Object>[] fields) {

		this.builder = builder;

		this.requiredFields = requiredFields;
		this.fields = fields;
	}
}
