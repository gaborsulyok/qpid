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
package org.apache.qpid.framing;

import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;

import java.nio.charset.Charset;

public class EncodingUtils
{
    private static final Logger _logger = Logger.getLogger(EncodingUtils.class);

    private static final String STRING_ENCODING = "iso8859-15";

    private static final Charset _charset = Charset.forName("iso8859-15");

    public static final int SIZEOF_UNSIGNED_SHORT = 2;
    public static final int SIZEOF_UNSIGNED_INT = 4;

    public static int encodedShortStringLength(String s)
    {
        if (s == null)
        {
            return 1;
        }
        else
        {
            return (short) (1 + s.length());
        }
    }

    public static int encodedLongStringLength(String s)
    {
        if (s == null)
        {
            return 4;
        }
        else
        {
            return 4 + s.length();
        }
    }

    public static int encodedLongStringLength(char[] s)
    {
        if (s == null)
        {
            return 4;
        }
        else
        {
            return 4 + s.length;
        }
    }

    public static int encodedLongstrLength(byte[] bytes)
    {
        if (bytes == null)
        {
            return 4;
        }
        else
        {
            return 4 + bytes.length;
        }
    }

    public static int encodedFieldTableLength(FieldTable table)
    {
        if (table == null)
        {
            // length is encoded as 4 octets
            return 4;
        }
        else
        {
            // length of the table plus 4 octets for the length
            return (int) table.getEncodedSize() + 4;
        }
    }
    
    public static int encodedContentLength(Content content)
    {
       	return (int)content.getEncodedSize();
    }
 
    public static void writeShortStringBytes(ByteBuffer buffer, String s)
    {
        if (s != null)
        {
            byte[] encodedString = new byte[s.length()];
            char[] cha = s.toCharArray();
            for (int i = 0; i < cha.length; i++)
            {
                encodedString[i] = (byte) cha[i];
            }
            writeBytes(buffer, encodedString);
        }
        else
        {
            // really writing out unsigned byte
            buffer.put((byte) 0);
        }
    }

    public static void writeLongStringBytes(ByteBuffer buffer, String s)
    {
        assert s == null || s.length() <= 0xFFFE;
        if (s != null)
        {
            int len = s.length();
            writeUnsignedInteger(buffer, s.length());
            byte[] encodedString = new byte[len];
            char[] cha = s.toCharArray();
            for (int i = 0; i < cha.length; i++)
            {
                encodedString[i] = (byte) cha[i];
            }
            buffer.put(encodedString);
        }
        else
        {
            writeUnsignedInteger(buffer, 0);
        }
    }

    public static void writeLongStringBytes(ByteBuffer buffer, char[] s)
    {
        assert s == null || s.length <= 0xFFFE;
        if (s != null)
        {
            int len = s.length;
            writeUnsignedInteger(buffer, s.length);
            byte[] encodedString = new byte[len];
            for (int i = 0; i < s.length; i++)
            {
                encodedString[i] = (byte) s[i];
            }
            buffer.put(encodedString);
        }
        else
        {
            writeUnsignedInteger(buffer, 0);
        }
    }

    public static void writeLongStringBytes(ByteBuffer buffer, byte[] bytes)
    {
        assert bytes == null || bytes.length <= 0xFFFE;
        if (bytes != null)
        {
            writeUnsignedInteger(buffer, bytes.length);
            buffer.put(bytes);
        }
        else
        {
            writeUnsignedInteger(buffer, 0);
        }
    }

    public static void writeUnsignedByte(ByteBuffer buffer, short b)
    {
        byte bv = (byte) b;
        buffer.put(bv);
    }

    public static void writeUnsignedShort(ByteBuffer buffer, int s)
    {
        // TODO: Is this comparison safe? Do I need to cast RHS to long?
        if (s < Short.MAX_VALUE)
        {
            buffer.putShort((short) s);
        }
        else
        {
            short sv = (short) s;
            buffer.put((byte) (0xFF & (sv >> 8)));
            buffer.put((byte) (0xFF & sv));
        }
    }


    public static long unsignedIntegerLength()
    {
        return 4;
    }

    public static void writeUnsignedInteger(ByteBuffer buffer, long l)
    {
        // TODO: Is this comparison safe? Do I need to cast RHS to long?
        if (l < Integer.MAX_VALUE)
        {
            buffer.putInt((int) l);
        }
        else
        {
            int iv = (int) l;

            // FIXME: This *may* go faster if we build this into a local 4-byte array and then
            // put the array in a single call.
            buffer.put((byte) (0xFF & (iv >> 24)));
            buffer.put((byte) (0xFF & (iv >> 16)));
            buffer.put((byte) (0xFF & (iv >> 8)));
            buffer.put((byte) (0xFF & iv));
        }
    }

