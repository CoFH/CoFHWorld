package cofh.cofhworld.data.block;

import net.minecraft.block.BlockState;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Predicate;

public class MaterialPropertyMaterial extends Material {

	private final Property[] materials;
	private final boolean inclusive;

	public MaterialPropertyMaterial(boolean inclusive, String... properties) {

		this.inclusive = inclusive;
		this.materials = Arrays.stream(properties).map(v -> Property.valueOf(v.toUpperCase(Locale.US))).distinct().toArray(Property[]::new);
	}

	@Override
	public boolean test(final BlockState blockState) {

		final net.minecraft.block.material.Material material = blockState.getMaterial();

		return inclusive ? Arrays.stream(materials).allMatch(prop -> prop.test(material)) : Arrays.stream(materials).noneMatch(prop -> prop.test(material));
	}

	public static enum Property {
		BLOCKS_MOVEMENT(net.minecraft.block.material.Material::blocksMovement),
		FLAMMABLE(net.minecraft.block.material.Material::isFlammable),
		LIQUID(net.minecraft.block.material.Material::isLiquid),
		OPAQUE(net.minecraft.block.material.Material::isOpaque),
		REPLACEABLE(net.minecraft.block.material.Material::isReplaceable),
		SOLID(net.minecraft.block.material.Material::isSolid);

		private Property(Predicate<net.minecraft.block.material.Material> func) {

			test = func;
		}

		public final Predicate<net.minecraft.block.material.Material> test;

		final public boolean test(net.minecraft.block.material.Material material) {

			return test.test(material);
		}
	}
}
