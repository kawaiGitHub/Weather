package com.test.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.core.os.HandlerCompat
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    companion object {
        private const val DEBUG_TAG ="AsyncSample"
        private const val WEATHERINFO_URL ="https://api.openweathermap.org/data/2.5/weather?lang=ja"
        private const val APP_ID ="@@@@@@@@@@@@@"
    }
    private var _list:MutableList<MutableMap<String,String>> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        _list = createList()
        val lv: ListView =findViewById(R.id.lv)
        val from = arrayOf("name")
        val to = intArrayOf(android.R.id.text1)
        val adapter = SimpleAdapter(this@MainActivity,_list,android.R.layout.simple_list_item_1,from, to)
        lv.adapter = adapter
        lv.onItemClickListener = ListItemClickListener()
    }
    private fun createList(): MutableList<MutableMap<String,String>> {
        var list:MutableList<MutableMap<String,String>> = mutableListOf()

        var city = mutableMapOf("name" to "札幌","q" to "Sapporo")
        list.add(city)
        city = mutableMapOf("name" to "仙台","q" to "Sendai" )
        list.add(city)
        city = mutableMapOf("name" to "東京","q" to "Tokyo")
        list.add(city)
        city = mutableMapOf("name" to "新潟","q" to "Niigata")
        list.add(city)
        city = mutableMapOf("name" to "大阪","q" to "Osaka")
        list.add(city)
        city = mutableMapOf("name" to "広島","q" to "Hiroshima")
        list.add(city)
        city = mutableMapOf("name" to "福岡","q" to "Fukuoka")
        list.add(city)
        return list
    }
    @UiThread
    private fun receiveWeatherInfo(urlFll:String) {
        val handler = HandlerCompat.createAsync(mainLooper)
        val backgroundReceiver = WeatherInfoBackgroundReceiver(handler,urlFll)
        val executeService = Executors.newSingleThreadExecutor()
        executeService.submit(backgroundReceiver)
    }
    private inner class WeatherInfoBackgroundReceiver(handler: Handler, url:String):Runnable{
        private val _handler = handler
        private val _url =  url

        @WorkerThread
        override fun run() {
            //TODO("Not yet implemented")
            var result =""
            val url = URL(_url)
            val con= url.openConnection() as? HttpURLConnection
            con?.let {
                try {
                    it.connectTimeout = 1000
                    it.readTimeout = 1000
                    it.requestMethod ="GET"
                    it.connect()
                    val stream = it.inputStream
                    result = is2String(stream)
                    it.inputStream.close()
                }
                catch (ex: SocketTimeoutException) {
                    Log.w(DEBUG_TAG,"通信タイムアウト",ex)
                }
                it.disconnect()
            }
            val postExecutor = WeatherInfoPostExecutor(result)
            _handler.post(postExecutor)
        }
        private fun is2String(stream: InputStream):String {
            val sb = StringBuilder()
            val reader = BufferedReader(InputStreamReader(stream,"UTF-8"))
            var line = reader.readLine()
            while (line != null) {
                sb.append(line)
                line = reader.readLine()
            }
            reader.close()
            return sb.toString()
        }
    }
    private inner class WeatherInfoPostExecutor(result: String): Runnable {
        private val _resuli = result

        @UiThread
        override fun run() {
            //TODO("Not yet implemented")
            val rootJSON = JSONObject(_resuli)
            val city = rootJSON.getString("name")
            //val coordJSON = rootJSON.getJSONObject("coord")
            //val latitude = coordJSON.getString("lat")
            //val longitude = coordJSON.getString("lon")
            val weatherJSONArray = rootJSON.getJSONArray("weather")
            val weatherJSON = weatherJSONArray.getJSONObject(0)
            val weather = weatherJSON.getString("description")
            val telop = "${city}の天気"
            val desc = "現在は${weather}です"
            val tv11: TextView =findViewById(R.id.tv11)
            val tv22: TextView =findViewById(R.id.tv22)
            tv11.text = telop
            tv22.text = desc

            val tv33: TextView =findViewById(R.id.tv33)
            val array = arrayListOf("今日の運勢は・・・大吉！！","今日の運勢は・・・中吉！","今日の運勢は・・・小吉！","今日の運勢は・・・凶。")
            val ran = Random().nextInt(array.count())
            tv33.text = array[ran]
        }
    }
    private inner class ListItemClickListener: AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            //TODO("Not yet implemented")
            val item = _list.get(position)
            val q = item.get("q")
            q?.let {
                val urlFll ="$WEATHERINFO_URL&q=$q&appid=$APP_ID"
                receiveWeatherInfo(urlFll)
            }
        }
    }
}

