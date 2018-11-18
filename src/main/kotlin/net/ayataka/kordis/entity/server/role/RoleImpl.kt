package net.ayataka.kordis.entity.server.role

import com.google.gson.JsonObject
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.Updatable
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.permission.Permission
import net.ayataka.kordis.entity.server.permission.PermissionSet
import net.ayataka.kordis.exception.NotFoundException
import net.ayataka.kordis.rest.Endpoint
import net.ayataka.kordis.utils.isNotEmpty
import net.ayataka.kordis.utils.json
import net.ayataka.kordis.utils.uRgb
import java.awt.Color

class RoleImpl(override val server: Server, client: DiscordClientImpl, json: JsonObject) : Role, Updatable, DiscordEntity(client, json["id"].asLong) {
    @Volatile override var name: String = ""
    @Volatile override var permissions: PermissionSet = PermissionSet(0)
    @Volatile override var color: Color = Color.BLACK
    @Volatile override var position: Int = -1
    @Volatile override var hoist: Boolean = false
    @Volatile override var managed: Boolean = false
    @Volatile override var mentionable: Boolean = false

    init {
        update(json)
    }

    override fun update(json: JsonObject) {
        name = json["name"].asString
        permissions = PermissionSet(json["permissions"].asInt)
        color = Color(json["color"].asInt)
        position = json["position"].asInt
        hoist = json["hoist"].asBoolean
        managed = json["managed"].asBoolean
        mentionable = json["mentionable"].asBoolean
    }

    override fun toString(): String {
        return "RoleImpl(name='$name', permissions=$permissions, color=$color, position=$position, hoist=$hoist, managed=$managed, mentionable=$mentionable)"
    }

    override suspend fun edit(block: RoleBuilder.() -> Unit) {
        checkExistence()
        checkPermission(server, Permission.MANAGE_ROLES)
        checkManageable(this)

        val updater = RoleBuilder(this).apply(block)

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
                "permissions" to updater.permissions?.compile()
            }

            if (updater.color != color) {
                "color" to updater.color?.uRgb()
            }
        }

        if (json.isNotEmpty()) {
            client.rest.request(
                    Endpoint.MODIFY_GUILD_ROLE.format("guild.id" to server.id, "role.id" to id),
                    json
            )
        }

        if (updater.position != position) {
            client.rest.request(
                    Endpoint.MODIFY_GUILD_ROLE_POSITIONS.format("guild.id" to server.id),
                    json {
                        "id" to id
                        "position" to updater.position
                    }
            )
        }
    }

    override suspend fun delete() {
        checkExistence()
        checkPermission(server, Permission.MANAGE_ROLES)
        checkManageable(this)

        client.rest.request(Endpoint.DELETE_GUILD_ROLE.format("guild.id" to server.id, "role.id" to id))
    }

    private fun checkExistence() {
        if (server.roles.find(id) == null) {
            throw NotFoundException()
        }
    }
}