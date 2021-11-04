package es.userquin.unocss

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.annotation.Nullable
import androidx.annotation.WorkerThread
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import java.net.FileNameMap
import java.net.URLConnection
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler
import android.webkit.WebResourceRequest
import androidx.annotation.RequiresApi


open class AstatFileNameMap: FileNameMap {
    override fun getContentTypeFor(fileName: String): String {
        val fragmentIndex = fileName.indexOf('#')
        val useName = if (fragmentIndex >= 0) {
            fileName.substring(0, fragmentIndex)
        } else {
            fileName
        }
        return when {
            useName.endsWith("/") -> "text/html"
            useName.endsWith(".html") -> "text/html"
            useName.endsWith(".htm") -> "text/html"
            useName.endsWith(".js") -> "application/javascript"
            useName.endsWith(".eot") -> "application/vnd.ms-fontobject"
            useName.endsWith(".ttf") -> "application/font-sfnt"
            useName.endsWith(".woff") -> "font/woff"
            useName.endsWith(".woff2") -> "font/woff2"
            useName.endsWith(".css") -> "text/css"
            useName.endsWith(".svg") -> "image/svg+xml"
            useName.endsWith(".png") -> "image/png"
            useName.endsWith(".jpeg") -> "image/jpg"
            useName.endsWith(".jpg") -> "image/jpg"
            useName.endsWith(".gif") -> "image/gif"
            else -> "text/plain"
        }
    }
}

inline fun WebResourceResponse.withHeaders(
    withResponseHeaders: MutableMap<String, String>.() -> Unit
): WebResourceResponse = apply {
    responseHeaders = mutableMapOf<String, String>().also {
        responseHeaders?.let { eh -> it.putAll(eh) }
        with(it) {
            withResponseHeaders()
        }
    }
}


open class AppAssetsPathHandler private constructor(
    val assetsPathHandler: WebViewAssetLoader.PathHandler
) : WebViewAssetLoader.PathHandler {

    constructor(context: Context): this(AssetsPathHandler(context))

    @WorkerThread
    @Nullable
    override fun handle(path: String) = when (path) {
        "", "/", "/#!", "/#!/" -> assetsPathHandler.handle("www/index.html")?.withHeaders {
            // SHOULD READ manifest.json and add the resources to hint browser before start parsing html

//            this["Link"] to mutableListOf<String>().apply {
//                this += "</fonts/montserrat-v15-latin-regular.woff2>;rel=preload;as=font;crossorigin"
//                this += "</fonts/montserrat-v15-latin-500.woff2>;rel=preload;as=font;crossorigin"
//                this += "</fonts/montserrat-v15-latin-700.woff2>;rel=preload;as=font;crossorigin"
//                this += "</fonts/montserrat-v15-latin-900.woff2>;rel=preload;as=font;crossorigin"
//                this += "</assets/index.5c1bd4a2.css>;rel=preload;as=style"
//                this += "</assets/index.41eb5654.js>;rel=modulepreload;as=script"
//                this += "</assets/vendor.b22f6197.js>;rel=modulepreload;as=script"
//            }.joinToString(", ")
        }
        else -> {
            println("${Thread.currentThread().name} => $path")
            assetsPathHandler.handle("www/$path")?.also {
                println(it.responseHeaders)
            }
        }
    }
}

class AppWebView constructor(
    context: Context,
) : WebView(context) {

    @SuppressLint("JavascriptInterface", "SetJavaScriptEnabled")
    inline fun init(debugEnabled: Boolean, load: WebView.() -> Unit) {
        println("${Thread.currentThread().name} => $debugEnabled => ${settings.userAgentString}")
        background = null
        setBackgroundColor(Color.argb(1, 0, 0, 0))
        isFocusable = true
        settings.apply {
            allowFileAccess = false
            javaScriptEnabled = true
            builtInZoomControls = true
            displayZoomControls = false
            setGeolocationEnabled(false)
            domStorageEnabled = true
            setSupportZoom(true)
            useWideViewPort = true
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                @Suppress("DEPRECATION")
                saveFormData = false
            }
            databaseEnabled = false
            val requestPathHandler = AppAssetsPathHandler(context)
            webViewClient = object : WebViewClientCompat() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ): WebResourceResponse?  = requestPathHandler.handle(request.url.path!!.removePrefix("/").toString())
                //                @Override
//                @RequiresApi(21)
//                override fun shouldInterceptRequest(
//                    view: WebView,
//                    request: WebResourceRequest
//                ): WebResourceResponse? {
//                    return assetLoader.shouldInterceptRequest(request.url)
//                }
//
//                // for API < 21
//                override fun shouldInterceptRequest(
//                    view: WebView,
//                    request: WebResourceRequest
//                ): WebResourceResponse? {
//                    return assetLoader.shouldInterceptRequest(Uri.parse(request))
//                }

            }
        }
        isFocusableInTouchMode = true
        isSaveEnabled = false
        try {
            clearHistory()
        } catch (_: Throwable) {
            // just ignore
        }
        try {
            clearCache(true)
        } catch (_: Throwable) {
            // just ignore
        }
        addJavascriptInterface(XCapiApp(), "XCapiApp")
        setWebContentsDebuggingEnabled(debugEnabled)
        URLConnection.setFileNameMap(AstatFileNameMap())
        with(this) {
            load()
        }
    }

}
