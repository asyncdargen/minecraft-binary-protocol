package ru.dargen.binaryprotocol.packet.type;

import ru.dargen.binaryprotocol.network.NetworkBound;
import ru.dargen.binaryprotocol.network.NetworkPhase;

//Ids for 1.19.4 minecraft protocol
public class PacketTypes {

    public interface Login {

        interface Out {

            PacketType DISCONNECT = new PacketType(0x00, NetworkBound.CLIENT, NetworkPhase.LOGIN);
            PacketType ENCRYPTION_BEGIN = new PacketType(0x01, NetworkBound.CLIENT, NetworkPhase.LOGIN);
            PacketType SUCCESS = new PacketType(0x02, NetworkBound.CLIENT, NetworkPhase.LOGIN);
            PacketType SET_COMPRESSION = new PacketType(0x03, NetworkBound.CLIENT, NetworkPhase.LOGIN);
            PacketType CUSTOM_PAYLOAD = new PacketType(0x04, NetworkBound.CLIENT, NetworkPhase.LOGIN);

        }

        interface In {

            PacketType START = new PacketType(0x00, NetworkBound.SERVER, NetworkPhase.LOGIN);
            PacketType ENCRYPTION_BEGIN = new PacketType(0x01, NetworkBound.SERVER, NetworkPhase.LOGIN);
            PacketType CUSTOM_PAYLOAD = new PacketType(0x02, NetworkBound.SERVER, NetworkPhase.LOGIN);

        }

    }

}
