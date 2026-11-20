package com.adina.spendlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adina.spendlog.ui.SpendLogApp
import com.adina.spendlog.ui.theme.SpendLogTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpendLogTheme {
                val spendLogViewModel: SpendLogViewModel = viewModel()
                SpendLogApp(viewModel = spendLogViewModel)
            }
        }
    }
}
