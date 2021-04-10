package cofh.cofhworld.data.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FluidMaterial extends Material {

	private final List<ResourceLocation> fluids;
	private final boolean inclusive;

	public FluidMaterial(boolean inclusive, String... fluids) {

		this.fluids = Arrays.stream(fluids).map(ResourceLocation::new).distinct().collect(Collectors.toList());

		this.inclusive = inclusive;
	}

	@Override
	public boolean test(BlockState blockState) {

		ResourceLocation name = blockState.getFluidState().getFluid().getRegistryName();
		if (name == null) {
			return false;
		}

		return inclusive == fluids.contains(name);
	}
}
