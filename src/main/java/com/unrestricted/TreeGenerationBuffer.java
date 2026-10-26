package com.unrestricted;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TreeGenerationBuffer {

    private static final int RADIUS = 12;
    private static final int BELOW = 3;
    private static final int ABOVE = 45;

    private static final ThreadLocal<TreeGenerationBuffer> ACTIVE =
            new ThreadLocal<>();

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

        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                for (int y = -BELOW; y <= ABOVE; y++) {

                    BlockPos pos = origin.add(x, y, z);

                    if (world.isOutOfHeightLimit(pos)) {
                        continue;
                    }

                    BlockState state = world.getBlockState(pos);

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

    public static void activate(TreeGenerationBuffer buffer) {
        ACTIVE.set(buffer);
    }

    public static TreeGenerationBuffer active() {
        return ACTIVE.get();
    }

    public static void clearActive() {
        ACTIVE.remove();
    }

    private boolean isInsideSnapshot(BlockPos pos) {
        return Math.abs(pos.getX() - origin.getX()) <= RADIUS
                && Math.abs(pos.getZ() - origin.getZ()) <= RADIUS
                && pos.getY() >= origin.getY() - BELOW
                && pos.getY() <= origin.getY() + ABOVE;
    }

    public boolean mayWriteAt(BlockPos pos) {
        if (!isInsideSnapshot(pos)) {
            return false;
        }

        BlockState original = originalStates.getOrDefault(
                pos.toImmutable(),
                Blocks.AIR.getDefaultState()
        );

        if (original.isAir()) {
            return true;
        }

        if (original.getBlock() instanceof SaplingBlock) {
            return true;
        }

        return TreePlacementRules.canReplace(original);
    }

    public String originalDescription(BlockPos pos) {
        if (!isInsideSnapshot(pos)) {
            return "FUERA_DE_CAPTURA";
        }

        BlockState state = originalStates.get(pos.toImmutable());

        if (state == null) {
            return Blocks.AIR.getTranslationKey();
        }

        return state.getBlock().toString();
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
            outsideSnapshotWrites++;
            original = null;
        }

        proposedStates.put(immutablePos, proposed);

        if (UnrestrictedTreesDebug.BUFFER_LOGS
                && totalWrites <= 12) {

            UnrestrictedTreesDebug.logBuffer(
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

        if (original.equals(proposed)) {
            return;
        }

        if (original.isAir()) {
            return;
        }

        if (original.getBlock() instanceof SaplingBlock) {
            return;
        }

        if (!TreePlacementRules.canReplace(original)) {
            protectedWrites++;

            if (firstProtectedPosition == null) {
                firstProtectedPosition = immutablePos;
            }
        }
    }

    public void printSummary(boolean vanillaSuccess) {
        if (!UnrestrictedTreesDebug.BUFFER_LOGS) {
            return;
        }

        UnrestrictedTreesDebug.logBuffer(
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
