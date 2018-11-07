package net.ayataka.kordis.entity.server

import kotlinx.serialization.json.json
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.Entity
import net.ayataka.kordis.entity.Mentionable
import net.ayataka.kordis.entity.Nameable
import net.ayataka.kordis.entity.server.permission.PermissionSet
import net.ayataka.kordis.entity.server.updater.RoleUpdater
import net.ayataka.kordis.rest.Endpoint
import net.ayataka.kordis.utils.uRgb
import java.awt.Color

interface Role : Mentionable, Nameable, Entity {
    /**
     * The server of this role
     */
    val server: Server

    /**
     * The allowed permissions of this role
     */
    val permissions: PermissionSet

    /**
     * The color of this role
     */
    val color: Color

    /**
     * The position of this role
     */
    val position: Int

    /**
     * Whether this role is hoisted
     *
     * Members in a hoisted role are displayed in their own grouping on the member list
     */
    val hoist: Boolean

    /**
     * Whether this role is managed
     */
    val managed: Boolean

    /**
     * Whether this role is mentionable
     */
    val mentionable: Boolean

    /**
     * The mention tag of this role
     */
    override val mention: String
        get() = "<@&$id>"

    suspend fun edit(block: RoleUpdater.() -> Unit) {
        val updater = RoleUpdater(this).apply(block)

        val json = json {
            if (updater.name != name) {
                "name" to updater.name
            }

            if (updater.hoist != hoist) {
                "hoist" to updater.hoist
            }

            if (updater.mentionable != mentionable) {
                "mentionable" to updater.mentionable
            }

            if (updater.permissions != permissions) {
                "permissions" to updater.permissions.compile()
            }

            if (updater.color != color) {
                "color" to updater.color.uRgb()
            }
        }

        if (json.isNotEmpty()) {
            (client as DiscordClientImpl).rest.request(
                    Endpoint.MODIFY_GUILD_ROLE.format(mapOf("guild.id" to server.id, "role.id" to id)),
                    json
            )
        }

        if (updater.position != position) {
            (client as DiscordClientImpl).rest.request(
                    Endpoint.MODIFY_GUILD_ROLE_POSITIONS.format(mapOf("guild.id" to server.id)),
                    json {
                        "id" to id
                        "position" to updater.position
                    }
            )
        }
    }
}