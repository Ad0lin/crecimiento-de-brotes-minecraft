
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

import java.net.URL;
import java.security.CodeSource;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(TreeFeature.class)
public abstract class TreeSimulationProbeMixin {

    private static final String DIAGNOSTIC_VERSION =
            "UT-DIAG-2026-10-26-A";

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

        System.out.println(
            "[Unrestricted Trees DIAGNOSTIC START]"
            + " | Version: " + DIAGNOSTIC_VERSION
            + " | Origin: "
            + context.getOrigin().toShortString()
        );

        try {
            CodeSource source =
                    TreeSimulationWorld.class
                            .getProtectionDomain()
                            .getCodeSource();

            URL location =
                    source == null
                            ? null
                            : source.getLocation();

            System.out.println(
                "[Unrestricted Trees CLASS SOURCE]"
                + " | Location: " + location
                + " | ClassLoader: "
                + TreeSimulationWorld.class
                        .getClassLoader()
        );

            BlockPos testPos =
                    context.getOrigin().up(2);

            BlockState originalBefore =
                    context.getWorld()
                            .getBlockState(testPos);

            TreeSimulationWorld simulation =
                    new TreeSimulationWorld(
                        context.getWorld()
                    );

            System.out.println(
                "[Unrestricted Trees DIAGNOSTIC STEP]"
                + " | Simulation created"
            );

            boolean staged =
                    simulation.getWorld()
                            .setBlockState(
                                testPos,
                                Blocks.OAK_LOG.getDefaultState(),
                                3
                            );

            System.out.println(
                "[Unrestricted Trees DIAGNOSTIC STEP]"
                + " | Virtual write returned: "
                + staged
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
                + " | Version: " + DIAGNOSTIC_VERSION
                + " | Resultado: "
                + (passed ? "PASS" : "FAIL")
                + " | Escrituras virtuales: "
                + simulation.getTotalWrites()
                + " | Virtual correcto: "
                + virtualCorrect
                + " | Mundo real intacto: "
                + realUnchanged
            );

        } catch (Throwable exception) {

            System.out.println(
                "[Unrestricted Trees DIAGNOSTIC ERROR]"
                + " | Version: " + DIAGNOSTIC_VERSION
                + " | Tipo: "
                + exception.getClass().getName()
                + " | Motivo: "
                + exception.getMessage()
            );

            // Registrar la traza completa para
            // localizar la clase y metodo exactos.
            exception.printStackTrace(System.out);

        } finally {

            System.out.println(
                "[Unrestricted Trees DIAGNOSTIC END]"
                + " | Version: " + DIAGNOSTIC_VERSION
            );
        }
    }
}
