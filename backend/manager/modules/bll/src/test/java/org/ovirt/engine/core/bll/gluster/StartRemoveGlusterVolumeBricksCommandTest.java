package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRemoveBricksParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

public class StartRemoveGlusterVolumeBricksCommandTest extends BaseCommandTest {
    @Mock
    GlusterVolumeDao volumeDao;
    @Mock
    protected BackendInternal backend;
    @Mock
    protected VDSBrokerFrontend vdsBrokerFrontend;

    private final Guid volumeId1 = new Guid("8bc6f108-c0ef-43ab-ba20-ec41107220f5");

    private final Guid volumeId2 = new Guid("b2cb2f73-fab3-4a42-93f0-d5e4c069a43e");

    private final Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

    @Mock
    protected Cluster cluster;

    GlusterAsyncTask asyncTaskToBeReturned = new GlusterAsyncTask();
    /**
     * The command under test.
     */
    @Spy
    @InjectMocks
    private StartRemoveGlusterVolumeBricksCommand cmd =
            new StartRemoveGlusterVolumeBricksCommand(new GlusterVolumeRemoveBricksParameters(), null);

    private void setVolumeId(Guid volumeId) {
        cmd.setGlusterVolumeId(volumeId);
        cmd.getParameters().setBricks(getBricks(volumeId, 1));
    }

    private List<GlusterBrickEntity> getBricks(Guid volumeId, int max) {
        List<GlusterBrickEntity> bricks = new ArrayList<>();
        for (Integer i = 0; i < max; i++) {
            GlusterBrickEntity brick1 = new GlusterBrickEntity();
            brick1.setVolumeId(volumeId);
            brick1.setServerName("server1");
            brick1.setStatus(GlusterStatus.UP);
            brick1.setBrickDirectory("/tmp/s" + i.toString());
            bricks.add(brick1);
        }
        return bricks;
    }

    @Before
    public void prepareMocks() {
        doReturn(getVds(VDSStatus.Up)).when(cmd).getUpServer();
        doReturn(getSingleBrickVolume(volumeId1)).when(volumeDao).getById(volumeId1);
        doReturn(getMultiBrickVolume(volumeId2)).when(volumeDao).getById(volumeId2);
        doReturn(cluster).when(cmd).getCluster();
        doReturn(vdsBrokerFrontend).when(cmd).getVdsBroker();
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsName("gfs1");
        vds.setClusterId(CLUSTER_ID);
        vds.setStatus(status);
        return vds;
    }

    private GlusterVolumeEntity getSingleBrickVolume(Guid volumeId) {
        GlusterVolumeEntity volume = getGlusterVolume(volumeId);
        volume.setStatus(GlusterStatus.UP);
        volume.setBricks(getBricks(volumeId, 1));
        volume.setClusterId(CLUSTER_ID);
        return volume;
    }

    private GlusterVolumeEntity getMultiBrickVolume(Guid volumeId) {
        GlusterVolumeEntity volume = getGlusterVolume(volumeId);
        volume.setStatus(GlusterStatus.UP);
        volume.setBricks(getBricks(volumeId, 2));
        volume.setClusterId(CLUSTER_ID);
        return volume;
    }

    private GlusterVolumeEntity getGlusterVolume(Guid id) {
        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.setId(id);
        volumeEntity.setName("test-vol");
        volumeEntity.addAccessProtocol(AccessProtocol.GLUSTER);
        volumeEntity.addTransportType(TransportType.TCP);
        volumeEntity.setVolumeType(GlusterVolumeType.DISTRIBUTE);
        return volumeEntity;
    }

    private void mockBackend(boolean succeeded, EngineError errorCode) {
        doReturn(backend).when(cmd).getBackend();
        doNothing().when(cmd).startSubStep();
        doReturn(asyncTaskToBeReturned).when(cmd).handleTaskReturn(asyncTaskToBeReturned);
        doNothing().when(cmd).updateBricksWithTaskID(asyncTaskToBeReturned);

        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setReturnValue(asyncTaskToBeReturned);
        vdsReturnValue.setSucceeded(succeeded);
        if (!succeeded) {
            vdsReturnValue.setVdsError(new VDSError(errorCode, ""));
        }
        when(vdsBrokerFrontend.runVdsCommand(eq(VDSCommandType.StartRemoveGlusterVolumeBricks), any()))
                .thenReturn(vdsReturnValue);
    }

    @Test
    public void executeCommand() {
        setVolumeId(volumeId2);
        mockBackend(true, null);
        assertTrue(cmd.validate());
        cmd.executeCommand();

        verify(cmd).startSubStep();
        verify(cmd).handleTaskReturn(asyncTaskToBeReturned);
        verify(cmd).updateBricksWithTaskID(asyncTaskToBeReturned);
        assertEquals(AuditLogType.START_REMOVING_GLUSTER_VOLUME_BRICKS, cmd.getAuditLogTypeValue());
    }

    @Test
    public void executeCommandWhenFailed() {
        setVolumeId(volumeId2);
        mockBackend(false, EngineError.GlusterVolumeRemoveBricksStartFailed);
        assertTrue(cmd.validate());
        cmd.executeCommand();

        assertEquals(AuditLogType.START_REMOVING_GLUSTER_VOLUME_BRICKS_FAILED, cmd.getAuditLogTypeValue());
    }

    @Test
    public void validateSucceeds() {
        setVolumeId(volumeId2);
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFails() {
        setVolumeId(volumeId1);
        assertFalse(cmd.validate());
    }

    @Test
    public void validateFailsOnNull() {
        assertFalse(cmd.validate());
    }
}
