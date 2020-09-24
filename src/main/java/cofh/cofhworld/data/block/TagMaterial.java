package cofh.cofhworld.data.block;

import cofh.cofhworld.util.LinkedHashList;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;

public abstract class TagMaterial extends Material {

	@Nullable
	public static Material of(Collection<ResourceLocation> tags, boolean inclusive) {

		switch (tags.size()) {
			case 0:
				return null;
			case 1:
				return new BlockTagMaterial(tags.iterator().next(), inclusive);
			default:
				return new BlockTagsMaterial(new LinkedHashList<>(tags), inclusive);
		}
	}

	final protected boolean inclusive;

	private TagMaterial(boolean inclusive) {

		this.inclusive = inclusive;
	}

	final private static class BlockTagMaterial extends TagMaterial {

		private final ResourceLocation tag;

		public BlockTagMaterial(ResourceLocation tag, boolean inclusive) {

			super(inclusive);
			this.tag = tag;
		}

		@Override
		public boolean test(BlockState blockState) {

			return inclusive == blockState.getBlock().getTags().contains(tag);
		}
	}

	final private static class BlockTagsMaterial extends TagMaterial {

		private final LinkedHashList<ResourceLocation> tags;

		public BlockTagsMaterial(LinkedHashList<ResourceLocation> tags, boolean inclusive) {

			super(inclusive);
			this.tags = tags;
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
