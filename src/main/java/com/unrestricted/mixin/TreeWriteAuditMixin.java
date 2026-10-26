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
 * Conservamos el mecanismo porque TreeFeatureMixin
 * depende del snapshot activo para proteger bloques
 * originales.
 *
 * Los logs quedan desactivados por defecto.
 */
@Mixin(value = TreeFeature.class, priority = 1100)
public abstract class TreeWriteAuditMixin {

    private static final ThreadLocal<TreeGenerationBuffer>
            CURRENT = new ThreadLocal<>();

    @Inject(
            method = "generate(Lnet/minecraft/world/gen/feature/util/FeatureContext;)Z",
            at = @At("HEAD")
    )
    private void unrestricted$begin(
            FeatureContext<TreeFeatureConfig> context,
            CallbackInfoReturnable<Boolean> cir) {

        TreeGenerationBuffer buffer = new TreeGenerationBuffer(
                context.getWorld(),
                context.getOrigin()
        );

        CURRENT.set(buffer);
        TreeGenerationBuffer.activate(buffer);
    }

    @Inject(
            method = "generate(Lnet/minecraft/world/gen/feature/util/FeatureContext;)Z",
            at = @At("RETURN")
    )
    private void unrestricted$finish(
            FeatureContext<TreeFeatureConfig> context,
            CallbackInfoReturnable<Boolean> cir) {

        TreeGenerationBuffer buffer = CURRENT.get();

        CURRENT.remove();
        TreeGenerationBuffer.clearActive();

        if (buffer != null) {
            buffer.printSummary(cir.getReturnValue());
        }
    }

    private static void unrestricted$record(
            ModifiableWorld world,
            BlockPos pos,
            BlockState state) {

        TreeGenerationBuffer buffer = CURRENT.get();

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
