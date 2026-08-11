# NearNode

NearNode is a decentralized, peer-to-peer (P2P) chat application for Android. 

Unlike traditional chat applications that rely on a central server, NearNode enables direct communication between devices on the same local network.

## Key Features

- **Peer-to-Peer Architecture**: No central server or intermediary is used for message exchange.
- **Service Discovery**: Automatically find other users on the local network using Android's Network Service Discovery (NSD).
- **Direct Communication**: Messages are sent directly between devices using secure socket connections.
- **Local Identity**: User profiles and data are managed locally on the device.

## How it Works

1. **Registration**: When the app starts, it registers a unique service on the local network via NSD.
2. **Discovery**: Users can search for other active NearNode instances nearby.
3. **Connection**: Once a peer is discovered, the app resolves its IP address and port to establish a direct socket connection for real-time messaging.
