package cofh.cofhworld.parser.distribution.builders;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.parser.distribution.builders.base.BuilderGenerator;
import cofh.cofhworld.world.distribution.DistributionSurface;
import cofh.cofhworld.world.distribution.DistributionTopBlock;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class BuilderSurface extends BuilderGenerator<DistributionSurface> {

	protected List<Material> material = Collections.emptyList();
	protected boolean topBlock;

	public void setMaterial(List<Material> material) {

		this.material = material;
	}

	public void setTopBlock(boolean topBlock) {

		this.topBlock = topBlock;
	}

	@Nonnull
	@Override
	public DistributionSurface build() {

		if (topBlock) {
			return new DistributionTopBlock(featureName, generator, material, clusterCount, retrogen);
		} else {
			return new DistributionSurface(featureName, generator, material, clusterCount, retrogen);
		}
	}
}
