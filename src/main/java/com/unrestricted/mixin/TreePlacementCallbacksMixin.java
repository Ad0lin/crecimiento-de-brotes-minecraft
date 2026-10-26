package com.unrestricted.mixin;

import com.unrestricted.TreeGenerationBuffer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * Protege los callbacks que TreeFeature entrega a los generadores Vanilla.
 * No modifica la comprobacion de altura ni obliga al arbol a crecer.
 *
 * Es una capa adicional a la proteccion existente de TreeFeatureMixin.
 */
@Mixin(TreeFeature.class)
public abstract class TreePlacementCallbacksMixin {

    private static final String INNER_GENERATE =
            "generate(Lnet/minecraft/world/StructureWorldAccess;"
            + "Lnet/minecraft/util/math/random/Random;"
            + "Lnet/minecraft/util/math/BlockPos;"
            + "Ljava/util/function/BiConsumer;"
            + "Ljava/util/function/BiConsumer;"
            + "Lnet/minecraft/world/gen/foliage/FoliagePlacer$BlockPlacer;"
            + "Lnet/minecraft/world/gen/feature/TreeFeatureConfig;)Z";

    private static final AtomicInteger REJECTED = new AtomicInteger();
    private static final int MAX_REJECTION_LOGS = 24;

    private static boolean unrestricted$allowed(BlockPos pos, String kind) {
        TreeGenerationBuffer snapshot = TreeGenerationBuffer.active();
        boolean allowed = snapshot != null && snapshot.mayWriteAt(pos);
        if (!allowed) {
            int count = REJECTED.incrementAndGet();
            if (count <= MAX_REJECTION_LOGS) {
                System.out.println(
                        "[Unrestricted Trees CALLBACK BLOCKED]"
                        + " | Tipo: " + kind
                        + " | Posicion: " + pos.toShortString()
                        + " | Original: "
                        + (snapshot == null ? "SIN_SNAPSHOT" : snapshot.originalDescription(pos))
                );
            }
        }
        return allowed;
    }

    // Parametro 4: callback de raices.
    @ModifyVariable(method = INNER_GENERATE, at = @At("HEAD"), argsOnly = true, index = 4)
    private BiConsumer<BlockPos, BlockState> unrestricted$guardRoots(
            BiConsumer<BlockPos, BlockState> original) {
        return (pos, state) -> {
            if (unrestricted$allowed(pos, "ROOT")) {
                original.accept(pos, state);
            }
        };
    }

    // Parametro 5: callback de tronco y ramas.
    @ModifyVariable(method = INNER_GENERATE, at = @At("HEAD"), argsOnly = true, index = 5)
    private BiConsumer<BlockPos, BlockState> unrestricted$guardTrunk(
            BiConsumer<BlockPos, BlockState> original) {
        return (pos, state) -> {
            if (unrestricted$allowed(pos, "TRUNK")) {
                original.accept(pos, state);
            }
        };
    }

    // Parametro 6: callback de hojas. Conservamos hasPlacedBlock de Vanilla.
    @ModifyVariable(method = INNER_GENERATE, at = @At("HEAD"), argsOnly = true, index = 6)
    private FoliagePlacer.BlockPlacer unrestricted$guardFoliage(
            FoliagePlacer.BlockPlacer original) {
        return new FoliagePlacer.BlockPlacer() {
            @Override
            public void placeBlock(BlockPos pos, BlockState state) {
                if (unrestricted$allowed(pos, "FOLIAGE")) {
                    original.placeBlock(pos, state);
                }
            }

            @Override
            public boolean hasPlacedBlock(BlockPos pos) {
                return original.hasPlacedBlock(pos);
            }
        };
    }
}
