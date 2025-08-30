package ru.chuvash.reprise

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform