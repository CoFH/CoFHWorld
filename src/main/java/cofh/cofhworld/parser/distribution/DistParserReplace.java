package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.parser.IDistributionParser;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.IConfigurableFeatureGenerator;
import cofh.cofhworld.world.distribution.DistributionReplace;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class DistParserReplace implements IDistributionParser {

    private final String[] FIELDS = new String[] { "block", "replacement" };

    @Override
    public String[] getRequiredFields() {
        return FIELDS;
    }

    @Nonnull
    @Override
    public IConfigurableFeatureGenerator getFeature(String featureName, Config genObject, boolean retrogen, Logger log) throws InvalidDistributionException {

        List<WeightedBlock> blockstates = new ArrayList<>();
        if (!BlockData.parseBlockList(genObject.getValue("block"), blockstates)) {
            throw new InvalidDistributionException("`block` not valid", genObject.origin());
        }

        List<Material> replacement = new ArrayList<>();
        if (!BlockData.parseMaterialList(genObject.getValue("material"), replacement)) {
            throw new InvalidDistributionException("`material` not valid", genObject.origin());
        }

        return new DistributionReplace(featureName, retrogen, replacement, blockstates);
    }
}
