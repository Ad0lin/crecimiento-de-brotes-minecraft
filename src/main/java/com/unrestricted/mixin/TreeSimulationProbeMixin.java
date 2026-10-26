
package com.unrestricted.mixin;

import com.unrestricted.TreeSimulationWorld;

import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TreeFeature.class)
public abstract class TreeSimulationProbeMixin {

    // Evita que la simulacion vuelva a iniciar
    // otra simulacion de forma recursiva.
    private static final ThreadLocal<Boolean> RUNNING =
            ThreadLocal.withInitial(() -> false);

    @Inject(
        method = "generate(Lnet/minecraft/world/gen/feature/util/FeatureContext;)Z",
        at = @At("HEAD")
    )
    private void unrestricted$probe(
            FeatureContext<TreeFeatureConfig> context,
            CallbackInfoReturnable<Boolean> cir) {

        if (RUNNING.get()) {
            return;
        }

        RUNNING.set(true);

        try {
            TreeSimulationWorld simulation =
                    new TreeSimulationWorld(
                        context.getWorld()
                    );

            // Usar un generador independiente:
            // no consumir el Random de Vanilla.
            Random probeRandom =
                    Random.create(987654321L);

            FeatureContext<TreeFeatureConfig> probeContext =
                    new FeatureContext<>(
                        context.getFeature(),
                        simulation.getWorld(),
                        context.getGenerator(),
                        probeRandom,
                        context.getOrigin(),
                        context.getConfig()
                    );

            System.out.println(
                "[Unrestricted Trees SIMULATION START]"
                + " | Origen: "
                + context.getOrigin().toShortString()
            );

            boolean generated =
                    ((TreeFeature) (Object) this)
                        .generate(probeContext);

            System.out.println(
                "[Unrestricted Trees SIMULATION RESULT]"
                + " | Resultado: " + generated
                + " | Escrituras virtuales: "
                + simulation.getTotalWrites()
                + " | Posiciones virtuales: "
                + simulation.getStagedBlocks().size()
            );

        } catch (RuntimeException exception) {

            // Fallo de la simulacion.
            // No cancelar el crecimiento Vanilla.
            System.out.println(
                "[Unrestricted Trees SIMULATION REJECTED]"
                + " | Tipo: "
                + exception.getClass().getSimpleName()
                + " | Motivo: "
                + exception.getMessage()
            );

        } finally {
            RUNNING.remove();
        }
    }
}
