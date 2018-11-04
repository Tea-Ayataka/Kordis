package net.ayataka.kordis.entity.server

import kotlinx.serialization.json.*
import net.ayataka.kordis.DiscordClient
import net.ayataka.kordis.entity.DiscordEntity
import java.awt.Color

class RoleImpl(client: DiscordClient, json: JsonObject) : Role, DiscordEntity(client, json["id"].long) {
    @Volatile
    override var name: String = ""
    @Volatile
    override var permissions: Int = -1
    @Volatile
    override var color: Color = Color.BLACK
    @Volatile
    override var position: Int = -1
    @Volatile
    override var isHoisted: Boolean = false
    @Volatile
    override var isManaged: Boolean = false
    @Volatile
    override var isMentionable: Boolean = false

    init {
        update(json)
    }

    fun update(json: JsonObject) {
        name = json["name"].content
        permissions = json["permissions"].int
        color = Color(json["color"].int)
        position = json["position"].int
        isHoisted = json["hoist"].boolean
        isManaged = json["managed"].boolean
        isMentionable = json["mentionable"].boolean
    }

    override fun edit(name: String?, permissions: Int?, color: Color?) {
        // initialize updater

        if (name != null && name != name) {
            // setName(name)
        }

        // update
    }

    override fun toString(): String {
        return "RoleImpl(name='$name', permissions=$permissions, color=$color, position=$position, isHoisted=$isHoisted, isManaged=$isManaged, isMentionable=$isMentionable)"
    }
}