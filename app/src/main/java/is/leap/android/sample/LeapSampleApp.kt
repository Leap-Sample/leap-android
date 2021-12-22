package `is`.leap.android.sample

import `is`.leap.android.aui.Leap
import `is`.leap.android.creator.LeapCreator
import android.app.Application
import `is`.leap.android.sample.data.LeapSampleSharedPref
import `is`.leap.android.sample.receiver.SampleAppReceiver
import android.content.IntentFilter
import android.util.Log

class LeapSampleApp : Application() {



    override fun onCreate() {
        super.onCreate()

        Leap.start("44a8633d-2c31-4ade-8535-6ebd39b8b262")
        LeapCreator.start("44a8633d-2c31-4ade-8535-6ebd39b8b262")

        Leap.setLeapEventCallbacks {
            Log.d("LeapCallbacks", "" + it.keys)
        }

        LeapSampleSharedPref.init(this)
        initialiseReceiver(this)
    }

    private fun initialiseReceiver(application: Application) {
        val receiver = SampleAppReceiver()
        val filter = IntentFilter("is.leap.android.sample.receiver.SampleAppReceiver")
        application.registerReceiver(receiver, filter)
    }
}