
package com.unrestricted;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TreeGenerationBuffer {

    // Area de captura alrededor del origen del arbol.
    // Es una region de diagnostico, no un limite
    // definitivo de generacion.
    private static final int RADIUS = 12;
    private static final int BELOW = 3;
    private static final int ABOVE = 45;

    private final Map<BlockPos, BlockState> originalStates =
            new LinkedHashMap<>();

    private final Map<BlockPos, BlockState> proposedStates =
            new LinkedHashMap<>();

    private final BlockPos origin;

    private int totalWrites;
    private int repeatedWrites;
    private int protectedWrites;
    private int outsideSnapshotWrites;

    private BlockPos firstProtectedPosition;

    public TreeGenerationBuffer(
            WorldAccess world,
            BlockPos origin) {

        this.origin = origin.toImmutable();

        // Capturar los estados ANTES de generar.
        // Guardamos solamente los bloques que
        // no son aire para reducir memoria.
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                for (int y = -BELOW; y <= ABOVE; y++) {

                    BlockPos pos =
                            origin.add(x, y, z);

                    if (world.isOutOfHeightLimit(pos)) {
                        continue;
                    }

                    BlockState state =
                            world.getBlockState(pos);

                    if (!state.isAir()) {
                        originalStates.put(
                            pos.toImmutable(),
                            state
                        );
                    }
                }
            }
        }
    }

    private boolean isInsideSnapshot(BlockPos pos) {

        return Math.abs(pos.getX() - origin.getX())
                    <= RADIUS
                && Math.abs(pos.getZ() - origin.getZ())
                    <= RADIUS
                && pos.getY() >= origin.getY() - BELOW
                && pos.getY() <= origin.getY() + ABOVE;
    }

    public void record(
            WorldAccess world,
            BlockPos pos,
            BlockState proposed) {

        BlockPos immutablePos = pos.toImmutable();

        boolean firstWrite =
                !proposedStates.containsKey(immutablePos);

        totalWrites++;

        if (!firstWrite) {
            repeatedWrites++;
        }

        boolean inside = isInsideSnapshot(immutablePos);

        BlockState original;

        if (inside) {
            original = originalStates.getOrDefault(
                immutablePos,
                Blocks.AIR.getDefaultState()
            );
        } else {
            // No podemos reconstruir con seguridad
            // el estado original fuera de la captura.
            outsideSnapshotWrites++;
            original = null;
        }

        // Registrar el ultimo estado propuesto
        // para cada posicion.
        proposedStates.put(immutablePos, proposed);

        if (totalWrites <= 12) {
            System.out.println(
                "[Unrestricted Trees SNAPSHOT DETAIL]"
                + " | Escritura: " + totalWrites
                + " | Posicion: "
                + immutablePos.toShortString()
                + " | Original: "
                + (original == null
                    ? "FUERA_DE_CAPTURA"
                    : original.getBlock()
                        .getTranslationKey())
                + " | Propuesto: "
                + proposed.getBlock().getTranslationKey()
                + " | Primera vez: " + firstWrite
            );
        }

        if (original == null) {
            return;
        }

        // Una escritura que mantiene el mismo estado
        // no cuenta como modificacion.
        if (original.equals(proposed)) {
            return;
        }

        // El aire inicial puede ocuparse.
        if (original.isAir()) {
            return;
        }

        // Contamos aparte los reemplazos sobre
        // bloques originalmente preexistentes.
        // Los brotes se excluyen provisionalmente
        // porque Vanilla debe poder consumirlos.
        if (original.getBlock() instanceof SaplingBlock) {
            return;
        }

        protectedWrites++;

        if (firstProtectedPosition == null) {
            firstProtectedPosition = immutablePos;
        }
    }

    public void printSummary(boolean vanillaSuccess) {

        System.out.println(
            "[Unrestricted Trees SNAPSHOT]"
            + " | Origen: " + origin.toShortString()
            + " | Vanilla success: " + vanillaSuccess
            + " | Escrituras: " + totalWrites
            + " | Posiciones: " + proposedStates.size()
            + " | Repetidas: " + repeatedWrites
            + " | Sobre bloques preexistentes: "
            + protectedWrites
            + " | Fuera de captura: "
            + outsideSnapshotWrites
            + " | Primer bloque preexistente: "
            + firstProtectedPosition
        );
    }

    public Map<BlockPos, BlockState> getProposedStates() {
        return Map.copyOf(proposedStates);
    }

    public Map<BlockPos, BlockState> getOriginalStates() {
        return Map.copyOf(originalStates);
    }

    public int getProtectedWrites() {
        return protectedWrites;
    }

    public int getOutsideSnapshotWrites() {
        return outsideSnapshotWrites;
    }
}
