/*
 * Copyright Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags and
 * the COPYRIGHT.txt file distributed with this work.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.syndesis.dv.server.endpoint;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import io.syndesis.dv.metadata.internal.DefaultMetadataInstance;
import io.syndesis.dv.metadata.internal.TeiidServer;
import io.syndesis.dv.openshift.TeiidOpenShiftClient;
import io.syndesis.dv.server.endpoint.DataVirtualizationService;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.teiid.runtime.EmbeddedConfiguration;

@ComponentScan(basePackageClasses = {DataVirtualizationService.class, DefaultMetadataInstance.class})
@TestConfiguration
public class ServiceTestConfiguration {

    @MockBean
    private TeiidOpenShiftClient TeiidOpenShiftClient;

    @Bean
    public TeiidServer teiidServer() {
        EmbeddedConfiguration ec = new EmbeddedConfiguration();
        TeiidServer server = new TeiidServer();
        server.start(ec);
        return server;
    }

    @MockBean(name="connectionExecutor")
    private ScheduledThreadPoolExecutor connectionExecutor;

}