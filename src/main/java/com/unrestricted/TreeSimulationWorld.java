
package com.unrestricted;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class TreeSimulationWorld
        implements InvocationHandler {

    private final StructureWorldAccess originalWorld;

    private final Map<BlockPos, BlockState> stagedBlocks =
            new LinkedHashMap<>();

    private final Map<BlockPos, BlockState> originalBlocks =
            new LinkedHashMap<>();

    private final StructureWorldAccess proxy;

    private int totalWrites = 0;

    // Lista deliberadamente pequeña.
    // No contiene metodos duplicados.
    private static final Set<String> READ_ONLY_METHODS =
            Set.of(
                "getBottomY",
                "getHeight",
                "getTopY",
                "getSeaLevel",
                "getBiome",
                "getBiomeForNoiseGen",
                "getDimension",
                "getRegistryManager",
                "getEnabledFeatures",
                "getAmbientDarkness",
                "getLightLevel",
                "getBrightness",
                "getSeed",
                "isClient",
                "isChunkLoaded",
                "isPosLoaded",
                "isRegionLoaded"
            );

    public TreeSimulationWorld(
            StructureWorldAccess originalWorld) {

        if (originalWorld == null) {
            throw new IllegalArgumentException(
                "Mundo original nulo"
            );
        }

        this.originalWorld = originalWorld;

        this.proxy = (StructureWorldAccess)
                Proxy.newProxyInstance(
                    StructureWorldAccess.class.getClassLoader(),
                    new Class<?>[] {
                        StructureWorldAccess.class
                    },
                    this
                );
    }

    public StructureWorldAccess getWorld() {
        return proxy;
    }

    public Map<BlockPos, BlockState> getStagedBlocks() {
        return Collections.unmodifiableMap(stagedBlocks);
    }

    public Map<BlockPos, BlockState> getOriginalBlocks() {
        return Collections.unmodifiableMap(originalBlocks);
    }

    public int getTotalWrites() {
        return totalWrites;
    }

    public BlockState getBlockState(BlockPos pos) {

        BlockState staged = stagedBlocks.get(pos);

        if (staged != null) {
            return staged;
        }

        return originalWorld.getBlockState(pos);
    }

    public boolean stage(BlockPos pos, BlockState proposed) {

        if (pos == null || proposed == null) {
            throw new IllegalArgumentException(
                "Posicion o estado nulo"
            );
        }

        if (originalWorld.isOutOfHeightLimit(pos)) {
            return false;
        }

        BlockPos key = pos.toImmutable();

        originalBlocks.putIfAbsent(
            key,
            originalWorld.getBlockState(key)
        );

        stagedBlocks.put(key, proposed);

        totalWrites++;

        return true;
    }

    @Override
    public Object invoke(
            Object proxyObject,
            Method method,
            Object[] args) throws Throwable {

        String name = method.getName();

        if (method.getDeclaringClass() == Object.class) {

            return switch (name) {
                case "toString" -> "TreeSimulationWorld";
                case "hashCode" ->
                        System.identityHashCode(proxyObject);
                case "equals" ->
                        proxyObject == args[0];
                default ->
                        throw new UnsupportedOperationException(name);
            };
        }

        if (args != null
                && args.length >= 1
                && args[0] instanceof BlockPos pos) {

            if (name.equals("getBlockState")) {
                return getBlockState(pos);
            }

            if (name.equals("getFluidState")) {
                return getBlockState(pos).getFluidState();
            }

            if (name.equals("isAir")) {
                return getBlockState(pos).isAir();
            }

            if (name.equals("testBlockState")
                    && args.length == 2) {

                @SuppressWarnings("unchecked")
                Predicate<BlockState> predicate =
                        (Predicate<BlockState>) args[1];

                return predicate.test(getBlockState(pos));
            }

            if (name.equals("testFluidState")
                    && args.length == 2) {

                @SuppressWarnings("unchecked")
                Predicate<FluidState> predicate =
                        (Predicate<FluidState>) args[1];

                return predicate.test(
                    getBlockState(pos).getFluidState()
                );
            }

            if (name.equals("setBlockState")
                    && args.length >= 2
                    && args[1] instanceof BlockState state) {

                return stage(pos, state);
            }
        }

        if (READ_ONLY_METHODS.contains(name)) {
            try {
                return method.invoke(originalWorld, args);
            } catch (InvocationTargetException exception) {
                throw exception.getCause();
            }
        }

        // Cualquier operacion no implementada
        // queda prohibida, nunca delegada.
        throw new UnsupportedOperationException(
            "Metodo no permitido en simulacion: " + name
        );
    }
}
