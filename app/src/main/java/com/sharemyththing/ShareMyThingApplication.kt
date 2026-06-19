package com.sharemyththing

import android.app.Application
import com.sharemyththing.data.ItemsRepository

class ShareMyThingApplication : Application() {
    lateinit var repository: ItemsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = ItemsRepository(this)
    }
}
