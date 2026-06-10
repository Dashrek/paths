package com.example.paths

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch
import java.util.Date
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun StopwatchDisplay(
    stoper: StoperViewModel,
    style: androidx.compose.ui.text.TextStyle
) {
    val isRunning by stoper.isRunning.collectAsStateWithLifecycle()
    val formattedTime by stoper.formattedTime.collectAsStateWithLifecycle()
    val formattedCurrentTime by stoper.formattedCurrentTime.collectAsStateWithLifecycle()
    val elapsedTime by stoper.elapsedTime.collectAsStateWithLifecycle()

    val monospacedStyle = style.copy(
        fontFeatureSettings = "tnum"
    )

    Box(
        modifier = Modifier
            .widthIn(min = 80.dp)
            .graphicsLayer(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (elapsedTime == 0L && !isRunning) formattedCurrentTime else formattedTime,
            style = monospacedStyle,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1
        )
    }
}

@Composable
fun AppNavigation(
    stoperVM: StoperViewModel,
    authVM: AuthViewModel = viewModel(),
    onRequestLocationPermission: () -> Unit = {}
) {
    val navController = rememberNavController()
    val roweryVM: ItemViewModel = viewModel(key = "rowery_vm")
    val piesiVM: ItemViewModel = viewModel(key = "piesi_vm")
    val addRouteVM: AddRouteViewModel = viewModel()

    val userId = authVM.currentUserId
    LaunchedEffect(userId) {
        roweryVM.setFilter(isRower = true, userId = userId)
        piesiVM.setFilter(isRower = false, userId = userId)
    }

    val rowerzystaItems by roweryVM.items.collectAsStateWithLifecycle()
    val pieszyItems by piesiVM.items.collectAsStateWithLifecycle()
    val isDarkMode by authVM.isDarkMode.collectAsStateWithLifecycle()
    val pendingTime by stoperVM.pendingRecord.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).stopwatchDao() }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        NavHost(navController = navController, startDestination = "main") {
            composable("main") {
                MainPagerScreen(
                    rowerzystaItems = rowerzystaItems,
                    pieszyItems = pieszyItems,
                    isDarkMode = isDarkMode == true,
                    stoper = stoperVM,
                    authVM = authVM,
                    onRequestLocationPermission = onRequestLocationPermission,
                    onNavigateToDetails = { type, index ->
                        navController.navigate("details/$type/$index")
                    },
                    onNavigateToAddRoute = {
                        navController.navigate("add_route")
                    }
                )
            }
            composable(
                "details/{type}/{index}",
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("index") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type")
                val index = backStackEntry.arguments?.getInt("index") ?: 0
                val list = if (type == "rowery") rowerzystaItems else pieszyItems
                DetailPagerScreen(
                    items = list,
                    initialIndex = index,
                    onBack = { navController.popBackStack() },
                    stoper = stoperVM,
                    onImageClick = { url -> navController.navigate("image_preview/${Uri.encode(url)}") },
                    authViewModel = authVM,
                    itemViewModel = if (type == "rowery") roweryVM else piesiVM
                )
            }
            composable("add_route") {
                AddRouteScreen(
                    itemViewModel = roweryVM,
                    authVM = authVM,
                    addRouteVM = addRouteVM,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("image_preview/{url}") { backStackEntry ->
                val url = backStackEntry.arguments?.getString("url") ?: ""
                FullScreenImagePreview(imageUrl = url, onBack = { navController.popBackStack() }, stoper = stoperVM)
            }
        }

        if (pendingTime != null) {
            AlertDialog(
                onDismissRequest = { stoperVM.clearPendingRecord() },
                title = { Text("Zapisz rekord", color = MaterialTheme.colorScheme.primary) },
                text = { Text("Czy chcesz zapisać swój czas: ${formatTime(pendingTime!!)}?", color = MaterialTheme.colorScheme.onSurface) },
                confirmButton = {
                    TextButton(onClick = {
                        stoperVM.saveRecord(dao, authVM.currentUserId)
                        stoperVM.clearPendingRecord()
                    }) {
                        Text("Zapisz")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { stoperVM.clearPendingRecord() }) {
                        Text("Anuluj")
                    }
                }
            )
        }
    }
}

