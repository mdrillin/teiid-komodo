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
package org.komodo.rest.service.unit;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.komodo.rest.RestLink.LinkType;
import org.komodo.rest.relational.KomodoRestUriBuilder.SettingNames;
import org.komodo.rest.relational.connection.RestConnection;
import org.komodo.rest.relational.json.KomodoJsonMarshaller;
import org.komodo.rest.relational.request.KomodoConnectionAttributes;
import org.komodo.rest.relational.response.RestConnectionSummary;

@SuppressWarnings( {"javadoc", "nls"} )
public class KomodoConnectionServiceTestInSuite extends AbstractKomodoServiceTest {

    @Rule
    public TestName testName = new TestName();

    public KomodoConnectionServiceTestInSuite() throws Exception {
        super();
    }

    @Test
    public void shouldGetConnections() throws Exception {
        String connectionName = "shouldGetConnections";
        createConnection(connectionName);

        // get
        URI uri = uriBuilder().workspaceConnectionsUri();
        HttpGet request = jsonRequest(uri, RequestType.GET);
        HttpResponse response = executeOk(request);

        String entities = extractResponse(response);
        assertThat(entities, is(notNullValue()));

        // System.out.println("Response:\n" + entities);
        // make sure the Dataservice JSON document is returned for each dataservice
        RestConnectionSummary[] connectionSummaries = KomodoJsonMarshaller.unmarshallArray(entities, RestConnectionSummary[].class);
        assertTrue(connectionSummaries.length > 0);

        RestConnection connection = null;
        for (RestConnectionSummary summary : connectionSummaries) {
        	RestConnection summaryConn = summary.getConnection();
            if (connectionName.equals(summaryConn.getId())) {
            	connection = summaryConn;
                break;
            }
        }
        assertNotNull(connection);
        assertNotNull(connection.getDataPath());
        assertNotNull(connection.getkType());
    }

    @Test
    public void shouldReturnEmptyListWhenNoDataservicesInWorkspace() throws Exception {
        URI uri = uriBuilder().workspaceDataservicesUri();
        HttpGet request = jsonRequest(uri, RequestType.GET);
        HttpResponse response = execute(request);

        String entity = extractResponse(response);
        assertThat(entity, is(notNullValue()));

        //System.out.println("Response:\n" + entity);

        RestConnection[] connections = KomodoJsonMarshaller.unmarshallArray(entity, RestConnection[].class);
        assertNotNull(connections);
        assertEquals(0, connections.length);
    }

    @Test
    public void shouldGetConnection() throws Exception {
        String connectionName = "shouldGetConnection";
        createConnection(connectionName);

        // get
        Properties settings = uriBuilder().createSettings(SettingNames.CONNECTION_NAME, connectionName);
        uriBuilder().addSetting(settings, SettingNames.PARENT_PATH, uriBuilder().workspaceConnectionsUri());

        URI uri = uriBuilder().connectionUri(LinkType.SELF, settings);
        HttpGet request = jsonRequest(uri, RequestType.GET);
        HttpResponse response = execute(request);

        String entity = extractResponse(response);
//        System.out.println("Response:\n" + entity);

        RestConnectionSummary connectionSummary = KomodoJsonMarshaller.unmarshall(entity, RestConnectionSummary.class);
        assertNotNull(connectionSummary);

        RestConnection conn = connectionSummary.getConnection();
        assertNotNull(conn);
        assertEquals(conn.getId(), connectionName);
    }

    @Test
    public void shouldFailCreateConnectionNoServiceCatalog() throws Exception {
        String connectionName = "shouldFailCreateConnectionNoServiceCatalog";

        // post
        Properties settings = uriBuilder().createSettings(SettingNames.CONNECTION_NAME, connectionName);
        uriBuilder().addSetting(settings, SettingNames.PARENT_PATH, uriBuilder().workspaceConnectionsUri());

        URI uri = uriBuilder().connectionUri(LinkType.SELF, settings);
        HttpPost request = jsonRequest(uri, RequestType.POST);

        KomodoConnectionAttributes rcAttr = new KomodoConnectionAttributes();
        rcAttr.setDescription("A description");

        addBody(request, rcAttr);
        HttpResponse response = execute(request);

        assertResponse(response, HttpStatus.SC_FORBIDDEN);
        String entity = extractResponse(response);
        assertTrue(entity.contains("missing one or more required parameters"));
    }

    @Test
    public void shouldFailUpdateConnectionNoServiceCatalog() throws Exception {
        String connectionName = "shouldFailUpdateConnectionNoServiceCatalog";

        createConnection(connectionName);

        // put
        Properties settings = uriBuilder().createSettings(SettingNames.CONNECTION_NAME, connectionName);
        uriBuilder().addSetting(settings, SettingNames.PARENT_PATH, uriBuilder().workspaceConnectionsUri());

        URI uri = uriBuilder().connectionUri(LinkType.SELF, settings);
        HttpPut request = jsonRequest(uri, RequestType.PUT);

        KomodoConnectionAttributes rcAttr = new KomodoConnectionAttributes();
        rcAttr.setDescription("A description");

        addBody(request, rcAttr);
        HttpResponse response = execute(request);

        assertResponse(response, HttpStatus.SC_FORBIDDEN);
        String entity = extractResponse(response);
        assertTrue(entity.contains("missing one or more required parameters"));
    }
}
