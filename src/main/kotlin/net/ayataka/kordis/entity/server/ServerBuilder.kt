package net.ayataka.kordis.entity.server

class ServerBuilder(val server: Server) {
    var name = server.name
    var region = server.region
    var afkChannel = server.afkChannel
    var afkTimeout = server.afkTimeout
    var defaultMessageNotificationLevel = server.defaultMessageNotificationLevel
    var verificationLevel = server.verificationLevel
    var explicitContentFilterLevel = server.explicitContentFilterLevel
    var mfaLevel = server.mfaLevel
    var icon: ByteArray? = null
}