@Composable
fun MainPagerScreen(
    rowerzystaItems: List<Item>,
    pieszyItems: List<Item>,
    isDarkMode: Boolean,
    stoper: StoperViewModel,
    authVM: AuthViewModel,
    onRequestLocationPermission: () -> Unit,
    onNavigateToDetails: (String, Int) -> Unit,
    onNavigateToAddRoute: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (pagerState.currentPage == 1) {
                FloatingActionButton(
                    onClick = onNavigateToAddRoute,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Dodaj trasę")
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) { page ->
            when (page) {
                0 -> ProfileScreen(authVM, onRequestLocationPermission)
                1 -> MainListContent(rowerzystaItems, pieszyItems, isDarkMode, stoper, onNavigateToDetails, authVM)
                2 -> FilterScreen(authVM)
            }
        }
    }
}

@Composable
fun StoperControlRow(stoper: StoperViewModel, isLandscape: Boolean) {
    val isRunning by stoper.isRunning.collectAsStateWithLifecycle()

    if (isLandscape) {
        Row(
            modifier = Modifier.graphicsLayer(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (isRunning) stoper.pause() else stoper.start()
            }) {
                Icon(
                    if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = { stoper.stop() }) {
                Icon(Icons.Default.Stop, contentDescription = "Zapisz rekord", tint = MaterialTheme.colorScheme.secondary)
            }
            StopwatchDisplay(stoper = stoper, style = MaterialTheme.typography.titleLarge)
        }
    } else {
        Column(
            modifier = Modifier.graphicsLayer(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                IconButton(onClick = { if (isRunning) stoper.pause() else stoper.start() }) {
                    Icon(
                        if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { stoper.stop() }) {
                    Icon(Icons.Default.Stop, contentDescription = "Zapisz rekord", tint = MaterialTheme.colorScheme.secondary)
                }
            }
            StopwatchDisplay(stoper = stoper, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun MainListContent(
    rowerzystaItems: List<Item>,
    pieszyItems: List<Item>,
    isDarkMode: Boolean,
    stoper: StoperViewModel,
    onNavigateToDetails: (String, Int) -> Unit,
    authVM: AuthViewModel
) {
    var rowerVisible by rememberSaveable { mutableStateOf(false) }
    var pieszyVisible by rememberSaveable { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.smallestScreenWidthDp >= 600
    val showMinimaps by authVM.showMinimaps.collectAsStateWithLifecycle()
    val shareLocation by authVM.shareLocation.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        if (isLandscape || isTablet) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically) {
                OutlinedIconButton(
                    onClick = { rowerVisible = !rowerVisible; pieszyVisible = false },
                    border = BorderStroke(1.dp, if (rowerVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        containerColor = if (rowerVisible) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        contentColor = if (rowerVisible) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.DirectionsBike,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp).graphicsLayer(scaleX = -1f)
                    )
                }

                StoperControlRow(stoper = stoper, isLandscape = true)

                OutlinedIconButton(
                    onClick = { pieszyVisible = !pieszyVisible; rowerVisible=false},
                    border = BorderStroke(1.dp, if (pieszyVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        containerColor = if (pieszyVisible) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        contentColor = if (pieszyVisible) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.DirectionsRun,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedIconButton(
                    onClick = { rowerVisible = !rowerVisible ; pieszyVisible = false},
                    border = BorderStroke(1.dp, if (rowerVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        containerColor = if (rowerVisible) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        contentColor = if (rowerVisible) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.DirectionsBike,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp).graphicsLayer(scaleX = -1f)
                    )
                }

                StoperControlRow(stoper = stoper, isLandscape = false)

                OutlinedIconButton(
                    onClick = { pieszyVisible = !pieszyVisible; rowerVisible=false},
                    border = BorderStroke(1.dp, if (pieszyVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        containerColor = if (pieszyVisible) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        contentColor = if (pieszyVisible) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.DirectionsRun,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        AnimatedVisibility(visible = rowerVisible, modifier = Modifier.weight(1f)) {
            HorizontalItemPager(rowerzystaItems, onNavigateToDetails = { index -> onNavigateToDetails("rowery", index) }, showMinimap = showMinimaps, shareLocation = shareLocation)
        }

        AnimatedVisibility(visible = pieszyVisible, modifier = Modifier.weight(1f)) {
            HorizontalItemPager(pieszyItems, onNavigateToDetails = { index -> onNavigateToDetails("pieszy", index) }, showMinimap = showMinimaps, shareLocation = shareLocation)
        }

        if (!rowerVisible && !pieszyVisible) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Wybierz kategorię, aby zobaczyć trasy", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
fun HorizontalItemPager(items: List<Item>, onNavigateToDetails: (Int) -> Unit, showMinimap: Boolean, shareLocation: Boolean) {
    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Brak tras w tej kategorii", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { items.size })
    
    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 16.dp
        ) { page ->
            ItemCard(
                item = items[page],
                onClick = { onNavigateToDetails(page) },
                showMinimap = showMinimap,
                shareLocation = shareLocation
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(items.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                Box(
                    modifier = Modifier.padding(2.dp).clip(CircleShape).background(color).size(8.dp)
                )
            }
        }
    }
}

@Composable
fun ItemCard(item: Item, onClick: () -> Unit, showMinimap: Boolean, shareLocation: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f).clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            if (showMinimap && item.pathPoints.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    val points = remember(item.id, item.pathPoints) {
                        item.pathPoints.map { LatLng(it.latitude, it.longitude) }
                    }
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(points.first(), 13f)
                    }
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        googleMapOptionsFactory = {
                            GoogleMapOptions().liteMode(true)
                        },
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false, 
                            scrollGesturesEnabled = false, 
                            zoomGesturesEnabled = false,
                            myLocationButtonEnabled = false
                        )
                    ) {
                        Polyline(points = points, color = MaterialTheme.colorScheme.primary, width = 5f)
                        Marker(state = rememberUpdatedMarkerState(position = points.first()), title = "Start")
                        Marker(state = rememberUpdatedMarkerState(position = points.last()), title = "Meta")
                    }
                }
            } else if (item.imageUrls.isNotEmpty()) {
                PhotoFan(imageUrls = item.imageUrls)
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(item.name, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                if (item.distance > 0) {
                    Text("Dystans: %.2f km".format(item.distance), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(item.shortDescription, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun PhotoFan(imageUrls: List<String>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        imageUrls.take(3).forEachIndexed { index, url ->
            val rotation = when (index) {
                0 -> -10f
                1 -> 0f
                else -> 10f
            }
            val translationX = when (index) {
                0 -> (-30).dp
                1 -> 0.dp
                else -> 30.dp
            }
            Card(
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer {
                        rotationZ = rotation
                        this.translationX = translationX.toPx()
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = (index * 2).dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun FilterScreen(authVM: AuthViewModel) {
    val showMinimaps by authVM.showMinimaps.collectAsStateWithLifecycle()
    val isDarkMode by authVM.isDarkMode.collectAsStateWithLifecycle()
    val currentIndex by authVM.colorSchemeIndex.collectAsStateWithLifecycle()

    val colors = listOf(
        0xFF6750A4 to "Fioletowy",
        0xFF006C4C to "Zielony",
        0xFFB90063 to "Różowy",
        0xFF0061A4 to "Niebieski",
        0xFF8B5000 to "Pomarańczowy"
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).statusBarsPadding().verticalScroll(rememberScrollState())) {
        Text("Ustawienia Wyglądu", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tryb ciemny", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
            Switch(checked = isDarkMode == true, onCheckedChange = { authVM.setDarkMode(it) })
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Pokazuj minimapy na liście", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
            Switch(checked = showMinimaps, onCheckedChange = { authVM.setShowMinimaps(it) })
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Wybierz schemat kolorów:", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            colors.forEachIndexed { index, (colorValue, name) ->
                val isSelected = currentIndex == index
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(colorValue))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .clickable { authVM.setColorSchemeIndex(index) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = name, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(authVM: AuthViewModel, onRequestLocationPermission: () -> Unit) {
    val user by authVM.user.collectAsStateWithLifecycle()
    val sharePhotos by authVM.sharePhotos.collectAsStateWithLifecycle()
    val shareLocation by authVM.shareLocation.collectAsStateWithLifecycle()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).statusBarsPadding().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Profil użytkownika", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(24.dp))
        
        if (user != null) {
            AsyncImage(
                model = user?.photoUrl,
                contentDescription = "Avatar",
                modifier = Modifier.size(100.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(user?.displayName ?: "Użytkownik", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Prywatność i Zgody", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Zezwalaj na dodawanie zdjęć", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
                Switch(checked = sharePhotos, onCheckedChange = { authVM.setSharePhotos(it) })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Udostępniaj lokalizację", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
                Switch(checked = shareLocation, onCheckedChange = { authVM.setShareLocation(it, onRequestLocationPermission) })
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { authVM.logout() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)) {
                Text("Wyloguj się")
            }
        } else {
            Text("Zaloguj się, aby synchronizować trasy", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { /* Tu logika logowania */ }) {
                Text("Zaloguj przez Google")
            }
        }
    }
}

@Composable
fun DetailPagerScreen(
    items: List<Item>,
    initialIndex: Int,
    onBack: () -> Unit,
    stoper: StoperViewModel,
    onImageClick: (String) -> Unit,
    authViewModel: AuthViewModel,
    itemViewModel: ItemViewModel
) {
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { items.size })
    
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            DetailContent(
                item = items[page],
                onBack = onBack,
                stoper = stoper,
                onImageClick = onImageClick,
                authVM = authViewModel,
                itemViewModel = itemViewModel
            )
        }
    }
}

@Composable
fun DetailContent(
    item: Item,
    onBack: () -> Unit,
    stoper: StoperViewModel,
    onImageClick: (String) -> Unit,
    authVM: AuthViewModel,
    itemViewModel: ItemViewModel
) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).stopwatchDao() }
    val user by authVM.user.collectAsStateWithLifecycle()
    val shareLocation by authVM.shareLocation.collectAsStateWithLifecycle()
    val records by dao.getRecordsForRoute(item.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()

    val points = remember(item.id, item.pathPoints) {
        item.pathPoints.map { LatLng(it.latitude, it.longitude) }
    }

    val cameraPositionState = rememberCameraPositionState(item.id) {
        position = CameraPosition.fromLatLngZoom(
            if (points.isNotEmpty()) points.first() else LatLng(52.2297, 21.0122),
            14f
        )
    }

    var editTitle by remember { mutableStateOf(false) }
    var tempTitle by remember { mutableStateOf(item.name) }
    var editShortDesc by remember { mutableStateOf(false) }
    var tempShortDesc by remember { mutableStateOf(item.shortDescription) }
    var editLongDesc by remember { mutableStateOf(false) }
    var tempLongDesc by remember { mutableStateOf(item.longDescription) }
    var editCategory by remember { mutableStateOf(false) }
    var tempIsRower by remember { mutableStateOf(item.type) }
    var isGalleryEditMode by remember { mutableStateOf(false) }
    var showRecordsDeleteDialog by remember { mutableStateOf(false) }
    var isMapFullScreen by rememberSaveable { mutableStateOf(false) }
    var isMapEditMode by rememberSaveable { mutableStateOf(false) }
    
    val editedPathPoints = remember { mutableStateListOf<LatLng>() }
    LaunchedEffect(item.pathPoints) {
        editedPathPoints.clear()
        editedPathPoints.addAll(item.pathPoints.map { LatLng(it.latitude, it.longitude) })
    }

    val currentPoints = remember(item.id, item.pathPoints, isMapEditMode, editedPathPoints.size) {
        if (isMapEditMode) editedPathPoints.toList()
        else item.pathPoints.map { LatLng(it.latitude, it.longitude) }
    }

    val isOwner = user?.uid == item.ownerId

    if (isMapFullScreen) {
        // Tworzymy osobny stan kamery dla trybu pełnoekranowego, aby uniknąć błędu:
        // "CameraPositionState may only be associated with one GoogleMap at a time"
        val fullScreenCameraPositionState = rememberCameraPositionState(item.id) {
            position = cameraPositionState.position
        }

        Dialog(
            onDismissRequest = { isMapFullScreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                    val fullMapUiSettings = remember(shareLocation, isMapEditMode) {
                        MapUiSettings(
                            myLocationButtonEnabled = shareLocation,
                            zoomControlsEnabled = false,
                            scrollGesturesEnabled = true,
                            zoomGesturesEnabled = true,
                            tiltGesturesEnabled = true
                        )
                    }
                    val fullMapProperties = remember(shareLocation) {
                        MapProperties(isMyLocationEnabled = shareLocation)
                    }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = fullScreenCameraPositionState,
                        uiSettings = fullMapUiSettings,
                        properties = fullMapProperties,
                        onMapClick = { latLng ->
                            if (isMapEditMode) {
                                editedPathPoints.add(latLng)
                            }
                        }
                    ) {
                        if (currentPoints.isNotEmpty()) {
                            Polyline(points = currentPoints, color = MaterialTheme.colorScheme.primary, width = 8f)
                            Marker(state = rememberUpdatedMarkerState(position = currentPoints.first()), title = "Start")
                            if (currentPoints.size > 1) {
                                Marker(state = rememberUpdatedMarkerState(position = currentPoints.last()), title = "Meta")
                            }
                        }
                    }

                    // Layer 2: UI Controls
                    Column(
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledIconButton(onClick = { scope.launch { fullScreenCameraPositionState.animate(CameraUpdateFactory.zoomIn()) } }) {
                            Icon(Icons.Default.Add, contentDescription = "Zoom In")
                        }
                        FilledIconButton(onClick = { scope.launch { fullScreenCameraPositionState.animate(CameraUpdateFactory.zoomOut()) } }) {
                            Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                        }
                    }

                    IconButton(
                        onClick = { isMapFullScreen = false },
                        modifier = Modifier.align(Alignment.TopStart).padding(16.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                    ) {
                        Icon(Icons.Default.FullscreenExit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }

                    if (isMapEditMode) {
                        Row(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp).navigationBarsPadding(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(onClick = { if (editedPathPoints.isNotEmpty()) editedPathPoints.removeAt(editedPathPoints.size - 1) }) {
                                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Cofnij")
                                Text("Cofnij")
                            }
                            Button(
                                onClick = { editedPathPoints.clear() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = "Resetuj")
                                Text("Resetuj")
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            "Dystans: %.2f km".format(if (isMapEditMode) calculatePathDistance(editedPathPoints) else item.distance),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (!isMapEditMode) {
                        StopwatchWithCallback(
                            viewModel = stoper,
                            routeId = item.id,
                            onStop = {},
                            modifier = Modifier.graphicsLayer().align(Alignment.BottomCenter).padding(bottom = 32.dp).padding(horizontal = 32.dp).navigationBarsPadding(),
                            isCompact = true
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(item.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            EditableSection(
                label = "Tytuł",
                value = item.name,
                isEditing = editTitle,
                onEditClick = { editTitle = true; tempTitle = item.name },
                onSaveClick = {
                    itemViewModel.updateRouteField(item.id, "name", tempTitle)
                    editTitle = false
                },
                onCancelClick = { editTitle = false },
                isOwner = isOwner
            ) {
                if (editTitle) {
                    OutlinedTextField(
                        value = tempTitle,
                        onValueChange = { tempTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tytuł trasy") }
                    )
                } else {
                    Text(item.name, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            EditableSection(
                label = "Krótki opis",
                value = item.shortDescription,
                isEditing = editShortDesc,
                onEditClick = { editShortDesc = true; tempShortDesc = item.shortDescription },
                onSaveClick = {
                    itemViewModel.updateRouteField(item.id, "shortDescription", tempShortDesc)
                    editShortDesc = false
                },
                onCancelClick = { editShortDesc = false },
                isOwner = isOwner
            ) {
                if (editShortDesc) {
                    OutlinedTextField(
                        value = tempShortDesc,
                        onValueChange = { tempShortDesc = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Krótki opis") }
                    )
                } else {
                    Text(item.shortDescription, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            EditableSection(
                label = "Kategoria",
                value = if (item.type) "Rower" else "Pieszo",
                isEditing = editCategory,
                onEditClick = { editCategory = true; tempIsRower = item.type },
                onSaveClick = {
                    itemViewModel.updateRouteField(item.id, "type", tempIsRower)
                    editCategory = false
                },
                onCancelClick = { editCategory = false },
                isOwner = isOwner
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = if (editCategory) tempIsRower else item.type,
                        onClick = { if (editCategory) tempIsRower = true },
                        label = { Text("Rower") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.DirectionsBike, null, Modifier.size(18.dp)) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = if (editCategory) !tempIsRower else !item.type,
                        onClick = { if (editCategory) tempIsRower = false },
                        label = { Text("Pieszo") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.DirectionsRun, null, Modifier.size(18.dp)) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Galeria zdjęć", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary)
                if (isOwner) {
                    if (isGalleryEditMode) {
                        IconButton(onClick = { isGalleryEditMode = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Zakończ edycję", tint = Color.Red)
                        }
                    } else {
                        IconButton(onClick = { isGalleryEditMode = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edytuj galerię", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            LazyRow(modifier = Modifier.height(120.dp).padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(item.imageUrls) { url ->
                    Box {
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier.size(150.dp).clip(RoundedCornerShape(8.dp)).clickable { onImageClick(url) },
                            contentScale = ContentScale.Crop
                        )
                        if (isGalleryEditMode) {
                            IconButton(
                                onClick = { itemViewModel.deleteImageFromRoute(item.id, url) },
                                modifier = Modifier.align(Alignment.TopEnd).background(Color.White.copy(alpha = 0.7f), CircleShape).size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Usuń zdjęcie", tint = Color.Red, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Trasa prywatna:", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
                Switch(
                    checked = item.privateStatus,
                    onCheckedChange = { itemViewModel.updateRouteField(item.id, "privateStatus", it) },
                    enabled = isOwner
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            EditableSection(
                label = "Pełny opis",
                value = item.longDescription,
                isEditing = editLongDesc,
                onEditClick = { editLongDesc = true; tempLongDesc = item.longDescription },
                onSaveClick = {
                    itemViewModel.updateRouteField(item.id, "longDescription", tempLongDesc)
                    editLongDesc = false
                },
                onCancelClick = { editLongDesc = false },
                isOwner = isOwner
            ) {
                if (editLongDesc) {
                    OutlinedTextField(
                        value = tempLongDesc,
                        onValueChange = { tempLongDesc = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Szczegółowy opis") },
                        minLines = 3
                    )
                } else {
                    Text(item.longDescription, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Ocena trasy", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.Center) {
                val db = Firebase.firestore
                var currentUserRating by remember { mutableStateOf(0) }
                
                LaunchedEffect(item.id, user?.uid) {
                    user?.uid?.let { uid ->
                        val ratingId = "${uid}_${item.id}"
                        db.collection("routeRatings").document(ratingId).get()
                            .addOnSuccessListener { doc ->
                                currentUserRating = (doc.get("rating") as? Long)?.toInt() ?: 0
                            }
                    }
                }

                repeat(5) { index ->
                    val starIndex = index + 1
                    IconButton(onClick = { 
                        user?.let { u ->
                            val newRating = if (currentUserRating == starIndex) 0 else starIndex
                            itemViewModel.rateRoute(item.id, u.uid, newRating)
                            currentUserRating = newRating
                        } 
                    }) {
                        Icon(
                            imageVector = if (starIndex <= currentUserRating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (starIndex <= currentUserRating) Color(0xFFFFB800) else MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Text("(%.1f)".format(item.averageRating), modifier = Modifier.align(Alignment.CenterVertically), style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Map Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Mapa trasy", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary)
                if (isOwner) {
                    if (isMapEditMode) {
                        IconButton(onClick = { if (editedPathPoints.isNotEmpty()) editedPathPoints.removeAt(editedPathPoints.size - 1) }) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Cofnij", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = {
                            val distance = calculatePathDistance(editedPathPoints.toList())
                            val geoPoints = editedPathPoints.map { GeoPoint(it.latitude, it.longitude) }
                            val centerPoint = if (geoPoints.isNotEmpty()) AveragePoint(geoPoints) else GeoPoint(0.0, 0.0)
                            val updates = mapOf("pathPoints" to geoPoints, "distance" to distance, "startLocation" to centerPoint)
                            itemViewModel.updateRouteFields(item.id, updates)
                            isMapEditMode = false
                        }) {
                            Icon(Icons.Default.Save, contentDescription = "Zapisz mapę", tint = Color.Blue)
                        }
                        IconButton(onClick = { 
                            isMapEditMode = false
                            editedPathPoints.clear()
                            editedPathPoints.addAll(item.pathPoints.map { LatLng(it.latitude, it.longitude) })
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Anuluj", tint = Color.Red)
                        }
                    } else {
                        IconButton(onClick = { isMapEditMode = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edytuj mapę", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            val view = LocalView.current
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent()
                                view.parent?.requestDisallowInterceptTouchEvent(true)
                            }
                        }
                    }
            ) {
                val inlineMapUiSettings = remember(shareLocation) {
                    MapUiSettings(
                        myLocationButtonEnabled = shareLocation,
                        zoomControlsEnabled = false,
                        scrollGesturesEnabled = true,
                        zoomGesturesEnabled = true,
                        tiltGesturesEnabled = true
                    )
                }
                val inlineMapProperties = remember(shareLocation) {
                    MapProperties(isMyLocationEnabled = shareLocation)
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = inlineMapUiSettings,
                    properties = inlineMapProperties,
                    onMapClick = { latLng -> if (isMapEditMode) editedPathPoints.add(latLng) }
                ) {
                    if (currentPoints.isNotEmpty()) {
                        Polyline(points = currentPoints, color = MaterialTheme.colorScheme.primary, width = 8f)
                        Marker(state = rememberUpdatedMarkerState(position = currentPoints.first()), title = "Start")
                        if (currentPoints.size > 1) {
                            Marker(state = rememberUpdatedMarkerState(position = currentPoints.last()), title = "Meta")
                        }
                    }
                }
                
                IconButton(
                    onClick = { isMapFullScreen = true },
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(Icons.Default.Fullscreen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            Text(
                "Dystans: %.2f km".format(if (isMapEditMode) calculatePathDistance(editedPathPoints) else item.distance),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Stopwatch
            if (!isMapEditMode) {
                StopwatchWithCallback(
                    viewModel = stoper,
                    routeId = item.id,
                    onStop = {},
                    modifier = Modifier.fillMaxWidth().graphicsLayer(),
                    isCompact = false
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Records
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Twoje rekordy", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = { showRecordsDeleteDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            records.sortedByDescending { it.timestamp }.forEach { record ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(formatTime(record.timeElapsed), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(java.text.DateFormat.getDateTimeInstance().format(record.timestamp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }

    if (showRecordsDeleteDialog) {
        RecordsManagementDialog(
            records = records,
            onDelete = { record -> scope.launch { dao.deleteRecord(record) } },
            onClose = { showRecordsDeleteDialog = false }
        )
    }
}

@Composable
fun EditableSection(
    label: String,
    value: String,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    isOwner: Boolean,
    content: @Composable () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            if (isOwner) {
                if (isEditing) {
                    IconButton(onClick = onSaveClick) {
                        Icon(Icons.Default.Save, contentDescription = "Zapisz", tint = Color.Blue)
                    }
                    IconButton(onClick = onCancelClick) {
                        Icon(Icons.Default.Close, contentDescription = "Anuluj", tint = Color.Red)
                    }
                } else {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edytuj", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        content()
    }
}

@Composable
fun RecordsManagementDialog(
    records: List<LocalStopwatchRecord>,
    onDelete: (LocalStopwatchRecord) -> Unit,
    onClose: () -> Unit
) {
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text("Zarządzaj rekordami") },
                    actions = {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Zamknij")
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                items(records.sortedByDescending { it.timestamp }) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(formatTime(record.timeElapsed), style = MaterialTheme.typography.bodyLarge)
                                Text(java.text.DateFormat.getDateTimeInstance().format(record.timestamp), style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { onDelete(record) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullScreenImagePreview(imageUrl: String, onBack: () -> Unit, stoper: StoperViewModel) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().graphicsLayer(
                scaleX = maxOf(1f, scale),
                scaleY = maxOf(1f, scale),
                translationX = offset.x,
                translationY = offset.y
            ).transformable(state = state),
            contentScale = ContentScale.Fit
        )
        
        IconButton(onClick = onBack, modifier = Modifier.padding(16.dp).statusBarsPadding().align(Alignment.TopStart)) {
            Icon(Icons.Default.Close, contentDescription = "Zamknij", tint = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRouteScreen(
    itemViewModel: ItemViewModel,
    authVM: AuthViewModel,
    addRouteVM: AddRouteViewModel,
    onNavigateBack: () -> Unit
) {
    val title = addRouteVM.name
    val shortDescription = addRouteVM.shortDesc
    val longDescription = addRouteVM.longDesc
    val isRower = addRouteVM.isRower
    val images = addRouteVM.selectedImages
    val points = addRouteVM.pathPoints
    val isUploading by itemViewModel.isUploading.collectAsStateWithLifecycle()
    val isFullScreen = addRouteVM.isMapFullScreen
    val privateStatus = addRouteVM.privateStatus
    val shareLocation by authVM.shareLocation.collectAsStateWithLifecycle()
    
    val scope = rememberCoroutineScope()
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> addRouteVM.selectedImages.addAll(uris) }
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            if (points.isNotEmpty()) points.first() else LatLng(52.2297, 21.0122), 
            13f
        )
    }

    if (isFullScreen) {
        BackHandler { addRouteVM.isMapFullScreen = false }
        Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng -> addRouteVM.pathPoints.add(latLng) },
                uiSettings = MapUiSettings(myLocationButtonEnabled = shareLocation),
                properties = MapProperties(isMyLocationEnabled = shareLocation)
            ) {
                if (points.isNotEmpty()) {
                    Polyline(points = points.toList(), color = MaterialTheme.colorScheme.primary, width = 5f)
                    Marker(state = rememberUpdatedMarkerState(position = points.first()), title = "Start")
                    if (points.size > 1) {
                        Marker(state = rememberUpdatedMarkerState(position = points.last()), title = "Meta")
                    }
                }
            }
            IconButton(
                onClick = { addRouteVM.isMapFullScreen = false },
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(Icons.Default.FullscreenExit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            
            val distance = calculatePathDistance(points.toList())
            Surface(
                modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Text(
                    "Dystans: %.2f km".format(distance), 
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { if (points.isNotEmpty()) addRouteVM.pathPoints.removeAt(points.size - 1) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Cofnij", modifier = Modifier.size(24.dp))
                }
                Button(
                    onClick = { addRouteVM.pathPoints.clear() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Resetuj trasę", modifier = Modifier.size(24.dp))
                }
            }
        }
    } else {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text("Dodaj nową trasę") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { addRouteVM.name = it },
                    label = { Text("Tytuł trasy") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = shortDescription,
                    onValueChange = { addRouteVM.shortDesc = it },
                    label = { Text("Krótki opis") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = longDescription,
                    onValueChange = { addRouteVM.longDesc = it },
                    label = { Text("Pełny opis") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Kategoria:", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
                    FilterChip(
                        selected = isRower,
                        onClick = { addRouteVM.isRower = true },
                        label = { Text("Rower") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.DirectionsBike, null, Modifier.size(18.dp)) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = !isRower,
                        onClick = { addRouteVM.isRower = false },
                        label = { Text("Pieszo") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.DirectionsRun, null, Modifier.size(18.dp)) }
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Trasa prywatna:", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
                    Switch(
                        checked = privateStatus,
                        onCheckedChange = { addRouteVM.privateStatus = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Mapa trasy", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                
                val addMapUiSettings = remember(shareLocation) {
                    MapUiSettings(
                        myLocationButtonEnabled = shareLocation,
                        zoomControlsEnabled = false,
                        scrollGesturesEnabled = true,
                        zoomGesturesEnabled = true,
                        tiltGesturesEnabled = true
                    )
                }
                val addMapProperties = remember(shareLocation) {
                    MapProperties(isMyLocationEnabled = shareLocation)
                }
                val addView = LocalView.current

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    awaitPointerEvent()
                                    addView.parent?.requestDisallowInterceptTouchEvent(true)
                                }
                            }
                        }
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng -> addRouteVM.pathPoints.add(latLng) },
                        uiSettings = addMapUiSettings,
                        properties = addMapProperties
                    ) {
                        if (points.isNotEmpty()) {
                            Polyline(points = points.toList(), color = MaterialTheme.colorScheme.primary, width = 5f)
                            Marker(state = rememberUpdatedMarkerState(position = points.first()), title = "Start")
                            if (points.size > 1) {
                                Marker(state = rememberUpdatedMarkerState(position = points.last()), title = "Meta")
                            }
                        }
                    }
                    IconButton(
                        onClick = { addRouteVM.isMapFullScreen = true },
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                    ) {
                        Icon(Icons.Default.Fullscreen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                
                val calculatedDistance = calculatePathDistance(points.toList())
                Text(
                    "Obliczony dystans: %.2f km".format(calculatedDistance),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { if (points.isNotEmpty()) addRouteVM.pathPoints.removeAt(points.size - 1) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Cofnij", modifier = Modifier.size(24.dp))
                    }
                    Button(
                        onClick = { addRouteVM.pathPoints.clear() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Resetuj", modifier = Modifier.size(24.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Zdjęcia", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Button(onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Text("Dodaj zdjęcia")
                }
                
                LazyRow(modifier = Modifier.height(120.dp).padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(images) { uri ->
                        Box {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(onClick = { addRouteVM.selectedImages.remove(uri) }, modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.Red, CircleShape)) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { 
                        scope.launch {
                            itemViewModel.uploadRoute(
                                item = addRouteVM.getNewItem(authVM.currentUserId),
                                imageUris = images,
                                onSuccess = { onNavigateBack() },
                                onFailure = { /* TODO: Błąd */ }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading && title.isNotBlank()
                ) {
                    if (isUploading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    else Text("Zapisz trasę")
                }
            }
        }
    }
}

@Composable
fun StopwatchWithCallback(
    modifier: Modifier = Modifier,
    viewModel: StoperViewModel,
    routeId: String,
    onStop: (Long) -> Unit,
    isCompact: Boolean = true
) {
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val activeRouteId by viewModel.activeRouteId.collectAsStateWithLifecycle()

    if (isCompact) {
        Row(
            modifier = modifier
                .graphicsLayer()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isThisRouteRunning = isRunning && activeRouteId == routeId
            IconButton(onClick = { 
                if (isThisRouteRunning) {
                    viewModel.pause()
                } else {
                    viewModel.start(routeId)
                }
            }) {
                Icon(
                    if (isThisRouteRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            StopwatchDisplay(stoper = viewModel, style = MaterialTheme.typography.titleMedium)

            IconButton(onClick = { 
                if (activeRouteId == routeId) {
                    viewModel.stop()
                }
            }) {
                Icon(Icons.Default.Stop, contentDescription = "Zatrzymaj i zapisz", tint = MaterialTheme.colorScheme.secondary)
            }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isThisRouteRunning = isRunning && activeRouteId == routeId
            
            StopwatchDisplay(stoper = viewModel, style = MaterialTheme.typography.headlineMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledIconButton(
                    onClick = { if (isThisRouteRunning) viewModel.pause() else viewModel.start(routeId) },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isThisRouteRunning) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        if (isThisRouteRunning) Icons.Default.Pause else Icons.Default.PlayArrow, 
                        contentDescription = null, 
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                FilledIconButton(
                    onClick = { if (activeRouteId == routeId) viewModel.stop() },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.Stop, 
                        contentDescription = null, 
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
