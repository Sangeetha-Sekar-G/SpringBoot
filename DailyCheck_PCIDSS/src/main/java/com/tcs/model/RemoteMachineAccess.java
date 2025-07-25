package com.tcs.model;

import java.util.Arrays;
import java.util.List;

public interface RemoteMachineAccess {

    public static final List<String> PRELIVE_BO_MACHINE_IDS = Arrays.asList("eua-bo-1220");

    public List<String> getMachineNames();
}
