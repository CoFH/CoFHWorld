package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.parser.IBuilder.BuilderField.Type;
import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import cofh.cofhworld.parser.IDistributionParser;
import cofh.cofhworld.parser.distribution.builders.BuilderReplace;
import cofh.cofhworld.parser.distribution.builders.base.BaseBuilder;
import cofh.cofhworld.world.distribution.DistributionReplace;

public class DistParserReplace implements IDistributionParser<DistributionReplace, BuilderReplace> {

    @Override
    public void getFields(IBuilderFieldRegistry<DistributionReplace, BuilderReplace> fields) {

        fields.setConstructor(BuilderReplace::new);

        fields.addOptionalField("retrogen", Type.RAW_BOOLEAN, BaseBuilder::setRetrogen);

        fields.addRequiredField("resource", Type.BLOCK_LIST, BuilderReplace::setResource, "block");
        fields.addRequiredField("material", Type.MATERIAL_LIST, BuilderReplace::setMaterial);
    }
}
