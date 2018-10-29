/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.security.rest.action.role;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.logging.DeprecationLogger;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.license.XPackLicenseState;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.action.RestBuilderListener;
import org.elasticsearch.xpack.core.security.action.role.PutRoleRequestBuilder;
import org.elasticsearch.xpack.core.security.action.role.PutRoleResponse;
import org.elasticsearch.xpack.core.security.client.SecurityClient;
import org.elasticsearch.xpack.security.rest.action.SecurityBaseRestHandler;

import java.io.IOException;

import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestRequest.Method.PUT;

/**
 * Rest endpoint to add a Role to the security index
 */
public class RestPutRoleAction extends SecurityBaseRestHandler {
    private static final Logger logger = LogManager.getLogger(RestPutRoleAction.class);
    private static final DeprecationLogger deprecationLogger = new DeprecationLogger(logger);

    public RestPutRoleAction(Settings settings, RestController controller, XPackLicenseState licenseState) {
        super(settings, licenseState);
        controller.registerHandler(POST, "/_xpack/security/role/{name}", this);
        controller.registerHandler(PUT, "/_xpack/security/role/{name}", this);

        // @deprecated: Remove in 6.0
        controller.registerAsDeprecatedHandler(POST, "/_shield/role/{name}", this,
                                               "[POST /_shield/role/{name}] is deprecated! Use " +
                                               "[POST /_xpack/security/role/{name}] instead.",
                                               deprecationLogger);
        controller.registerAsDeprecatedHandler(PUT, "/_shield/role/{name}", this,
                                               "[PUT /_shield/role/{name}] is deprecated! Use " +
                                               "[PUT /_xpack/security/role/{name}] instead.",
                                               deprecationLogger);
    }

    @Override
    public String getName() {
        return "xpack_security_put_role_action";
    }

    @Override
    public RestChannelConsumer innerPrepareRequest(RestRequest request, NodeClient client) throws IOException {
        PutRoleRequestBuilder requestBuilder = new SecurityClient(client)
                .preparePutRole(request.param("name"), request.requiredContent(), request.getXContentType())
                .setRefreshPolicy(request.param("refresh"));
        return channel -> requestBuilder.execute(new RestBuilderListener<PutRoleResponse>(channel) {
            @Override
            public RestResponse buildResponse(PutRoleResponse putRoleResponse, XContentBuilder builder) throws Exception {
                return new BytesRestResponse(RestStatus.OK, builder.startObject().field("role", putRoleResponse).endObject());
            }
        });
    }
}
