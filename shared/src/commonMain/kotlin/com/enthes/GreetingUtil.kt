package com.enthes

fun sayHello(to: String): String =
    "Hello, $to!"

fun BadlyNamedDetektDemo(value: Int): Int {
    val result=value+42
    return result
}

private fun unusedDetektDemo(): Int = 99
