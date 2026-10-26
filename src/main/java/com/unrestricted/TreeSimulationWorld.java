
package com.unrestricted;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mundo temporal experimental.
 *
 * Intercepta escrituras y lecturas de bloques
 * sin modificar el mundo real.
 *
 * Los metodos se identifican mediante los
 * mappings de Fabric Loader.
 *
 * Cualquier operacion desconocida se rechaza.
 */
public final class TreeSimulationWorld
        implements InvocationHandler {

    private final StructureWorldAccess originalWorld;

    private final Map<BlockPos, BlockState> stagedBlocks =
            new LinkedHashMap<>();

    private final Map<BlockPos, BlockState> originalBlocks =
            new LinkedHashMap<>();

    private final StructureWorldAccess proxy;

    private final String setBlockStateName;
    private final String setBlockStateDeepName;
    private final String getBlockStateName;
    private final String getFluidStateName;

    private int totalWrites = 0;

    public TreeSimulationWorld(
            StructureWorldAccess originalWorld) {

        if (originalWorld == null) {
            throw new IllegalArgumentException(
                "El mundo original no puede ser nulo"
            );
        }

        this.originalWorld = originalWorld;

        MappingResolver mappings =
                FabricLoader.getInstance()
                        .getMappingResolver();

        /*
         * Convertimos los nombres intermediarios
         * a los utilizados realmente por Minecraft.
         *
         * Las firmas corresponden a MC 1.21.11.
         */

        setBlockStateName =
                mappings.mapMethodName(
                    "intermediary",
                    "net.minecraft.class_1945",
                    "method_8652",
                    "(Lnet/minecraft/class_2338;"
                    + "Lnet/minecraft/class_2680;I)Z"
                );

        setBlockStateDeepName =
                mappings.mapMethodName(
                    "intermediary",
                    "net.minecraft.class_1945",
                    "method_30092",
                    "(Lnet/minecraft/class_2338;"
                    + "Lnet/minecraft/class_2680;II)Z"
                );

        getBlockStateName =
                mappings.mapMethodName(
                    "intermediary",
                    "net.minecraft.class_1922",
                    "method_8320",
                    "(Lnet/minecraft/class_2338;)"
                    + "Lnet/minecraft/class_2680;"
                );

        getFluidStateName =
                mappings.mapMethodName(
                    "intermediary",
                    "net.minecraft.class_1922",
                    "method_8316",
                    "(Lnet/minecraft/class_2338;)"
                    + "Lnet/minecraft/class_3610;"
                );

        proxy = (StructureWorldAccess)
                Proxy.newProxyInstance(
                    StructureWorldAccess.class
                            .getClassLoader(),
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
     * Lee primero el bloque provisional.
     *
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
     * Guarda un bloque virtualmente.
     *
     * No llama a setBlockState del mundo real.
     */
    public boolean stage(
            BlockPos pos,
            BlockState proposed) {

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

    /**
     * Identifica un metodo por nombre resuelto,
     * cantidad y tipos de parametros.
     */
    private boolean matchesBlockWrite(
            Method method,
            Object[] args,
            String expectedName,
            int expectedArgumentCount) {

        return method.getName().equals(expectedName)
                && args != null
                && args.length == expectedArgumentCount
                && args[0] instanceof BlockPos
                && args[1] instanceof BlockState
                && method.getReturnType() == boolean.class;
    }

    @Override
    public Object invoke(
            Object proxyObject,
            Method method,
            Object[] args) throws Throwable {

        String name = method.getName();

        // Metodos basicos de Object.
        if (method.getDeclaringClass()
                == Object.class) {

            return switch (name) {

                case "toString" ->
                        "TreeSimulationWorld";

                case "hashCode" ->
                        System.identityHashCode(
                            proxyObject
                        );

                case "equals" ->
                        proxyObject == (
                            args == null ? null : args[0]
                        );

                default ->
                        throw new UnsupportedOperationException(
                            "Object: " + name
                        );
            };
        }

        /*
         * Escritura de tres parametros.
         *
         * setBlockState(pos, state, flags)
         */
        if (matchesBlockWrite(
                method,
                args,
                setBlockStateName,
                3)) {

            return stage(
                (BlockPos) args[0],
                (BlockState) args[1]
            );
        }

        /*
         * Escritura de cuatro parametros.
         *
         * setBlockState(pos, state, flags, depth)
         */
        if (matchesBlockWrite(
                method,
                args,
                setBlockStateDeepName,
                4)) {

            return stage(
                (BlockPos) args[0],
                (BlockState) args[1]
            );
        }

        /*
         * Lectura de bloques.
         */
        if (name.equals(getBlockStateName)
                && args != null
                && args.length == 1
                && args[0] instanceof BlockPos pos
                && method.getReturnType()
                        == BlockState.class) {

            return getBlockState(pos);
        }

        /*
         * Lectura de fluidos.
         *
         * Respeta los bloques provisionales.
         */
        if (name.equals(getFluidStateName)
                && args != null
                && args.length == 1
                && args[0] instanceof BlockPos pos
                && method.getReturnType()
                        == FluidState.class) {

            return getBlockState(pos).getFluidState();
        }

        /*
         * Politica de seguridad:
         *
         * Nunca delegamos operaciones desconocidas
         * al mundo real.
         */
        throw new UnsupportedOperationException(
            "Metodo no permitido en simulacion: "
                    + name
        );
    }
}
