
package com.unrestricted;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TreeGenerationBuffer {

    private final Map<BlockPos, BlockState> originalStates =
            new LinkedHashMap<>();

    private final Map<BlockPos, BlockState> proposedStates =
            new LinkedHashMap<>();

    private int totalWrites = 0;
    private int blockedWrites = 0;
    private BlockPos firstBlockedPosition = null;

    public void record(
            WorldAccess world,
            BlockPos pos,
            BlockState proposed) {

        BlockPos immutablePos = pos.toImmutable();

        // Guardar el estado original solo la primera vez.
        originalStates.computeIfAbsent(
            immutablePos,
            p -> world.getBlockState(p)
        );

        BlockState original =
                originalStates.get(immutablePos);

        totalWrites++;

        // La misma generacion puede escribir varias
        // veces sobre una posicion provisional.
        proposedStates.put(immutablePos, proposed);

        if (!original.equals(proposed)
                && !original.isAir()
                && !TreePlacementRules.canReplace(original)) {

            blockedWrites++;

            if (firstBlockedPosition == null) {
                firstBlockedPosition = immutablePos;
            }
        }
    }

    public int getTotalWrites() {
        return totalWrites;
    }

    public int getUniquePositions() {
        return proposedStates.size();
    }

    public int getBlockedWrites() {
        return blockedWrites;
    }

    public BlockPos getFirstBlockedPosition() {
        return firstBlockedPosition;
    }

    public Map<BlockPos, BlockState> getProposedStates() {
        return Map.copyOf(proposedStates);
    }

    public Map<BlockPos, BlockState> getOriginalStates() {
        return Map.copyOf(originalStates);
    }
}
