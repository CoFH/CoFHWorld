package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.IGeneratorFieldRegistry;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderCluster;

public class GenParserCluster implements AbstractGenParserResource<BuilderCluster> {

	private final boolean sparse;

	public GenParserCluster(boolean sparse) {

		this.sparse = sparse;
	}

	@Override
	public void getFields(IGeneratorFieldRegistry<BuilderCluster> fields) {

		AbstractGenParserResource.super.getFields(fields);
		fields.setBuilder(() -> {
			BuilderCluster builder = new BuilderCluster();
			builder.setType(sparse ? BuilderCluster.Type.SPARSE : BuilderCluster.Type.TINY); // TODO: via config?
			return builder;
		});

		fields.addRequiredField("cluster-size", Type.NUMBER, BuilderCluster::setSize);
	}

}
