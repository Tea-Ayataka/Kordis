package net.ayataka.kordis.entity.collection

import net.ayataka.kordis.entity.user.Member

interface MemberSet : NameableEntitySet<Member>, IterableEntitySet<Member> {
    fun findByTag(tag: String, ignoreCase: Boolean = false): Member?
}