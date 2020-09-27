package cofh.cofhworld.parser.generator.builders.base;

import cofh.cofhworld.data.shape.PlaneShape;
import cofh.cofhworld.data.shape.Shape2D;
import cofh.cofhworld.util.random.WeightedEnum;
import cofh.cofhworld.world.generator.WorldGen;

import java.util.Collections;

public abstract class BuilderShape<T extends WorldGen> extends BaseBuilder<T> {

	private static final Shape2D CIRCLE = new Shape2D(Collections.singletonList(new WeightedEnum<>(PlaneShape.CIRCLE)), Collections.emptyList(), Collections.emptyList());

	protected Shape2D shape = CIRCLE;

	public void setShape(Shape2D shape) {

		this.shape = shape;
	}

}
