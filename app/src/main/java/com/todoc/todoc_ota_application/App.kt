package com.todoc.todoc_ota_application

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.todoc.todoc_ota_application.databinding.ActivityMainBinding
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModelStoreOwner
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.todoc.todoc_ota_application.feature.main.MainViewModel

class App : Application() {
    private val TAG = this.javaClass.simpleName
//    companion object {
//
//        lateinit var application: Application
//        lateinit var appContext: Context
//        fun ApplicationContext() : Context {
//            return application.applicationContext       // application context 관리
//        }
//        private var mainViewModel: MainViewModel? = null
//
//        fun initialize(context: Context) {
//            appContext = context.applicationContext
//            Log.e("MyApplication", "statusViewModel 초기화")
//            if (context is ViewModelStoreOwner) {
//                if (mainViewModel == null) {
//                    mainViewModel = MainViewModel.getInstance(context)
//
//                }
//            } else {
//                throw IllegalArgumentException("Context must be a ViewModelStoreOwner")
//            }
//        }
//
//        fun getMainViewModel(): MainViewModel? {
//            return mainViewModel
//        }
//
//
//    }


    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

    }
}
