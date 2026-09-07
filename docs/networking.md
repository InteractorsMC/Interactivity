# Networking

_[Go back](README.md)_

## Packets that affect physics
Client inputs affecting the predicted physics state are recorded so the client can re-simulate them after receiving an authoritative server state.
This is required for [client-side prediction](physics.md#client-side-prediction), because all packets sent from client are stored to be able to recalculate physics on errors.

## Network Pipeline
Here's how network pipeline is built.

Default networking transports:
- TCP (Minecraft networking)
- UDP (Can be disabled on client because client can have network issues)
- Local

All bodies spawn/remove/data packets must be sent using Minecraft Networking to ensure that they reach client in case of any lost.
All physical bodies states and player inputs must be sent using Network Manager to ensure using the fastest transport.

Player connection initialization for UDP. This example does mix implementation and abstraction logic for more clarity.
(protocol doesn't really matter here, but we using it because TCP is a Minecraft Networking Wrapper,
and Local Transport doesn't have any networking logic, so both doesn't require any configuration)
```
                                Player Join
                                     v
                      `NetworkManager#configurePlayer`
                                     v
    Network manager sortes all transports by priority and availability
                  `NetworkTransport#isAvailable(Player player)`
                                     v
`NetworkManagerImpl#tryProtocols(NetworkTransport[] availableProtocols)
    We assume udp have highest priority and available for player
                                     v
`NetworkTransport#initializeConnection(Player player, Consumer<Boolean> onComplete)`
UDPTransport will send player a packet using Minecraft Networking that will contain
connection address and port. Once receiveing a packet, client will send UDP packet,
     and wait for server response, finishing configuration by sending a packet
      that states the fact client-server connection works fine in both sides.
                                     v
  Once finishing handshake NetworkTransport will call our `onComplete` callback.
    If handshake fails (took >10 seconds, can be specified in config), we call
 NetworkManagerImpl#tryProtocols, but remove UDPTransport from availableProtocols.
However we can retry handshakes in future, if client haven't disabled UDP transport.
```