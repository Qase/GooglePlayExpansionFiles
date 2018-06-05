package wtf.qase.customgoogleplay

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var ctx: Context
    private lateinit var expansionFilesHelper: ExpansionFilesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Toast.makeText(
            this@MainActivity,
            "${BuildConfig.VERSION_CODE}",
            Toast.LENGTH_LONG
        ).show()

        ctx = this@MainActivity.applicationContext
        expansionFilesHelper = ExpansionFilesHelper(this@MainActivity)
    }

    override fun onStart() {
        expansionFilesHelper.start(ctx)
        super.onStart()
    }

    override fun onStop() {
        expansionFilesHelper.stop(ctx)
        super.onStop()
    }
}
