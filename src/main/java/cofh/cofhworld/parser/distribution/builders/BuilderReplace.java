package cofh.cofhworld.parser.distribution.builders;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.parser.distribution.builders.base.BaseBuilder;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.distribution.DistributionReplace;

import javax.annotation.Nonnull;
import java.util.List;

public class BuilderReplace extends BaseBuilder<DistributionReplace> {

	protected List<WeightedBlock> resource;
	protected List<Material> material;

	public void setResource(List<WeightedBlock> resource) {

		this.resource = resource;
	}

	public void setMaterial(List<Material> material) {

		this.material = material;
	}

	@Nonnull
	@Override
	public DistributionReplace build() {

		return new DistributionReplace(featureName, retrogen, material, resource);
	}
}
