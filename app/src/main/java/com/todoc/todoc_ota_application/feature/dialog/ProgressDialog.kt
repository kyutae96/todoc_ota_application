package com.todoc.todoc_ota_application.feature.dialog

import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.text.toSpannable
import androidx.lifecycle.lifecycleScope
import com.todoc.todoc_ota_application.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text

object ProgressDialog {
    private val TAG = this.javaClass.simpleName
    var progressDialog: AlertDialog? = null
    var retryProgressDialog: AlertDialog? = null
    var errorProgressDialog: AlertDialog? = null

    var job: Job? = null

    /*fun showProgressDialog(context: Context, message: String = context.getString(R.string.updating)) {
        if (progressDialog?.isShowing == true) return
        if (job != null && job?.isActive == true) return
        val view = LayoutInflater.from(context).inflate(R.layout.map_dialog_progress, null)
        val progressTextView = view.findViewById<TextView>(R.id.progressText)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val mapDialogInfoText = view.findViewById<TextView>(R.id.mapDialogInfoTxt)


        job = CoroutineScope(Dispatchers.Main).launch {
            MyApplication.getStatusViewModel()?.blePercentFlow
                ?.collect { response ->
                    Log.d("progressDialog", "map percent → msg: ${response.msg}, percent: ${response.percent}")
                    progressTextView?.text = "${response.msg}"
                    progressBar?.progress = response.percent
                }
        }


        val text = if (loadLanguagePreference(context) == "ko"){
            SpannableStringBuilder()
                .append("외부기를 제거".toBoldText())
                .append("하거나\n")
                .append("앱을 종료".toBoldText())
                .append("하지 마세요.")
        }else if (loadLanguagePreference(context) == "en"){
            SpannableStringBuilder()
                .append("Do not ".toSpannable())
                .append("remove the device".toBoldText())
                .append(" or\n")
                .append("close the app".toBoldText())
                .append(".")
        } else {
            "-----"
        }

//        val text = SpannableStringBuilder()
//            .append("외부기를 제거".toBoldText())
//            .append("하거나\n")
//            .append("앱을 종료".toBoldText())
//            .append("하지 마세요.")


        mapDialogInfoText.text = text


        progressDialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)
            .create()
        progressDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        progressDialog?.show()
    }

    fun dismissProgressDialog() {
        progressDialog?.dismiss()
        job?.cancel()
        job = null

        progressDialog = null
    }



    fun showRetryDialog(context: Context, onRetry: () -> Unit, onInit:() -> Unit) {
        val view = LayoutInflater.from(context).inflate(R.layout.map_retry_dialog_progress, null)
        val retryDeviceMapBtn = view.findViewById<Button>(R.id.retryDeviceMapBtn)
        val initDeviceMapBtn = view.findViewById<Button>(R.id.initDeviceMapBtn)

        val builder  = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)


        retryProgressDialog = builder.create()


        retryDeviceMapBtn.setOnClickListener { onRetry(); dismissRetryDialog()}
        initDeviceMapBtn.setOnClickListener { showErrorDialog(context, onInit) }

        retryProgressDialog!!.show()
    }

    fun dismissRetryDialog(){
        retryProgressDialog?.dismiss()
        retryProgressDialog = null
    }

    fun showErrorDialog(context: Context, onInit:() -> Unit) {
        val view = LayoutInflater.from(context).inflate(R.layout.map_error_dialog_progress, null)

        val deleteDeviceMapBtn = view.findViewById<Button>(R.id.deleteDeviceMapBtn)
        val cancelDeivceMapBtn = view.findViewById<Button>(R.id.cancelDeivceMapBtn)

        val builder  = AlertDialog.Builder(context)
            .setView(view)

        errorProgressDialog = builder.create()

        deleteDeviceMapBtn.setOnClickListener {
            onInit()
        }

        cancelDeivceMapBtn.setOnClickListener { dismissErrorDialog() }

        errorProgressDialog!!.show()
    }
    fun dismissErrorDialog(){
        errorProgressDialog?.dismiss()
        errorProgressDialog = null
    }*/


}