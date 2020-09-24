package cofh.cofhworld.data.block;

import net.minecraft.block.BlockState;

import java.util.function.Predicate;

public abstract class Material implements Predicate<BlockState> {

	final public static Material ALWAYS = new Material() {

		@Override
		public boolean test(BlockState blockState) {

			return true;
		}
	};

	@Override
	public abstract boolean test(BlockState blockState);

	@Override
	final public Material and(Predicate<? super BlockState> other) {

		return new Material() {

			@Override
			public boolean test(BlockState blockState) {

				return Material.this.test(blockState) && other.test(blockState);
			}
		};
	}

	@Override
	final public Material negate() {

		return new Material() {

			@Override
			public boolean test(BlockState blockState) {

				return !Material.this.test(blockState);
			}
		};
	}

	@Override
	final public Material or(Predicate<? super BlockState> other) {


		return new Material() {

			@Override
			public boolean test(BlockState blockState) {

				return Material.this.test(blockState) || other.test(blockState);
			}
		};
	}
}
