package com.viableindustries.waterreporter.api.models.territory;

/**
 * Created by brendanmcintyre on 2/21/17.
 */

public class TerritoryHolder {

    private static Territory territory;

    public static Territory getTerritory() {

        return territory;

    }

    public static void setTerritory(Territory territory) {

        TerritoryHolder.territory = territory;

    }

}
