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

/*
 * This file is auto-generated by Qpid Gentools v.0.1 - do not modify.
 * Supported AMQP version:
 *   8-0
 */

package org.apache.qpid.framing;

import java.io.DataOutput;
import java.io.IOException;

import org.apache.qpid.AMQException;
import org.apache.qpid.codec.MarkableDataInput;

public class ChannelOpenOkBody extends AMQMethodBodyImpl implements EncodableAMQDataBlock, AMQMethodBody
{

    public static final int CLASS_ID =  20;
    public static final int METHOD_ID = 11;

    public static final ChannelOpenOkBody INSTANCE_0_8 = new ChannelOpenOkBody(true);
    public static final ChannelOpenOkBody INSTANCE_0_9 = new ChannelOpenOkBody(false);

    public static ChannelOpenOkBody getInstance(ProtocolVersion protocolVersion, MarkableDataInput input)
            throws IOException
    {
        final boolean isAMQP08 = ProtocolVersion.v8_0.equals(protocolVersion);
        ChannelOpenOkBody instance = isAMQP08 ? INSTANCE_0_8 : INSTANCE_0_9;
        if(!isAMQP08)
        {
            EncodingUtils.readBytes(input);
        }
        return instance;
    }
    // Fields declared in specification
    private final boolean _isAMQP08;
    // Constructor

    private ChannelOpenOkBody(boolean isAMQP08)
    {
        _isAMQP08 = isAMQP08;
    }

    public int getClazz()
    {
        return CLASS_ID;
    }

    public int getMethod()
    {
        return METHOD_ID;
    }


    protected int getBodySize()
    {
        return _isAMQP08 ? 0 : 4;
    }

    public void writeMethodPayload(DataOutput buffer) throws IOException
    {
        if(!_isAMQP08)
        {
            buffer.writeInt(0);
        }
    }

    public boolean execute(MethodDispatcher dispatcher, int channelId) throws AMQException
	{
        return dispatcher.dispatchChannelOpenOk(this, channelId);
	}

    public String toString()
    {
        return "[ChannelOpenOkBody]";
    }

    public static void process(final MarkableDataInput in,
                               final ProtocolVersion protocolVersion,
                               final ClientChannelMethodProcessor dispatcher) throws IOException
    {
        if(!ProtocolVersion.v8_0.equals(protocolVersion))
        {
            EncodingUtils.readBytes(in);
        }

        if(!dispatcher.ignoreAllButCloseOk())
        {
            dispatcher.receiveChannelOpenOk();
        }
    }
}
