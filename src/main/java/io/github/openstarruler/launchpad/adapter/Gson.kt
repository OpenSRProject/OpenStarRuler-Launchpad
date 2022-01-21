package io.github.openstarruler.launchpad.adapter

import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.Reader

@Throws(JsonIOException::class, JsonSyntaxException::class)
inline fun <reified T> Gson.fromJson(json: Reader): T = fromJson(json, object: TypeToken<T>() {}.type)

@Throws(JsonIOException::class, JsonSyntaxException::class)
inline fun <reified T> Gson.fromJson(json: String): T = fromJson(json, object: TypeToken<T>() {}.type)