    public static void writeFieldTableBytes(ByteBuffer buffer, FieldTable table)
    {
        if (table != null)
        {
            table.writeToBuffer(buffer);
        }
        else
        {
            EncodingUtils.writeUnsignedInteger(buffer, 0);
        }
    }

    public static void writeContentBytes(ByteBuffer buffer, Content content)
    {
    	content.writePayload(buffer);
    }

    public static void writeBooleans(ByteBuffer buffer, boolean[] values)
    {
        byte packedValue = 0;
        for (int i = 0; i < values.length; i++)
        {
            if (values[i])
            {
                packedValue = (byte) (packedValue | (1 << i));
            }
        }

        buffer.put(packedValue);
    }

    /**
     * This is used for writing longstrs.
     *
     * @param buffer
     * @param data
     */
    public static void writeLongstr(ByteBuffer buffer, byte[] data)
    {
        if (data != null)
        {
            writeUnsignedInteger(buffer, data.length);
            buffer.put(data);
        }
        else
        {
            writeUnsignedInteger(buffer, 0);
        }
    }

    public static void writeTimestamp(ByteBuffer buffer, long timestamp)
    {
        writeUnsignedInteger(buffer, 0/*timestamp msb*/);
        writeUnsignedInteger(buffer, timestamp);
    }

    public static boolean[] readBooleans(ByteBuffer buffer)
    {
        byte packedValue = buffer.get();
        boolean[] result = new boolean[8];

        for (int i = 0; i < 8; i++)
        {
            result[i] = ((packedValue & (1 << i)) != 0);
        }
        return result;
    }

    public static FieldTable readFieldTable(ByteBuffer buffer) throws AMQFrameDecodingException
    {
        long length = buffer.getUnsignedInt();
        if (length == 0)
        {
            return null;
        }
        else
        {
            return FieldTableFactory.newFieldTable(buffer, length);
        }
    }

    public static Content readContent(ByteBuffer buffer) throws AMQFrameDecodingException
    {
        long length = buffer.getUnsignedInt();
    	Content content = new Content();
        content.populateFromBuffer(buffer, length);
        return content;
    }

    public static String readShortString(ByteBuffer buffer)
    {
        short length = buffer.getUnsigned();
        if (length == 0)
        {
            return null;
        }
        else
        {
            // this may seem rather odd to declare two array but testing has shown
            // that constructing a string from a byte array is 5 (five) times slower
            // than constructing one from a char array.
            // this approach here is valid since we know that all the chars are
            // ASCII (0-127)
            byte[] stringBytes = new byte[length];
            buffer.get(stringBytes, 0, length);
            char[] stringChars = new char[length];
            for (int i = 0; i < stringChars.length; i++)
            {
                stringChars[i] = (char) stringBytes[i];
            }

            return new String(stringChars);
        }
    }

    public static String readLongString(ByteBuffer buffer)
    {
        long length = buffer.getUnsignedInt();
        if (length == 0)
        {
            return "";
        }
        else
        {
            // this may seem rather odd to declare two array but testing has shown
            // that constructing a string from a byte array is 5 (five) times slower
            // than constructing one from a char array.
            // this approach here is valid since we know that all the chars are
            // ASCII (0-127)
            byte[] stringBytes = new byte[(int) length];
            buffer.get(stringBytes, 0, (int) length);
            char[] stringChars = new char[(int) length];
            for (int i = 0; i < stringChars.length; i++)
            {
                stringChars[i] = (char) stringBytes[i];
            }
            return new String(stringChars);
        }
    }

    public static byte[] readLongstr(ByteBuffer buffer) throws AMQFrameDecodingException
    {
        long length = buffer.getUnsignedInt();
        if (length == 0)
        {
            return null;
        }
        else
        {
            byte[] result = new byte[(int) length];
            buffer.get(result);
            return result;
        }
    }

    public static long readTimestamp(ByteBuffer buffer)
    {
        // Discard msb from AMQ timestamp
        buffer.getUnsignedInt();
        return buffer.getUnsignedInt();
    }


    static byte[] hexToByteArray(String id)
    {
        // Should check param for null, long enough for this check, upper-case and trailing char
        String s = (id.charAt(1) == 'x') ? id.substring(2) : id;    // strip 0x

        int len = s.length();
        int byte_len = len / 2;
        byte[] b = new byte[byte_len];

        for (int i = 0; i < byte_len; i++)
        {
            // fixme: refine these repetitive subscript calcs.
            int ch = i * 2;

            byte b1 = Byte.parseByte(s.substring(ch, ch + 1), 16);
            byte b2 = Byte.parseByte(s.substring(ch + 1, ch + 2), 16);

            b[i] = (byte) (b1 * 16 + b2);
        }

        return (b);
    }

