package ca.bcit.binged

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import ca.bcit.binged.ui.navigation.AppNavGraph
import ca.bcit.binged.ui.navigation.BottomNavBar
import ca.bcit.binged.ui.theme.BingedTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BingedTheme {
                BingedApp()
            }
        }
    }
}

@Composable
fun BingedApp() {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        AppNavGraph(navController, Modifier.padding(innerPadding))
    }
}