package amal.global.amal

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class AmalApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AmalRoomDatabase.getDatabase(this, applicationScope) }
}