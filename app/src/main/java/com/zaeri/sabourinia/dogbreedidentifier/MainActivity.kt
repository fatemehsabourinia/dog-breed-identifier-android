package com.zaeri.sabourinia.dogbreedidentifier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.zaeri.sabourinia.dogbreedidentifier.theme.DogBreedIdentifierTheme
import com.zaeri.sabourinia.dogbreedidentifier.ui.DogBreedDemoScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DogBreedIdentifierTheme(dynamicColor = false) {
                Surface {
                    DogBreedDemoScreen()
                }
            }
        }
    }
}
