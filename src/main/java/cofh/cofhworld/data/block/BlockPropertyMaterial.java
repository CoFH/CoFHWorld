package cofh.cofhworld.data.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;

import java.util.Map;

public class BlockPropertyMaterial extends BlockMaterial {

	final private static IllegalStateException CONTROL_FLOW = new IllegalStateException();


	final private Map<IProperty<?>, Object> properties;

	public BlockPropertyMaterial(Block block, Map<IProperty<?>, Object> properties, boolean inclusive) {

		super(block, inclusive);
		this.properties = properties;
	}

	@Override
	public boolean test(BlockState blockState) {

		if (!super.test(blockState))
			return false;

		try {
			properties.forEach((property, value) -> {
				if (value == blockState.get(property)) {
					if (!inclusive) {
						throw CONTROL_FLOW;
					}
				} else if (inclusive) {
					throw CONTROL_FLOW;
				}
			});
		} catch (IllegalArgumentException e) {
			// yeah, exceptions for control flow are terrible. easy though!
			return !inclusive;
		}

		return inclusive;
	}
}
