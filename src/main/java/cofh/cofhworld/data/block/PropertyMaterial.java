package cofh.cofhworld.data.block;

import cofh.cofhworld.util.Tuple;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class PropertyMaterial extends Material {

	@Nullable
	public static Material of(List<Tuple<String, String>> props, boolean inclusive) {

		switch (props.size()) {
			case 0:
				return null;
			case 1:
				return new RawPropertyMaterial(props.iterator().next(), inclusive);
			default:
				return new RawPropertiesMaterial(props, inclusive);
		}
	}

	final protected boolean inclusive;

	public PropertyMaterial(boolean inclusive) {

		this.inclusive = inclusive;
	}

	final private static class RawPropertyMaterial extends PropertyMaterial {

		final private Tuple<String, String> property;

		public RawPropertyMaterial(Tuple<String, String> property, boolean inclusive) {

			super(inclusive);
			this.property = property;
		}

		@Override
		public boolean test(BlockState blockState) {

			StateContainer<Block, BlockState> container = blockState.getBlock().getStateContainer();

			String value = property.getB();
			Property<?> prop = container.getProperty(property.getA());
			if (prop == null) {
				return value == null & inclusive;
			} else if (value != null) {
				return prop.parseValue(value).isPresent() == inclusive;
			} else
				return !inclusive;
		}
	}

	final private static class RawPropertiesMaterial extends PropertyMaterial {

		final private Stream<Tuple<String, String>> properties;

		public RawPropertiesMaterial(List<Tuple<String, String>> properties, boolean inclusive) {

			super(inclusive);
			this.properties = properties.stream();
		}

		@Override
		public boolean test(BlockState blockState) {

			StateContainer<Block, BlockState> container = blockState.getBlock().getStateContainer();

			Predicate<Tuple<String, String>> test = v -> {
				String value = v.getB();
				Property<?> prop = container.getProperty(v.getA());
				if (prop == null) {
					return value == null;
				} else if (value != null) {
					return prop.parseValue(value).isPresent();
				} else
					return false;
			};

			return inclusive ? properties.allMatch(test) : properties.noneMatch(test);
		}
	}

	@Nullable
	public static Material of(Block block, List<Tuple<Property<?>, ?>> props, boolean inclusive) {

		switch (props.size()) {
			case 0:
				return null;
			case 1:
				return new BlockPropertyMaterial(block, props.iterator().next(), inclusive);
			default:
				return new BlockPropertiesMaterial(block, props, inclusive);
		}
	}

	final private static class BlockPropertyMaterial extends BlockMaterial {

		final private Tuple<Property<?>, ?> property;

		public BlockPropertyMaterial(Block block, Tuple<Property<?>, ?> property, boolean inclusive) {

			super(block, inclusive);
			this.property = property;
		}

		@Override
		public boolean test(BlockState blockState) {

			if (!super.test(blockState))
				return false;

			return blockState.get(property.getA()) == property.getB() == inclusive;
		}
	}

	final private static class BlockPropertiesMaterial extends BlockMaterial {

		final private Stream<Tuple<Property<?>, ?>> properties;

		public BlockPropertiesMaterial(Block block, List<Tuple<Property<?>, ?>> properties, boolean inclusive) {

			super(block, inclusive);
			this.properties = properties.stream();
		}

		@Override
		public boolean test(BlockState blockState) {

			if (!super.test(blockState))
				return false;

			Predicate<Tuple<Property<?>, ?>> test = v -> blockState.get(v.getA()) == v.getB();

			return inclusive ? properties.anyMatch(test) : properties.noneMatch(test);
		}
	}
}
