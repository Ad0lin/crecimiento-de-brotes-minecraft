package com.unrestricted.mixin;

import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.TestableWorld;
import net.minecraft.util.math.BlockPos;
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
        cir.setReturnValue(true);
    }
}
