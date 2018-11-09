package net.ayataka.kordis.entity.server.updater

import net.ayataka.kordis.entity.server.Server

class ServerUpdater(val server: Server) {
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