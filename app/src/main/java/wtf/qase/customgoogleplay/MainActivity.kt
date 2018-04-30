package wtf.qase.customgoogleplay

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var ctx: Context
    private lateinit var expansionFilesHelper: ExpansionFilesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
