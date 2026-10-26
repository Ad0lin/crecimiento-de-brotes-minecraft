
package com.unrestricted.mixin;

import com.unrestricted.TreePlacementRules;

import net.minecraft.class_2338;
import net.minecraft.class_2944;
import net.minecraft.class_3746;
import net.minecraft.class_4643;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(class_2944.class)
public abstract class TreeFeatureMixin {

    private static final AtomicInteger SPACE_TEST_COUNT =
            new AtomicInteger(0);

    private static final int MAX_SPACE_LOGS = 30;

    /**
     * Conserva la proteccion existente.
     *
     * No autoriza que un arbol reemplace
     * bloques que nuestra politica protege.
     */
    @Inject(
        method = "canReplace(Lnet/minecraft/world/TestableWorld;Lnet/minecraft/util/math/BlockPos;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void unrestricted$protectBlocks(
            class_3746 world,
            class_2338 pos,
            CallbackInfoReturnable<Boolean> cir) {

        boolean allowed = world.method_16358(
            pos,
            TreePlacementRules::canReplace
        );

        if (!allowed) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Diagnostico de altura disponible.
     *
     * No altera el resultado de Vanilla.
     * Solo registra los valores calculados.
     */
    @Inject(
        method = "getTopPosition(Lnet/minecraft/world/TestableWorld;ILnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/gen/feature/TreeFeatureConfig;)I",
        at = @At("RETURN")
    )
    private void unrestricted$diagnoseSpace(
            class_3746 world,
            int requestedHeight,
            class_2338 origin,
            class_4643 config,
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
