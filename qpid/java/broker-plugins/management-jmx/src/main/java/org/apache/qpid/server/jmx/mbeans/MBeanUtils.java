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
package org.apache.qpid.server.jmx.mbeans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.management.JMException;
import javax.management.OperationsException;

import org.apache.log4j.Logger;
import org.apache.qpid.server.jmx.AMQManagedObject;
import org.apache.qpid.server.jmx.MBeanProvider;
import org.apache.qpid.server.jmx.ManagedObject;
import org.apache.qpid.server.model.ConfiguredObject;
import org.apache.qpid.server.model.ConfiguredObjectFinder;
import org.apache.qpid.server.model.Exchange;
import org.apache.qpid.server.model.Queue;
import org.apache.qpid.server.model.VirtualHost;
import org.apache.qpid.server.plugin.QpidServiceLoader;

public class MBeanUtils
{
    private static final Logger LOGGER = Logger.getLogger(MBeanUtils.class);

    public static Queue findQueueFromQueueName(VirtualHost virtualHost, String queueName) throws OperationsException
    {
        Queue queue = ConfiguredObjectFinder.findConfiguredObjectByName(virtualHost.getQueues(), queueName);
        if (queue == null)
        {
            throw new OperationsException("No such queue \""+queueName+"\"");
        }
        else
        {
            return queue;
        }
    }

    public static  Exchange findExchangeFromExchangeName(VirtualHost virtualHost, String exchangeName) throws OperationsException
    {
        Exchange exchange = ConfiguredObjectFinder.findConfiguredObjectByName(virtualHost.getExchanges(), exchangeName);
        if (exchange == null)
        {
            throw new OperationsException("No such exchange \""+exchangeName+"\"");
        }
        else
        {
            return exchange;
        }
    }

    public static Collection<ManagedObject> createAdditionalMBeansFromProviders(ConfiguredObject child, AMQManagedObject mbean) throws JMException
    {
        List<ManagedObject> mbeans = new ArrayList<ManagedObject>();
        QpidServiceLoader<MBeanProvider> qpidServiceLoader = new QpidServiceLoader<MBeanProvider>();
        for (MBeanProvider provider : qpidServiceLoader.instancesOf(MBeanProvider.class))
        {
            if (provider.isChildManageableByMBean(child))
            {
                if(LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Provider of type " + provider.getType() + " will create MBean for child : " + child);
                }
                ManagedObject childMBean = provider.createMBean(child, mbean);
                mbeans.add(childMBean);
            }
        }
        return mbeans;
    }

}
