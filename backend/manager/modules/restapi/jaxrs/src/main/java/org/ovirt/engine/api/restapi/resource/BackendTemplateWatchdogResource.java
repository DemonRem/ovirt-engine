/*
Copyright (c) 2015 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Watchdog;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.TemplateWatchdogResource;
import org.ovirt.engine.api.restapi.types.WatchdogMapper;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateWatchdogResource
        extends AbstractBackendActionableResource<Watchdog, VmWatchdog>
        implements TemplateWatchdogResource {

    private Guid templateId;

    public BackendTemplateWatchdogResource(String watchdogId, Guid templateId) {
        super(watchdogId, Watchdog.class, VmWatchdog.class);
        this.templateId = templateId;
    }

    @Override
    public Watchdog get() {
        VmWatchdog entity = getWatchdog();
        if (entity == null) {
            return notFound();
        }
        return addLinks(populate(map(entity), entity));
    }

    private VmWatchdog getWatchdog() {
        List<VmWatchdog> entities = getBackendCollection(
            VmWatchdog.class,
            QueryType.GetWatchdog,
            new IdQueryParameters(templateId)
        );
        for (VmWatchdog current : entities) {
            if (Objects.equals(current.getId(), guid)) {
                return current;
            }
        }
        return null;
    }

    @Override
    public CreationResource getCreationResource(String ids) {
        return inject(new BackendCreationResource(ids));
    }

    @Override
    public Watchdog addParents(Watchdog watchdog) {
        Template template = new Template();
        template.setId(templateId.toString());
        watchdog.setTemplate(template);
        return watchdog;
    }

    @Override
    public Watchdog update(Watchdog watchdog) {
        return performUpdate(
            watchdog, new WatchdogResolver(), ActionType.UpdateWatchdog, new UpdateParametersProvider());
    }

    @Override
    public Response remove() {
        get();
        WatchdogParameters parameters = new WatchdogParameters();
        parameters.setId(templateId);
        parameters.setVm(false);
        return performAction(ActionType.RemoveWatchdog, parameters);
    }

    private class UpdateParametersProvider implements ParametersProvider<Watchdog, VmWatchdog> {
        @Override
        public ActionParametersBase getParameters(Watchdog model, VmWatchdog entity) {
            WatchdogParameters parameters = new WatchdogParameters();
            if (model.isSetAction()) {
                parameters.setAction(WatchdogMapper.map(model.getAction()));
            }
            else {
                parameters.setAction(entity.getAction());
            }
            if (model.isSetModel()) {
                parameters.setModel(WatchdogMapper.map(model.getModel()));
            }
            else {
                parameters.setModel(entity.getModel());
            }
            parameters.setId(templateId);
            parameters.setVm(false);
            return parameters;
        }
    }

    private class WatchdogResolver extends EntityIdResolver<Guid> {
        @Override
        public VmWatchdog lookupEntity(Guid id) throws BackendFailureException {
            return getWatchdog();
        }
    }
}
