// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings as AndroidSettings
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import helium314.keyboard.keyboard.KeyboardSwitcher
import helium314.keyboard.keyboard.KeyboardTheme
import helium314.keyboard.latin.R
import helium314.keyboard.latin.settings.Settings as HeliSettings
import helium314.keyboard.latin.utils.UncachedInputMethodManagerUtils
import helium314.keyboard.latin.utils.prefs
import helium314.keyboard.latin.utils.protectedPrefs
import helium314.keyboard.settings.screens.*

// Precise user color palette matching the screenshot
val Purple = Color(0xFFB000FF)
val Pink = Color(0xFFFF42E8)
val Dark = Color(0xFF09000F)
val SB_DarkBg = Dark
val CardColor = Color(0xFF1C1028)
val NeonBorder = Color(0xFF8534A9)
val NeonGlow = Color(0xFFB000FF)

val TextWhite = Color(0xFFFFFFFF)
val TextMuted = Color(0xFFC7B8E6)
val TextDim = Color(0xFF82709E)

val SoftBoardM3ColorScheme = darkColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    primaryContainer = CardColor,
    onPrimaryContainer = Color(0xFFF3E8FF),
    secondary = Pink,
    background = Dark,
    onBackground = Color.White,
    surface = CardColor,
    onSurface = Color.White,
    outline = NeonBorder
)

data class SoftBoardThemeItem(
    val name: String,
    val heliThemeKey: String,
    val category: String,
    val previewResId: Int,
    val gradient: List<Color>,
    val isCrown: Boolean = false
)

@Composable
fun NeonText(
    text: String,
    size: Int = 20,
    color: Color = Color.White,
    fontWeight: FontWeight = FontWeight.Bold,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = color,
        fontSize = size.sp,
        fontWeight = fontWeight,
        modifier = modifier
    )
}

@Composable
fun NeonCard(
    modifier: Modifier = Modifier,
    glowAlpha: Float = 0.8f,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = (14 * glowAlpha).dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Purple,
                spotColor = Pink
            )
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF321044),
                        CardColor,
                        Color(0xFF13091E)
                    )
                ),
                RoundedCornerShape(22.dp)
            )
            .border(
                1.5.dp,
                NeonBorder.copy(alpha = glowAlpha),
                RoundedCornerShape(22.dp)
            )
            .padding(15.dp),
        content = content
    )
}

