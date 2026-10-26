
package com.unrestricted;

import net.minecraft.block.BlockState;
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

/**
 * Mundo temporal para generar arboles sin colocar
 * sus bloques directamente en el mundo real.
 *
 * Esta clase todavia no se conecta a TreeFeature.
 *
 * Las operaciones desconocidas se rechazan para
 * evitar que una simulacion escriba en el mundo real.
 */
public final class TreeSimulationWorld
        implements InvocationHandler {

    private final StructureWorldAccess originalWorld;

    private final Map<BlockPos, BlockState> stagedBlocks =
            new LinkedHashMap<>();

    private final Map<BlockPos, BlockState> originalBlocks =
            new LinkedHashMap<>();

    private final StructureWorldAccess proxy;

    private int totalWrites;

    public TreeSimulationWorld(
            StructureWorldAccess originalWorld) {

        this.originalWorld = originalWorld;

        this.proxy = (StructureWorldAccess)
                Proxy.newProxyInstance(
                    StructureWorldAccess.class
                        .getClassLoader(),
                    new Class<?>[] {
                        StructureWorldAccess.class
                    },
                    this
                );
    }

    /**
     * Se entrega este mundo al generador.
     * Nunca se debe entregar originalWorld
     * durante una simulacion.
     */
    public StructureWorldAccess getWorld() {
        return proxy;
    }

    public Map<BlockPos, BlockState> getStagedBlocks() {
        return Collections.unmodifiableMap(
            stagedBlocks
        );
    }

    public Map<BlockPos, BlockState> getOriginalBlocks() {
        return Collections.unmodifiableMap(
            originalBlocks
        );
    }

    public int getTotalWrites() {
        return totalWrites;
    }

    /**
     * Obtiene el estado provisional si existe.
     * Si no existe, consulta el mundo original.
     */
    public BlockState getBlockState(BlockPos pos) {

        BlockState staged = stagedBlocks.get(pos);

        if (staged != null) {
            return staged;
        }

        return originalWorld.getBlockState(pos);
    }

    /**
     * Registra una colocacion SIN aplicarla.
     */
    public boolean stage(
            BlockPos pos,
            BlockState state) {

        if (pos == null || state == null) {
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

        stagedBlocks.put(key, state);
        totalWrites++;

        return true;
    }

    /**
     * Solo delegamos consultas explicitas.
     * No delegamos automaticamente otros metodos
     * porque podrian provocar cambios reales.
     */
    private static final Set<String> READ_ONLY_METHODS =
            Set.of(
                "getBottomY",
                "getHeight",
                "getTopY",
                "getTopInclusive",
                "getBottomY",
                "getSeaLevel",
                "getLightLevel",
                "getBrightness",
                "getAmbientDarkness",
                "getBiome",
                "getBiomeForNoiseGen",
                "getDimension",
                "getDimensionEntry",
                "getRegistryManager",
                "getEnabledFeatures",
                "getGeneratorStoredBiome",
                "getColor",
                "getWorldBorder",
                "getRandom",
                "isClient",
                "isChunkLoaded",
                "isPosLoaded",
                "isRegionLoaded",
                "isSkyVisibleAllowingSea"
            );

    @Override
    public Object invoke(
            Object proxyObject,
            Method method,
            Object[] arguments) throws Throwable {

        String name = method.getName();

        if (method.getDeclaringClass()
                == Object.class) {

            return switch (name) {
                case "toString" ->
                    "TreeSimulationWorld";
                case "hashCode" ->
                    System.identityHashCode(proxyObject);
                case "equals" ->
                    proxyObject == arguments[0];
                default ->
                    throw new UnsupportedOperationException(
                        name
                    );
            };
        }

        if ("getBlockState".equals(name)
                && arguments != null
                && arguments.length == 1
                && arguments[0] instanceof BlockPos pos) {

            return getBlockState(pos);
        }

        if ("isAir".equals(name)
                && arguments != null
                && arguments.length == 1
                && arguments[0] instanceof BlockPos pos) {

            return getBlockState(pos).isAir();
        }

        if ("getFluidState".equals(name)
                && arguments != null
                && arguments.length == 1
                && arguments[0] instanceof BlockPos pos) {

            return getBlockState(pos).getFluidState();
        }

        if ("testBlockState".equals(name)
                && arguments != null
                && arguments.length == 2
                && arguments[0] instanceof BlockPos pos) {

            @SuppressWarnings("unchecked")
            Predicate<BlockState> predicate =
                    (Predicate<BlockState>) arguments[1];

            return predicate.test(getBlockState(pos));
        }

        if ("testFluidState".equals(name)
                && arguments != null
                && arguments.length == 2
                && arguments[0] instanceof BlockPos pos) {

            @SuppressWarnings("unchecked")
            Predicate<net.minecraft.fluid.FluidState>
                    predicate =
                    (Predicate<net.minecraft.fluid.FluidState>)
                            arguments[1];

            return predicate.test(
                getBlockState(pos).getFluidState()
            );
        }

        if ("setBlockState".equals(name)
                && arguments != null
                && arguments.length >= 2
                && arguments[0] instanceof BlockPos pos
                && arguments[1] instanceof BlockState state) {

            return stage(pos, state);
        }

        /*
         * Un bloque modificado en la simulacion
         * no debe devolver una entidad de bloque
         * procedente del mundo original.
         */
        if ("getBlockEntity".equals(name)
                && arguments != null
                && arguments.length >= 1
                && arguments[0] instanceof BlockPos pos) {

            if (stagedBlocks.containsKey(pos)) {
                return null;
            }

            throw new UnsupportedOperationException(
                "Lectura de BlockEntity no simulada"
            );
        }

        /*
         * Los siguientes metodos tienen
         * efectos secundarios y NO se permiten.
         */
        if ("removeBlock".equals(name)
                || "breakBlock".equals(name)
                || "setBlockEntity".equals(name)
                || "removeBlockEntity".equals(name)
                || "scheduleBlockTick".equals(name)
                || "scheduleFluidTick".equals(name)
                || "spawnEntity".equals(name)
                || "syncWorldEvent".equals(name)
                || "emitGameEvent".equals(name)) {

            throw new UnsupportedOperationException(
                "Operacion no aislada: " + name
            );
        }

        if (READ_ONLY_METHODS.contains(name)) {
            try {
                return method.invoke(
                    originalWorld,
                    arguments
                );
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        /*
         * Politica de seguridad:
         * cualquier metodo que no hayamos
         * implementado se rechaza.
         */
        throw new UnsupportedOperationException(
            "Metodo no implementado en simulacion: "
                + name
        );
    }
}
