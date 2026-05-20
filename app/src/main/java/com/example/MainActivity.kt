package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Appointment
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CartItem
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Product

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Force Right-to-Left (RTL) for standard Arabic application experience
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing),
                        bottomBar = {
                            // We will display a beautiful Material 3 bottom navigation bar
                        }
                    ) { innerPadding ->
                        // The primary entry structure of our clinic dashboard
                        ClinicAppDashboard(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun ClinicAppDashboard(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .shadow(16.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .testTag("main_bottom_navigation")
            ) {
                val tabs = listOf(
                    Triple(0, "الرئيسية", Icons.Default.Home),
                    Triple(1, "العلاجات", Icons.Default.Info),
                    Triple(2, "احجز موعد", Icons.Default.DateRange),
                    Triple(3, "مواعيدي", Icons.Default.CheckCircle),
                    Triple(4, "المتجر", Icons.Default.ShoppingCart),
                    Triple(5, "استشارة AI", Icons.Default.Star)
                )

                tabs.forEach { (index, title, icon) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_tab_$index")
                    )
                }
            }
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Animated transitions between main views
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "MainTabsAnimation"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> HomeScreen(viewModel = viewModel, onNavigateToTab = { viewModel.selectTab(it) })
                    1 -> TreatmentsGuideScreen()
                    2 -> BookAppointmentScreen(viewModel = viewModel)
                    3 -> MyAppointmentsScreen(viewModel = viewModel)
                    4 -> ShopScreen(viewModel = viewModel)
                    5 -> AiAdvisorScreen(viewModel = viewModel)
                }
            }
        }
    }
}

