package net.ayataka.kordis.entity.collection

import net.ayataka.kordis.entity.user.Member

class MemberListImpl : MemberList, NameableEntityListImpl<Member>() {
    override fun findByTag(tag: String, ignoreCase: Boolean): Member? {
        return entities.values.find { it.tag.equals(tag, ignoreCase) }
    }
}