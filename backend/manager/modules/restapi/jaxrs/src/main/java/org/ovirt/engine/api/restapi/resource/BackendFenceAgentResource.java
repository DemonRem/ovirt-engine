package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Agent;
import org.ovirt.engine.api.resource.FenceAgentResource;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.FenceAgentCommandParameterBase;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendFenceAgentResource extends AbstractBackendSubResource<Agent, FenceAgent> implements FenceAgentResource {

    public BackendFenceAgentResource(String fenceAgentId) {
        super(fenceAgentId, Agent.class, FenceAgent.class);
    }

    @Override
    public Agent get() {
        return performGet(QueryType.GetFenceAgentById, new IdQueryParameters(guid));
    }

    @Override
    public Agent update(Agent agent) {
        QueryIdResolver<Guid> agentResolver =
                new QueryIdResolver<>(QueryType.GetFenceAgentById, IdQueryParameters.class);
        FenceAgent entity = getEntity(agentResolver, true);
        return performUpdate(agent,
                entity,
                map(entity),
                agentResolver,
                ActionType.UpdateFenceAgent,
                new UpdateParametersProvider());
    }

    protected class UpdateParametersProvider implements ParametersProvider<Agent, FenceAgent> {
        @Override
        public ActionParametersBase getParameters(Agent incoming, FenceAgent entity) {
            FenceAgentCommandParameterBase updateParams = new FenceAgentCommandParameterBase();
            FenceAgent agent = map(incoming, entity);
            updateParams.setAgent(agent);
            return updateParams;
        }
    }

    @Override
    public Response remove() {
        get();
        FenceAgentCommandParameterBase params = new FenceAgentCommandParameterBase();
        FenceAgent agent = new FenceAgent();
        agent.setId(guid);
        params.setAgent(agent);
        return performAction(ActionType.RemoveFenceAgent, params);
    }
}
