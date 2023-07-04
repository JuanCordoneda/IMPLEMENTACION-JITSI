package com.safecard.android.model.dataobjects;

import com.safecard.android.Consts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alonso on 07-02-18.
 */

public class SectorData implements Serializable {
    //API
    private int sectorId;
    private String sectorName;
    private boolean useQr;
    private boolean useRc;
    private boolean useInv;
    private int sectorLocalId = 12;
    private List<GateData> gates;

    //LOCAL
    private String defaultControlType;
    private int rootId;

    public SectorData() {
        gates = new ArrayList<>();
        defaultControlType = Consts.CONTROLTYPE_NONE;
    }

    public int getSectorId() {
        return sectorId;
    }

    public void setSectorId(int sectorId) {
        this.sectorId = sectorId;
    }

    public String getSectorName() {
        return sectorName;
    }

    public void setSectorName(String sectorName) {
        this.sectorName = sectorName;
    }

    public boolean isUseQr() {
        return useQr;
    }

    public void setUseQr(boolean useQr) {
        this.useQr = useQr;
    }

    public boolean isUseRc() {
        return useRc;
    }

    public void setUseRc(boolean useRc) {
        this.useRc = useRc;
    }

    public boolean isUseInv() {
        return useInv;
    }

    public void setUseInv(boolean useInv) {
        this.useInv = useInv;
    }

    public List<GateData> getGates() {
        return gates;
    }

    public void setGates(List<GateData> gates) {
        this.gates = gates;
    }

    public String getDefaultControlType() {
        return defaultControlType;
    }

    public void setDefaultControlType(String defaultControlType) {
        this.defaultControlType = defaultControlType;
    }

    @Override
    public String toString() {
        return "SectorData{" +
                "sectorId=" + sectorId +
                ", sectorName='" + sectorName + '\'' +
                ", useQr=" + useQr +
                ", useRc=" + useRc +
                ", useInv=" + useInv +
                ", sectorLocalId=" + sectorLocalId +
                ", gates=" + gates +
                ", defaultControlType='" + defaultControlType + '\'' +
                ", rootId=" + rootId +
                '}';
    }

    public int getRootId() {
        return rootId;
    }

    public void setRootId(int rootId) {
        this.rootId = rootId;
    }

    public int getSectorLocalId() {
        return sectorLocalId;
    }

    public void setSectorLocalId(int sectorLocalId) {
        this.sectorLocalId = sectorLocalId;
    }
}
