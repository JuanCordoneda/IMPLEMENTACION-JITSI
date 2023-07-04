package com.safecard.android.model.dataobjects;

import java.io.Serializable;

/**
 * Created by Alonso on 07-02-18.
 */

public class GateData implements Serializable {

    private int gateId;
    private String gateName;
    private String gateColor;
    private String gateCode;
    private int gateType;
    private boolean ownersGate;
    private boolean active;

    public void setGateId(int gateId) {
        this.gateId = gateId;
    }

    public void setGateName(String gateName) {
        this.gateName = gateName;
    }

    public void setGateColor(String gateColor) {
        this.gateColor = gateColor;
    }

    public void setGateCode(String gateCode) {
        this.gateCode = gateCode;
    }

    public void setGateType(int gateType) {
        this.gateType = gateType;
    }

    public void setOwnersGate(boolean ownersGate) {
        this.ownersGate = ownersGate;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getGateId() {
        return gateId;
    }

    public String getGateName() {
        return gateName;
    }

    public String getGateColor() {
        return gateColor;
    }

    public String getGateCode() {
        return gateCode;
    }

    public int getGateType() {
        return gateType;
    }

    public boolean isOwnersGate() {
        return ownersGate;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return "GateData{" +
                "gateId=" + gateId +
                ", gateName='" + gateName + '\'' +
                ", gateColor='" + gateColor + '\'' +
                ", gateCode='" + gateCode + '\'' +
                ", gateType=" + gateType +
                ", ownersGate=" + ownersGate +
                ", active=" + active +
                '}';
    }
}
