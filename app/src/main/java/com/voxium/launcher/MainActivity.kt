package com.voxium.launcher

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.widget.Button
import android.widget.LinearLayout
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private val spaces = listOf("Java", "Bedrock", "Starling")
    private val prefs by lazy { getSharedPreferences("voxium_spaces", MODE_PRIVATE) }
    private var activeSpace = "Java"
    private var activeTab = "Home"
    private lateinit var body: LinearLayout
    private val bg = Color.rgb(12, 15, 24)
    private val panelColor = Color.rgb(25, 31, 45)
    private val accent = Color.rgb(151, 128, 255)
    private val muted = Color.rgb(164, 174, 194)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activeSpace = prefs.getString("active_space", "Java") ?: "Java"
        window.statusBarColor = bg
        window.navigationBarColor = bg
        if (android.os.Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
        render()
    }

    private fun render() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(20), dp(18), dp(16))
            setBackgroundColor(bg)
        }
        val scroll = ScrollView(this).apply { addView(root) }
        setContentView(scroll)

        label(root, "VOXIUM", 30, Color.WHITE, true)
        label(root, "THREE SPACES. ONE LAUNCHER.", 11, accent, true, 4)
        label(root, "Choose your environment", 14, muted, false, 18)

        val switcher = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        spaces.forEach { space ->
            val caption = when (space) { "Java" -> "☕\nJAVA"; "Bedrock" -> "🧱\nBEDROCK"; else -> "⚡\nSTARLING" }
            val button = Button(this).apply {
                text = caption
                isAllCaps = false
                textSize = 13f
                minWidth = 0
                minHeight = dp(78)
                setPadding(dp(4), dp(8), dp(4), dp(8))
                setTextColor(Color.WHITE)
                background = rounded(if (activeSpace == space) 0xFF493B78.toInt() else panelColor)
                stateListAnimator = null
                setOnClickListener {
                    activeSpace = space
                    activeTab = "Home"
                    prefs.edit().putString("active_space", space).apply()
                    render()
                }
            }
            switcher.addView(button, LinearLayout.LayoutParams(0, dp(72), 1f).apply { marginEnd = dp(5) })
        }
        root.addView(switcher)
        label(root, "$activeSpace SPACE", 21, Color.WHITE, true, 20)

        val tabs = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val tabNames = listOf("Home", "Versions", if (activeSpace == "Bedrock") "Add-ons" else "Mods", "Library", "Accounts", "Settings")
        tabNames.forEachIndexed { index, tab ->
            val button = Button(this).apply {
                text = tab
                isAllCaps = false
                textSize = 12f
                minWidth = dp(76)
                minHeight = dp(44)
                setPadding(dp(14), 0, dp(14), 0)
                setTextColor(Color.WHITE)
                background = rounded(if (activeTab == tab) accent else panelColor)
                stateListAnimator = null
                setOnClickListener { activeTab = tab; render() }
            }
            tabs.addView(button, LinearLayout.LayoutParams(-2, dp(46)).apply {
                marginEnd = dp(7)
                if (index == 0) marginStart = dp(1)
            })
        }
        val tabScroller = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            clipToPadding = false
            addView(tabs)
        }
        root.addView(tabScroller)
        body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(16), 0, 0)
        }
        root.addView(body)
        renderContent()
    }

    private fun renderContent() {
        when (activeTab) {
            "Home" -> {
                label(body, "Welcome to $activeSpace", 22, Color.WHITE, true)
                val description = when (activeSpace) {
                    "Java" -> "Java Edition launcher space. JVM engine integration is next."
                    "Bedrock" -> "Bedrock worlds, resource packs and add-ons live here."
                    else -> "Native Rust-based Starling environment. Not a general-purpose JVM."
                }
                label(body, description, 14, muted, false, 7)
                panel("Your $activeSpace space") { label(this, "Your selected space is saved on this device. Each environment will keep its own launcher data.", 13, muted) }
                panel("Quick actions") {
                    action("Browse versions") { activeTab = "Versions"; render() }
                    action(if (activeSpace == "Bedrock") "Browse add-ons" else "Browse mods") { activeTab = if (activeSpace == "Bedrock") "Add-ons" else "Mods"; render() }
                    action("Open accounts") { activeTab = "Accounts"; render() }
                }
            }
            "Versions" -> {
                label(body, "$activeSpace versions", 22, Color.WHITE, true)
                label(body, "Sample catalogue entries. Real version sources and installers are not connected yet.", 13, muted, false, 5)
                val versions = when (activeSpace) {
                    "Java" -> listOf("1.21.10 · Release", "1.21.9 · Release", "1.21 Snapshot")
                    "Bedrock" -> listOf("Installed Bedrock · Local", "1.21.80+ · Supported family")
                    else -> listOf("Starling 26.3-horizon · Native runtime")
                }
                versions.forEach { panel(it) { label(this, "Installation will be enabled after the engine is integrated.", 13, muted) } }
            }
            "Mods", "Add-ons" -> {
                label(body, if (activeSpace == "Bedrock") "Bedrock add-ons" else "$activeSpace content", 22, Color.WHITE, true)
                val content = when (activeSpace) {
                    "Java" -> listOf("Sodium · Performance", "Lithium · Performance", "Xaero's Minimap · Utility")
                    "Bedrock" -> listOf("Resource packs", "Behaviour packs", "World templates")
                    else -> listOf("No Starling-compatible catalogue connected yet")
                }
                content.forEach { panel(it) { label(this, "Sample entry only. Downloads and compatibility checks are not active.", 13, muted) } }
            }
            "Library" -> {
                label(body, "$activeSpace library", 22, Color.WHITE, true)
                panel("No installed profiles yet") { label(this, "Installed versions and worlds will appear here after engine integration.", 13, muted) }
            }
            "Accounts" -> {
                label(body, "$activeSpace accounts", 22, Color.WHITE, true)
                panel("Authentication integration pending") { label(this, "The selected engine will own its supported sign-in flow. This app does not collect credentials.", 13, muted) }
            }
            "Settings" -> {
                label(body, "$activeSpace settings", 22, Color.WHITE, true)
                val key = "setting_" + activeSpace.lowercase() + "_performance"
                val enabled = prefs.getBoolean(key, false)
                panel("Performance preference") {
                    label(this, "Example setting saved separately for $activeSpace.", 13, muted)
                    action(if (enabled) "Enabled · tap to disable" else "Disabled · tap to enable") {
                        prefs.edit().putBoolean(key, !enabled).apply()
                        render()
                    }
                }
                panel("Data boundary") { label(this, "Each engine will receive its own data root. Final paths depend on upstream storage requirements.", 13, muted) }
            }
        }
        label(body, "VOXIUM 0.1.0 · Native Android shell", 11, muted, false, 24)
    }

    private fun panel(title: String, content: LinearLayout.() -> Unit) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(13), dp(14), dp(13))
            background = rounded(panelColor)
        }
        label(box, title, 16, Color.WHITE, true)
        box.content()
        body.addView(box, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(9) })
    }

    private fun action(title: String, click: () -> Unit) {
        val button = Button(this).apply {
            text = title
            isAllCaps = false
            setTextColor(Color.WHITE)
            minHeight = dp(48)
            setPadding(dp(16), dp(8), dp(16), dp(8))
            background = rounded(0xFF493B78.toInt())
            stateListAnimator = null
            setOnClickListener { click() }
        }
        body.addView(button, LinearLayout.LayoutParams(-1, dp(46)).apply { topMargin = dp(6) })
    }

    private fun label(parent: LinearLayout, value: String, size: Int, color: Int, bold: Boolean = false, top: Int = 0) {
        val view = TextView(this).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
            if (top > 0) setPadding(0, dp(top), 0, dp(5))
        }
        parent.addView(view, LinearLayout.LayoutParams(-1, -2))
    }

    private fun rounded(color: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(15).toFloat()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
