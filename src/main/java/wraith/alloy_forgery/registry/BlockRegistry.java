package wraith.alloy_forgery.registry;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.registry.Registry;
import wraith.alloy_forgery.blocks.ForgeControllerBlock;
import wraith.alloy_forgery.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class BlockRegistry {
    public static final HashMap<String, Block> BLOCKS = new HashMap<>();

    public static void loadBlocks() {
        BLOCKS.put("brick_forge_controller", new ForgeControllerBlock(FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES).strength(2.5f, 7f).sounds(BlockSoundGroup.STONE)));
        BLOCKS.put("stone_brick_forge_controller", new ForgeControllerBlock(FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES).strength(2.5f, 7f).sounds(BlockSoundGroup.STONE)));
        BLOCKS.put("blackstone_forge_controller", new ForgeControllerBlock(FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES).strength(3f, 8f).sounds(BlockSoundGroup.STONE)));
    }

    public static void registerBlocks() {
        for (Map.Entry<String, Block> entry : BLOCKS.entrySet()) {
            Registry.register(Registry.BLOCK, Utils.ID(entry.getKey()), entry.getValue());
        }
    }

}
