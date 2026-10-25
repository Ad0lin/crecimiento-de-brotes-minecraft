package com.unrestricted.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TreeFeature.class)
public class TreeFeatureMixin {

    @Inject(
        method = "canReplace",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private static void forceReplaceBlocks(TestableWorld world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (world instanceof StructureWorldAccess structureWorld) {
            BlockState state = structureWorld.getBlockState(pos);
            
            // Protegemos el suelo para que NO destruya la tierra de la base ni rompa la lógica del árbol
            if (state.isOf(Blocks.DIRT) || state.isOf(Blocks.GRASS_BLOCK) || 
                state.isOf(Blocks.PODZOL) || state.isOf(Blocks.MYCELIUM) || 
                state.isOf(Blocks.ROOTED_DIRT) || state.isOf(Blocks.MUD) ||
                state.isOf(Blocks.FARMLAND) || state.isOf(Blocks.COARSE_DIRT)) {
                return; // Deja que Minecraft maneje el suelo normalmente
            }
        }

        // Permite reemplazar paredes, obsidianas u otros bloques circundantes para el crecimiento masivo
        cir.setReturnValue(true);
    }
}
