package org.ovirt.engine.core.common.businessentities;


import java.io.Serializable;

public class VmBalloonInfo implements Serializable {

    private static final long serialVersionUID = -6215874051244541874L;
    private Long currentMemory;
    private Long balloonMaxMemory;
    private Long balloonTargetMemory;
    private Long balloonMinMemory;
    private boolean balloonDeviceEnabled;

    public Long getCurrentMemory() {
        return currentMemory;
    }

    public void setCurrentMemory(Long currentMemory) {
        this.currentMemory = currentMemory;
    }

    public Long getBalloonMaxMemory() {
        return balloonMaxMemory;
    }

    public void setBalloonMaxMemory(Long balloonMaxMemory) {
        this.balloonMaxMemory = balloonMaxMemory;
    }

    public Long getBalloonTargetMemory() {
        return balloonTargetMemory;
    }

    public void setBalloonTargetMemory(Long balloonTargetMemory) {
        this.balloonTargetMemory = balloonTargetMemory;
    }

    public Long getBalloonMinMemory() {
        return balloonMinMemory;
    }

    public void setBalloonMinMemory(Long balloonMinMemory) {
        this.balloonMinMemory = balloonMinMemory;
    }

    public boolean isBalloonDeviceEnabled() {
        return balloonDeviceEnabled;
    }

    public void setBalloonDeviceEnabled(boolean balloonDeviceEnabled) {
        this.balloonDeviceEnabled = balloonDeviceEnabled;
    }
}
