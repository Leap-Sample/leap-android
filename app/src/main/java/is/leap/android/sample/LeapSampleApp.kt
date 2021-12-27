package `is`.leap.android.sample

import `is`.leap.android.LeapEventCallbacks
import `is`.leap.android.aui.Leap
import `is`.leap.android.creator.LeapCreator
import android.app.Application
import `is`.leap.android.sample.data.LeapSampleSharedPref
import `is`.leap.android.sample.receiver.SampleAppReceiver
import android.content.IntentFilter
import android.util.Log
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject

class LeapSampleApp : Application() {


    override fun onCreate() {
        super.onCreate()

        Leap.start("44a8633d-2c31-4ade-8535-6ebd39b8b262")
        LeapCreator.start("44a8633d-2c31-4ade-8535-6ebd39b8b262")

        val mixpanel = MixpanelAPI.getInstance(this, "a6665720e68a9a1458e7ae6fa11a55d1")

        Leap.setLeapEventCallbacks(LeapEventCallbacks() {
//            override fun onEvent(p0: MutableMap<String, String>?) {
 
            val jsonObject = JSONObject(it as Map<String, String>?)
            Log.d("LeapCallbacks", "JSON: $jsonObject")
            mixpanel.track(it?.get("eventName") + "_r", jsonObject)
            mixpanel.flush()
            //Log.d("LeapCallbacks", "" + it?.keys + "\n" + it?.values)
//            }
        })

        LeapSampleSharedPref.init(this)
        initialiseReceiver(this)


    }

    private fun initialiseReceiver(application: Application) {
        val receiver = SampleAppReceiver()
        val filter = IntentFilter("is.leap.android.sample.receiver.SampleAppReceiver")
        application.registerReceiver(receiver, filter)
    }
}