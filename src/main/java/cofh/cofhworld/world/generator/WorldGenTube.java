package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.CurveShape;
import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.random.UniformRandomProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.world.IWorld;

import java.util.List;
import java.util.Random;

/**
 * @deprecated TODO: implement
 */
@Deprecated
public class WorldGenTube extends WorldGen {

	private final List<WeightedBlock> core, hull;
	private final WeightedBlock[] genBlock;

	private CurveShape curve = new CurveShape.DIRECTED(null, new ConstantProvider(0));

	private INumberProvider radius = new ConstantProvider(4), hullThickness = new ConstantProvider(1);
	private INumberProvider length = new UniformRandomProvider(20, 40);

	private boolean taper = true;

	public WorldGenTube(List<WeightedBlock> innerResource, List<WeightedBlock> outerResource, List<WeightedBlock> block) {

		core = innerResource;
		hull = outerResource;
		genBlock = block.toArray(new WeightedBlock[0]);
	}

	@Override
	public boolean generate(IWorld worldIn, Random rand, final DataHolder data) {

		return false;
	}
}
