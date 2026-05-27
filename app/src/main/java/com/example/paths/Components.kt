package com.example.paths

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation(stoperViewModel: StoperViewModel) {
    val navController = rememberNavController()
    
    val roweryVM: ItemViewModel = viewModel(key = "rowery_vm")
    val piesiVM: ItemViewModel = viewModel(key = "piesi_vm")

    val roweryItems by roweryVM.items.collectAsStateWithLifecycle()
    val piesiItems by piesiVM.items.collectAsStateWithLifecycle()

    // Wynosimy stan widoczności, aby móc go zresetować przyciskiem "Do góry"
    var rowerVisible by remember { mutableStateOf(false) }
    var pieszyVisible by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            MainScreenContent(
                roweryItems = roweryItems,
                piesiItems = piesiItems,
                isLandscape = isLandscape,
                stoper = stoperViewModel,
                rowerVisible = rowerVisible,
                onRowerVisibleChange = { rowerVisible = it; if(it) pieszyVisible = false },
                pieszyVisible = pieszyVisible,
                onPieszyVisibleChange = { pieszyVisible = it; if(it) rowerVisible = false },
                onNavigateToDetails = { category, index ->
                    navController.navigate("details/$category/$index")
                }
            )
        }

        composable(
            route = "details/{category}/{index}",
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("index") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category")
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            val list = if (category == "rowery") roweryItems else piesiItems

            DetailPagerScreen(
                items = list,
                initialIndex = index,
                onBack = { 
                    // Przycisk "Do góry" lub powrót z Pager(0) -> reklamy
                    rowerVisible = false
                    pieszyVisible = false
                    navController.popBackStack("home", false)
                },
                stoper = stoperViewModel,
                onImageClick = { url ->
                    val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                    navController.navigate("imagePreview/$encodedUrl")
                }
            )
        }

        composable(
            route = "imagePreview/{url}",
            arguments = listOf(navArgument("url") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
            val url = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
            FullScreenImagePreview(
                url = url, 
                onBack = { navController.popBackStack() },
                stoper = stoperViewModel
            )
        }
    }
}

