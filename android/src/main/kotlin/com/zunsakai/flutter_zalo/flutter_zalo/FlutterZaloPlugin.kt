package com.zunsakai.flutter_zalo.flutter_zalo

import android.app.Activity
import android.content.Context
import android.util.Log
import com.zing.zalo.zalosdk.core.helper.AppInfo.getApplicationHashKey
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** FlutterZaloPlugin */
class FlutterZaloPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private var zaloAPI: ZaloAPI? = null
    private val LOG_TAG = FlutterZaloPlugin::class.java.simpleName
    private var result: MethodChannel.Result? = null
    private lateinit var context: Context
    private var activity: Activity? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext;
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_zalo")
        channel.setMethodCallHandler(this)
        zaloAPI = ZaloAPI()
        zaloAPI?.setContext(context)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity;
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity;
    }

    override fun onDetachedFromActivity() {
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        if (activity == null) {
            result.error("Error", "Activity is null", null)
            return
        }

        this.result = result
        when (call.method) {
            "init" -> {
                val hashkey = getApplicationHashKey(context);
                Log.v(
                    LOG_TAG,
                    "---------------------------------------------------------------------------"
                );
                Log.v(
                    LOG_TAG,
                    "|     Please add this Hash Key to Zalo developers dashboard for Login     |"
                );
                Log.v(LOG_TAG, "|     Hash Key: $hashkey                              |");
                Log.v(
                    LOG_TAG,
                    "---------------------------------------------------------------------------"
                );
                result.success(hashkey);
            }

            "logIn" -> {
                try {
                    zaloAPI?.logIn(activity!!)
                    result.success(true)
                } catch (e: Exception) {
                    result.error("Error", e.message, null)
                }
            }

            "isAccessTokenValid" -> {
                result.success(zaloAPI?.isAccessTokenValid())
            }

            "getAccessToken" -> {
                result.success(zaloAPI?.getAccessToken())
            }

            "isRefreshAccessTokenValid" -> {
                result.success(zaloAPI?.isRefreshAccessTokenValid())
            }

            "refreshAccessToken" -> {
                result.success(zaloAPI?.refreshAccessToken())
            }

            "getProfile" -> {
                zaloAPI?.getProfile { userData ->
                    val profile = mapOf<String, Any>(
                        "id" to userData.getId(),
                        "name" to userData.getName(),
                        "pictureUrl" to userData.getPictureUrl()
                    )
                    result.success(profile)
                }
            }

            "logOut" -> {
                result.success(zaloAPI?.logout())
            }

            else -> {
                result.notImplemented()
            }
        }
    }
}
