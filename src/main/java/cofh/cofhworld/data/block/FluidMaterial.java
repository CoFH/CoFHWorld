package cofh.cofhworld.data.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.stream.Stream;

public class FluidMaterial extends Material {

	private final Stream<ResourceLocation> fluids;
	private final boolean inclusive;

	public FluidMaterial(boolean inclusive, String... fluids) {

		this.fluids = Arrays.stream(fluids).map(ResourceLocation::new);

		this.inclusive = inclusive;
	}

	@Override
	public boolean test(BlockState blockState) {

		ResourceLocation name = blockState.getFluidState().getFluid().getRegistryName();
		if (name == null) {
			return false;
		}

		return inclusive ? fluids.anyMatch(name::equals) : fluids.noneMatch(name::equals);
	}
}
