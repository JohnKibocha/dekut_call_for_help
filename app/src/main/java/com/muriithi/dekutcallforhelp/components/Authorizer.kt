// app/src/main/java/com/muriithi/dekutcallforhelp/components/Authorizer.kt
package com.muriithi.dekutcallforhelp.components

import com.muriithi.dekutcallforhelp.beans.User


class Authorizer {

    fun authorizeAdmin(user: User): Boolean {
        return user.superuser
    }

}