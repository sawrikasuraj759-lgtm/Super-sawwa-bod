/*
 * Copyright (C) 2014 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */
package helium314.keyboard.keyboard

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import helium314.keyboard.latin.R
import helium314.keyboard.latin.common.AllColors
import helium314.keyboard.latin.common.ColorType
import helium314.keyboard.latin.common.Colors
import helium314.keyboard.latin.common.DefaultColors
import helium314.keyboard.latin.common.DynamicColors
import helium314.keyboard.latin.settings.Defaults
import helium314.keyboard.latin.settings.Settings
import helium314.keyboard.latin.utils.ResourceUtils
import helium314.keyboard.latin.utils.brightenOrDarken
import helium314.keyboard.latin.utils.isBrightColor
import helium314.keyboard.latin.utils.isGoodContrast
import helium314.keyboard.latin.utils.prefs
import helium314.keyboard.settings.SettingsActivity
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.EnumMap
import androidx.core.graphics.toColorInt

class KeyboardTheme // Note: The themeId should be aligned with "themeId" attribute of Keyboard style in values/themes-<style>.xml.
private constructor(val themeId: Int, @JvmField val mStyleId: Int) {
    override fun equals(other: Any?) = if (other === this) true
        else (other as? KeyboardTheme)?.themeId == themeId

    override fun hashCode(): Int {
        return themeId
    }

    companion object {
        // old themes, now called styles
        const val STYLE_MATERIAL = "Material"
        const val STYLE_HOLO = "Holo"
        const val STYLE_ROUNDED = "Rounded"

        // new themes that are just colors
        const val THEME_PURPLE_NEON = "purple_neon"
        const val THEME_BLUE_GALAXY = "blue_galaxy"
        const val THEME_SUNSET_GLOW = "sunset_glow"
        const val THEME_FOREST_GREEN = "forest_green"
        const val THEME_PINK_DREAM = "pink_dream"
        const val THEME_BLACK_GOLD = "black_gold"

        const val THEME_LIGHT = "light"
        const val THEME_HOLO_WHITE = "holo_white"
        const val THEME_DARK = "dark"
        const val THEME_DARKER = "darker"
        const val THEME_BLACK = "black"
        const val THEME_DYNAMIC = "dynamic"
        const val THEME_BLUE_GRAY = "blue_gray"
        const val THEME_BROWN = "brown"
        const val THEME_CHOCOLATE = "chocolate"
        const val THEME_CLOUDY = "cloudy"
        const val THEME_FOREST = "forest"
        const val THEME_INDIGO = "indigo"
        const val THEME_OCEAN = "ocean"
        const val THEME_PINK = "pink"
        const val THEME_SAND = "sand"
        const val THEME_VIOLETTE = "violette"
        fun getAvailableDefaultColors(prefs: SharedPreferences, isNight: Boolean) = listOfNotNull(
            THEME_PURPLE_NEON,
            THEME_BLUE_GALAXY,
            THEME_SUNSET_GLOW,
            THEME_FOREST_GREEN,
            THEME_PINK_DREAM,
            THEME_BLACK_GOLD,
            if (!isNight) THEME_LIGHT else null, THEME_DARK,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) THEME_DYNAMIC else null,
            if (prefs.getString(Settings.PREF_THEME_STYLE, Defaults.PREF_THEME_STYLE) == STYLE_HOLO) THEME_HOLO_WHITE else null,
            THEME_DARKER,
            THEME_BLACK,
            if (!isNight) THEME_BLUE_GRAY else null,
            if (!isNight) THEME_BROWN else null,
            THEME_CHOCOLATE,
            THEME_CLOUDY,
            THEME_FOREST,
            if (!isNight) THEME_INDIGO else null,
            if (!isNight) THEME_PINK else null,
            THEME_OCEAN,
            if (!isNight) THEME_SAND else null,
            THEME_VIOLETTE
        )
        val STYLES = arrayOf(STYLE_MATERIAL, STYLE_HOLO, STYLE_ROUNDED)

        // These should be aligned with Keyboard.themeId and Keyboard.Case.keyboardTheme
        // attributes' values in attrs.xml.
        private const val THEME_ID_HOLO_BASE = 0
        private const val THEME_ID_LXX_BASE = 1
        private const val THEME_ID_LXX_BASE_BORDER = 2
        private const val THEME_ID_ROUNDED_BASE = 3
        private const val THEME_ID_ROUNDED_BASE_BORDER = 4
        private const val DEFAULT_THEME_ID = THEME_ID_LXX_BASE

        private val KEYBOARD_THEMES = arrayOf(
            KeyboardTheme(THEME_ID_HOLO_BASE, R.style.KeyboardTheme_HoloBase),
            KeyboardTheme(THEME_ID_LXX_BASE, R.style.KeyboardTheme_LXX_Base),
            KeyboardTheme(THEME_ID_LXX_BASE_BORDER, R.style.KeyboardTheme_LXX_Base_Border),
            KeyboardTheme(THEME_ID_ROUNDED_BASE, R.style.KeyboardTheme_Rounded_Base),
            KeyboardTheme(THEME_ID_ROUNDED_BASE_BORDER, R.style.KeyboardTheme_Rounded_Base_Border)
        )

        // named colors, with names from old settings
        const val COLOR_ACCENT = "accent"
        const val COLOR_GESTURE = "gesture"
        const val COLOR_SUGGESTION_TEXT = "suggestion_text"
        const val COLOR_TEXT = "text"
        const val COLOR_HINT_TEXT = "hint_text"
        const val COLOR_KEYS = "keys"
        const val COLOR_FUNCTIONAL_KEYS = "functional_keys"
        const val COLOR_SPACEBAR = "spacebar"
        const val COLOR_SPACEBAR_TEXT = "spacebar_text"
        const val COLOR_BACKGROUND = "background"

        @JvmStatic
        fun getKeyboardTheme(context: Context): KeyboardTheme {
            val prefs = context.prefs()
            val style = prefs.getString(Settings.PREF_THEME_STYLE, Defaults.PREF_THEME_STYLE)
            val borders = prefs.getBoolean(Settings.PREF_THEME_KEY_BORDERS, Defaults.PREF_THEME_KEY_BORDERS)
            val matchingId = when (style) {
                STYLE_HOLO -> THEME_ID_HOLO_BASE
                STYLE_ROUNDED -> if (borders) THEME_ID_ROUNDED_BASE_BORDER else THEME_ID_ROUNDED_BASE
                else -> if (borders) THEME_ID_LXX_BASE_BORDER else THEME_ID_LXX_BASE
            }
            return KEYBOARD_THEMES.firstOrNull { it.themeId == matchingId } ?: KEYBOARD_THEMES[DEFAULT_THEME_ID]
        }

        fun getThemeActionAndEmojiKeyLabelFlags(themeId: Int): Int {
            return if (themeId == THEME_ID_LXX_BASE || themeId == THEME_ID_ROUNDED_BASE) Key.LABEL_FLAGS_KEEP_BACKGROUND_ASPECT_RATIO else 0
        }

        @JvmStatic
        fun getColorsForCurrentTheme(context: Context): Colors {
            val prefs = context.prefs()
            val isNight = SettingsActivity.forceNight
                ?: (ResourceUtils.isNight(context.resources) && prefs.getBoolean(Settings.PREF_THEME_DAY_NIGHT, Defaults.PREF_THEME_DAY_NIGHT))
            val themeName = SettingsActivity.forceTheme ?: if (isNight)
                prefs.getString(Settings.PREF_THEME_COLORS_NIGHT, Defaults.PREF_THEME_COLORS_NIGHT)
            else
                prefs.getString(Settings.PREF_THEME_COLORS, Defaults.PREF_THEME_COLORS)
            val themeStyle = prefs.getString(Settings.PREF_THEME_STYLE, Defaults.PREF_THEME_STYLE)

            return getThemeColors(themeName!!, themeStyle!!, context, prefs, isNight)
        }

        private fun getThemeColors(themeName: String, themeStyle: String, context: Context, prefs: SharedPreferences, isNight: Boolean): Colors {
            val hasBorders = prefs.getBoolean(Settings.PREF_THEME_KEY_BORDERS, Defaults.PREF_THEME_KEY_BORDERS)
            val backgroundImage = Settings.readUserBackgroundImage(context, isNight)
            return when (themeName) {
                THEME_DYNAMIC -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) DynamicColors(context, themeStyle, hasBorders, backgroundImage)
                    else getThemeColors(THEME_LIGHT, themeStyle, context, prefs, isNight)
                }
                THEME_LIGHT -> DefaultColors(
                    themeStyle,
                    hasBorders,
                    ContextCompat.getColor(context, R.color.gesture_trail_color_lxx_light),
                    ContextCompat.getColor(context, R.color.keyboard_background_lxx_light_border),
                    ContextCompat.getColor(context, R.color.key_background_normal_lxx_light_border),
                    ContextCompat.getColor(context, R.color.key_background_functional_lxx_light_border),
                    ContextCompat.getColor(context, R.color.key_background_normal_lxx_light_border),
                    ContextCompat.getColor(context, R.color.key_text_color_lxx_light),
                    ContextCompat.getColor(context, R.color.key_hint_letter_color_lxx_light),
                    keyboardBackground = backgroundImage
                )
                THEME_DARK -> DefaultColors(
                    themeStyle,
                    hasBorders,
                    ContextCompat.getColor(context, R.color.gesture_trail_color_lxx_dark),
                    "#263238".toColorInt(),
                    "#364248".toColorInt(),
                    "#2d393f".toColorInt(),
                    "#364248".toColorInt(),
                    ContextCompat.getColor(context, R.color.key_text_color_lxx_dark),
                    ContextCompat.getColor(context, R.color.key_hint_letter_color_lxx_dark),
                    keyboardBackground = backgroundImage
                )
                THEME_HOLO_WHITE -> DefaultColors(
                    themeStyle,
                    hasBorders,
                    Color.WHITE,
                    "#282828".toColorInt(),
                    Color.WHITE, // drawable is transparent
                    "#444444".toColorInt(), // should be 222222, but the key drawable is already grey
                    Color.WHITE,
                    Color.WHITE,
                    "#282828".toColorInt(),
                    Color.WHITE,
                    "#80FFFFFF".toColorInt(),
                    keyboardBackground = backgroundImage
                )
                THEME_DARKER -> DefaultColors(
                    themeStyle,
                    hasBorders,
                    ContextCompat.getColor(context, R.color.gesture_trail_color_lxx_dark),
                    ContextCompat.getColor(context, R.color.keyboard_background_lxx_dark_border),
                    ContextCompat.getColor(context, R.color.key_background_normal_lxx_dark_border),
                    ContextCompat.getColor(context, R.color.key_background_functional_lxx_dark_border),
                    ContextCompat.getColor(context, R.color.key_background_normal_lxx_dark_border),
                    ContextCompat.getColor(context, R.color.key_text_color_lxx_dark),
                    ContextCompat.getColor(context, R.color.key_hint_letter_color_lxx_dark),
                    keyboardBackground = backgroundImage
                )
                THEME_PURPLE_NEON, THEME_VIOLETTE -> DefaultColors(
                    themeStyle,
                    hasBorders,
                    Color.rgb(255, 66, 232),      // accent: Electric Magenta / Pink (#FF42E8)
                    Color.rgb(18, 2, 36),         // background: Deep Neon Dark Violet (#120224)
                    Color.rgb(65, 16, 99),        // keys: Rich Glowing Violet (#411063)
                    Color.rgb(107, 29, 153),      // functional keys: Neon Purple (#6B1D99)
                    Color.rgb(65, 16, 99),        // spacebar
                    Color.WHITE,                  // key text: Pure Crisp White
                    Color.rgb(224, 176, 255),     // hint text: Lilac Glow
                    Color.rgb(255, 66, 232),      // suggestion text
                    Color.rgb(224, 176, 255),     // spacebar text
                    Color.rgb(255, 66, 232),      // gesture trail
                    keyboardBackground = backgroundImage
                )
                THEME_BLUE_GALAXY, THEME_OCEAN -> DefaultColors(
                    themeStyle,
                    hasBorders,
                    Color.rgb(0, 240, 255),       // accent: Cyan Neon (#00F0FF)
                    Color.rgb(7, 17, 38),         // background: Deep Cosmic Navy (#071126)
                    Color.rgb(13, 51, 102),       // keys: Cosmic Galaxy Blue (#0D3366)
                    Color.rgb(2, 132, 199),       // functional keys: Electric Blue (#0284C7)
                    Color.rgb(13, 51, 102),       // spacebar
                    Color.WHITE,                  // key text: White
                    Color.rgb(125, 211, 252),     // hint text: Cyan Ice Glow
                    Color.rgb(0, 240, 255),       // suggestion text
                    Color.rgb(125, 211, 252),     // spacebar text
                    Color.rgb(0, 240, 255),       // gesture trail
                    keyboardBackground = backgroundImage
                )
                THEME_SUNSET_GLOW, THEME_CHOCOLATE -> DefaultColors(
                    themeStyle,
                    hasBorders,
                    Color.rgb(249, 115, 22),      // accent: Radiant Sunset Orange (#F97316)
                    Color.rgb(43, 9, 9),          // background: Deep Sunset Crimson (#2B0909)
                    Color.rgb(105, 25, 25),       // keys: Fiery Crimson-Orange (#691919)
                    Color.rgb(194, 65, 12),       // functional keys: Burning Flame (#C2410C)
                    Color.rgb(105, 25, 25),       // spacebar
                    Color.WHITE,                  // key text: White
                    Color.rgb(253, 230, 138),     // hint text: Warm Gold
                    Color.rgb(249, 115, 22),      // suggestion text
                    Color.rgb(253, 230, 138),     // spacebar text
                    Color.rgb(249, 115, 22),      // gesture trail
                    keyboardBackground = backgroundImage
                )
                THEME_FOREST_GREEN, THEME_FOREST -> DefaultColors(
                    themeStyle,
                    hasBorders,
                    Color.rgb(16, 185, 129),      // accent: Electric Emerald (#10B981)
                    Color.rgb(3, 31, 20),         // background: Deep Emerald Night (#031F14)
                    Color.rgb(6, 78, 59),         // keys: Emerald Green (#064E3B)
                    Color.rgb(4, 120, 87),        // functional keys: Bright Jade (#047857)
                    Color.rgb(6, 78, 59),         // spacebar
                    Color.WHITE,                  // key text: White
                    Color.rgb(167, 243, 208),     // hint text: Mint Glow
                    Color.rgb(52, 211, 153),      // suggestion text
                    Color.rgb(167, 243, 208),     // spacebar text
                    Color.rgb(16, 185, 129),      // gesture trail
                    keyboardBackground = backgroundImage
                )
                THEME_PINK_DREAM, THEME_PINK -> DefaultColors(
                    themeStyle,
                    hasBorders,
                    Color.rgb(255, 66, 232),      // accent: Neon Hot Pink (#FF42E8)
                    Color.rgb(41, 2, 24),         // background: Deep Magenta Night (#290218)
                    Color.rgb(112, 8, 64),        // keys: Radiant Berry Pink (#700840)
                    Color.rgb(190, 24, 93),       // functional keys: Electric Berry (#BE185D)
                    Color.rgb(112, 8, 64),        // spacebar
                    Color.WHITE,                  // key text: White
                    Color.rgb(251, 207, 232),     // hint text: Soft Pink Glow
                    Color.rgb(255, 66, 232),      // suggestion text
                    Color.rgb(251, 207, 232),     // spacebar text
                    Color.rgb(255, 66, 232),      // gesture trail
                    keyboardBackground = backgroundImage
                )
                THEME_BLACK_GOLD, THEME_BLACK -> DefaultColors(
                    themeStyle,
                    hasBorders,
                    Color.rgb(245, 158, 11),      // accent: Royal Gold (#F59E0B)
                    Color.rgb(10, 9, 8),          // background: Luxury Pitch Black (#0A0908)
                    Color.rgb(36, 32, 28),        // keys: Charcoal Obsidian (#24201C)
                    Color.rgb(68, 52, 30),        // functional keys: Antique Bronze (#44341E)
                    Color.rgb(36, 32, 28),        // spacebar
                    Color.rgb(254, 243, 199),     // key text: Warm Imperial Gold-White (#FEF3C7)
                    Color.rgb(217, 119, 6),       // hint text: Golden Amber (#D97706)
                    Color.rgb(245, 158, 11),      // suggestion text
                    Color.rgb(217, 119, 6),       // spacebar text
                    Color.rgb(245, 158, 11),      // gesture trail
                    keyboardBackground = backgroundImage
                )
                THEME_BLUE_GRAY -> DefaultColors(
                    themeStyle,
                    hasBorders,
                    Color.rgb(120, 144, 156),
                    Color.rgb(236, 239, 241),
                    Color.WHITE,
                    Color.rgb(207, 216, 220),
                    Color.WHITE,
                    Color.BLACK,
                    Color.BLACK,
                    keyboardBackground = backgroundImage
                )
                THEME_BROWN -> DefaultColors(
                    themeStyle,
                    hasBorders,
                    Color.rgb(141, 110, 99),
                    Color.rgb(239, 235, 233),
                    Color.WHITE,
                    Color.rgb(215, 204, 200),
                    Color.WHITE,
                    Color.BLACK,
                    Color.BLACK,
                    keyboardBackground = backgroundImage
                )
                THEME_CLOUDY -> DefaultColors(
                    themeStyle,
                    hasBorders,
                    Color.rgb(255, 113, 129),
                    Color.rgb(81, 97, 113),
                    Color.rgb(117, 128, 142),
                    Color.rgb(99, 109, 121),
                    Color.rgb(117, 128, 142),
                    Color.WHITE,
                    Color.WHITE,
                    keyboardBackground = backgroundImage
                )
                THEME_INDIGO -> DefaultColors(
                    themeStyle,
                    hasBorders,
                    Color.rgb(92, 107, 192),
                    Color.rgb(232, 234, 246),
                    Color.WHITE,
                    Color.rgb(197, 202, 233),
                    Color.WHITE,
                    Color.BLACK,
                    Color.BLACK,
                    keyboardBackground = backgroundImage
                )
                THEME_SAND -> DefaultColors(
                    themeStyle,
                    hasBorders,
                    Color.rgb(110, 155, 255),
                    Color.rgb(242, 232, 218),
                    Color.WHITE,
                    Color.rgb(234, 211, 185),
                    Color.WHITE,
                    Color.BLACK,
                    Color.BLACK,
                    keyboardBackground = backgroundImage
                )
                else -> { // user-defined theme
                    val colorSettings = readUserColors(prefs, themeName)
                    val colors = readUserColorTheme(themeStyle, hasBorders, colorSettings, context, isNight, backgroundImage)
                    if (readUserMoreColors(prefs, themeName) == 2)
                        AllColors(readUserAllColors(prefs, themeName, colors), themeStyle, hasBorders, backgroundImage)
                    else {
                        colors
                    }
                }
            }
        }

        fun readUserColorTheme(themeStyle: String, hasBorders: Boolean, colorSettings: List<ColorSetting>, context: Context, isNight: Boolean, backgroundImage: Drawable?): Colors {
            return DefaultColors(
                themeStyle,
                hasBorders,
                determineUserColor(colorSettings, context, COLOR_ACCENT, isNight),
                determineUserColor(colorSettings, context, COLOR_BACKGROUND, isNight),
                determineUserColor(colorSettings, context, COLOR_KEYS, isNight),
                determineUserColor(colorSettings, context, COLOR_FUNCTIONAL_KEYS, isNight),
                determineUserColor(colorSettings, context, COLOR_SPACEBAR, isNight),
                determineUserColor(colorSettings, context, COLOR_TEXT, isNight),
                determineUserColor(colorSettings, context, COLOR_HINT_TEXT, isNight),
                determineUserColor(colorSettings, context, COLOR_SUGGESTION_TEXT, isNight),
                determineUserColor(colorSettings, context, COLOR_SPACEBAR_TEXT, isNight),
                determineUserColor(colorSettings, context, COLOR_GESTURE, isNight),
                backgroundImage,
            )
        }

        fun writeUserColors(prefs: SharedPreferences, themeName: String, colors: List<ColorSetting>) {
            val key = Settings.PREF_USER_COLORS_PREFIX + themeName
            val value = Json.encodeToString(colors.filter { it.color != null || it.auto == false })
            prefs.edit { putString(key, value) }
            KeyboardSwitcher.getInstance().setThemeNeedsReload()
        }

        fun readUserColors(prefs: SharedPreferences, themeName: String): List<ColorSetting> {
            val key = Settings.PREF_USER_COLORS_PREFIX + themeName
            return Json.decodeFromString(prefs.getString(key, Defaults.PREF_USER_COLORS)!!)
        }

        fun writeUserMoreColors(prefs: SharedPreferences, themeName: String, value: Int) {
            val key = Settings.PREF_USER_MORE_COLORS_PREFIX + themeName
            prefs.edit { putInt(key, value) }
            KeyboardSwitcher.getInstance().setThemeNeedsReload()
        }

        fun readUserMoreColors(prefs: SharedPreferences, themeName: String): Int {
            val key = Settings.PREF_USER_MORE_COLORS_PREFIX + themeName
            return prefs.getInt(key, Defaults.PREF_USER_MORE_COLORS)
        }

        fun writeUserAllColors(prefs: SharedPreferences, themeName: String, colorMap: EnumMap<ColorType, Int>) {
            val key = Settings.PREF_USER_ALL_COLORS_PREFIX + themeName
            prefs.edit { putString(key, colorMap.map { "${it.key},${it.value}" }.joinToString(";")) }
            KeyboardSwitcher.getInstance().setThemeNeedsReload()
        }

        fun readUserAllColors(prefs: SharedPreferences, themeName: String, fallback: Colors?): EnumMap<ColorType, Int> {
            val key = Settings.PREF_USER_ALL_COLORS_PREFIX + themeName
            val colorsString = prefs.getString(key, Defaults.PREF_USER_ALL_COLORS)!!
            val colorMap = EnumMap<ColorType, Int>(ColorType::class.java)
            colorsString.split(";").forEach {
                val ct = try {
                    ColorType.valueOf(it.substringBefore(",").uppercase())
                } catch (_: IllegalArgumentException) {
                    return@forEach
                }
                val i = it.substringAfter(",").toIntOrNull() ?: return@forEach
                colorMap[ct] = i
            }
            if (fallback != null && colorMap.size < ColorType.entries.size) {
                ColorType.entries.forEach {
                    if (it in colorMap) return@forEach
                    colorMap[it] = fallback.get(it)
                }
            }
            return colorMap
        }

        fun getUnusedThemeName(initialName: String, prefs: SharedPreferences): String {
            val existingNames = getExistingThemeNames(prefs)
            if (initialName !in existingNames) return initialName
            var i = 1
            while ("$initialName$i" in existingNames)
                i++
            return "$initialName$i"
        }

        private fun getExistingThemeNames(prefs: SharedPreferences) =
            prefs.all.keys.mapNotNull {
                when {
                    it.startsWith(Settings.PREF_USER_COLORS_PREFIX) -> it.substringAfter(Settings.PREF_USER_COLORS_PREFIX)
                    it.startsWith(Settings.PREF_USER_ALL_COLORS_PREFIX) -> it.substringAfter(Settings.PREF_USER_ALL_COLORS_PREFIX)
                    it.startsWith(Settings.PREF_USER_MORE_COLORS_PREFIX) -> it.substringAfter(Settings.PREF_USER_MORE_COLORS_PREFIX)
                    else -> null
                }
            }.toSortedSet()

        // returns false if not renamed due to invalid name or collision
        fun renameUserColors(from: String, to: String, prefs: SharedPreferences): Boolean {
            if (to.isBlank()) return false // don't want that
            if (to == from) return true // nothing to do
            val existingNames = getExistingThemeNames(prefs)
            if (to in existingNames) return false
            // all good, now rename
            prefs.edit {
                if (prefs.contains(Settings.PREF_USER_COLORS_PREFIX + from)) {
                    putString(Settings.PREF_USER_COLORS_PREFIX + to, prefs.getString(Settings.PREF_USER_COLORS_PREFIX + from, ""))
                    remove(Settings.PREF_USER_COLORS_PREFIX + from)
                }
                if (prefs.contains(Settings.PREF_USER_ALL_COLORS_PREFIX + from)) {
                    putString(Settings.PREF_USER_ALL_COLORS_PREFIX + to, prefs.getString(Settings.PREF_USER_ALL_COLORS_PREFIX + from, ""))
                    remove(Settings.PREF_USER_ALL_COLORS_PREFIX + from)
                }
                if (prefs.contains(Settings.PREF_USER_MORE_COLORS_PREFIX + from)) {
                    putInt(Settings.PREF_USER_MORE_COLORS_PREFIX + to, prefs.getInt(Settings.PREF_USER_MORE_COLORS_PREFIX + from, 0))
                    remove(Settings.PREF_USER_MORE_COLORS_PREFIX + from)
                }
                if (prefs.getString(Settings.PREF_THEME_COLORS, Defaults.PREF_THEME_COLORS) == from)
                    putString(Settings.PREF_THEME_COLORS, to)
                if (prefs.getString(Settings.PREF_THEME_COLORS_NIGHT, Defaults.PREF_THEME_COLORS_NIGHT) == from)
                    putString(Settings.PREF_THEME_COLORS_NIGHT, to)
            }
            return true
        }

        fun determineUserColor(colors: List<ColorSetting>, context: Context, colorName: String, isNight: Boolean): Int {
            val c = colors.firstOrNull { it.name == colorName }
            val color = c?.color
            val auto = c?.auto ?: true
            return if (auto || color == null)
                determineAutoColor(colors, colorName, isNight, context)
            else color
        }

        private fun determineAutoColor(colors: List<ColorSetting>, colorName: String, isNight: Boolean, context: Context): Int {
            when (colorName) {
                COLOR_ACCENT -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                        // try determining accent color on Android 10 & 11, accent is not available in resources
                        val wrapper: Context = ContextThemeWrapper(context, android.R.style.Theme_DeviceDefault)
                        val value = TypedValue()
                        if (wrapper.theme.resolveAttribute(android.R.attr.colorAccent, value, true)) return value.data
                    }
                    return ContextCompat.getColor(Settings.getDayNightContext(context, isNight), R.color.accent)
                }
                COLOR_GESTURE -> return determineUserColor(colors, context, COLOR_ACCENT, isNight)
                COLOR_SUGGESTION_TEXT ->
                    return determineUserColor(colors, context, COLOR_TEXT, isNight)
                COLOR_TEXT -> {
                    // base it on background color, and not key, because it's also used for suggestions
                    val background = determineUserColor(colors, context, COLOR_BACKGROUND, isNight)
                    return if (isBrightColor(background)) {
                        // but if key borders are enabled, we still want reasonable contrast
                        if (!context.prefs().getBoolean(Settings.PREF_THEME_KEY_BORDERS, Defaults.PREF_THEME_KEY_BORDERS)
                            || isGoodContrast(Color.BLACK, determineUserColor(colors, context, COLOR_KEYS, isNight))
                        ) Color.BLACK
                        else Color.GRAY
                    } else Color.WHITE
                }
                COLOR_HINT_TEXT -> {
                    return if (isBrightColor(determineUserColor(colors, context, COLOR_KEYS, isNight))) Color.DKGRAY
                    else determineUserColor(colors, context, COLOR_TEXT, isNight)
                }
                COLOR_KEYS ->
                    return brightenOrDarken(determineUserColor(colors, context, COLOR_BACKGROUND, isNight), isNight)
                COLOR_FUNCTIONAL_KEYS ->
                    return brightenOrDarken(determineUserColor(colors, context, COLOR_KEYS, isNight), true)
                COLOR_SPACEBAR -> return determineUserColor(colors, context, COLOR_KEYS, isNight)
                COLOR_SPACEBAR_TEXT -> {
                    val spacebar = determineUserColor(colors, context, COLOR_SPACEBAR, isNight)
                    val hintText = determineUserColor(colors, context, COLOR_HINT_TEXT, isNight)
                    if (isGoodContrast(hintText, spacebar)) return hintText and -0x7f000001 // add some transparency
                    val text = determineUserColor(colors, context, COLOR_TEXT, isNight)
                    if (isGoodContrast(text, spacebar)) return text and -0x7f000001
                    return if (isBrightColor(spacebar)) Color.BLACK and -0x7f000001
                    else Color.WHITE and -0x7f000001
                }
                COLOR_BACKGROUND -> return ContextCompat.getColor(
                    Settings.getDayNightContext(context, isNight),
                    R.color.keyboard_background
                )
                else -> return ContextCompat.getColor(Settings.getDayNightContext(context, isNight), R.color.keyboard_background)
            }
        }
    }
}

@Serializable
data class ColorSetting(val name: String, val auto: Boolean?, val color: Int?) {
    var displayName = name
}