@Composable
private fun WelcomeFeatureItem(
    icon: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x591F0838))
            .border(BorderStroke(0.8.dp, NeonBorder.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Purple.copy(alpha = 0.6f), Color(0x33B000FF)))),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = desc,
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun SoftBoardWelcomeDialog(
    onDismiss: () -> Unit,
    onExploreThemes: () -> Unit,
    onGetStarted: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "WelcomeGlow")
        val glowScale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "GlowScale"
        )
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "GlowAlpha"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .shadow(
                    elevation = 28.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Purple,
                    spotColor = Pink
                )
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF28073D),
                            Color(0xFF140224),
                            Color(0xFF090013)
                        )
                    )
                )
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(
                                Purple,
                                Pink,
                                Color(0xFF67E8F9),
                                Purple
                            )
                        )
                    ),
                    RoundedCornerShape(28.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                // Glowing Logo with Animated Halo
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(86.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .scale(glowScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Purple.copy(alpha = 0.5f * glowAlpha),
                                        Pink.copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_home_logo),
                        contentDescription = "SoftBoard Logo",
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(16.dp, RoundedCornerShape(20.dp), ambientColor = Purple, spotColor = Pink)
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Welcome Header
                Text(
                    text = "Welcome to SoftBoard",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.White,
                                Color(0xFFF3E8FF),
                                Pink,
                                Color(0xFF67E8F9)
                            )
                        ),
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(Modifier.height(3.dp))

                Text(
                    text = "Smart Keyboard, Smarter You ✨",
                    color = Color(0xFFE9D5FF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Made by Yashi 💜",
                    color = Pink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(Modifier.height(16.dp))

                // Feature Highlights
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    WelcomeFeatureItem(
                        icon = "⚡",
                        title = "Fast & Smooth Typing",
                        desc = "Smart gesture typing, quick corrections & clipboard"
                    )
                    WelcomeFeatureItem(
                        icon = "🎨",
                        title = "6 Glowing Neon & Galaxy Themes",
                        desc = "Purple Neon, Blue Galaxy, Sunset Glow & more"
                    )
                    WelcomeFeatureItem(
                        icon = "🔒",
                        title = "100% Private & Offline",
                        desc = "No keystrokes or data ever leaves your device"
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Primary Button
                Button(
                    onClick = onGetStarted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = Purple, spotColor = Pink),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF9333EA),
                                        Color(0xFFD946EF),
                                        Color(0xFF8B5CF6)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Text(
                                text = "Get Started & Type Live 🚀",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onExploreThemes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(21.dp),
                    border = BorderStroke(1.dp, NeonBorder.copy(alpha = 0.8f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🎨", fontSize = 13.sp)
                        Text(
                            text = "Explore Neon Themes",
                            color = Color(0xFFF3E8FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SoftBoardApp(
    onClickBack: () -> Unit
) {
    val context = LocalContext.current
    val imm = remember { context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }
    var selectedTab by remember { mutableIntStateOf(0) }
    var activeSubscreen by remember { mutableStateOf<String?>(null) }
    var showProDialog by remember { mutableStateOf(false) }
    var showWelcomeDialog by rememberSaveable { mutableStateOf(true) }

    var isEnabled by remember { mutableStateOf(UncachedInputMethodManagerUtils.isThisImeEnabled(context, imm)) }
    var isCurrent by remember { mutableStateOf(UncachedInputMethodManagerUtils.isThisImeCurrent(context, imm)) }

    fun refreshImeStatus() {
        isEnabled = UncachedInputMethodManagerUtils.isThisImeEnabled(context, imm)
        isCurrent = UncachedInputMethodManagerUtils.isThisImeCurrent(context, imm)
    }

    BackHandler {
        if (activeSubscreen != null) {
            activeSubscreen = null
        } else if (selectedTab != 0) {
            selectedTab = 0
        } else {
            onClickBack()
        }
    }

    MaterialTheme(colorScheme = SoftBoardM3ColorScheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF070010),
                            Color(0xFF140226),
                            Color(0xFF090014),
                            Color(0xFF04000A)
                        )
                    )
                )
        ) {
            if (activeSubscreen != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Dark)
                ) {
                    when (activeSubscreen) {
                        SettingsDestination.About -> AboutScreen(onClickBack = { activeSubscreen = null })
                        SettingsDestination.TextCorrection -> TextCorrectionScreen(onClickBack = { activeSubscreen = null })
                        SettingsDestination.Preferences -> PreferencesScreen(onClickBack = { activeSubscreen = null })
                        SettingsDestination.Toolbar -> ToolbarScreen(onClickBack = { activeSubscreen = null })
                        SettingsDestination.Appearance -> AppearanceScreen(onClickBack = { activeSubscreen = null })
                        SettingsDestination.Languages -> LanguageScreen(onClickBack = { activeSubscreen = null })
                        SettingsDestination.Layouts -> SecondaryLayoutScreen(onClickBack = { activeSubscreen = null })
                        SettingsDestination.Dictionaries -> DictionaryScreen(onClickBack = { activeSubscreen = null })
                        SettingsDestination.Advanced -> AdvancedSettingsScreen(onClickBack = { activeSubscreen = null })
                        else -> activeSubscreen = null
                    }
                }
            } else {
                Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = {
                        BottomNavigationBar(selectedTab = selectedTab) {
                            selectedTab = it
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(160))
                            },
                            label = "TabContentAnimation"
                        ) { tab ->
                            when (tab) {
                                0 -> HomeScreen(
                                    isEnabled = isEnabled,
                                    isCurrent = isCurrent,
                                    onGetStarted = {
                                        refreshImeStatus()
                                        if (!isEnabled) {
                                            context.startActivity(Intent(AndroidSettings.ACTION_INPUT_METHOD_SETTINGS))
                                            Toast.makeText(context, "Please enable SoftBoard in Android Settings", Toast.LENGTH_LONG).show()
                                        } else if (!isCurrent) {
                                            imm.showInputMethodPicker()
                                        } else {
                                            selectedTab = 1
                                            Toast.makeText(context, "SoftBoard is active and ready!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onProClick = { showProDialog = true },
                                    onShowWelcome = { showWelcomeDialog = true }
                                )
                                1 -> ThemesScreen(
                                    onNavigateTo = { activeSubscreen = it },
                                    onBackClick = { selectedTab = 0 }
                                )
                                2 -> ToolsScreen(
                                    onNavigateTo = { activeSubscreen = it },
                                    onBackClick = { selectedTab = 0 }
                                )
                                3 -> SettingsScreen(
                                    onNavigateTo = { activeSubscreen = it },
                                    onBackClick = { selectedTab = 0 }
                                )
                            }
                        }
                    }
                }
            }

            if (showProDialog) {
                AlertDialog(
                    onDismissRequest = { showProDialog = false },
                    containerColor = CardColor,
                    titleContentColor = Color.White,
                    textContentColor = TextMuted,
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👑 ", fontSize = 24.sp)
                            Text("SoftBoard Pro", fontWeight = FontWeight.Bold, color = Color.Yellow)
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("All Premium Neon Features Unlocked!", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("• 6 Glowing Cosmic & Neon Themes")
                            Text("• Intelligent Text Correction & Dictionaries")
                            Text("• Quick Clipboard Manager & Voice Typing")
                            Text("• 100% Offline & Private (No Data Leaves Device)")
                            Text("• Handcrafted with love by Yashi 💜", color = Pink)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showProDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Purple)
                        ) {
                            Text("Awesome", color = Color.White)
                        }
                    }
                )
            }

            if (showWelcomeDialog) {
                SoftBoardWelcomeDialog(
                    onDismiss = { showWelcomeDialog = false },
                    onExploreThemes = {
                        showWelcomeDialog = false
                        selectedTab = 1
                    },
                    onGetStarted = {
                        showWelcomeDialog = false
                        refreshImeStatus()
                        if (!isEnabled) {
                            context.startActivity(Intent(AndroidSettings.ACTION_INPUT_METHOD_SETTINGS))
                            Toast.makeText(context, "Please enable SoftBoard in Android Settings", Toast.LENGTH_LONG).show()
                        } else if (!isCurrent) {
                            imm.showInputMethodPicker()
                        } else {
                            selectedTab = 0
                            Toast.makeText(context, "SoftBoard is active and ready!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

// ---------------------------------------------------------
// 1. HOME SCREEN (100% MATCHING THE SCREENSHOT)
// ---------------------------------------------------------
@Composable
fun HomeScreen(
    isEnabled: Boolean = true,
    isCurrent: Boolean = true,
    onGetStarted: () -> Unit,
    onProClick: () -> Unit = {},
    onShowWelcome: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "NeonPulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Row: [ ✨ Welcome ] ... [ 👑 Pro ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x802A0B4D))
                    .border(
                        BorderStroke(1.2.dp, Purple.copy(alpha = glowAlpha)),
                        RoundedCornerShape(50)
                    )
                    .clickable { onShowWelcome() }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✨ ", fontSize = 13.sp)
                    Text(
                        "Welcome",
                        color = Color(0xFFF3E8FF),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x802A0B4D))
                    .border(
                        BorderStroke(1.5.dp, Pink.copy(alpha = glowAlpha)),
                        RoundedCornerShape(50)
                    )
                    .clickable { onProClick() }
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("👑 ", fontSize = 14.sp)
                    Text(
                        "Pro",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Glowing Keyboard App Icon (Top Center)
        Image(
            painter = painterResource(R.drawable.ic_home_logo),
            contentDescription = "SoftBoard Logo",
            modifier = Modifier
                .size(95.dp)
                .shadow(
                    elevation = (18 * glowAlpha).dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = Purple,
                    spotColor = Pink
                )
        )

        Spacer(Modifier.height(10.dp))

        // Title: SoftBoard with soft purple-white gradient
        Text(
            text = "SoftBoard",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            style = TextStyle(
                brush = Brush.horizontalGradient(
                    listOf(Color.White, Color(0xFFFDE8FF), Color(0xFFF3B8FF))
                )
            ),
            letterSpacing = 0.5.sp
        )

        // Subtitle: Smart Keyboard, Smarter You
        Text(
            text = "Smart Keyboard, Smarter You",
            fontSize = 15.sp,
            color = Color(0xFFEDE4F8),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 3.dp)
        )

        Spacer(Modifier.height(4.dp))

        // Glowing Pink Decorative Flourish Underline
        Image(
            painter = painterResource(R.drawable.ic_pink_swoosh),
            contentDescription = null,
            modifier = Modifier
                .width(230.dp)
                .height(20.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.height(4.dp))

        // Subtext: Made by Yashi 💜
        Text(
            text = "Made by Yashi 💜",
            fontSize = 13.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )

        var showHomeWelcomeBanner by rememberSaveable { mutableStateOf(true) }
        if (showHomeWelcomeBanner) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xCC2A0740),
                                Color(0xCC180326),
                                Color(0xCC350752)
                            )
                        )
                    )
                    .border(
                        BorderStroke(1.dp, Brush.horizontalGradient(listOf(Purple, Pink, Color(0xFF38BDF8)))),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onShowWelcome() }
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Pink.copy(alpha = 0.5f), Color.Transparent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👋", fontSize = 16.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome to SoftBoard! ✨",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Smart, fast & private typing with glowing neon themes.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(
                        onClick = { showHomeWelcomeBanner = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = TextDim,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // 3D Neon Glowing Keyboard Hero Graphic
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(205.dp)
                .offset(y = floatOffset.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.img_keyboard_hero),
                contentDescription = "SoftBoard 3D Neon Keyboard",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(12.dp))

        // Unified 4-Feature Glass Card with Internal Vertical Dividers
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = (14 * glowAlpha).dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Purple,
                    spotColor = Pink
                )
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xCC260C48),
                            Color(0xCC16052B),
                            Color(0xCC0E021C)
                        )
                    )
                )
                .border(
                    BorderStroke(1.5.dp, NeonBorder.copy(alpha = glowAlpha)),
                    RoundedCornerShape(24.dp)
                )
                .padding(vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: Fast Typing
                FeatureColumn(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(23.dp)
                        )
                    },
                    title = "Fast",
                    subtitle = "Typing",
                    modifier = Modifier.weight(1f)
                )

                // Divider 1
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(Color(0x33B000FF))
                )

                // Column 2: Secure & Private
                FeatureColumn(
                    icon = {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF33134D),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    },
                    title = "Secure &",
                    subtitle = "Private",
                    modifier = Modifier.weight(1f)
                )

                // Divider 2
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(Color(0x33B000FF))
                )

                // Column 3: Beautiful Themes
                FeatureColumn(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    title = "Beautiful",
                    subtitle = "Themes",
                    modifier = Modifier.weight(1f)
                )

                // Divider 3
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(Color(0x33B000FF))
                )

                // Column 4: Powerful Tools
                FeatureColumn(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    title = "Powerful",
                    subtitle = "Tools",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // Big Glowing "➤ Get Started" Button
        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .shadow(
                    elevation = (22 * glowAlpha).dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Pink,
                    spotColor = Purple
                ),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFE800B8),
                                Color(0xFFA100FF),
                                Color(0xFF5800E8)
                            )
                        )
                    )
                    .border(
                        BorderStroke(1.5.dp, Color.White.copy(alpha = glowAlpha)),
                        RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (!isEnabled) "Get Started - Enable Keyboard"
                               else if (!isCurrent) "Set As Default Keyboard"
                               else "Get Started",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // Live Keyboard Test Section
        var testTypingText by remember { mutableStateOf("") }
        val testContext = LocalContext.current
        val testImm = remember { testContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = (14 * glowAlpha).dp,
                    shape = RoundedCornerShape(22.dp),
                    ambientColor = Purple,
                    spotColor = Pink
                )
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xCC260C48),
                            Color(0xCC16052B),
                            Color(0xCC0E021C)
                        )
                    )
                )
                .border(
                    BorderStroke(1.5.dp, NeonBorder.copy(alpha = glowAlpha)),
                    RoundedCornerShape(22.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF33134D)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = null,
                                tint = Pink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Test Keyboard",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Try typing live below",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isCurrent) Color(0x334ADE80)
                                else if (isEnabled) Color(0x33FBBF24)
                                else Color(0x33EF4444)
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isCurrent) Color(0xFF4ADE80)
                                    else if (isEnabled) Color(0xFFFBBF24)
                                    else Color(0xFFEF4444)
                                ),
                                RoundedCornerShape(50)
                            )
                            .clickable {
                                if (!isEnabled) {
                                    testContext.startActivity(Intent(AndroidSettings.ACTION_INPUT_METHOD_SETTINGS))
                                } else if (!isCurrent) {
                                    testImm.showInputMethodPicker()
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isCurrent) "✓ Active" else if (isEnabled) "! Switch" else "! Disabled",
                            color = if (isCurrent) Color(0xFF4ADE80) else if (isEnabled) Color(0xFFFBBF24) else Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!isCurrent) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF8B123E), Color(0xFFC026D3))))
                            .clickable {
                                if (!isEnabled) {
                                    testContext.startActivity(Intent(AndroidSettings.ACTION_INPUT_METHOD_SETTINGS))
                                } else {
                                    testImm.showInputMethodPicker()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (!isEnabled) "⚠️ SoftBoard Enable කරන්න (Tap to Enable)" else "👉 SoftBoard Keyboard එක තෝරන්න (Tap to Switch)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = testTypingText,
                    onValueChange = { testTypingText = it },
                    placeholder = {
                        Text(
                            text = "Tap here to test typing, emojis & theme...",
                            color = TextDim,
                            fontSize = 13.sp
                        )
                    },
                    trailingIcon = {
                        if (testTypingText.isNotEmpty()) {
                            IconButton(onClick = { testTypingText = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = TextDim,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple,
                        unfocusedBorderColor = NeonBorder.copy(alpha = 0.6f),
                        focusedContainerColor = Color(0x800D021C),
                        unfocusedContainerColor = Color(0x800D021C),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 3
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Hello 👋", "SoftBoard 💜", "12345 🔢").forEach { sample ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF281140))
                                .border(BorderStroke(0.8.dp, NeonBorder.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                                .clickable {
                                    testTypingText = if (testTypingText.isEmpty()) sample else "$testTypingText $sample"
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = sample,
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun FeatureColumn(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF6B1894), Color(0xFF370B52))
                    )
                )
                .border(BorderStroke(1.2.dp, Color(0xFF9C27B0).copy(alpha = 0.6f)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp
        )
        Text(
            text = subtitle,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp
        )
    }
}

// ---------------------------------------------------------
// 2. THEMES SCREEN (100% MATCHING THE USER SCREENSHOT)
// ---------------------------------------------------------
@Composable
fun ThemesScreen(
    onNavigateTo: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val prefs = remember { context.prefs() }

    val currentSavedTheme = prefs.getString(HeliSettings.PREF_THEME_COLORS, KeyboardTheme.THEME_PURPLE_NEON) ?: KeyboardTheme.THEME_PURPLE_NEON
    var selectedCategory by remember { mutableStateOf("Popular") }
    var activeThemeName by remember {
        mutableStateOf(
            when (currentSavedTheme) {
                KeyboardTheme.THEME_BLUE_GALAXY, KeyboardTheme.THEME_OCEAN -> "Blue Galaxy"
                KeyboardTheme.THEME_SUNSET_GLOW, KeyboardTheme.THEME_CHOCOLATE -> "Sunset Glow"
                KeyboardTheme.THEME_FOREST_GREEN, KeyboardTheme.THEME_FOREST -> "Forest Green"
                KeyboardTheme.THEME_PINK_DREAM, KeyboardTheme.THEME_PINK -> "Pink Dream"
                KeyboardTheme.THEME_BLACK_GOLD, KeyboardTheme.THEME_BLACK -> "Black Gold"
                else -> "Purple Neon"
            }
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ThemesGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ThemesGlowAlpha"
    )

    val categories = listOf("Popular", "Nature", "Dark", "Colorful")

    val allThemes = listOf(
        SoftBoardThemeItem(
            name = "Purple Neon",
            heliThemeKey = KeyboardTheme.THEME_PURPLE_NEON,
            category = "Popular",
            previewResId = R.drawable.theme_preview_purple_neon,
            gradient = listOf(Color(0xFF340763), Color(0xFF7B1FA2), Color(0xFFB000FF), Color(0xFFFF42E8))
        ),
        SoftBoardThemeItem(
            name = "Blue Galaxy",
            heliThemeKey = KeyboardTheme.THEME_BLUE_GALAXY,
            category = "Dark",
            previewResId = R.drawable.theme_preview_blue_galaxy,
            gradient = listOf(Color(0xFF0C1938), Color(0xFF0369A1), Color(0xFF06B6D4), Color(0xFF38BDF8))
        ),
        SoftBoardThemeItem(
            name = "Sunset Glow",
            heliThemeKey = KeyboardTheme.THEME_SUNSET_GLOW,
            category = "Nature",
            previewResId = R.drawable.theme_preview_sunset_glow,
            gradient = listOf(Color(0xFF450A0A), Color(0xFFDC2626), Color(0xFFF97316), Color(0xFFFBBF24))
        ),
        SoftBoardThemeItem(
            name = "Forest Green",
            heliThemeKey = KeyboardTheme.THEME_FOREST_GREEN,
            category = "Nature",
            previewResId = R.drawable.theme_preview_forest_green,
            gradient = listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF10B981), Color(0xFF34D399))
        ),
        SoftBoardThemeItem(
            name = "Pink Dream",
            heliThemeKey = KeyboardTheme.THEME_PINK_DREAM,
            category = "Colorful",
            previewResId = R.drawable.theme_preview_pink_dream,
            gradient = listOf(Color(0xFF500724), Color(0xFFDB2777), Color(0xFFF472B6), Color(0xFFFF80DF))
        ),
        SoftBoardThemeItem(
            name = "Black Gold",
            heliThemeKey = KeyboardTheme.THEME_BLACK_GOLD,
            category = "Dark",
            previewResId = R.drawable.theme_preview_black_gold,
            gradient = listOf(Color(0xFF1C1917), Color(0xFF451A03), Color(0xFFB45309), Color(0xFFF59E0B)),
            isCrown = true
        )
    )

    val displayedThemes = remember(selectedCategory) {
        when (selectedCategory) {
            "Nature" -> allThemes.filter { it.name in listOf("Sunset Glow", "Forest Green", "Blue Galaxy") }
            "Dark" -> allThemes.filter { it.name in listOf("Purple Neon", "Blue Galaxy", "Black Gold") }
            "Colorful" -> allThemes.filter { it.name in listOf("Purple Neon", "Sunset Glow", "Pink Dream", "Forest Green") }
            else -> allThemes
        }
    }

    fun applyTheme(themeItem: SoftBoardThemeItem) {
        activeThemeName = themeItem.name
        SettingsActivity.forceTheme = null
        SettingsActivity.forceNight = null

        val p = context.prefs()
        p.edit {
            putString(HeliSettings.PREF_THEME_COLORS, themeItem.heliThemeKey)
            putString(HeliSettings.PREF_THEME_COLORS_NIGHT, themeItem.heliThemeKey)
            putBoolean(HeliSettings.PREF_THEME_KEY_BORDERS, true)
            putString(HeliSettings.PREF_THEME_STYLE, KeyboardTheme.STYLE_ROUNDED)
            putBoolean(HeliSettings.PREF_THEME_DAY_NIGHT, false)
        }

        try {
            context.protectedPrefs().edit {
                putString(HeliSettings.PREF_THEME_COLORS, themeItem.heliThemeKey)
                putString(HeliSettings.PREF_THEME_COLORS_NIGHT, themeItem.heliThemeKey)
                putBoolean(HeliSettings.PREF_THEME_KEY_BORDERS, true)
                putString(HeliSettings.PREF_THEME_STYLE, KeyboardTheme.STYLE_ROUNDED)
                putBoolean(HeliSettings.PREF_THEME_DAY_NIGHT, false)
            }
        } catch (_: Exception) {}

        try {
            KeyboardSwitcher.getInstance().forceReloadTheme(context)
        } catch (_: Exception) {}

        Toast.makeText(context, "${themeItem.name} තේමාව සක්‍රිය විය! (Theme applied)", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Bar: ← Themes  🔍
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(4.dp))
                NeonText(text = "Themes", size = 22)
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Themes",
                    tint = Color.White
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Hero Banner Card: Choose Your Keyboard Theme (Matching the Screenshot 100%)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 14.dp, shape = RoundedCornerShape(22.dp), ambientColor = Purple)
                .clip(RoundedCornerShape(22.dp))
                .border(BorderStroke(1.5.dp, Purple.copy(alpha = glowAlpha)), RoundedCornerShape(22.dp))
        ) {
            Image(
                painter = painterResource(R.drawable.banner_themes_hero),
                contentDescription = "Choose Your Keyboard Theme",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(14.dp))

        // Category Filter Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSel = selectedCategory == category
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSel) Brush.horizontalGradient(listOf(Color(0xFF8A00E6), Color(0xFFFF42E8)))
                            else Brush.horizontalGradient(listOf(Color(0x9918072E), Color(0x9918072E)))
                        )
                        .border(
                            BorderStroke(1.2.dp, if (isSel) Pink else Color(0xFF4A186D)),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedCategory = category }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val iconText = when (category) {
                        "Popular" -> "★ "
                        "Nature" -> "🍃 "
                        "Dark" -> "🌙 "
                        else -> "🎨 "
                    }
                    Text(
                        text = "$iconText$category",
                        color = if (isSel) Color.White else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 2-Column Grid of 6 High-Fidelity Keyboard Themes
        for (i in displayedThemes.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ThemeCardItem(
                    theme = displayedThemes[i],
                    isSelected = activeThemeName == displayedThemes[i].name,
                    glowAlpha = glowAlpha,
                    onClick = { applyTheme(displayedThemes[i]) },
                    modifier = Modifier.weight(1f)
                )
                if (i + 1 < displayedThemes.size) {
                    ThemeCardItem(
                        theme = displayedThemes[i + 1],
                        isSelected = activeThemeName == displayedThemes[i + 1].name,
                        glowAlpha = glowAlpha,
                        onClick = { applyTheme(displayedThemes[i + 1]) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        Spacer(Modifier.height(8.dp))

        // Wide Capsule Button: "Create Custom Theme ›"
        Button(
            onClick = { onNavigateTo(SettingsDestination.Appearance) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .shadow(
                    elevation = (18 * glowAlpha).dp,
                    shape = RoundedCornerShape(30.dp),
                    ambientColor = Pink,
                    spotColor = Purple
                ),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF8A00E6),
                                Color(0xFFB000FF),
                                Color(0xFFFF42E8)
                            )
                        )
                    )
                    .border(
                        BorderStroke(1.2.dp, Color.White.copy(alpha = glowAlpha)),
                        RoundedCornerShape(30.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Create Custom Theme",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun ThemeCardItem(
    theme: SoftBoardThemeItem,
    isSelected: Boolean,
    glowAlpha: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ThemeCardScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isSelected) (16 * glowAlpha).dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Purple,
                spotColor = Pink
            )
            .clip(RoundedCornerShape(20.dp))
            .border(
                BorderStroke(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    color = if (isSelected) Pink.copy(alpha = glowAlpha) else Color(0x668534A9)
                ),
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(theme.previewResId),
            contentDescription = theme.name,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )

        // Overlay dynamic selection indicators if user selects a different theme
        if (isSelected) {
            // Selected Checkmark badge in Top-Right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Pink)
                    .border(BorderStroke(1.2.dp, Color.White), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Bottom-right Selected Checkmark icon
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(listOf(Pink, Color(0xFF9C27B0)))
                    )
                    .border(BorderStroke(1.dp, Color.White), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------
// 3. TOOLS SCREEN
// ---------------------------------------------------------
@Composable
fun ToolsScreen(
    onNavigateTo: (String) -> Unit,
    onBackClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showTranslatorDialog by remember { mutableStateOf(false) }
    var showCalcDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                NeonText(text = "Tools", size = 22)
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Tools",
                    tint = Color.White
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .shadow(elevation = 14.dp, shape = RoundedCornerShape(22.dp), ambientColor = Purple)
                .clip(RoundedCornerShape(22.dp))
                .border(BorderStroke(1.5.dp, Purple.copy(alpha = 0.8f)), RoundedCornerShape(22.dp))
        ) {
            Image(
                painter = painterResource(R.drawable.img_banner_tools),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xDD1A0733), Color(0x771A0733))
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Purple, Pink))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Useful Tools\nfor Better Typing",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 22.sp
                        )
                        Text(
                            text = "Explore handy tools...",
                            color = TextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ToolItemCard(
                icon = Icons.Default.Spellcheck,
                title = "Text Correction",
                subtitle = "Auto correct\nyour typing",
                onClick = { onNavigateTo(SettingsDestination.TextCorrection) },
                modifier = Modifier.weight(1f)
            )
            ToolItemCard(
                icon = Icons.Default.Translate,
                title = "Text Translator",
                subtitle = "Translate text\ninstantly",
                onClick = { showTranslatorDialog = true },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ToolItemCard(
                icon = Icons.Default.ContentPaste,
                title = "Clipboard",
                subtitle = "Manage clipboard\nquickly",
                onClick = { onNavigateTo(SettingsDestination.Toolbar) },
                modifier = Modifier.weight(1f)
            )
            ToolItemCard(
                icon = Icons.Default.MenuBook,
                title = "Dictionary",
                subtitle = "Find word\nmeanings",
                onClick = { onNavigateTo(SettingsDestination.Dictionaries) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ToolItemCard(
                icon = Icons.Default.Mic,
                title = "Voice Input",
                subtitle = "Type with\nvoice",
                onClick = {
                    Toast.makeText(context, "Voice input toolbar key is enabled on SoftBoard!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f)
            )
            ToolItemCard(
                icon = Icons.Default.Calculate,
                title = "Number Pad",
                subtitle = "Quick\ncalculator",
                onClick = { showCalcDialog = true },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))
    }

    if (showTranslatorDialog) {
        var inputTranslate by remember { mutableStateOf("") }
        var resultTranslate by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showTranslatorDialog = false },
            containerColor = CardColor,
            titleContentColor = Color.White,
            textContentColor = TextMuted,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Translate, contentDescription = null, tint = Pink)
                    Spacer(Modifier.width(8.dp))
                    Text("Text Translator", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = inputTranslate,
                        onValueChange = { inputTranslate = it },
                        placeholder = { Text("Enter text to translate...", color = TextDim) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Purple,
                            unfocusedBorderColor = NeonBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Button(
                        onClick = {
                            if (inputTranslate.isNotBlank()) {
                                resultTranslate = "✓ Translation feature active in keyboard toolbar!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Purple),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Translate", color = Color.White)
                    }
                    if (resultTranslate.isNotEmpty()) {
                        Text(resultTranslate, color = Color(0xFF4ADE80), fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTranslatorDialog = false }) {
                    Text("Close", color = Pink)
                }
            }
        )
    }

    if (showCalcDialog) {
        AlertDialog(
            onDismissRequest = { showCalcDialog = false },
            containerColor = CardColor,
            titleContentColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = Purple)
                    Spacer(Modifier.width(8.dp))
                    Text("Number Pad & Calculator", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            text = {
                Column {
                    Text("SoftBoard has a dedicated Number Row and secondary NumPad layout accessible via the '?123' key on the keyboard.", color = TextMuted)
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            showCalcDialog = false
                            onNavigateTo(SettingsDestination.Layouts)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Purple),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Configure Number Layout", color = Color.White)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCalcDialog = false }) {
                    Text("Done", color = Pink)
                }
            }
        )
    }
}

@Composable
fun ToolItemCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(135.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp), ambientColor = Purple)
            .clip(RoundedCornerShape(20.dp))
            .background(CardColor)
            .border(BorderStroke(1.2.dp, NeonBorder), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF33134D)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Pink,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextDim,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------
// 4. SETTINGS SCREEN
// ---------------------------------------------------------
@Composable
fun SettingsScreen(
    onNavigateTo: (String) -> Unit,
    onBackClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                NeonText(text = "Settings", size = 22)
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Settings",
                    tint = Color.White
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(22.dp), ambientColor = Purple)
                .clip(RoundedCornerShape(22.dp))
                .background(CardColor)
                .border(BorderStroke(1.5.dp, Purple.copy(alpha = 0.8f)), RoundedCornerShape(22.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Purple, Color(0xFFC084FC), Pink)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "SoftBoard",
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "v1.1.0",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF3B1A66))
                        .border(BorderStroke(1.2.dp, Pink.copy(alpha = 0.8f)), RoundedCornerShape(50))
                        .clickable { onNavigateTo(SettingsDestination.About) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("About App", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "General Settings",
            color = TextMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        SettingsGroup {
            SettingsItemRow(
                icon = Icons.Default.Language,
                title = "Languages & Layouts",
                subtitle = "English (US)",
                onClick = { onNavigateTo(SettingsDestination.Languages) }
            )
            HorizontalDivider(color = NeonBorder.copy(alpha = 0.4f), thickness = 0.8.dp)
            SettingsItemRow(
                icon = Icons.Default.Tune,
                title = "Preferences",
                onClick = { onNavigateTo(SettingsDestination.Preferences) }
            )
            HorizontalDivider(color = NeonBorder.copy(alpha = 0.4f), thickness = 0.8.dp)
            SettingsItemRow(
                icon = Icons.Default.Palette,
                title = "Appearance",
                onClick = { onNavigateTo(SettingsDestination.Appearance) }
            )
            HorizontalDivider(color = NeonBorder.copy(alpha = 0.4f), thickness = 0.8.dp)
            SettingsItemRow(
                icon = Icons.Default.Build,
                title = "Toolbar",
                onClick = { onNavigateTo(SettingsDestination.Toolbar) }
            )
            HorizontalDivider(color = NeonBorder.copy(alpha = 0.4f), thickness = 0.8.dp)
            SettingsItemRow(
                icon = Icons.Default.Spellcheck,
                title = "Text Correction",
                onClick = { onNavigateTo(SettingsDestination.TextCorrection) }
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Advanced Settings",
            color = TextMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        SettingsGroup {
            SettingsItemRow(
                icon = Icons.Default.Keyboard,
                title = "Secondary Layouts",
                onClick = { onNavigateTo(SettingsDestination.Layouts) }
            )
            HorizontalDivider(color = NeonBorder.copy(alpha = 0.4f), thickness = 0.8.dp)
            SettingsItemRow(
                icon = Icons.Default.MenuBook,
                title = "Dictionaries",
                onClick = { onNavigateTo(SettingsDestination.Dictionaries) }
            )
            HorizontalDivider(color = NeonBorder.copy(alpha = 0.4f), thickness = 0.8.dp)
            SettingsItemRow(
                icon = Icons.Default.Handyman,
                title = "Advanced",
                onClick = { onNavigateTo(SettingsDestination.Advanced) }
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "About",
            color = TextMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        SettingsGroup {
            SettingsItemRow(
                icon = Icons.Default.Info,
                title = "About",
                onClick = { onNavigateTo(SettingsDestination.About) }
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardColor)
            .border(BorderStroke(1.2.dp, NeonBorder), RoundedCornerShape(20.dp))
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF33134D)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Pink,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextDim,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ---------------------------------------------------------
// 5. BOTTOM NAVIGATION BAR
// ---------------------------------------------------------
@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .shadow(elevation = 18.dp, shape = RoundedCornerShape(32.dp), ambientColor = Purple)
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xF50D021C))
            .border(BorderStroke(1.5.dp, NeonBorder), RoundedCornerShape(32.dp))
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = selectedTab == 0,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(0) }
            )

            Box(
                modifier = Modifier
                    .height(28.dp)
                    .width(1.dp)
                    .background(Color(0x338534A9))
            )

            BottomNavItem(
                icon = Icons.Default.Palette,
                label = "Themes",
                isSelected = selectedTab == 1,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(1) }
            )

            Box(
                modifier = Modifier
                    .height(28.dp)
                    .width(1.dp)
                    .background(Color(0x338534A9))
            )

            BottomNavItem(
                icon = Icons.Default.Build,
                label = "Tools",
                isSelected = selectedTab == 2,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(2) }
            )

            Box(
                modifier = Modifier
                    .height(28.dp)
                    .width(1.dp)
                    .background(Color(0x338534A9))
            )

            BottomNavItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = selectedTab == 3,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(3) }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (isSelected) Brush.verticalGradient(
                    listOf(Color(0xFF8A00E6), Color(0xFF4A0082))
                )
                else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else Color(0xFFB5A4CE),
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = label,
                color = if (isSelected) Color.White else Color(0xFFB5A4CE),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            if (isSelected) {
                Spacer(Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White)
                )
            }
        }
    }
}
