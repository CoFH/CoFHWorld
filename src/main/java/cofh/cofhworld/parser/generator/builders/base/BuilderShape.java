package cofh.cofhworld.parser.generator.builders.base;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.shape.PlaneShape;
import cofh.cofhworld.data.shape.Shape2D;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.util.random.WeightedEnum;

import java.util.Collections;
import java.util.List;

public abstract class BuilderShape<T> extends BaseBuilder<T> {

	private static final Shape2D CIRCLE = new Shape2D(Collections.singletonList(new WeightedEnum<>(PlaneShape.CIRCLE)), Collections.emptyList(), Collections.emptyList());

	protected Shape2D shape = CIRCLE;

	public BuilderShape(List<WeightedBlock> resource, List<Material> material) {

		super(resource, material);
	}

	public void setShape(Shape2D shape) {

		this.shape = shape;
	}

}
