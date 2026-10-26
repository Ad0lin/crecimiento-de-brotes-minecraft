
package com.unrestricted.mixin;

import com.unrestricted.TreePlacementRules;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(TreeFeature.class)
public abstract class TreeFeatureMixin {

    private static final AtomicInteger SPACE_TEST_COUNT =
            new AtomicInteger(0);

    private static final int MAX_SPACE_LOGS = 30;

    /**
     * Conserva la proteccion existente.
     *
     * No autoriza reemplazar bloques protegidos.
     * No fuerza el crecimiento.
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

    /**
     * Diagnostico de espacio disponible.
     *
     * No altera ningun resultado de Vanilla.
     */
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

        int testNumber =
                SPACE_TEST_COUNT.incrementAndGet();

        if (testNumber > MAX_SPACE_LOGS) {
            return;
        }

        int availableHeight = cir.getReturnValue();

        System.out.println(
            "[Unrestricted Trees SPACE TEST]"
            + " | Prueba: " + testNumber
            + " | Origen: " + origin.toShortString()
            + " | Altura solicitada: " + requestedHeight
            + " | Altura disponible: " + availableHeight
            + " | Diferencia: "
            + (requestedHeight - availableHeight)
        );
    }
}
