
package com.unrestricted.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(TreeFeature.class)
public abstract class TreeSpaceDiagnosticMixin {

    private static final Set<String> INSPECTED =
            ConcurrentHashMap.newKeySet();

    @Inject(
        method = "getTopPosition(Lnet/minecraft/world/TestableWorld;ILnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/gen/feature/TreeFeatureConfig;)I",
        at = @At("RETURN")
    )
    private void unrestricted$inspectObstacles(
            TestableWorld world,
            int requestedHeight,
            BlockPos origin,
            TreeFeatureConfig config,
            CallbackInfoReturnable<Integer> cir) {

        int result = cir.getReturnValue();

        if (result >= requestedHeight) {
            return;
        }

        String key = origin.toShortString();

        // Evitar repetir el informe por cada polvo de hueso
        if (!INSPECTED.add(key)) {
            return;
        }

        System.out.println(
            "[Unrestricted Trees] INSPECCION INICIADA"
            + " | Posicion: " + key
            + " | Altura solicitada: " + requestedHeight
            + " | Resultado Vanilla: " + result
        );

        // Examinar el area alrededor de los cuatro brotes.
        // No se modifica ningun bloque.
        for (int y = 0; y <= 3; y++) {
            for (int x = -2; x <= 3; x++) {
                for (int z = -2; z <= 3; z++) {

                    BlockPos target = origin.add(x, y, z);

                    world.testBlockState(target, state -> {
                        if (!state.isAir()) {
                            System.out.println(
                                "[Unrestricted Trees] BLOQUE"
                                + " | Relativo: "
                                + x + "," + y + "," + z
                                + " | Posicion: "
                                + target.toShortString()
                                + " | Tipo: "
                                + state.getBlock().getTranslationKey()
                            );
                        }
                        return true;
                    });
                }
            }
        }

        System.out.println(
            "[Unrestricted Trees] INSPECCION FINALIZADA"
        );
    }
}
