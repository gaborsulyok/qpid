/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.qpid.server.management.plugin.servlet.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.qpid.server.model.Binding;
import org.apache.qpid.server.model.Broker;
import org.apache.qpid.server.model.ConfiguredObject;
import org.apache.qpid.server.model.Connection;
import org.apache.qpid.server.model.Exchange;
import org.apache.qpid.server.model.Publisher;
import org.apache.qpid.server.model.Session;
import org.apache.qpid.server.model.Statistics;
import org.apache.qpid.server.model.VirtualHost;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

public class ConnectionServlet extends AbstractServlet
{


    private Broker _broker;
    private static final Comparator DEFAULT_COMPARATOR = new KeyComparator("name");


    public ConnectionServlet(Broker broker)
    {
        _broker = broker;
    }

    protected void onGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);

        response.setHeader("Cache-Control","no-cache");
        response.setHeader("Pragma","no-cache");
        response.setDateHeader ("Expires", 0);

        String[] sortKeys = request.getParameterValues("sort");
        Comparator comparator;
        if(sortKeys == null || sortKeys.length == 0)
        {
            comparator = DEFAULT_COMPARATOR;
        }
        else
        {
            comparator = new MapComparator(sortKeys);
        }


        Collection<VirtualHost> vhosts = _broker.getVirtualHosts();
        Collection<Connection> exchanges = new ArrayList<Connection>();
        List<Map<String,Object>> outputObject = new ArrayList<Map<String,Object>>();

        final PrintWriter writer = response.getWriter();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

        String vhostName = null;
        String connectionName = null;

        if(request.getPathInfo() != null && request.getPathInfo().length()>0)
        {
            String path = request.getPathInfo().substring(1);
            String[] parts = path.split("/");

            vhostName = parts.length == 0 ? "" : parts[0];
            if(parts.length > 1)
            {
                connectionName = parts[1];
                if(connectionName.equals("") && parts.length > 2)
                {
                    connectionName = "/" + parts[2];
                }
            }
        }

        for(VirtualHost vhost : vhosts)
        {
            if(vhostName == null || vhostName.equals(vhost.getName()))
            {

                for(Connection connection : vhost.getConnections())
                {
                    if(connectionName == null || connectionName.equals(connection.getName()))
                    {
                        outputObject.add(convertToObject(connection));
                    }
                    if(connectionName != null)
                    {
                        break;
                    }
                }
                if(vhostName != null)
                {
                    break;
                }
            }
        }

        Collections.sort(outputObject, comparator);
        mapper.writeValue(writer, outputObject);

    }

    private Map<String, Object> convertObjectToMap(final ConfiguredObject confObject)
    {
        Map<String, Object> object = new LinkedHashMap<String, Object>();

        for(String name : confObject.getAttributeNames())
        {
            Object value = confObject.getAttribute(name);
            if(value != null)
            {
                object.put(name, value);
            }
        }

        Statistics statistics = confObject.getStatistics();
        Map<String, Object> statMap = new HashMap<String, Object>();
        for(String name : statistics.getStatisticNames())
        {
            Object value = statistics.getStatistic(name);
            if(value != null)
            {
                statMap.put(name, value);
            }
        }

        if(!statMap.isEmpty())
        {
            object.put("statistics", statMap);
        }
        return object;
    }

    private Map<String,Object> convertToObject(final Connection connection)
    {
        Map<String, Object> object = convertObjectToMap(connection);


        List<Map<String,Object>> sessions = new ArrayList<Map<String, Object>>();

        for(Session session : connection.getSessions())
        {
            sessions.add(convertObjectToMap(session));
        }

        if(!sessions.isEmpty())
        {
            object.put("sessions", sessions);
        }



        return object;
    }

}
