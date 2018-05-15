package cofh.cofhworld.world.generator;

import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.util.WeightedRandomNBTTag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;

import java.util.List;
import java.util.Random;

public class WorldGenStructure extends WorldGenerator {

	private final WeightedRandomBlock[] material;
	private final Template template = new Template();

	public WorldGenStructure(List<WeightedRandomNBTTag> templates, List<WeightedRandomBlock> mat) {

		material = mat.toArray(new WeightedRandomBlock[mat.size()]);
		template.read((NBTTagCompound) templates.get(0).tag);
	}

	@Override
	public boolean generate(World world, Random random, BlockPos pos) {

		Rotation[] arotation = Rotation.values();
		Rotation rotation = arotation[random.nextInt(arotation.length)];
		Mirror[] amirror = Mirror.values();
		Mirror mirror = amirror[random.nextInt(amirror.length)];

		PlacementSettings placementsettings = (new PlacementSettings()).setRotation(rotation).setMirror(mirror).setRandom(random);

		BlockPos blockpos = template.transformedSize(rotation);
		int j = 0;
		int k = 0;
		int l = 256;

		for (int i1 = 0; i1 < blockpos.getX(); ++i1) {
			for (int j1 = 0; j1 < blockpos.getZ(); ++j1) {
				l = Math.min(l, world.getHeight(pos.getX() + i1 + j, pos.getZ() + j1 + k));
			}
		}

		BlockPos blockpos1 = template.getZeroPositionWithTransform(pos.add(j, l, k), mirror, rotation);
		placementsettings.setIntegrity(0.9F);
		template.addBlocksToWorld(world, blockpos1, placementsettings, 20);


		return false;
	}

}
