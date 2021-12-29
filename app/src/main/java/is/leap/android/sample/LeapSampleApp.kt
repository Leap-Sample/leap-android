package `is`.leap.android.sample

import `is`.leap.android.aui.Leap
import android.app.Application
import `is`.leap.android.sample.data.LeapSampleSharedPref
import `is`.leap.android.sample.receiver.SampleAppReceiver
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject

class LeapSampleApp : Application() {


    override fun onCreate() {
        super.onCreate()

        Leap.withBuilder("44a8633d-2c31-4ade-8535-6ebd39b8b262")
            .addProperty("test", "test_value")
            .start()
//        LeapCreator.start("44a8633d-2c31-4ade-8535-6ebd39b8b262")

        val mixpanel = MixpanelAPI.getInstance(this, "a6665720e68a9a1458e7ae6fa11a55d1")

        Leap.setLeapEventCallbacks {
            val jsonObject = JSONObject(it as Map<String, String>)
            Log.d("LeapCallbacks", "JSON: $jsonObject")
            Toast.makeText(this, "LeapCallbacks for " + it["eventName"], Toast.LENGTH_SHORT)
                .show()
            mixpanel.track(it["eventName"] + "_r", jsonObject)
            mixpanel.flush()
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