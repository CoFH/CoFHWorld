package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderCluster;
import cofh.cofhworld.world.generator.WorldGenCluster;

public class GenParserCluster implements AbstractGenParserResource<WorldGenCluster, BuilderCluster> {

	private final boolean sparse;

	public GenParserCluster(boolean sparse) {

		this.sparse = sparse;
	}

	@Override
	public void getFields(IBuilderFieldRegistry<WorldGenCluster, BuilderCluster> fields) {

		AbstractGenParserResource.super.getFields(fields);
		fields.setConstructor(() -> {
			BuilderCluster builder = new BuilderCluster();
			builder.setType(sparse ? BuilderCluster.Type.SPARSE : BuilderCluster.Type.TINY); // TODO: via config?
			return builder;
		});

		fields.addRequiredField("cluster-size", Type.NUMBER, BuilderCluster::setSize);
	}

}
