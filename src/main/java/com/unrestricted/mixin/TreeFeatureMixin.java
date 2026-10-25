package com.unrestricted.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.RedstoneWireBlock;
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
            
            // 1. Protegemos el suelo para que el piso quede 100% intacto
            if (state.isOf(Blocks.DIRT) || state.isOf(Blocks.GRASS_BLOCK) || 
                state.isOf(Blocks.PODZOL) || state.isOf(Blocks.MYCELIUM) || 
                state.isOf(Blocks.ROOTED_DIRT) || state.isOf(Blocks.MUD) ||
                state.isOf(Blocks.FARMLAND) || state.isOf(Blocks.COARSE_DIRT)) {
                return; 
            }
            
            // 2. Protegemos toda la Redstone, Dispensadores, Observadores y Automatización
            if (state.isOf(Blocks.DISPENSER) || state.isOf(Blocks.DROPPER) || 
                state.isOf(Blocks.OBSERVER) || state.isOf(Blocks.HOPPER) ||
                state.isOf(Blocks.REPEATER) || state.isOf(Blocks.COMPARATOR) || 
                state.isOf(Blocks.LEVER) || state.isOf(Blocks.TRIPWIRE) ||
                state.isOf(Blocks.DAYLIGHT_DETECTOR) || state.isOf(Blocks.TARGET) ||
                state.getBlock() instanceof RedstoneWireBlock ||
                state.getBlock() instanceof RedstoneTorchBlock ||
                state.getBlock() instanceof ButtonBlock) {
                return; // ¡No tocar la maquinaria ni la redstone! El árbol crecerá alrededor o se detendrá si choca.
            }
        }

        // 3. Para paredes, techos y bloques estructurales del árbol-casa, permitimos el crecimiento libre
        cir.setReturnValue(true);
    }
}
