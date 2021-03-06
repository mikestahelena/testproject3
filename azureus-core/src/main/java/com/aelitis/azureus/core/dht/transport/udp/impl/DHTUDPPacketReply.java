/*
 * Created on 21-Jan-2005
 * Created by Paul Gardner
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package com.aelitis.azureus.core.dht.transport.udp.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

import com.aelitis.azureus.core.dht.DHT;
import com.aelitis.azureus.core.dht.netcoords.DHTNetworkPosition;
import com.aelitis.azureus.core.dht.transport.DHTTransportContact;
import com.aelitis.azureus.core.dht.transport.udp.DHTTransportUDP;
import com.aelitis.azureus.core.dht.transport.udp.impl.packethandler.DHTUDPPacketNetworkHandler;
import com.aelitis.net.udp.uc.PRUDPPacketReply;

/**
 * @author parg
 * 
 */

public class DHTUDPPacketReply extends PRUDPPacketReply implements DHTUDPPacket {
    public static final int DHT_HEADER_SIZE = PRUDPPacketReply.PR_HEADER_SIZE + 8 + // con id
            1 + // ver
            1 + // net
            4 + // instance
            1 + // flags
            1; // flags2

    private DHTTransportUDPImpl transport;

    private long connection_id;
    private byte protocol_version;
    private byte vendor_id = DHTTransportUDP.VENDOR_ID_NONE;
    private int network;
    private int target_instance_id;
    private byte flags;
    private byte flags2;

    private long skew;

    private DHTNetworkPosition[] network_positions;

    public DHTUDPPacketReply(DHTTransportUDPImpl _transport, int _type, int _trans_id, long _conn_id, DHTTransportContact _local_contact,
            DHTTransportContact _remote_contact) {
        super(_type, _trans_id);

        transport = _transport;

        connection_id = _conn_id;

        protocol_version = _remote_contact.getProtocolVersion();

        // System.out.println( "reply to " + _remote_contact.getAddress() + ", proto=" + protocol_version );

        // the target might be at a higher protocol version that us, so trim back if necessary
        // as we obviously can't talk a higher version than what we are!

        if (protocol_version > _transport.getProtocolVersion()) {

            protocol_version = _transport.getProtocolVersion();
        }

        target_instance_id = _local_contact.getInstanceID();

        skew = _remote_contact.getClockSkew();

        flags = transport.getGenericFlags();

        flags2 = transport.getGenericFlags2();
    }

    protected DHTUDPPacketReply(DHTUDPPacketNetworkHandler network_handler, InetSocketAddress originator, DataInputStream is, int type, int trans_id)

    throws IOException {
        super(type, trans_id);

        setAddress(originator);

        connection_id = is.readLong();

        protocol_version = is.readByte();

        // System.out.println( "reply prot=" + protocol_version );

        if (protocol_version >= DHTTransportUDP.PROTOCOL_VERSION_VENDOR_ID) {

            vendor_id = is.readByte();
        }

        if (protocol_version >= DHTTransportUDP.PROTOCOL_VERSION_NETWORKS) {

            network = is.readInt();
        }

        if (protocol_version < (network == DHT.NW_CVS ? DHTTransportUDP.PROTOCOL_VERSION_MIN_CVS : DHTTransportUDP.PROTOCOL_VERSION_MIN)) {

            throw (DHTUDPUtils.INVALID_PROTOCOL_VERSION_EXCEPTION);
        }

        // we can only get the correct transport after decoding the network...

        transport = network_handler.getTransport(this);

        target_instance_id = is.readInt();

        if (protocol_version >= DHTTransportUDP.PROTOCOL_VERSION_PACKET_FLAGS) {

            flags = is.readByte();
        }

        if (protocol_version >= DHTTransportUDP.PROTOCOL_VERSION_PACKET_FLAGS2) {

            flags2 = is.readByte();
        }
    }

    public DHTTransportUDPImpl getTransport() {
        return (transport);
    }

    protected int getTargetInstanceID() {
        return (target_instance_id);
    }

    public long getConnectionId() {
        return (connection_id);
    }

    protected long getClockSkew() {
        return (skew);
    }

    public byte getProtocolVersion() {
        return (protocol_version);
    }

    protected byte getVendorID() {
        return (vendor_id);
    }

    public int getNetwork() {
        return (network);
    }

    public byte getGenericFlags() {
        return (flags);
    }

    public byte getGenericFlags2() {
        return (flags2);
    }

    public void setNetwork(int _network) {
        network = _network;
    }

    protected DHTNetworkPosition[] getNetworkPositions() {
        return (network_positions);
    }

    protected void setNetworkPositions(DHTNetworkPosition[] _network_positions) {
        network_positions = _network_positions;
    }

    public void serialise(DataOutputStream os)

    throws IOException {
        super.serialise(os);

        // add to this and you need to adjust HEADER_SIZE above

        os.writeLong(connection_id);

        os.writeByte(protocol_version);

        if (protocol_version >= DHTTransportUDP.PROTOCOL_VERSION_VENDOR_ID) {

            os.writeByte(DHTTransportUDP.VENDOR_ID_ME);
        }

        if (protocol_version >= DHTTransportUDP.PROTOCOL_VERSION_NETWORKS) {

            os.writeInt(network);
        }

        os.writeInt(target_instance_id);

        if (protocol_version >= DHTTransportUDP.PROTOCOL_VERSION_PACKET_FLAGS) {

            os.writeByte(flags);
        }

        if (protocol_version >= DHTTransportUDP.PROTOCOL_VERSION_PACKET_FLAGS2) {

            os.writeByte(flags2);
        }
    }

    public String getString() {
        return (super.getString() + ",[con=" + connection_id + ",prot=" + protocol_version + ",ven=" + vendor_id + ",net=" + network + ",fl="
                + flags + "/" + flags2 + "]");
    }
}