package com.unrestricted;

public final class UnrestrictedTreesDebug {

    /**
     * Debug general del mod.
     *
     * false = sin logs
     * true  = logs activos
     */
    public static final boolean ENABLED = false;

    /**
     * Si algun dia queres reactivar solamente
     * ciertos grupos, podriamos separar por flags.
     * Por ahora dependen todos del debug general.
     */
    public static final boolean SPACE_LOGS = ENABLED;
    public static final boolean WRITE_LOGS = ENABLED;
    public static final boolean BUFFER_LOGS = ENABLED;

    private UnrestrictedTreesDebug() {
    }

    public static void logSpace(String message) {
        if (SPACE_LOGS) {
            System.out.println(message);
        }
    }

    public static void logWrite(String message) {
        if (WRITE_LOGS) {
            System.out.println(message);
        }
    }

    public static void logBuffer(String message) {
        if (BUFFER_LOGS) {
            System.out.println(message);
        }
    }
}
