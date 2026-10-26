
package com.unrestricted.mixin;

import com.unrestricted.TreeGenerationBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ModifiableWorld;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Auditoria de intentos de escritura de TreeFeature.
 *
 * La prioridad 1100 permite registrar los intentos
 * antes de que otros Mixins de prioridad 1000
 * puedan cancelarlos.
 *
 * IMPORTANTE: los registros NO garantizan
 * que la escritura se haya aplicado al mundo.
 */
@Mixin(value = TreeFeature.class, priority = 1100)
public abstract class TreeWriteAuditMixin {

    @Inject(
        method = "generate(Lnet/minecraft/world/gen/feature/util/FeatureContext;)Z",
        at = @At("HEAD")
    )
    private void unrestricted$begin(
            FeatureContext<TreeFeatureConfig> context,
            CallbackInfoReturnable<Boolean> cir) {

        TreeGenerationBuffer.activate(
            new TreeGenerationBuffer(
                context.getWorld(),
                context.getOrigin()
            )
        );
    }

    @Inject(
        method = "generate(Lnet/minecraft/world/gen/feature/util/FeatureContext;)Z",
        at = @At("RETURN")
    )
    private void unrestricted$finish(
            FeatureContext<TreeFeatureConfig> context,
            CallbackInfoReturnable<Boolean> cir) {

        TreeGenerationBuffer buffer = TreeGenerationBuffer.active();

        TreeGenerationBuffer.clearActive();

        if (buffer != null) {
            buffer.printSummary(cir.getReturnValue());
        }
    }

    private static void unrestricted$record(
            ModifiableWorld world,
            BlockPos pos,
            BlockState state) {

        TreeGenerationBuffer buffer = TreeGenerationBuffer.active();

        if (buffer == null) {
            return;
        }

        if (world instanceof WorldAccess access) {
            buffer.record(access, pos, state);
        }
    }

    @Inject(
        method = "setBlockState(Lnet/minecraft/world/ModifiableWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V",
        at = @At("HEAD")
    )
    private void unrestricted$recordNormal(
            ModifiableWorld world,
            BlockPos pos,
            BlockState state,
            CallbackInfo ci) {

        unrestricted$record(world, pos, state);
    }

    @Inject(
        method = "setBlockStateWithoutUpdatingNeighbors(Lnet/minecraft/world/ModifiableWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V",
        at = @At("HEAD")
    )
    private static void unrestricted$recordSilent(
            ModifiableWorld world,
            BlockPos pos,
            BlockState state,
            CallbackInfo ci) {

        unrestricted$record(world, pos, state);
    }
}
