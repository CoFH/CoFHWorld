package cofh.cofhworld.parser.distribution.builders;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.parser.distribution.builders.base.BuilderGenerator;
import cofh.cofhworld.world.distribution.DistributionUnderMaterial;

import javax.annotation.Nonnull;
import java.util.List;

public class BuilderUnderMaterial extends BuilderGenerator<DistributionUnderMaterial> {

	protected List<Material> surface, material;

	public void setSurface(List<Material> surface) {

		this.surface = surface;
	}

	public void setMaterial(List<Material> material) {

		this.material = material;
	}

	@Nonnull
	@Override
	public DistributionUnderMaterial build() {

		return new DistributionUnderMaterial(featureName, generator, material, surface, clusterCount, retrogen);
	}
}
