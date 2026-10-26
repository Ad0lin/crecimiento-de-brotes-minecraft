
package com.unrestricted.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ModifiableWorld;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.TreeFeature;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(TreeFeature.class)
public abstract class TreeWriteAuditMixin {

    private static final AtomicInteger LOG_COUNT =
            new AtomicInteger();

    private static final int MAX_LOGS = 100;

    private static void unrestricted$audit(
            String method,
            ModifiableWorld world,
            BlockPos pos,
            BlockState proposed) {

        if (!(world instanceof WorldAccess access)) {
            return;
        }

        if (LOG_COUNT.get() >= MAX_LOGS) {
            return;
        }

        BlockState existing = access.getBlockState(pos);

        // Solo registrar posiciones que ya tienen
        // un bloque, para identificar sobrescrituras.
        if (existing.isAir()) {
            return;
        }

        if (LOG_COUNT.getAndIncrement() >= MAX_LOGS) {
            return;
        }

        System.out.println(
            "[Unrestricted Trees AUDIT]"
            + " | Metodo: " + method
            + " | Posicion: " + pos.toShortString()
            + " | Existente: "
            + existing.getBlock().getTranslationKey()
            + " | Propuesto: "
            + proposed.getBlock().getTranslationKey()
        );
    }

    @Inject(
        method = "setBlockState(Lnet/minecraft/world/ModifiableWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V",
        at = @At("HEAD")
    )
    private void unrestricted$auditNormalWrite(
            ModifiableWorld world,
            BlockPos pos,
            BlockState state,
            CallbackInfo ci) {

        unrestricted$audit(
            "setBlockState",
            world,
            pos,
            state
        );
    }

    @Inject(
        method = "setBlockStateWithoutUpdatingNeighbors(Lnet/minecraft/world/ModifiableWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V",
        at = @At("HEAD")
    )
    private static void unrestricted$auditSilentWrite(
            ModifiableWorld world,
            BlockPos pos,
            BlockState state,
            CallbackInfo ci) {

        unrestricted$audit(
            "setBlockStateWithoutUpdatingNeighbors",
            world,
            pos,
            state
        );
    }
}
