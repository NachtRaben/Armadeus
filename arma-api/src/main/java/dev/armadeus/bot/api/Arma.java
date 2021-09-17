package dev.armadeus.bot.api;

public final class Arma {

    private static ArmaCore core;

    public static void setCore(ArmaCore core) {
        if(Arma.core != null) {
            throw new UnsupportedOperationException("Cannot redefine singleton Arma-Core");
        }
        Arma.core = core;
    }

    public static ArmaCore getCore() {
        return core;
    }
}
