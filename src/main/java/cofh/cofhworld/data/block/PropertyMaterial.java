package cofh.cofhworld.data.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;

import java.util.Map;

public class PropertyMaterial extends Material {

	final private static IllegalStateException CONTROL_FLOW = new IllegalStateException();

	final private Map<String, String> properties;
	final private boolean exclusive;

	public PropertyMaterial(Map<String, String> properties, boolean inclusive) {

		this.properties = properties;
		this.exclusive = !inclusive;
	}

	@Override
	public boolean test(BlockState blockState) {

		StateContainer<Block, BlockState> container = blockState.getBlock().getStateContainer();

		try {
			properties.forEach((property, value) -> {
				IProperty<?> prop = container.getProperty(property);
				if (prop == null) {
					if (value != null & !exclusive)
						throw CONTROL_FLOW;
				} else if (value != null) {
					if (prop.parseValue(value).isPresent() == exclusive)
						throw CONTROL_FLOW;
				} else if (exclusive) {
					throw CONTROL_FLOW;
				}
			});
		} catch (IllegalArgumentException e) {
			// yeah, exceptions for control flow are terrible. easy though!
			return !exclusive;
		}

		return exclusive;
	}
}
