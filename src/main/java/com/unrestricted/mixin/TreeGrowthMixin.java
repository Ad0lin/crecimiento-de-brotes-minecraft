package com.unrestricted.mixin;

import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({SaplingBlock.class, MushroomPlantBlock.class, FungusBlock.class})
public class TreeGrowthMixin {

    // Salta la comprobación de bloques adyacentes al intentar crecer o validarse
    @Inject(method = "canPlaceAt", at = @At("HEAD"), cancellable = true)
    private void allowGrowthAnywhere(BlockState state, BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // Permite que el brote/hongo viva y crezca sin importar lo que tenga al lado (norte, oeste, etc.)
        cir.setReturnValue(true);
    }
}
