package com.Kelasor.app.ui.screens.elm

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.util.Base64

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapboxGlobe(
    modifier: Modifier = Modifier,
    onMapReady: (WebView) -> Unit = {}
) {
    val context = LocalContext.current
    
    // We use a generic mapir token since map.ir provides vector tiles.
    // In production, the user's specific API key should be used.
    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="initial-scale=1,maximum-scale=1,user-scalable=no" />
            <link href="https://cdn.map.ir/web-sdk/1.4.2/css/mapp.min.css" rel="stylesheet" />
            <link href="https://cdn.map.ir/web-sdk/1.4.2/css/fa/style.css" rel="stylesheet" />
            <style>
                body { margin: 0; padding: 0; background-color: #001021; overflow: hidden; }
                #map { position: absolute; top: 0; bottom: 0; width: 100%; border-radius: 32px; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script src="https://cdn.map.ir/web-sdk/1.4.2/js/jquery-3.2.1.min.js"></script>
            <script src="https://cdn.map.ir/web-sdk/1.4.2/js/mapp.env.js"></script>
            <script src="https://cdn.map.ir/web-sdk/1.4.2/js/mapp.min.js"></script>
            <script>
                // We use Map.ir Web SDK, which uses Mapbox GL JS natively
                window.map = new Mapp({
                    element: '#map',
                    presets: {
                        latlng: {
                            lat: 32.4279,
                            lng: 53.6880
                        },
                        zoom: 4.5
                    },
                    // Using map.ir public testing token or the user's own token
                    apiKey: 'YOUR_MAPIR_API_KEY'
                });
                
                map.addPlugin("MapirZoom", { position: "bottom-left" });
                map.addPlugin("MapirLogo", { position: "bottom-right" });

                map.on('load', function() {
                    // Send message to Android that map is ready
                    if (window.Android) {
                        window.Android.onMapReady();
                    }
                    
                    // Add Atmosphere/Globe fallback styling to Dark Mode
                    map.setPaintProperty('background', 'background-color', '#001021');
                });
            </script>
        </body>
        </html>
    """.trimIndent()
    
    val base64Html = Base64.getEncoder().encodeToString(htmlContent.toByteArray())

    val webView = remember {
        WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                setSupportZoom(false)
                displayZoomControls = false
            }
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    onMapReady(this@apply)
                }
            }
            webChromeClient = WebChromeClient()
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            loadData(base64Html, "text/html; charset=utf-8", "base64")
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize()
    )

    DisposableEffect(Unit) {
        onDispose {
            webView.destroy()
        }
    }
}
