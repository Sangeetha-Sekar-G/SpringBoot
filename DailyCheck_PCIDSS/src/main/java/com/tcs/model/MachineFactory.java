package com.tcs.model;

public class MachineFactory {

    public static Machine createMachine(final String type){
        return switch (type) {
            case "PRELIVE" -> new MachinePreLive();
            default -> null;
        };
    }
}
