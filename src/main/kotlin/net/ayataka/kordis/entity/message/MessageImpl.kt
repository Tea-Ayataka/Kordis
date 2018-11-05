package net.ayataka.kordis.entity.message

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import net.ayataka.kordis.DiscordClientImpl
import net.ayataka.kordis.entity.DiscordEntity

class MessageImpl(clientImpl: DiscordClientImpl, json: JsonObject) : Message, DiscordEntity(clientImpl, json["id"].long) {

}