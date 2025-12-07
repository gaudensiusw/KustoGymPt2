package com.example.projekuas.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projekuas.data.UserProfile // Dipertahankan, mungkin masih dipakai di tempat lain
import com.example.projekuas.utils.rememberBitmapFromBase64
import com.example.projekuas.viewmodel.HomeViewModelFactory
import com.example.projekuas.viewmodel.MemberStatItem
import com.example.projekuas.viewmodel.TrainerViewModel
import com.example.projekuas.viewmodel.TrainerUiState // Diperlukan untuk GlobalMemberStatsCard
import java.text.SimpleDateFormat
import java.util.*

// Warna (Pastikan ini sesuai tema Anda)
val BlueHeader = Color(0xFF1E88E5)
val BgGray = Color(0xFFF5F9FF)
val CardStatGray = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun TrainerMembersScreen(
    factory: HomeViewModelFactory,
    onNavigateUp: () -> Unit,
    onNavigateToDetail: (String) -> Unit // Navigasi ke Member Detail
) {
    val viewModel: TrainerViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsState()

    // Logic pull refresh
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isRefreshing,
        onRefresh = { viewModel.refreshData() }
    )

    var searchText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    Scaffold(
        containerColor = BgGray,
        topBar = {
            // TopBar dikosongkan karena Header kustom diintegrasikan ke dalam Body
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            Column(Modifier.fillMaxSize()) {

                // --- CUSTOM HEADER (BIRU) DENGAN SEARCH BAR & ICONS ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BlueHeader)
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
                ) {
                    Column {
                        // Title Row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onNavigateUp) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Text(
                                "My Members",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { /* Filter Logic pop up */ }) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "Filter",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Search Bar (White Background)
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            placeholder = { Text("Search members...", color = Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                disabledContainerColor = Color.White,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                    }
                }

                // --- CONTENT BODY ---
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // 1. STATISTIK GLOBAL (Card Putih overlap/dibawah header)
                    item {
                        GlobalMemberStatsCard(state)
                        Spacer(Modifier.height(16.dp))
                    }

                    // 2. FILTER CHIPS
                    item {
                        FilterChipsRow(selectedFilter) { selectedFilter = it }
                        Spacer(Modifier.height(16.dp))
                    }

                    // 3. MEMBER LIST
                    val filteredList = state.myMembers.filter { member ->
                        val textMatch =
                            member.name.contains(searchText, true) || member.email.contains(
                                searchText,
                                true
                            )
                        val filterMatch = when (selectedFilter) {
                            "All" -> true
                            "Excellent" -> member.attendanceRate >= 90
                            "Good" -> member.attendanceRate >= 75 && member.attendanceRate < 90
                            "Fair" -> member.attendanceRate < 75
                            else -> true
                        }
                        textMatch && filterMatch
                    }

                    if (state.isLoading) {
                        item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
                    } else if (filteredList.isEmpty()) {
                        item {
                            Text(
                                "No members found.",
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        items(filteredList) { member ->
                            MemberCardItem(
                                member = member, // member adalah MemberStatItem
                                onClick = {
                                    // Tambahkan validasi sebelum memanggil fungsi navigasi
                                    if (member.memberId.isNotBlank()) {
                                        // Navigasi ke HomeNavHost
                                        onNavigateToDetail(member.memberId)
                                    } else {
                                        // Opsional: Tampilkan pesan error jika ID kosong
                                        android.util.Log.e(
                                            "NAV_ERROR",
                                            "Member ID is missing for member: ${member.name}"
                                        )
                                        // Atau tampilkan Toast
                                    }
                                })
                            Spacer(Modifier.height(12.dp))
                        }
                    }


                    // Spacer untuk Bottom Navigation agar tidak tertutup
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }

            PullRefreshIndicator(
                state.isRefreshing,
                pullRefreshState,
                Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

// --- KOMPONEN PENDUKUNG ---

@Composable
fun GlobalMemberStatsCard(state: TrainerUiState) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f)) {
                        GlobalStatItem(
                            value = "${state.totalMembersThisWeek}",
                            label = "Total Members"
                        )
                    }

                    Box(Modifier.width(1.dp).height(30.dp).background(Color.LightGray))

                    Box(Modifier.weight(1f)) {
                        GlobalStatItem(
                            value = "${state.avgParticipants.toInt()}%",
                            label = "Avg Attendance"
                        )
                    }

                    Box(Modifier.width(1.dp).height(30.dp).background(Color.LightGray))

                    Box(Modifier.weight(1f)) {
                        GlobalStatItem(
                            value = "${state.activeMembersCount}",
                            label = "Active This Week"
                        )
                    }
                }
            }
        }

        @Composable
        fun GlobalStatItem(value: String, label: String) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth() // Pastikan sentral
            ) {
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(Modifier.height(4.dp))
                Text(
                    label,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
        }

        @Composable
        fun FilterChipsRow(selected: String, onSelect: (String) -> Unit) {
            // Scrollable Row agar filter chips banyak bisa di-scroll
            // Mengubah Row biasa menjadi Row yang bisa di-scroll jika ingin mendukung lebih banyak chip
            // Disini saya pakai Row biasa seperti kode Anda, tapi jika mau scrollable, ganti dengan LazyRow
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Excellent", "Good", "Fair").forEach { filter ->
                    val isSelected = selected == filter
                    val bgColor = if (isSelected) BlueHeader else Color.White
                    val txtColor = if (isSelected) Color.White else Color.Gray
                    val border = if (isSelected) null else androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color.LightGray
                    )

                    Surface(
                        color = bgColor,
                        shape = RoundedCornerShape(20.dp),
                        border = border,
                        modifier = Modifier.clickable { onSelect(filter) }
                            .height(32.dp) // Tambahkan height agar sama
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = filter,
                                color = txtColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        @Composable
        fun MemberCardItem(member: MemberStatItem, onClick: () -> Unit) {
            // Format Tanggal
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val joinDateStr = try {
                dateFormatter.format(Date(member.joinDate))
            } catch (e: Exception) {
                "-"
            }
            val lastClassFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val lastClassStr =
                if (member.lastClassDate > 0) lastClassFormatter.format(Date(member.lastClassDate)) else "-"

            // Logic Warna & Label Progress
            val (progressLabel, progressColor, progressBg) = when {
                member.attendanceRate >= 90 -> Triple(
                    "Excellent Progress",
                    Color(0xFF4CAF50),
                    Color(0xFFE8F5E9)
                )

                member.attendanceRate >= 75 -> Triple(
                    "Good Progress",
                    Color(0xFF1976D2),
                    Color(0xFFE3F2FD)
                )

                else -> Triple("Fair Progress", Color(0xFFFF9800), Color(0xFFFFF3E0))
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() }
            ) {
                Column(Modifier.padding(16.dp)) {
                    // Header: Avatar, Nama, Chat Icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        // --- PERBAIKAN VISUAL DI SINI ---
                        val bitmap = rememberBitmapFromBase64(member.profilePictureUrl)

                        if (bitmap != null) {
                            // Jika ada gambar, tampilkan gambar (Crop Circle)
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(50.dp)       // Ukuran fix
                                    .clip(CircleShape), // Potong jadi bulat
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Jika TIDAK ada gambar, tampilkan Inisial
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(BlueHeader),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.name.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        }
                        // --------------------------------

                        Spacer(Modifier.width(12.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                member.name.ifEmpty { "Unnamed Member" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(member.email, color = Color.Gray, fontSize = 12.sp)
                        }

                        IconButton(onClick = { /* Chat Action */ }) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                contentDescription = "Chat",
                                tint = Color.Gray
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Stats Row (Gray Boxes)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MemberStatBox(
                            label = "Classes",
                            value = "${member.totalClasses}",
                            modifier = Modifier.weight(1f)
                        )
                        MemberStatBox(
                            label = "Attendance",
                            value = "${member.attendanceRate.toInt()}%",
                            modifier = Modifier.weight(1f)
                        )
                        MemberStatBox(
                            label = "Member Since",
                            value = joinDateStr,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Footer: Progress & View Details
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = progressBg,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    null,
                                    tint = progressColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    progressLabel,
                                    color = progressColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Last class: $lastClassStr", fontSize = 10.sp, color = Color.Gray)

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "View Details",
                                    color = BlueHeader,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    Icons.Default.ChevronRight,
                                    null,
                                    tint = BlueHeader,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Komponen Kotak Abu-abu Center Align
        @Composable
        fun MemberStatBox(label: String, value: String, modifier: Modifier) {
            Surface(
                color = CardStatGray, // Abu-abu muda
                shape = RoundedCornerShape(8.dp),
                modifier = modifier
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally // Center Align
                ) {
                    Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        label,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
