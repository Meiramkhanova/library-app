package com.adina.libraryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adina.libraryapp.ui.LibraryApp
import com.adina.libraryapp.ui.theme.LibraryAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LibraryAppTheme {
                val libraryViewModel: LibraryViewModel = viewModel()
                LibraryApp(viewModel = libraryViewModel)
            }
        }
    }
}
