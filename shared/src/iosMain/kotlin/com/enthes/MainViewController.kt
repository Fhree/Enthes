package com.enthes

import androidx.compose.ui.window.ComposeUIViewController
import com.enthes.di.initKoin
import org.koin.mp.KoinPlatformTools

@Suppress("FunctionNaming")
fun MainViewController() = ComposeUIViewController(
    configure = {
        if (KoinPlatformTools.defaultContext().getOrNull() == null) {
            initKoin()
        }
    },
) {
    App()
}