@Composable
fun MainScreenContent(
    roweryItems: List<Item>,
    piesiItems: List<Item>,
    isLandscape: Boolean,
    stoper: StoperViewModel,
    rowerVisible: Boolean,
    onRowerVisibleChange: (Boolean) -> Unit,
    pieszyVisible: Boolean,
    onPieszyVisibleChange: (Boolean) -> Unit,
    onNavigateToDetails: (String, Int) -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Button(
                    onClick = { onRowerVisibleChange(!rowerVisible) },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (rowerVisible) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        if (rowerVisible) "\uD83D\uDEB4" else "\uD83D\uDEB4\u200D➡\uFE0F",
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Stopwatch(
                    modifier = Modifier.weight(if (!isLandscape) 1.2f else 2.5F).fillMaxWidth(),
                    viewModel = stoper
                )

                Button(
                    onClick = { onPieszyVisibleChange(!pieszyVisible) },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pieszyVisible) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        if (pieszyVisible) "\uD83C\uDFC3" else "\uD83C\uDFC3\u200D➡\uFE0F",
                        maxLines = 2,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(modifier = Modifier.weight(7.5f)) {
                if (rowerVisible) {
                    ButtonField(
                        lista = roweryItems,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        isLandscape = isLandscape,
                        onNavigateToDetails = { index -> onNavigateToDetails("rowery", index) }
                    )
                }
                if (pieszyVisible) {
                    ButtonField(
                        lista = piesiItems,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        isLandscape = isLandscape,
                        onNavigateToDetails = { index -> onNavigateToDetails("piesi", index) }
                    )
                }
                if (!rowerVisible && !pieszyVisible) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Reklama", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun ButtonField(lista: List<Item>, modifier: Modifier, isLandscape: Boolean, onNavigateToDetails: (Int) -> Unit) {
    if (!isLandscape) {
        LazyColumn(
            modifier = modifier.fillMaxWidth().padding(top = 4.dp).border(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            itemsIndexed(lista, key = { _, item -> item.id }) { index, item ->
                ItemRow(item = item, isLandscape = isLandscape, onClick = { onNavigateToDetails(index) })
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = modifier.fillMaxWidth().padding(top = 4.dp).border(1.dp, MaterialTheme.colorScheme.outlineVariant),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(lista, key = { _, item -> item.id }) { index, item ->
                ItemRow(item = item, isLandscape = isLandscape, onClick = { onNavigateToDetails(index) })
            }
        }
    }
}

@Composable
fun PhotoFan(urls: List<String>, modifier: Modifier = Modifier, scale: Float = 1f) {
    val displayImages = remember(urls) { urls.take(3) }
    val baseWidth = 80.dp * scale
    val baseHeight = 110.dp * scale
    val fanHeight = 110.dp * scale
    val offsetBase = 25.dp * scale
    val strokeWidth = (1.5.dp * scale).coerceAtLeast(1.dp)

    Box(modifier = modifier.fillMaxWidth().height(fanHeight), contentAlignment = Alignment.Center) {
        displayImages.forEachIndexed { index, url ->
            val rotation = when (index) {
                0 -> if (displayImages.size > 1) -20f else 0f
                1 -> if (displayImages.size == 3) 0f else 20f
                2 -> 20f
                else -> 0f
            }
            val offsetX = when (index) {
                0 -> if (displayImages.size > 1) -offsetBase else 0.dp
                1 -> if (displayImages.size == 3) 0.dp else offsetBase
                2 -> offsetBase
                else -> 0.dp
            }
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(baseWidth, baseHeight)
                    .graphicsLayer { rotationZ = rotation; translationX = offsetX.toPx() }
                    .clip(RoundedCornerShape(4.dp))
                    .border(strokeWidth, Color.White, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun ItemRow(item: Item, isLandscape: Boolean, onClick: () -> Unit) {
    val avg = remember(item.scores) { if (item.scores.isNotEmpty()) item.scores.average() else 0.0 }
    
    if (!isLandscape) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 4.dp, horizontal = 6.dp)
                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)).padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PhotoFan(urls = item.getImages(), modifier = Modifier.width(80.dp), scale = 0.65f)
            Column(modifier = Modifier.padding(start = 25.dp)) {
                Text(text = item.name, style = MaterialTheme.typography.titleSmall, fontSize = 13.sp, maxLines = 1)
                Text(text = item.shortDescription, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, maxLines = 2, color = Color.DarkGray)
                Text(text = "★ ${"%.1f".format(avg)}", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color(0xFFE65100))
            }
        }
    } else {
        Column(
            modifier = Modifier.clickable { onClick() }.padding(2.dp)
                .border(0.5.dp, Color.LightGray, RoundedCornerShape(6.dp)).padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PhotoFan(urls = item.getImages(), modifier = Modifier.fillMaxWidth(), scale = 0.5f)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = item.name, style = MaterialTheme.typography.titleSmall, fontSize = 11.sp, maxLines = 1)
            Text(text = "★ ${"%.1f".format(avg)}", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color(0xFFFFA000))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPagerScreen(
    items: List<Item>,
    initialIndex: Int,
    onBack: () -> Unit,
    stoper: StoperViewModel,
    onImageClick: (String) -> Unit = {}
) {
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { items.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        if (pagerState.currentPage > 0) {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        } else {
                            onBack() // Powrót do reklam
                        }
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                title = { Stopwatch(viewModel = stoper) },
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Do góry (Reklamy)")
                    }
                }
            )
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(padding),
            pageSpacing = 16.dp,
            key = { index -> items[index].id }
        ) { pageIndex ->
            DetailContent(itemik = items[pageIndex], onImageClick = onImageClick)
        }
    }
}

@Composable
fun DetailContent(itemik: Item, onImageClick: (String) -> Unit = {}) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text(text = itemik.name, style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = itemik.shortDescription, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            LazyRow(modifier = Modifier.fillMaxWidth().height(300.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(itemik.imageUrls, key = { it }) { url ->
                    AsyncImage(
                        model = url, contentDescription = null, contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(300.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.5.dp, Color.White, RoundedCornerShape(8.dp))
                            .clickable { onImageClick(url) }
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = itemik.longDescription, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
        }
    }
}

@Composable
fun FullScreenImagePreview(url: String, onBack: () -> Unit, stoper: StoperViewModel) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isControlsVisible by remember { mutableStateOf(true) }

    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        offset += offsetChange
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { isControlsVisible = !isControlsVisible }
                )
            }
    ) {
        AsyncImage(
            model = url,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .transformable(state = state),
            contentScale = ContentScale.Fit
        )

        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Powrót",
                        tint = Color.White
                    )
                }

                Stopwatch(viewModel = stoper)
            }
        }
    }
}
