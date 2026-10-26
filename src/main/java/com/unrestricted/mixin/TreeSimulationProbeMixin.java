
package com.unrestricted.mixin;

import com.unrestricted.TreeSimulationWorld;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(TreeFeature.class)
public abstract class TreeSimulationProbeMixin {

    // Una sola prueba por inicio del juego.
    private static final AtomicBoolean TESTED =
            new AtomicBoolean(false);

    @Inject(
        method = "generate(Lnet/minecraft/world/gen/feature/util/FeatureContext;)Z",
        at = @At("HEAD")
    )
    private void unrestricted$smokeTest(
            FeatureContext<TreeFeatureConfig> context,
            CallbackInfoReturnable<Boolean> cir) {

        if (!TESTED.compareAndSet(false, true)) {
            return;
        }

        try {
            BlockPos testPos =
                    context.getOrigin().up(2);

            BlockState originalBefore =
                    context.getWorld()
                        .getBlockState(testPos);

            TreeSimulationWorld simulation =
                    new TreeSimulationWorld(
                        context.getWorld()
                    );

            boolean staged =
                    simulation.getWorld().setBlockState(
                        testPos,
                        Blocks.OAK_LOG.getDefaultState(),
                        3
                    );

            BlockState virtualAfter =
                    simulation.getWorld()
                        .getBlockState(testPos);

            BlockState realAfter =
                    context.getWorld()
                        .getBlockState(testPos);

            boolean virtualCorrect =
                    virtualAfter.isOf(Blocks.OAK_LOG);

            boolean realUnchanged =
                    originalBefore.equals(realAfter);

            boolean passed =
                    staged
                    && virtualCorrect
                    && realUnchanged
                    && simulation.getTotalWrites() == 1;

            System.out.println(
                "[Unrestricted Trees SMOKE TEST]"
                + " | Resultado: "
                + (passed ? "PASS" : "FAIL")
                + " | Posicion: "
                + testPos.toShortString()
                + " | Escrituras virtuales: "
                + simulation.getTotalWrites()
                + " | Virtual correcto: "
                + virtualCorrect
                + " | Mundo real intacto: "
                + realUnchanged
            );

        } catch (Throwable exception) {

            // Esta prueba no debe provocar
            // un crash ni cancelar el arbol Vanilla.
            System.out.println(
                "[Unrestricted Trees SMOKE TEST ERROR]"
                + " | Tipo: "
                + exception.getClass().getName()
                + " | Motivo: "
                + exception.getMessage()
            );
        }
    }
}