// ==========================================
// 1. HOME SCREEN (الرئيسية)
// ==========================================
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Natural Tones Profile Header Row (منسق بالكامل حسب الشكل الفني المتناسق)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3A6931)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "م",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(
                            text = "مسعود للحجامة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D322C)
                        )
                        Text(
                            text = "الرقيبة، الوادي",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5D6259)
                        )
                    }
                }
                
                IconButton(
                    onClick = { /* Notifications placeholder */ },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F2ED))
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "التنبيهات",
                        tint = Color(0xFF43493E),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. High-Fidelity Hero Banner (احجز جلستك القادمة)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF3A6931))
                    .drawBehind {
                        // Soft decorative circles matching html
                        drawCircle(
                            color = Color.White.copy(alpha = 0.1f),
                            radius = size.minDimension * 0.45f,
                            center = Offset(size.width * 0.1f, size.height * 0.85f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.05f),
                            radius = size.minDimension * 0.35f,
                            center = Offset(size.width * 0.15f, size.height * 0.15f)
                        )
                    }
                    .padding(20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "احجز جلستك القادمة",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "احصل على استشارة طبية متكاملة وفق أفضل المعايير الصحية والوقائية لضمان سلامتك وراحتك.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { onNavigateToTab(2) }, // Navigate to booking tab
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD7E8CD),
                            contentColor = Color(0xFF002106)
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = "احجز موعداً الآن",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Natural Tones Services Main Grid (4 Core Categories matching HTML styling and colors)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "خدماتنا الرئيسية",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF43493E),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Item 1: حجامة طبية
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFF2F7EE))
                            .border(1.dp, Color(0xFFE0E3DB), RoundedCornerShape(24.dp))
                            .clickable { onNavigateToTab(1) } // Treatments guide
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFF3A6931),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "حجامة طبية",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C19)
                        )
                    }

                    // Item 2: إبر صينية
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFFFF4ED))
                            .border(1.dp, Color(0xFFE0E3DB), RoundedCornerShape(24.dp))
                            .clickable { onNavigateToTab(1) }
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF934500),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "إبر صينية",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C19)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Item 3: سم النحل
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFFEECEC))
                            .border(1.dp, Color(0xFFE0E3DB), RoundedCornerShape(24.dp))
                            .clickable { onNavigateToTab(1) }
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFFBA1A1A),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "سم النحل",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C19)
                        )
                    }

                    // Item 4: المتجر الطبي
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFE7F3FF))
                            .border(1.dp, Color(0xFFE0E3DB), RoundedCornerShape(24.dp))
                            .clickable { onNavigateToTab(4) } // Shop tab
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color(0xFF0061A4),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "المتجر",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C19)
                        )
                    }
                }
            }
        }

        // 4. Healthy Tip of Day (نصيحة اليوم - مقتبسة بالكامل من مستند التصميم)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFE7E9E2))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "نصيحة اليوم",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF43493E),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "يفضل شرب كميات كافية من الماء الدافئ بعد جلسة الحجامة لتنشيط الدورة الدموية ومساعدة الكلى على فلترة الفضلات بشكل أفضل.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1A1C19),
                            lineHeight = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF3A6931),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // 5. Fast Action Buttons for AI Interaction and Quick Booking
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onNavigateToTab(2) }, // Book Appt
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                        .testTag("quick_book_btn"),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Text(text = "حجز موعد جديد", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    }
                }

                OutlinedButton(
                    onClick = { onNavigateToTab(5) }, // AI Advisor
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("quick_ai_btn"),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text(text = "استشارة ذكية (AI)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Prophetic Vision & Mission Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "رؤيتنا ورسالتنا",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "رؤيتنا ورسالتنا",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "المساهمة في تحسين صحة المجتمع من خلال توفير علاجات طبيعية آمنة وفعالة، وتوعية الناس بأهمية الحجامة الطبية وفوائدها الصحية والالتزام بالنظافة والتعقيم التام وفق المعايير الصحية.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 24.sp
                    )
                }
            }
        }

        // Clinic Services Highlights
        item {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "خدمات عيادتنا المتميزة",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ServiceHighlightCard("الحجامة العلاجية", "تخليص الجسم من السموم والآلام", Icons.Default.Favorite, onNavigateToTab)
                    ServiceHighlightCard("الإبر الصينية", "استعادة مسارات الطاقة وعلاج الأعصاب", Icons.Default.List, onNavigateToTab)
                    ServiceHighlightCard("سم النحل", "مناعة قوية ومضاد طبيعي للروماتيزم", Icons.Default.ThumbUp, onNavigateToTab)
                    ServiceHighlightCard("الحجامة الرياضية", "مخصصة للرياضيين لبناء واستشفاء العضلات", Icons.Default.Build, onNavigateToTab)
                }
            }
        }

        // About the Specialist Card (أخصائي الحجامة مسعود)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Custom drawn beautiful abstract Doctor avatar in light green
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .drawBehind {
                                // Draw stylized doctor glass stethoscope or shield icon
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.15f),
                                    radius = size.minDimension * 0.4f
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = "المختص مسعود",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تحت إشراف الأخصائي مسعود",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "أخصائي مرخص ومحترف وملم بالمعايير الوقائية العالية لضمان النظافة والتعقيم لكل مريض.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Location & Contact Info Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "بيانات العيادة والاتصال",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Divider(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))

                    // 1. Address
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "العنوان",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "العنوان الفرعي ومقر العيادة:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "حي 20 أوت الشرقية، الرقيبة، الوادي (الجزائر)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // 2. Phone
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "الهاتف",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "رقم الهاتف للاستفسار وحجز الفترات:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "0797901525",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    // Direct Interactive Contact actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:0797901525")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Call, contentDescription = "اتصل", modifier = Modifier.size(18.dp))
                                Text(text = "اتصال مباشر", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Button(
                            onClick = {
                                // Open Google Maps with Reguiba, El Oued query
                                val mapUri = Uri.parse("geo:0,0?q=Reguiba,El+Oued,Algeria")
                                val mapIntent = Intent(Intent.ACTION_VIEW, mapUri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                try {
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    val webMapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=Reguiba,El+Oued,Algeria"))
                                    context.startActivity(webMapIntent)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = "الخريطة", modifier = Modifier.size(18.dp))
                                Text(text = "موقع العيادة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceHighlightCard(
    title: String,
    desc: String,
    icon: ImageVector,
    onNavigateToTab: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(130.dp)
            .clickable { onNavigateToTab(1) }, // Open guide
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ==========================================
// 2. TREATMENTS GUIDE SCREEN (دليل العلاجات)
// ==========================================
@Composable
fun TreatmentsGuideScreen() {
    val treatments = listOf(
        TreatmentDetail(
            title = "الحجامة الطبية والعلاجية",
            iconName = "ic_cup",
            bulletPoints = listOf(
                "تنظيم ضغط الدم في الشرايين وتخفيف تدفق التجلطات.",
                "سحب السموم والترسبات الضارة من الطبقة السفلية للجلد.",
                "التخفيف الفوري لآلام الروماتيزم، وعرق النسا، وآلام أسفل الظهر والرقبة.",
                "مكافحة اضطرابات النوم، والتوتر المزمن، والصداع النصفي (الشقيقة)."
            ),
            preCare = "الصيام لمدة لا تقل عن ساعتين قبل الجلسة، الاغتسال بماء دافئ، وتجنب التدخين أو الإجهاد العنيف.",
            postCare = "تغطية الجروح وتجنب غسلها بالماء البارد لمدة 24 ساعة، الامتناع عن أكل الألبان واللحوم الحمراء الثقيلة ليوم واحد، وتناول سوائل محلاة بالعسل وتناول التمر."
        ),
        TreatmentDetail(
            title = "الحجامة الوقائية والرياضية",
            iconName = "ic_sport",
            bulletPoints = listOf(
                "أفضل أوقاتها السنوية هي أيام الشفاء النبوية (17، 19، 21 من الشهر الهجري).",
                "تحفيز استشفاء العضلات المتشنجة وزيادة ضخ الدم فيها للرياضيين.",
                "تجديد كرات الدم الحمراء وتحفيز هرمونات النشاط والنمو الطبيعية.",
                "الوقاية من انسداد الأوعية الدموية وارتفاع نسبة الكوليسترول."
            ),
            preCare = "النوم الكافي ليلة الجلسة وحفظ مستويات جيدة من رطوبة الجسم بتناول الماء.",
            postCare = "الحصول على راحة كافية وتجنب ممارسة التمارين الرياضية أو رفع الأوزان لمدة 24 إلى 48 ساعة."
        ),
        TreatmentDetail(
            title = "جلسات الإبر الصينية الطبية",
            iconName = "ic_needle",
            bulletPoints = listOf(
                "تنشيط نقاط الطاقة الحسية للجسم وإرسال إشارات للجهاز العصبي بتسكين الألم.",
                "مفيدة جداً لعلاج التهابات الأعصاب الطرفية والشلل الوجهي (أبو وجيه).",
                "علاج السمنة الموضعية والمساهمة في استرخاء الجهاز الهضمي والقولون.",
                "علاج الأرق الحاد وتثبيط آلام المفاصل المزمنة."
            ),
            preCare = "ارتداء ملابس فضفاضة مريحة، وعدم الحضور على معدة فارغة تماماً (تناول وجبة خفيفة).",
            postCare = "شرب ماء دافئ، وتجنب التعرض المباشر لتيارات الهواء البارد بعد الجلسة مباشرة."
        ),
        TreatmentDetail(
            title = "العلاج بسم النحل الفعال",
            iconName = "ic_bee",
            bulletPoints = listOf(
                "سم النحل يحوي مركب 'الميليتين' وهو مضاد التهاب طبيعي أقوى بمرات من الكورتيزون.",
                "تحفيز إنتاج الكورتيزول الداخلي للغدد لعلاج داء الروماتويد المناعي.",
                "تقوية جدران الشعيرات والتحفيز الذاتي للجهاز الليمفاوي والمناعي.",
                "المساعدة في علاج تصلب الشرايين واعتلال الأعصاب."
            ),
            preCare = "تخضع الجلسة الأولى لاختبار الحساسية البسيط تحت الجلد للتأكد تماماً من عدم وجود حساسية ضد سم النحل.",
            postCare = "وضع كمدات ماء دافئ في مواضع اللدغات إذا ظهر تورم خفيف، وتجنب حك المنطقة المصابة."
        ),
        TreatmentDetail(
            title = "النصائح الغذائية والوقائية",
            iconName = "ic_health",
            bulletPoints = listOf(
                "الالتزام ببرنامج غذائي يناسب الخلط الطبيعي لجسمك (دموي، بلغمي، صفراوي).",
                "شرب مغلي الأعشاب المفيدة كالبابونج والقسط الهندي لتصفية مجاري الدم.",
                "تقليل شرب الكافيين والسكريات المصنعة التي تزيد من أكسدة الجسم والسموم.",
                "دمج الحجامة مرة كل ستة أشهر كعادة وقائية ممتازة لحيوية الشباب المستمرة."
            ),
            preCare = "الاستعداد النفسي والبدني السليم والبعد عن الغضب المصاحب لارتفاع ضغط الدم.",
            postCare = "المحافظة على أوراد الرقية الشرعية والأذكار والدعاء بالبركة والشفاء قبل وبعد الجلسة."
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("treatments_guide_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "دليل العلاجات الطبيعية والوقائية",
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 22.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "نوفر لك شرحاً وافياً وتوجيهات عملية متوافقة مع الطب النبوي الموصى به والطب الطبيعي الحديث",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(treatments) { treatment ->
            TreatmentGuideCard(treatment = treatment)
        }
    }
}

data class TreatmentDetail(
    val title: String,
    val iconName: String,
    val bulletPoints: List<String>,
    val preCare: String,
    val postCare: String
)

@Composable
fun TreatmentGuideCard(treatment: TreatmentDetail) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded }
            .testTag("treatment_card_${treatment.title}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (treatment.iconName) {
                            "ic_cup" -> Icons.Default.FavoriteBorder
                            "ic_sport" -> Icons.Default.Star
                            "ic_needle" -> Icons.Default.Menu
                            "ic_bee" -> Icons.Default.Notifications
                            else -> Icons.Default.Info
                        }
                        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        text = treatment.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "إغلاق التفاصيل" else "عرض التفاصيل",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    Text(
                        text = "الفوائد الطبية وأبرز الخصائص:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    treatment.bulletPoints.forEach { point ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                            Text(
                                text = point,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "💡 قبل الجلسة (التحضير):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = treatment.preCare,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "🌱 لافتة ما بعد الجلسة (العناية):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = treatment.postCare,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. BOOK APPOINTMENT SCREEN (حجز موعد)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentScreen(viewModel: MainViewModel) {
    val context = LocalContext.current

    var patientName by remember { mutableStateOf("") }
    var patientPhone by remember { mutableStateOf("") }
    var selectedTherapy by remember { mutableStateOf("حجامة طبية وعلاجية") }
    var appointmentDate by remember { mutableStateOf("2026-05-25") }
    var appointmentTime by remember { mutableStateOf("صباحاً (09:00 - 11:00)") }
    var gender by remember { mutableStateOf("ذكر") }
    var notes by remember { mutableStateOf("") }

    var expandedDropdown by remember { mutableStateOf(false) }

    val therapyOptions = listOf(
        "حجامة طبية وعلاجية",
        "حجامة وقائية ورياضية",
        "جلسة إبر صينية",
        "علاج بسم النحل",
        "نصيحة واستشارة غذائية"
    )

    val timeSlots = listOf(
        "صباحاً (09:00 - 11:00)",
        "ظهراً (12:00 - 14:00)",
        "مساءً (15:00 - 17:00)",
        "ليلاً (19:00 - 21:00)"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("book_appointment_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "حجز جلسة علاجية جديدة",
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 22.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "يرجى ملء البيانات بدقة، سيتم حفظ الموعد في سجلات هاتفك وإخطارك فور تأكيده من الأخصائي.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Full name input
                    OutlinedTextField(
                        value = patientName,
                        onValueChange = { patientName = it },
                        label = { Text("الاسم الكامل للمريض") },
                        placeholder = { Text("مثال: أحمد مصطفى") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_patient_name")
                    )

                    // 2. Phone input
                    OutlinedTextField(
                        value = patientPhone,
                        onValueChange = { patientPhone = it },
                        label = { Text("رقم الهاتف للتواصل") },
                        placeholder = { Text("مثال: 0797901525") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_patient_phone")
                    )

                    // 3. Therapy dropdown selector
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedTherapy,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("نوع الخدمة المطلوبة") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            leadingIcon = { Icon(Icons.Default.List, contentDescription = null) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("dropdown_therapy")
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            therapyOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        selectedTherapy = option
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // 4. Gender Selection
                    Column {
                        Text(
                            text = "الجنس (لتنظيم الطاقم المعالج):",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { gender = "ذكر" }
                                    .minimumInteractiveComponentSize()
                            ) {
                                RadioButton(
                                    selected = gender == "ذكر",
                                    onClick = { gender = "ذكر" },
                                    modifier = Modifier.testTag("radio_male")
                                )
                                Text("ذكر", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 4.dp))
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { gender = "أنثى" }
                                    .minimumInteractiveComponentSize()
                            ) {
                                RadioButton(
                                    selected = gender == "أنثى",
                                    onClick = { gender = "أنثى" },
                                    modifier = Modifier.testTag("radio_female")
                                )
                                Text("أنثى", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }

                    // 5. Date selection
                    OutlinedTextField(
                        value = appointmentDate,
                        onValueChange = { appointmentDate = it },
                        label = { Text("تاريخ الجلسة المفضل (YYYY-MM-DD)") },
                        placeholder = { Text("سنة-شهر-يوم، مثلاً: 2026-05-25") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_appointment_date")
                    )

                    // 6. Time Slot custom chips
                    Column {
                        Text(
                            text = "الفترة والوقت المناسب:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            timeSlots.forEach { slot ->
                                val isSelected = appointmentTime == slot
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { appointmentTime = slot },
                                    label = { Text(slot, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("chip_time_$slot")
                                )
                            }
                        }
                    }

                    // 7. Notes input
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات طبية أو أعراض تعاني منها") },
                        placeholder = { Text("مثال: أعاني من صداع نصفي مستمر منذ شهرين...") },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_appointment_notes")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Booking Actions
                    Button(
                        onClick = {
                            if (patientName.isBlank() || patientPhone.isBlank() || appointmentDate.isBlank()) {
                                Toast.makeText(context, "الرجاء كـتابة الاسم ورقم الهاتف والتاريخ أولاً!", Toast.LENGTH_LONG).show()
                                return@Button
                            }

                            viewModel.bookAppointment(
                                patientName = patientName,
                                patientPhone = patientPhone,
                                therapyType = selectedTherapy,
                                appointmentDate = appointmentDate,
                                appointmentTime = appointmentTime,
                                gender = gender,
                                notes = notes,
                                onSuccess = {
                                    Toast.makeText(context, "تم حفظ الموعد بنجاح في هاتفك! يرجى إرسال الطلب للأخصائي لتأكيده.", Toast.LENGTH_LONG).show()
                                    
                                    // Generate WhatsApp URL with prefilled text
                                    val formattedPhone = "213797901525" // Algerian format
                                    val arabicMessage = """
                                        السلام عليكم ورحمة الله وبركاته،
                                        أود تأكيد موعد جلسة علاجية في عيادة الأخصائي مسعود:
                                        - الاسم: $patientName
                                        - الهاتف: $patientPhone
                                        - العلاج: $selectedTherapy
                                        - التاريخ: $appointmentDate
                                        - الوقت: $appointmentTime
                                        - الجنس: $gender
                                        - الأعراض والملاحظات: $notes
                                        وشكراً لكم.
                                    """.trimIndent()

                                    val waIntent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(arabicMessage)}")
                                    }
                                    try {
                                        context.startActivity(waIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "لم نجد تطبيق واتساب، تم حفظ الموعِد محلياً للمتابعة.", Toast.LENGTH_LONG).show()
                                    }

                                    // Clear fields
                                    patientName = ""
                                    patientPhone = ""
                                    notes = ""
                                    viewModel.selectTab(3) // Switch to My Bookings tab
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_booking_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "تأكيد وإرسال الموعد للأخصائي", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. MY APPOINTMENTS SCREEN (مواعيدي)
// ==========================================
@Composable
fun MyAppointmentsScreen(viewModel: MainViewModel) {
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Preset reminders & advice for clients
    val preparationChecklist = listOf(
        "تجنب تناول الطعام (الصيام) قبل جلسة الحجامة بـ3 ساعات على الأكثر.",
        "أعلم الأخصائي بجميع الأدوية التي تتناولها خاصة مميعات الدم ومسيلات الأسبرين.",
        "ينبغي الاستحمام بماء دافئ وتنظيف الجسم جيداً قبل الحضور للعيادة.",
        "تجنب المجهود البدني العنيف، والرياضة الشاقة والجمباز ليلة ويوم الموعد.",
        "التوكل على الله واستحضار نية الاستشفاء بالطب النبوي المبارك."
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("my_appointments_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "متابعة مواعيدي وجلساتي",
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 22.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "سجل متكامل بجلساتك الطبية وحالتها المباشرة مع النصائح الضرورية قبل الحضور.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Checklist for Preparing
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "تعليمات هامة قبل حضورك للجلسة:",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    preparationChecklist.forEachIndexed { i, tip ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("${i + 1}.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(text = tip, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "قائمة جلساتك لـدى العيادة:",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (appointments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "لا توجد مواعيد محجوزة حالياً",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "احجز موعدك الأول لتظهر تفاصيله هنا ومتابعته.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.selectTab(2) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("احجز جلسة الآن", color = Color.White)
                        }
                    }
                }
            }
        } else {
            items(appointments) { appointment ->
                AppointmentCard(appointment = appointment, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AppointmentCard(appointment: Appointment, viewModel: MainViewModel) {
    val context = LocalContext.current
    val statusColor = when (appointment.status) {
        "قيد الانتظار" -> MaterialTheme.colorScheme.secondary
        "مؤكد" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .testTag("appointment_card_${appointment.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointment.therapyType,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                // Styled Status Pill
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = appointment.status,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        fontSize = 12.sp
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "تاريخ الحفل والجلسة:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = Icons.Default.DateRange, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(text = appointment.appointmentDate, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Column {
                    Text(text = "الفترة المقررة:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = Icons.Default.Check, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(text = appointment.appointmentTime, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "المريض:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(text = "${appointment.patientName} (${appointment.gender})", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                }

                Column {
                    Text(text = "الهاتف ومراسلة العيادة:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(text = appointment.patientPhone, style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (appointment.notes.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "الأعراض الطبية: ${appointment.notes}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (appointment.status != "ملغى") {
                    OutlinedButton(
                        onClick = { viewModel.cancelAppointment(appointment.id) },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("cancel_appointment_btn_${appointment.id}")
                    ) {
                        Text("إلغاء الحجز", fontSize = 13.sp)
                    }
                } else {
                    Button(
                        onClick = { viewModel.deleteAppointment(appointment.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("delete_appointment_btn_${appointment.id}")
                    ) {
                        Text("حذف من السجل", fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = {
                        val therapistPhone = "213797901525"
                        val waText = """
                            السلام عليكم، أود المتابعة بخصوص حجز الجلسة العلاجية (${appointment.therapyType}) المقررة بتاريخ (${appointment.appointmentDate}) باسم (${appointment.patientName}) في أسرع وقت. وشكراً لكم.
                        """.trimIndent()
                        val waUri = Uri.parse("https://api.whatsapp.com/send?phone=$therapistPhone&text=${Uri.encode(waText)}")
                        val waIntent = Intent(Intent.ACTION_VIEW, waUri)
                        try {
                            context.startActivity(waIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "الواتساب غير متوفر على هذا الجهاز.", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.2f).testTag("chat_therapist_btn_${appointment.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text("متابعة الأخصائي", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. NATURAL E-STORE SCREEN (متجر الأعشاب والعسل)
// ==========================================
@Composable
fun ShopScreen(viewModel: MainViewModel) {
    val itemsInCart by viewModel.cart.collectAsStateWithLifecycle()
    var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }
    var showCartDialog by remember { mutableStateOf(false) }

    val products = viewModel.productsList
    val cartCount = itemsInCart.sumOf { it.quantity }
    val cartTotal = viewModel.getCartTotal()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("shop_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Store Title & Brief message
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "متجر مسعود للمنتجات الطبيعية",
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 20.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "منتجات علاجية وغذائية طبيعية خالية من الكيماويات ومختارة بعناية.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Cart floating badge
                BadgeBox(cartCount = cartCount, onClick = { showCartDialog = true })
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Products Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(products) { product ->
                    ProductCard(
                        product = product,
                        onProductClick = { selectedProductForDetail = product },
                        onAddToCart = { viewModel.addToCart(product) }
                    )
                }
            }
        }

        // Cart summary bottom floating bar
        if (cartCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                    .fillMaxWidth()
                    .clickable { showCartDialog = true }
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .testTag("floating_cart_bar")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color.White.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cartCount.toString(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = "عرض سلة المشتريات",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }

                    Text(
                        text = "${String.format("%.0f", cartTotal)} د.ج",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    // Detail Dialog overlay
    selectedProductForDetail?.let { product ->
        ProductDetailDialog(
            product = product,
            onDismiss = { selectedProductForDetail = null },
            onAddToCart = {
                viewModel.addToCart(product)
                selectedProductForDetail = null
            }
        )
    }

    // Cart Dialog Overlay
    if (showCartDialog) {
        CartDialog(
            cartItems = itemsInCart,
            cartTotal = cartTotal,
            viewModel = viewModel,
            onDismiss = { showCartDialog = false }
        )
    }
}

@Composable
fun BadgeBox(cartCount: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .shadow(2.dp, CircleShape)
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .clickable { onClick() }
            .testTag("shop_badge_btn"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = "السلة",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        if (cartCount > 0) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape)
                    .align(Alignment.TopEnd)
                    .shadow(1.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cartCount.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onProductClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clickable { onProductClick() }
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Draw a high contrast natural healing placeholder instead of raw remote image URLs
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                DrawPlaceholderGraphic(product.imageUrlPlaceholder)
            }

            Column(
                modifier = Modifier
                    .weight(1.7f)
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = product.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        minLines = 2,
                        lineHeight = 18.sp,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${String.format("%.0f", product.price)} د.ج",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = { onAddToCart() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("add_to_cart_btn_${product.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "أضف", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// Custom Vector drawings for products listing to keep code compact, native and robust
@Composable
fun DrawPlaceholderGraphic(type: String) {
    val emerald = MaterialTheme.colorScheme.primary
    val honey = MaterialTheme.colorScheme.secondary

    Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        val w = size.width
        val h = size.height

        when (type) {
            "honey_sdr", "royal_honey_mix" -> {
                // Draw Honeypot jar
                val path = Path().apply {
                    moveTo(w * 0.35f, h * 0.25f)
                    lineTo(w * 0.65f, h * 0.25f)
                    lineTo(w * 0.72f, h * 0.40f)
                    quadraticTo(w * 0.85f, h * 0.65f, w * 0.70f, h * 0.85f)
                    lineTo(w * 0.30f, h * 0.85f)
                    quadraticTo(w * 0.15f, h * 0.65f, w * 0.28f, h * 0.40f)
                    close()
                }
                drawPath(path, honey)
                // Draw lid
                drawRoundRect(
                    color = emerald,
                    topLeft = Offset(w * 0.30f, h * 0.15f),
                    size = androidx.compose.ui.geometry.Size(w * 0.40f, h * 0.10f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
                )
            }
            "black_seed_oil", "olive_oil" -> {
                // Medicine Bottle drawing with dropped oil
                drawRoundRect(
                    color = emerald,
                    topLeft = Offset(w * 0.35f, h * 0.30f),
                    size = androidx.compose.ui.geometry.Size(w * 0.30f, h * 0.55f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(15f, 15f)
                )
                // Cap
                drawRect(
                    color = honey,
                    topLeft = Offset(w * 0.43f, h * 0.20f),
                    size = androidx.compose.ui.geometry.Size(w * 0.14f, h * 0.10f)
                )
                // Droplet
                drawCircle(
                    color = honey,
                    radius = 8.dp.toPx(),
                    center = Offset(w * 0.50f, h * 0.60f)
                )
            }
            "cupping_set" -> {
                // Drawing cupping glass suction shape with red air-valve top
                drawCircle(
                    color = emerald,
                    radius = 24.dp.toPx(),
                    center = Offset(w * 0.50f, h * 0.55f),
                    style = Stroke(width = 4.dp.toPx())
                )
                drawRect(
                    color = Color.Red,
                    topLeft = Offset(w * 0.46f, h * 0.22f),
                    size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.12f)
                )
                drawRect(
                    color = emerald,
                    topLeft = Offset(w * 0.40f, h * 0.72f),
                    size = androidx.compose.ui.geometry.Size(w * 0.20f, h * 0.05f)
                )
            }
            else -> {
                // Herb leaf / plant bundle design
                drawCircle(
                    color = emerald.copy(alpha = 0.2f),
                    radius = 26.dp.toPx(),
                    center = Offset(w * 0.50f, w * 0.50f)
                )
                drawCircle(
                    color = honey,
                    radius = 12.dp.toPx(),
                    center = Offset(w * 0.50f, w * 0.50f)
                )
            }
        }
    }
}

@Composable
fun ProductDetailDialog(
    product: Product,
    onDismiss: () -> Unit,
    onAddToCart: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .testTag("product_detail_dialog_${product.id}"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(onClick = { onDismiss() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        DrawPlaceholderGraphic(product.imageUrlPlaceholder)
                    }
                }

                item {
                    Text(
                        text = "عن المنتج:",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 24.sp
                    )
                }

                item {
                    Text(
                        text = "الفوائد والخصائص العلاجية:",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(product.benefits) { benefit ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                        Text("✔", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = benefit, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "سعر السلعة:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text(
                                text = "${String.format("%.0f", product.price)} د.ج",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Button(
                            onClick = { onAddToCart() },
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("detail_dialog_add_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
                                Text("إضافة إلى السلّة", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartDialog(
    cartItems: List<CartItem>,
    cartTotal: Double,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(550.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .testTag("cart_dialog_container"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سلة المشتريات الطبية",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { onDismiss() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                if (cartItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), modifier = Modifier.size(52.dp))
                            Text("السلة فارغة حالياً!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(cartItems) { item ->
                            CartItemRow(item = item, viewModel = viewModel)
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "الإجمالي:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${String.format("%.0f", cartTotal)} د.ج",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    if (cartItems.isNotEmpty()) {
                        Button(
                            onClick = {
                                val invoiceText = cartItems.joinToString("\n") {
                                    "- ${it.product.name} (عدد ${it.quantity}) بسعر ${String.format("%.0f", it.product.price * it.quantity)} د.ج"
                                }
                                val formattedTherapistPhone = "213797901525"
                                val finalMessage = """
                                    السلام عليكم ورحمة الله وبركاته،
                                    أود حجز وشراء المنتجات الطبيعية التالية من المتجر:
                                    $invoiceText
                                    
                                    إجمالي الفاتورة: ${String.format("%.0f", cartTotal)} د.ج
                                    يرجى تجهيز الطلب للاستلام من مقر العيادة بالرقيبة، وشكراً لكم.
                                """.trimIndent()

                                val sendIntent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://api.whatsapp.com/send?phone=$formattedTherapistPhone&text=${Uri.encode(finalMessage)}")
                                }
                                try {
                                    context.startActivity(sendIntent)
                                    viewModel.clearCart()
                                    onDismiss()
                                    Toast.makeText(context, "تم إرسال الفاتورة عبر الواتساب وتصفير السلة بنجاح!", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "لم نتمكن من فتح الواتساب لمشاركة الطلب.", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_order_cart_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White)
                                Text("إرسال فـاتورة الشراء بالواتساب", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(item: CartItem, viewModel: MainViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.3f)) {
                Text(text = item.product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "${String.format("%.0f", item.product.price)} د.ج للواحدة", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Diminish Quantity
                IconButton(
                    onClick = { viewModel.updateCartQuantity(item.product.id, -1) },
                    modifier = Modifier.size(28.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                ) {
                    Text("-", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Text(text = item.quantity.toString(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)

                // Increase Quantity
                IconButton(
                    onClick = { viewModel.updateCartQuantity(item.product.id, 1) },
                    modifier = Modifier.size(28.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Text("+", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// ==========================================
// 6. AI SMART ADVISOR SCREEN (استشارة AI الذكية)
// ==========================================
@Composable
fun AiAdvisorScreen(viewModel: MainViewModel) {
    val chatHistory by viewModel.chatHistory.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    var inputMessage by remember { mutableStateOf("") }
    val listState = rememberScrollState()

    // Recommended Question Chips
    val recommendedChips = listOf(
        "فوائد الحجامة لصداع الرأس النبوي؟",
        "شروط الحجامة الوقائية ومتى موعدها؟",
        "هل سم النحل يعالج عرق النسا والمفاصل؟",
        "إرشادات التغذية وتناول الأكل بعد الجلسة؟"
    )

    // Keep chat scrolled down to end automatically inside an effect when history additions happen
    LaunchedEffect(chatHistory.size, isChatLoading) {
        listState.animateScrollTo(listState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_advisor_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Chat Header with status indications
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.Green, CircleShape)
                    )
                    Column {
                        Text(
                            text = "المستشار الصحي لمسعود",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "جاهز للإجابة على جميع الاستفسارات الطبية فوراً",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.clearChat() },
                    modifier = Modifier.testTag("clear_chat_history_btn")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "تصفير الدردشة", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Mid area Chat Messages box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .verticalScroll(listState)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                chatHistory.forEach { chatItem ->
                    if (chatItem.first.isNotEmpty()) {
                        // User message layout container
                        UserMessageBubble(text = chatItem.first)
                    }
                    if (chatItem.second.isNotEmpty()) {
                        // Advisor response bubble
                        AdvisorMessageBubble(text = chatItem.second)
                    }
                }

                // Custom AI typing loading indicator
                if (isChatLoading) {
                    AdvisorMessageBubble(text = "يقوم المستشار الآن بصياغة إجابة علمية دقيقة، يرجى الانتظار لحظة...")
                }
            }
        }

        // Recommend Chips row + Send message container
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Recommendation chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recommendedChips.forEach { chipQuery ->
                    InputChip(
                        selected = false,
                        onClick = { viewModel.sendMessageToAI(chipQuery) },
                        label = { Text(chipQuery, fontSize = 11.sp) },
                        modifier = Modifier.testTag("chip_faq_$chipQuery")
                    )
                }
            }

            // Text Input Field Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    placeholder = { Text("اطرح سؤالك هنا على معالج العيادة...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_textfield"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    trailingIcon = {
                        if (inputMessage.isNotEmpty()) {
                            IconButton(onClick = { inputMessage = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "مسح")
                            }
                        }
                    }
                )

                IconButton(
                    onClick = {
                        if (inputMessage.isNotBlank()) {
                            viewModel.sendMessageToAI(inputMessage)
                            inputMessage = ""
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(2.dp, CircleShape)
                        .testTag("chat_send_button")
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "إرسال", modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
fun UserMessageBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start // RTL means start is actual Right side of device layout inside current provider
    ) {
        Box(
            modifier = Modifier
                .shadow(1.dp, RoundedCornerShape(bottomStart = 16.dp, topStart = 16.dp, topEnd = 16.dp))
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(bottomStart = 16.dp, topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}

@Composable
fun AdvisorMessageBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End // Advisor bubble at left side
    ) {
        Box(
            modifier = Modifier
                .shadow(1.dp, RoundedCornerShape(bottomEnd = 16.dp, topStart = 16.dp, topEnd = 16.dp))
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(bottomEnd = 16.dp, topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .widthIn(max = 290.dp)
                .border(0.5.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(bottomEnd = 16.dp, topStart = 16.dp, topEnd = 16.dp))
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )
        }
    }
}
