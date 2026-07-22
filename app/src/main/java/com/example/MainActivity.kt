package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.PosViewModel
import com.example.ui.screens.MainPosScreen
import com.example.ui.theme.PosTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val incomingUri = extractIncomingUri(intent)

        setContent {
            PosTheme {
                MainPosScreen(
                    viewModel = viewModel,
                    incomingFileUri = incomingUri
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val incomingUri = extractIncomingUri(intent)
        if (incomingUri != null) {
            setContent {
                PosTheme {
                    MainPosScreen(
                        viewModel = viewModel,
                        incomingFileUri = incomingUri
                    )
                }
            }
        }
    }

    private fun extractIncomingUri(intent: Intent?): Uri? {
        if (intent == null) return null
        if (intent.action == Intent.ACTION_VIEW) {
            return intent.data
        }
        return null
    }
}
