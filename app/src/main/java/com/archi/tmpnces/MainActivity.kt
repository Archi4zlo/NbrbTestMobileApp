package com.archi.tmpnces

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archi.tmpnces.presentation.root.DefaultRootComponent
import com.archi.tmpnces.presentation.root.RootContent
import com.archi.tmpnces.ui.theme.TmpNcesTheme
import com.arkivanov.decompose.retainedComponent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var rootComponentFactory: DefaultRootComponent.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = retainedComponent { componentContext ->
            rootComponentFactory.create(componentContext)
        }

        enableEdgeToEdge()
        setContent {
            TmpNcesTheme {
                RootContent(component = root)
            }
        }
    }
}