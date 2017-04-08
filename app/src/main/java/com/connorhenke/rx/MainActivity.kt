package com.connorhenke.rx

import android.animation.ObjectAnimator
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.*
import android.widget.*
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.subscriptions.ArrayCompositeSubscription
import io.reactivex.schedulers.Schedulers
import okhttp3.HttpUrl
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    val secret = "B6Zw_49GcypVZmY3CwEmlOXcgLM635wHhHU76gBWGlnTwneim_Cx66lH32lE93BO"
    var client: OkHttpClient? = null
    var subscriptions : CompositeDisposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        subscriptions = CompositeDisposable()
        client = OkHttpClient()

        val sun = findViewById(R.id.main_sun)
        val rotate : RotateAnimation = RotateAnimation(0.0f, 360f, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 5000
        rotate.interpolator = LinearInterpolator()
        rotate.repeatCount = RotateAnimation.INFINITE
        rotate.repeatMode = RotateAnimation.RESTART
        sun.startAnimation(rotate)
        val root = findViewById(R.id.main_container) as ConstraintLayout
        val button = findViewById(R.id.main_button)
        val seanView = findViewById(R.id.main_sean) as ImageView
        val title = findViewById(R.id.main_title) as TextView
        val meme = findViewById(R.id.main_meme) as TextView
        val cloud = findViewById(R.id.cloud)
        var date = Date().time - 5000
        val observable = RxView.clicks(button).share()

        subscriptions?.add(
                observable
                        .filter {
                            Date().time - date > 5000
                        }
                        .observeOn(Schedulers.computation())
                        .map {
                            Thread.sleep(2000)
                            date = Date().time
                            val sean = getSeanPaul()
                            val json = JSONObject(sean).getJSONObject("response")
                            val artist = json.getJSONObject("artist")
                            val obj = SeanPaul()
                            obj.img = artist.getString("image_url")
                            obj.name = artist.getString("name")
                            obj.meme = artist.getBoolean("is_meme_verified")
                            obj
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { sean ->
                            Picasso.with(this)
                                    .load(sean.img)
                                    .resize(100, 100)
                                    .centerCrop()
                                    .transform(RoundedTransformation(100, 0))
                                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                                    .into(seanView)

                            title.setText(sean.name)
                            title.alpha = 0.0f
                            title.animate()
                                    .alpha(1.0f)
                                    .setDuration(600)
                                    .start()
                        }

        )
        subscriptions?.add(
                observable
                        .subscribe {
                            val anim = TranslateAnimation(0.0f, 650.0f, 0.0f, 0.0f)
                            anim.interpolator = AccelerateInterpolator()
                            anim.duration = 600
                            anim.fillAfter = true
                            cloud.startAnimation(anim)
                        }
        )
    }

    fun getSeanPaul(): String {
        val request = Request.Builder()
                .addHeader("Authorization", "Bearer " + secret)
                .addHeader("Accept", "application/json")
                .url("https://api.genius.com/artists/740?client_id=BeEeEB9i94W1Je7vYFv247qpNmN3-mao3v1XwRZSFDu3aiwIunDK7M35nJZ7Po3A&response_type=code")
                .get()
                .build()

        val response = client?.newCall(request)?.execute()
        return response?.body()!!.string()
    }

    override fun onResume() {
        super.onResume()
        subscriptions = CompositeDisposable()
    }

    override fun onPause() {
        super.onPause()
        subscriptions?.clear()
    }
}
