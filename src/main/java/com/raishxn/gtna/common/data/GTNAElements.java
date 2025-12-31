package com.raishxn.gtna.common.data;

import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.common.data.GTElements;

public class GTNAElements {

    public static Element ECHOITE;
    public static void init() {
    ECHOITE= GTElements.createAndRegister(570, 570, -1, null, "echoite", "Ec", false);
}
}
