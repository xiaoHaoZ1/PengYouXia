package com.pengyouxia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pengyouxia.app.navigation.AppNavHost
import com.pengyouxia.core.designsystem.theme.PyxTheme
import dagger.hilt.android.AndroidEntryPoint

/** 主入口 Activity：唯一的一级导航容器。 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PyxTheme { AppNavHost() }
        }
    }
}
