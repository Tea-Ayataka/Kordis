package net.ayataka.kordis.entity.server.channel

interface ServerVoiceChannel : ServerChannel {
    val category: Category?

    /**
     * The bitrate of this voice channel
     */
    val bitrate: Int

    /**
     * The user limit of this voice channel
     * 0 for unlimited
     */
    val userLimit: Int
}