package net.ayataka.kordis.entity

import net.ayataka.kordis.entity.collection.NameableEntitySet
import net.ayataka.kordis.entity.message.Message
import net.ayataka.kordis.entity.server.channel.ServerChannel
import net.ayataka.kordis.entity.server.channel.ServerChannelBuilder
import net.ayataka.kordis.entity.server.channel.category.ChannelCategory
import net.ayataka.kordis.entity.server.channel.text.ServerTextChannel
import net.ayataka.kordis.entity.server.channel.voice.ServerVoiceChannel
import net.ayataka.kordis.entity.server.member.Member
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.entity.server.permission.PermissionSet
import net.ayataka.kordis.entity.server.role.Role
import net.ayataka.kordis.entity.user.User
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Bulk delete messages
 */
suspend fun Collection<Message>.deleteAll() {
    mapNotNull { it.channel as? ServerTextChannel }.distinctBy { it }.forEach {
        it.deleteMessages(this
                .filter { it.channel == it }
                .filter { it.timestamp.isAfter(Instant.now().minus(14, ChronoUnit.DAYS)) }
                .map { it.id })
    }
}

/**
 * Edit the channel
 */
suspend fun ServerChannel.edit(block: ServerChannelBuilder.() -> Unit) {
    (this as? ServerTextChannel)?.edit(block) ?: (this as? ServerVoiceChannel)?.edit(block) ?: (this as? ChannelCategory)?.edit(block)
}

/**
 * Create a permission set
 */
fun permissions(vararg permission: Permission) = PermissionSet().apply { addAll(permission) }

/**
 * Create a permission set from bitmask
 */
fun Int.toPermissions() = PermissionSet(this)

/**
 * Get the member by a user
 */
fun NameableEntitySet<Member>.find(user: User) = find(user.id)

/**
 * Get a member by its tag
 */
fun NameableEntitySet<Member>.findByTag(tag: String, ignoreCase: Boolean = false): Member? =
        find { it.tag.equals(tag, ignoreCase) }

/**
 * Get a user by its tag
 */
fun NameableEntitySet<User>.findByTag(tag: String, ignoreCase: Boolean = false): User? =
        find { it.tag.equals(tag, ignoreCase) }

/**
 * Get the current bot user as member
 */
val NameableEntitySet<Member>.botUser
    get() = find { it.id == it.client.botUser.id }!!

/**
 * Get the '@everyone' role
 */
val NameableEntitySet<Role>.everyone
    get() = find { it.position == 0 && it.name == "@everyone" }
            ?: throw IllegalStateException("couldn't find @everyone role")