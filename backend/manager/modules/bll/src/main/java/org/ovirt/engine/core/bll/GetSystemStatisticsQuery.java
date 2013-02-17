package org.ovirt.engine.core.bll;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.constants.QueryConstants;
import org.ovirt.engine.core.common.queries.GetSystemStatisticsQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetSystemStatisticsQuery<P extends GetSystemStatisticsQueryParameters> extends QueriesCommandBase<P> {
    public GetSystemStatisticsQuery(P parameters) {
        super(parameters);
    }

    public java.util.HashMap<String, Integer> getSystemStatistics() {
        //
        // int max = (Parameters as GetSystemStatisticsQueryParameters).Max;
        // Dictionary<String, Int32> res = new Dictionary<String, Int32>();
        // QueriesCommandBase query;
        // List<IVdcQueryable> tmp;
        // query = CommandsFactory.CreateQueryCommand(
        // VdcQueryType.Search, new SearchParameters("vms:",
        // SearchType.VM){MaxCount = max});
        // query.Execute();
        // tmp = (List<IVdcQueryable>)query.QueryReturnValue.ReturnValue;
        // int total_vms = tmp.Count;
        // query = CommandsFactory.CreateQueryCommand(
        // VdcQueryType.Search, new SearchParameters("vms: status != down",
        // SearchType.VM) { MaxCount = max });
        // query.Execute();
        // tmp = (List<IVdcQueryable>)query.QueryReturnValue.ReturnValue;
        // int active_vms = tmp.Count;
        // List<DbUser> users = DbFacade.Instance.GetAllFromUsers();
        // int total_users = users.Count, active_users = 0;
        // foreach (DbUser user in users)
        // {
        // if (user.IsLogedin)
        // {
        // active_users++;
        // }
        // }
        // query = CommandsFactory.CreateQueryCommand(
        // VdcQueryType.Search, new SearchParameters("Hosts", SearchType.VDS) {
        // MaxCount = max });
        // query.Execute();
        // tmp = (List<IVdcQueryable>)query.QueryReturnValue.ReturnValue;
        // int total_vds = tmp.Count, active_vds = 0;
        // foreach (VDS vds in tmp)
        // {
        // if ((vds.status == VDSStatus.Up) || (vds.status ==
        // VDSStatus.PreparingForMaintenance))
        // {
        // active_vds++;
        // }
        // }
        //

        java.util.HashMap<String, Integer> res = new java.util.HashMap<String, Integer>();

        // VMs:
        int total_vms = DbFacade.getInstance().getSystemStatisticsValue("VM", "");
        String[] activeVmStatuses = { (String.valueOf(VMStatus.Up.getValue())),
                (String.valueOf(VMStatus.PoweringUp.getValue())),
                (String.valueOf(VMStatus.PoweredDown.getValue())),
                (String.valueOf(VMStatus.MigratingTo.getValue())),
                (String.valueOf(VMStatus.WaitForLaunch.getValue())),
                (String.valueOf(VMStatus.RebootInProgress.getValue())),
                (String.valueOf(VMStatus.PoweringDown.getValue())),
                (String.valueOf(VMStatus.Paused.getValue())),
                (String.valueOf(VMStatus.Unknown.getValue())) };
        int active_vms = DbFacade.getInstance()
                .getSystemStatisticsValue("VM", StringUtils.join(activeVmStatuses, ','));

        int down_vms = (total_vms - active_vms) < 0 ? 0 : (total_vms - active_vms);

        // Hosts:
        int total_vds = DbFacade.getInstance().getSystemStatisticsValue("HOST", "");

        String[] activeVdsStatuses = { (String.valueOf(VDSStatus.Up.getValue())),
                (String.valueOf(VDSStatus.PreparingForMaintenance.getValue()))};
        int active_vds = DbFacade.getInstance().getSystemStatisticsValue("HOST",
                StringUtils.join(activeVdsStatuses, ','));
        int maintenance_vds = DbFacade.getInstance().getSystemStatisticsValue("HOST",
                (String.valueOf(VDSStatus.Maintenance.getValue())));
        int down_vds = (total_vds - active_vds - maintenance_vds) < 0 ? 0 : (total_vds - active_vds - maintenance_vds);

        // Users:
        int total_users = DbFacade.getInstance().getSystemStatisticsValue("USER", "");
        int active_users = DbFacade.getInstance().getSystemStatisticsValue("USER", "1");

        // Storage Domains:
        int total_storage_domains = DbFacade.getInstance().getSystemStatisticsValue("TSD", "");
        int active_storage_domains = DbFacade.getInstance().getSystemStatisticsValue("ASD",
                (Integer.toString(StorageDomainStatus.Active.getValue())));

        res.put(QueryConstants.SYSTEM_STATS_TOTAL_VMS_FIELD, total_vms);
        res.put(QueryConstants.SYSTEM_STATS_ACTIVE_VMS_FIELD, active_vms);
        res.put(QueryConstants.SYSTEM_STATS_DOWN_VMS_FIELD, down_vms);
        res.put(QueryConstants.SYSTEM_STATS_TOTAL_HOSTS_FIELD, total_vds);
        res.put(QueryConstants.SYSTEM_STATS_ACTIVE_HOSTS_FIELD, active_vds);
        res.put(QueryConstants.SYSTEM_STATS_MAINTENANCE_HOSTS_FIELD, maintenance_vds);
        res.put(QueryConstants.SYSTEM_STATS_DOWN_HOSTS_FIELD, down_vds);
        res.put(QueryConstants.SYSTEM_STATS_TOTAL_USERS_FIELD, total_users);
        res.put(QueryConstants.SYSTEM_STATS_ACTIVE_USERS_FIELD, active_users);
        res.put(QueryConstants.SYSTEM_STATS_TOTAL_STORAGE_DOMAINS_FIELD, total_storage_domains);
        res.put(QueryConstants.SYSTEM_STATS_ACTIVE_STORAGE_DOMAINS_FIELD, active_storage_domains);

        return res;
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getSystemStatistics());
    }
}
