package app.solocoin.solocoin.ui.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import app.solocoin.solocoin.R
import app.solocoin.solocoin.repo.SolocoinRepository
import com.google.gson.JsonObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.android.ext.android.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Created by Vijay Daita 5/23

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class RedeemCodeActivity : AppCompatActivity() {

    private val solocoinRepository: SolocoinRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_redeem_code)

        var codeTextView = findViewById<TextView>(R.id.redeemed_code)
        var intent = getIntent()
        val body = JsonObject()
        body.addProperty("reward_sponsor_id", intent.getStringExtra("code"))
        var call: Call<JsonObject> = solocoinRepository.redeemRewards(body)
        call.enqueue(object: Callback<JsonObject?>{
            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                codeTextView.setText(getString(R.string.claim_error))
            }

            override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                val resp = response.body();
                resp?.let{
                    codeTextView.setText(resp["couponCode"].asString)
                }
            }

        })

    }
}