    public static char[] convertToHexCharArray(byte[] from)
    {
        int length = from.length;
        char[] result_buff = new char[length * 2 + 2];

        result_buff[0] = '0';
        result_buff[1] = 'x';

        int bite;
        int dest = 2;

        for (int i = 0; i < length; i++)
        {
            bite = from[i];

            if (bite < 0)
            {
                bite += 256;
            }

            result_buff[dest++] = hex_chars[bite >> 4];
            result_buff[dest++] = hex_chars[bite & 0x0f];
        }

        return (result_buff);
    }

    public static String convertToHexString(byte[] from)
    {
        return (new String(convertToHexCharArray(from)));
    }

    public static String convertToHexString(ByteBuffer bb)
    {
        int size = bb.limit();

        byte[] from = new byte[size];

        // Is this not the same.
        //bb.get(from, 0, length);
        for (int i = 0; i < size; i++)
        {
            from[i] = bb.get(i);
        }

        return (new String(convertToHexCharArray(from)));
    }

    private static char hex_chars[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    //**** new methods

    // AMQP_BOOLEAN_PROPERTY_PREFIX

    public static void writeBoolean(ByteBuffer buffer, Boolean aBoolean)
    {
        buffer.put((byte) (aBoolean ? 1 : 0));
    }

    public static Boolean readBoolean(ByteBuffer buffer)
    {
        byte packedValue = buffer.get();
        return (packedValue == 1);
    }

    public static int encodedBooleanLength()
    {
        return 1;
    }

    // AMQP_BYTE_PROPERTY_PREFIX
    public static void writeByte(ByteBuffer buffer, Byte aByte)
    {
        buffer.put(aByte);
    }

    public static Byte readByte(ByteBuffer buffer)
    {
        return buffer.get();
    }

    public static int encodedByteLength()
    {
        return 1;
    }


    // AMQP_SHORT_PROPERTY_PREFIX
    public static void writeShort(ByteBuffer buffer, Short aShort)
    {
        buffer.putShort(aShort);
    }

    public static Short readShort(ByteBuffer buffer)
    {
        return buffer.getShort();
    }

    public static int encodedShortLength()
    {
        return 2;
    }

    // INTEGER_PROPERTY_PREFIX
    public static void writeInteger(ByteBuffer buffer, Integer aInteger)
    {
        buffer.putInt(aInteger);
    }

    public static Integer readInteger(ByteBuffer buffer)
    {
        return buffer.getInt();
    }

    public static int encodedIntegerLength()
    {
        return 4;
    }

    // AMQP_LONG_PROPERTY_PREFIX
    public static void writeLong(ByteBuffer buffer, Long aLong)
    {
        buffer.putLong(aLong);
    }

    public static Long readLong(ByteBuffer buffer)
    {
        return buffer.getLong();
    }

    public static int encodedLongLength()
    {
        return 8;
    }

    // Float_PROPERTY_PREFIX
    public static void writeFloat(ByteBuffer buffer, Float aFloat)
    {
        buffer.putFloat(aFloat);
    }

    public static Float readFloat(ByteBuffer buffer)
    {
        return buffer.getFloat();
    }

    public static int encodedFloatLength()
    {
        return 4;
    }


    // Double_PROPERTY_PREFIX
    public static void writeDouble(ByteBuffer buffer, Double aDouble)
    {
        buffer.putDouble(aDouble);
    }

    public static Double readDouble(ByteBuffer buffer)
    {
        return buffer.getDouble();
    }

    public static int encodedDoubleLength()
    {
        return 8;
    }


    public static byte[] readBytes(ByteBuffer buffer)
    {
        short length = buffer.getUnsigned();
        if (length == 0)
        {
            return null;
        }
        else
        {
            byte[] dataBytes = new byte[length];
            buffer.get(dataBytes, 0, length);

            return dataBytes;
        }
    }

    public static void writeBytes(ByteBuffer buffer, byte[] data)
    {
        if (data != null)
        {
            // TODO: check length fits in an unsigned byte            
            writeUnsignedByte(buffer, (short) data.length);
            buffer.put(data);
        }
        else
        {
            // really writing out unsigned byte
            buffer.put((byte) 0);
        }
    }

    //CHAR_PROPERTY
    public static int encodedCharLength()
    {
        return encodedByteLength();
    }

    public static char readChar(ByteBuffer buffer)
    {
        //This is valid as we know that the Character is ASCII 0..127
        return (char) buffer.get();
    }

    public static void writeChar(ByteBuffer buffer, char character)
    {
        //This is valid as we know that the Character is ASCII 0..127
        writeByte(buffer, (byte) character);
    }

}
