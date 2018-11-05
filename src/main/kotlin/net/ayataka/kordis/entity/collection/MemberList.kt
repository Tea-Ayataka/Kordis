package net.ayataka.kordis.entity.collection

import net.ayataka.kordis.entity.user.Member

interface MemberList : NameableEntityList<Member>, IterableEntityList<Member> {
    fun findByTag(tag: String, ignoreCase: Boolean = false): Member?
}