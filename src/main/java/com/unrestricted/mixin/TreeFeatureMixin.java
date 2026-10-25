package com.unrestricted.mixin;

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
        // Permite que la estructura del árbol pase por alto restricciones de bloques cercanos
        cir.setReturnValue(true);
    }
}
