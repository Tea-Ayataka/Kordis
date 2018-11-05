package net.ayataka.kordis.entity.server.channel

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import kotlinx.serialization.json.int
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity
import net.ayataka.kordis.entity.server.Server
import net.ayataka.kordis.entity.server.permission.PermissionSet
import net.ayataka.kordis.entity.server.permission.RolePermissionOverwrite
import net.ayataka.kordis.entity.server.permission.UserPermissionOverwrite

open class ServerChannelImpl(
        override val server: Server,
        client: DiscordClientImpl,
        id: Long
) : ServerChannel, DiscordEntity(client, id) {
    final override var name = ""
    final override var position = -1
    final override val userPermissionOverwrites = mutableSetOf<UserPermissionOverwrite>()
    final override val rolePermissionOverwrites = mutableSetOf<RolePermissionOverwrite>()

    fun loadPermissionOverwrites(json: JsonObject) {
        userPermissionOverwrites.clear()
        rolePermissionOverwrites.clear()

        json["permission_overwrites"].jsonArray.forEach {
            if (it.jsonObject["type"].content == "user") {
                userPermissionOverwrites.add(UserPermissionOverwrite(
                        client.users.find(it.jsonObject["id"].long)!!,
                        PermissionSet(it.jsonObject["allow"].int),
                        PermissionSet(it.jsonObject["deny"].int)
                ))
                return@forEach
            }

            if (it.jsonObject["type"].content == "role") {
                rolePermissionOverwrites.add(RolePermissionOverwrite(
                        server.roles.find(it.jsonObject["id"].long)!!,
                        PermissionSet(it.jsonObject["allow"].int),
                        PermissionSet(it.jsonObject["deny"].int)
                ))
                return@forEach
            }
        }
    }
}