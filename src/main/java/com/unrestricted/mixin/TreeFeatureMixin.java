
package com.unrestricted.mixin;

import com.unrestricted.TreePlacementRules;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeature;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TreeFeature.class)
public abstract class TreeFeatureMixin {

    /**
     * Impide que canReplace autorice bloques
     * fuera de nuestra politica conservadora.
     *
     * No fuerza el crecimiento.
     * No modifica bloques.
     */
    @Inject(
        method = "canReplace(Lnet/minecraft/world/TestableWorld;Lnet/minecraft/util/math/BlockPos;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void unrestricted$protectBlocks(
            TestableWorld world,
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir) {

        boolean allowed = world.testBlockState(
            pos,
            TreePlacementRules::canReplace
        );

        if (!allowed) {
            cir.setReturnValue(false);
        }
    }
}
