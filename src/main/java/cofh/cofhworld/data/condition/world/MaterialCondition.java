package cofh.cofhworld.data.condition.world;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.world.generator.WorldGen;
import net.minecraft.world.IWorldReader;

import java.util.List;
import java.util.Random;

public class MaterialCondition implements ICondition {

	final private Material[] material;

	public MaterialCondition(List<Material> material) {

		this.material = material.toArray(new Material[0]);
	}

	@Override
	public boolean checkCondition(IWorldReader world, Random rand, DataHolder data) {

		return WorldGen.canGenerateInBlock(world, data.getPosition(), material);
	}
}
