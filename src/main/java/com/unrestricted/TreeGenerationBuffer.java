
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

        // Registrar si esta posicion ya fue procesada.
        // Se comprueba antes de actualizar proposedStates.
        boolean firstWrite =
                !proposedStates.containsKey(immutablePos);

        // Guardar el estado observado la primera vez
        // que registramos esta posicion.
        originalStates.computeIfAbsent(
            immutablePos,
            p -> world.getBlockState(p)
        );

        BlockState original =
                originalStates.get(immutablePos);

        totalWrites++;

        // Diagnostico de las primeras 12 operaciones.
        // No modifica ni bloquea ninguna escritura.
        if (totalWrites <= 12) {

            System.out.println(
                "[Unrestricted Trees BUFFER DETAIL]"
                + " | Escritura: " + totalWrites
                + " | Posicion: "
                + immutablePos.toShortString()
                + " | Original observado: "
                + original.getBlock().getTranslationKey()
                + " | Propuesto: "
                + proposed.getBlock().getTranslationKey()
                + " | Primera vez: " + firstWrite
            );
        }

        // Una misma generacion puede escribir varias
        // veces sobre una posicion provisional.
        proposedStates.put(immutablePos, proposed);

        // Deteccion preliminar de conflictos.
        // No cancela ninguna operacion.
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
