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
package org.apache.qpid.server.store.berkeleydb.replication;

import java.util.Collection;

import org.apache.qpid.server.model.ReplicationNode;
import org.apache.qpid.server.model.State;
import org.apache.qpid.server.model.VirtualHost;
import org.apache.qpid.server.store.berkeleydb.EnvironmentFacade;
import org.apache.qpid.server.store.berkeleydb.EnvironmentFacadeFactory;

public class ReplicatedEnvironmentFacadeFactory implements EnvironmentFacadeFactory
{

    @Override
    public EnvironmentFacade createEnvironmentFacade(VirtualHost virtualHost, boolean isMessageStore)
    {
        Collection<ReplicationNode> replicationNodes = virtualHost.getChildren(ReplicationNode.class);
        if (replicationNodes == null || replicationNodes.size() != 1)
        {
            throw new IllegalStateException("Expected exactly one replication node but got "
                    + (replicationNodes == null ? 0 : replicationNodes.size()) + " nodes");
        }
        ReplicationNode localNode = replicationNodes.iterator().next();
        if (!(localNode instanceof LocalReplicationNode))
        {
            throw new IllegalStateException("Cannot find local replication node among virtual host nodes");
        }
        LocalReplicationNode localReplicationNode = (LocalReplicationNode) localNode;
        localReplicationNode.attainDesiredState();

        if (localReplicationNode.getActualState() == State.ACTIVE)
        {
            return localReplicationNode.getReplicatedEnvironmentFacade();
        }

        throw new IllegalStateException("Cannot create environment facade as the replication node is not in the right state");

    }

    @Override
    public String getType()
    {
        return ReplicatedEnvironmentFacade.TYPE;
    }
}
