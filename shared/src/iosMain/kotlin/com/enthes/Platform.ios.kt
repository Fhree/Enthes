package com.enthes

import platform.UIKit.UIDevice

actual fun getPlatform(): Platform = object : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}
