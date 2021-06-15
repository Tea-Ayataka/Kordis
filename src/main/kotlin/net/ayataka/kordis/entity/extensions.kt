package net.ayataka.kordis.entity

import net.ayataka.kordis.entity.collection.NameableEntitySet
import net.ayataka.kordis.entity.message.Message
import net.ayataka.kordis.entity.server.channel.ServerChannel
import net.ayataka.kordis.entity.server.channel.ServerChannelBuilder
import net.ayataka.kordis.entity.server.channel.category.ChannelCategory
import net.ayataka.kordis.entity.server.channel.text.ServerTextChannel
import net.ayataka.kordis.entity.server.channel.voice.ServerVoiceChannel
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.entity.server.permission.PermissionSet
import net.ayataka.kordis.entity.server.role.Role
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Bulk delete messages
 */
suspend fun Collection<Message>.deleteAll() {
    mapNotNull { it.channel as? ServerTextChannel }.distinctBy { it }.forEach { channel ->
        channel.deleteMessages(this
                .filter { it.channel == channel }
                .filter { it.timestamp.isAfter(Instant.now().minus(14, ChronoUnit.DAYS)) }
                .map { it.id })
    }
}

/**
 * Edit the channel
 */
suspend fun ServerChannel.edit(block: ServerChannelBuilder.() -> Unit) {
    (this as? ServerTextChannel)?.edit(block) ?: (this as? ServerVoiceChannel)?.edit(block)
    ?: (this as? ChannelCategory)?.edit(block)
}

/**
 * Create a permission set
 */
fun permissions(vararg permission: Permission) = PermissionSet().apply { addAll(permission) }

/**
 * Create a permission set from bitmask
 */
fun Int.toPermissions() = PermissionSet(this.toLong())
fun Long.toPermissions() = PermissionSet(this)

/**
 * Get the '@everyone' role
 */
val NameableEntitySet<Role>.everyone
    get() = find { it.id == it.server.id }
            ?: throw IllegalStateException("couldn't find @everyone role")