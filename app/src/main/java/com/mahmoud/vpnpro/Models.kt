package com.mahmoud.vpnpro

import org.json.JSONArray

data class Server(val id: String, val country: String, val flag: String, val name: String,
                  val protocol: String, val host: String, val port: Int)
data class NetConfig(val id: String, val name: String, val flag: String, val carrier: String,
                     val plan: String, val type: String, val recommended: Boolean)

object Parser {
    fun servers(json: String): List<Server> = JSONArray(json).let { a ->
        (0 until a.length()).map { a.getJSONObject(it).run {
            Server(getString("id"), getString("country"), optString("flag"), getString("name"),
                getString("protocol"), getString("host"), getInt("port")) } }
    }
    fun configs(json: String): List<NetConfig> = JSONArray(json).let { a ->
        (0 until a.length()).map { a.getJSONObject(it).run {
            NetConfig(getString("id"), getString("name"), optString("flag"), optString("carrier"),
                optString("plan"), optString("type"), optBoolean("recommended")) } }
    }
}
