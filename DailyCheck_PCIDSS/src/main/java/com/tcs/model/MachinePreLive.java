package com.tcs.model;

import java.util.List;

public class MachinePreLive implements Machine{

    @Override
    public List<String> getMachineNames() {
        return PRELIVE_BO_MACHINE_IDS;
    }
}
