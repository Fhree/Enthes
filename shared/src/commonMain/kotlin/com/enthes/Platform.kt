package com.enthes

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform