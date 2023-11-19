/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * 微件选取列表：展示微件的描述信息和预览图片
 * Sample activity to demonstrate how to get the app's appwidgets info and request the user to pin
 * them in the launcher.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val widgetManager = AppWidgetManager.getInstance(this)

        // 根据包名查询应用注册的所有微件，每个微件对应一个AppWidgetProviderInfo
        // Get a list of our app widget providers to retrieve their info
        val widgetProviders = widgetManager.getInstalledProvidersForPackage(packageName, null)

        setContent {
            val colors = if (isSystemInDarkTheme()) {
                darkColors()
            } else {
                lightColors()
            }
            MaterialTheme(colors) {
                Scaffold {
                    LazyColumn(contentPadding = it) {
                        item {
                            AppInfoText()  // 微件选取列表上方的App信息
                        }

                        // If the launcher does not support pinning request show a banner
                        if (!widgetManager.isRequestPinAppWidgetSupported) {
                            item {
                                PinUnavailableBanner()
                            }
                        }

                        items(widgetProviders) { providerInfo ->
                            WidgetInfoCard(providerInfo)  // 根据AppWidgetProviderInfo生成微件卡片
                        }
                    }
                }
            }
        }
    }

}

/**
 * 将指定的微件固定在桌面上
 * Extension method to request the launcher to pin the given AppWidgetProviderInfo
 *
 * Note: the optional success callback to retrieve if the widget was placed might be unreliable
 * depending on the default launcher implementation. Also, it does not callback if user cancels the
 * request.
 */
private fun AppWidgetProviderInfo.pin(context: Context) {
    val successCallback = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, AppWidgetPinnedReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    // 将微件固定在桌面上
    AppWidgetManager.getInstance(context).requestPinAppWidget(provider, null, successCallback)
}

@Composable
private fun PinUnavailableBanner() {
    Text(
        text = stringResource(
            id = R.string.placeholder_main_activity_pin_unavailable
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.error)
            .padding(16.dp),
        color = MaterialTheme.colors.onError
    )
}

/**
 * 微件选取列表上方的App信息
 */
@Composable
private fun AppInfoText() {
    Text(
        text = stringResource(id = R.string.placeholder_main_activity),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

/**
 * 在卡片中显示provider中的微件信息
 * Display the app widget info from the provider.
 *
 * This class contains all the info we provide via the XML meta-data for each provider.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun WidgetInfoCard(providerInfo: AppWidgetProviderInfo) {
    val context = LocalContext.current
    val label = providerInfo.loadLabel(context.packageManager)
    val description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        providerInfo.loadDescription(context).toString()
    } else {
        "Description not available"
    }
    val preview = painterResource(id = providerInfo.previewImage)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
            backgroundColor = Color.Transparent,  // 卡片背景色
        onClick = {
            providerInfo.pin(context)  // 调用扩展方法，将微件固定到桌面
        }
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.body1
                )
            }
            Image(painter = preview, contentDescription = description)
        }
    }
}
