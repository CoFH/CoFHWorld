package cofh.cofhworld.data.block;

import cofh.cofhworld.util.LinkedHashList;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;

public class TagMaterial extends Material {

	final private static TagMaterial INSTANCE = new TagMaterial();

	public static TagMaterial of(Collection<ResourceLocation> tags, boolean inclusive) {

		switch (tags.size()) {
			case 0:
				return INSTANCE;
			case 1:
				return new BlockTagMaterial(tags.iterator().next(), inclusive);
			default:
				return new BlockTagsMaterial(new LinkedHashList<ResourceLocation>(tags), inclusive);
		}
	}

	private TagMaterial() {

	}

	@Override
	public boolean test(BlockState blockState) {

		return false;
	}

	final private static class BlockTagMaterial extends TagMaterial {

		private final ResourceLocation tag;
		final private boolean inclusive;

		public BlockTagMaterial(ResourceLocation tag, boolean inclusive) {

			this.tag = tag;
			this.inclusive = inclusive;
		}

		@Override
		public boolean test(BlockState blockState) {

			return inclusive == blockState.getBlock().getTags().contains(tag);
		}
	}

	final private static class BlockTagsMaterial extends TagMaterial {

		private final LinkedHashList<ResourceLocation> tags;
		final private boolean inclusive;

		public BlockTagsMaterial(LinkedHashList<ResourceLocation> tags, boolean inclusive) {

			this.tags = tags;
			this.inclusive = inclusive;
		}

		@Override
		public boolean test(BlockState blockState) {

			LinkedHashList<ResourceLocation> tags = this.tags;
			for (ResourceLocation test : blockState.getBlock().getTags())
				if (tags.contains(test))
					return inclusive;
			return inclusive;
		}
	}
}
