package com.unrestricted.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
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
        // 1. Proteger el suelo base (la tierra y césped de tu piso se quedan 100% intactos)
        boolean isSoil = world.testBlockState(pos, state -> 
            state.isOf(Blocks.DIRT) || state.isOf(Blocks.GRASS_BLOCK) || 
            state.isOf(Blocks.PODZOL) || state.isOf(Blocks.MYCELIUM) || 
            state.isOf(Blocks.ROOTED_DIRT) || state.isOf(Blocks.MUD) ||
            state.isOf(Blocks.FARMLAND) || state.isOf(Blocks.COARSE_DIRT)
        );
        if (isSoil) {
            return;
        }

        // 2. Proteger la Redstone, Dispensadores y Automatización
        boolean isMachine = world.testBlockState(pos, state -> 
            state.isOf(Blocks.DISPENSER) || state.isOf(Blocks.DROPPER) || 
            state.isOf(Blocks.OBSERVER) || state.isOf(Blocks.HOPPER) ||
            state.isOf(Blocks.REPEATER) || state.isOf(Blocks.COMPARATOR) || 
            state.isOf(Blocks.LEVER) || state.isOf(Blocks.TRIPWIRE) ||
            state.isOf(Blocks.DAYLIGHT_DETECTOR) || state.isOf(Blocks.TARGET) ||
            state.getBlock() instanceof RedstoneWireBlock ||
            state.getBlock() instanceof RedstoneTorchBlock
        );
        if (isMachine) {
            return;
        }

        // 3. Permitir el crecimiento libre atravesando techos y paredes para tu árbol-casa
        cir.setReturnValue(true);
    }
}
