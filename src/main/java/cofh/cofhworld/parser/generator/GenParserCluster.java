package cofh.cofhworld.parser.generator;

import cofh.cofhworld.parser.Field.Type;
import cofh.cofhworld.parser.FieldBuilder;
import cofh.cofhworld.parser.generator.base.AbstractGenParserResource;
import cofh.cofhworld.parser.generator.builders.BuilderCluster;

public class GenParserCluster extends AbstractGenParserResource {

	private final boolean sparse;

	public GenParserCluster(boolean sparse) {

		this.sparse = sparse;
	}

	@Override
	public FieldBuilder getFields(FieldBuilder fields) {

		fields = super.getFields(fields);
		fields.setBuilder(() -> {
			BuilderCluster builder = new BuilderCluster();
			builder.setType(sparse ? BuilderCluster.Type.SPARSE : BuilderCluster.Type.TINY); // TODO: via config?
			return builder;
		});

		fields.addRequiredField("cluster-size", Type.NUMBER, BuilderCluster::setSize);

		return fields;
	}

}
