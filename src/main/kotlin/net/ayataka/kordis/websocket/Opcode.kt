package net.ayataka.kordis.websocket

enum class Opcode(val code: Int) {
    /**
     * dispatches an event (Receive)
     */
    DISPATCH(0),

    /**
     * used for ping checking (Send/Receive)
     */
    HEARTBEAT(1),

    /**
     * used for client handshake (Send)
     */
    IDENTIFY(2),

    /**
     * used to update the client status (Send)
     */
    STATUS_UPDATE(3),

    /**
     * used to join/move/leave voice channels (Send)
     */
    VOICE_STATE_UPDATE(4),

    /**
     * used to resume a closed connection (Send)
     */
    RESUME(6),

    /**
     * used to tell clients to reconnect to the gateway (Receive)
     */
    RECONNECT(7),

    /**
     * used to request guild members (Send)
     */
    REQUEST_GUILD_MEMBERS(8),

    /**
     * used to notify client they have an invalid session id (Receive)
     */
    INVALID_SESSION(9),

    /**
     * sent immediately after connecting, contains heartbeat and server debug information (Receive)
     */
    HELLO(10),

    /**
     * sent immediately following a client heartbeat that was received (Receive)
     */
    HEARTBEAT_ACK(11)
}