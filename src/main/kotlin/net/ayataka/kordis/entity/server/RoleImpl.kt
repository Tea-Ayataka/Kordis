package net.ayataka.kordis.entity.server

import kotlinx.serialization.json.*
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.Updatable
import net.ayataka.kordis.entity.server.permission.PermissionSet
import java.awt.Color

class RoleImpl(client: DiscordClientImpl, json: JsonObject, override val server: Server) : Role, Updatable, DiscordEntity(client, json["id"].long) {
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
        name = json["name"].content
        permissions = PermissionSet(json["permissions"].int)
        color = Color(json["color"].int)
        position = json["position"].int
        hoist = json["hoist"].boolean
        managed = json["managed"].boolean
        mentionable = json["mentionable"].boolean
    }

    override fun toString(): String {
        return "RoleImpl(name='$name', permissions=$permissions, color=$color, position=$position, hoist=$hoist, managed=$managed, mentionable=$mentionable)"
    }
}