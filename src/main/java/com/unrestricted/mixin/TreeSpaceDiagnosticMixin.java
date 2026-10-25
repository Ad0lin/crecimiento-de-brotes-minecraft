
package com.unrestricted.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TreeFeature.class)
public abstract class TreeSpaceDiagnosticMixin {

    @Inject(
        method = "getTopPosition(Lnet/minecraft/world/TestableWorld;ILnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/gen/feature/TreeFeatureConfig;)I",
        at = @At("RETURN")
    )
    private void unrestricted$diagnoseSpace(
            TestableWorld world,
            int requestedHeight,
            BlockPos origin,
            TreeFeatureConfig config,
            CallbackInfoReturnable<Integer> cir) {

        int availableHeight = cir.getReturnValue();

        if (availableHeight < requestedHeight) {
            System.out.println(
                "[Unrestricted Trees] Crecimiento limitado"
                + " | Posicion: " + origin.toShortString()
                + " | Altura solicitada: " + requestedHeight
                + " | Altura disponible: " + availableHeight
            );
        }
    }
}
