package net.ayataka.kordis.utils

import java.util.regex.Pattern

/**
 * A query based finding utilities.(users,channels,roles,servers...etc)
 *
 * @since 0.0.1-SNAPSHOT
 * @author Hazuku
 */

//=======DISCORD_ID=======
//ID
//========================
val DISCORD_ID: Pattern = Pattern.compile("\\d{17,20}")
//=======DISCRIMINATED_NAME=======
//Username#0000
//Group 1 -> Username(name)
//Group 2 -> 0000(tag)
//========================
val FULL_USER_REF = Pattern.compile("(\\S.{0,30}\\S)\\s*#(\\d{4})")
//=======USER_MENTION=======
//Raw -> <@!1145141919810>
//Displayed -> @Hazuku
//Group 1 -> 1145141919810(id)
//========================
val USER_MENTION = Pattern.compile("<@!?(\\d{17,20})>") // $1 -> ID
//=======CHANNEL_MENTION=======
//Raw -> <#364364931>
//Displayed -> #general
//Group 1 -> 364364931(id)
//========================
val CHANNEL_MENTION = Pattern.compile("<#(\\d{17,20})>") // $1 -> ID
//=======ROLE_MENTION=======
//Raw -> <@&1234567890>
//Displayed -> @admin
//Group 1 -> 1234567890(id)
//========================
val ROLE_MENTION = Pattern.compile("<@&(\\d{17,20})>") // $1 -> ID
//=======EMOJI_MENTION=======
//Raw -> <:thonk:0987654321>
//Displayed -> :thonk:
//Group 1 -> thonk(name)
//Group 2 -> 0987654321(id)
//========================
val EMOJI_MENTION = Pattern.compile("<:(.{2,32}):(\\d{17,20})>")