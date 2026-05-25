package com.example.paths

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
@Composable
fun ButtonField(lista: List<Item>, modifier: Modifier, isLandscape: Boolean) {
    if (!isLandscape) {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .border(1.dp, Color.Gray.copy(alpha = 0.5f))
        ) {
            items(lista) { item ->
                ItemRow(item = item, isLandscape = isLandscape)
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .border(1.dp, Color.Gray.copy(alpha = 0.5f)),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(lista) { item ->
                ItemRow(item = item, isLandscape = isLandscape)
            }
        }
    }
}

@Composable
fun PhotoFan(urls: List<String>, modifier: Modifier = Modifier, scale: Float = 1f) {
    val displayImages = urls.take(3)
    val baseWidth = 80.dp * scale
    val baseHeight = 110.dp * scale
    val fanHeight = 110.dp * scale
    val offsetBase = 25.dp * scale
    val strokeWidth = (1.5.dp * scale).coerceAtLeast(1.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(fanHeight),
        contentAlignment = Alignment.Center
    ) {
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
                    .graphicsLayer {
                        rotationZ = rotation
                        translationX = offsetX.toPx()
                    }
                    .clip(RoundedCornerShape(4.dp))
                    .border(strokeWidth, Color.White, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun ItemRow(item: Item, isLandscape: Boolean) {
    if (!isLandscape) {
        // Skala 65% dla listy (Portrait)
        Row(
            modifier = Modifier
                .fillMaxWidth().clickable(onClick=DetailPagerScreen())
                .padding(vertical = 4.dp, horizontal = 6.dp)
                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PhotoFan(
                urls = item.getImages(),
                modifier = Modifier.width(70.dp),
                scale = 0.65f
            )

            Column(modifier = Modifier.padding(start = 25.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Text(
                    text = item.shortDescription,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    maxLines = 2,
                    lineHeight = 12.sp,
                    color = Color.DarkGray
                )
                val avg = if (item.scores.isNotEmpty()) item.scores.average() else 0.0
                Text(
                    text = "★ ${"%.1f".format(avg)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = Color(0xFFE65100)
                )
            }
        }
    } else {
        // Skala 50% dla siatki (Landscape) - bez opisu
        Column(
            modifier = Modifier
                .padding(2.dp)
                .border(0.5.dp, Color.LightGray, RoundedCornerShape(6.dp))
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PhotoFan(
                urls = item.getImages(),
                modifier = Modifier.fillMaxWidth(),
                scale = 0.5f
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                fontSize = 11.sp,
                maxLines = 1
            )
            val avg = if (item.scores.isNotEmpty()) item.scores.average() else 0.0
            Text(
                text = "★ ${"%.1f".format(avg)}",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = Color(0xFFFFA000)
            )
        }
    }
}
@Composable
fun DetailPagerScreen(
    items: List<Item>,
    initialIndex: Int,
    onBack: () -> Unit,
    stoper: StoperViewModel
){
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { items.size }
    )

    Scaffold(
        topBar = {
            // Przycisk Wróć
            Button(onClick = onBack) {
                Text("Wróć")
            }
        }
    ) { padding ->
        // Główny komponent do przesuwania lewo/prawo
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            pageSpacing = 16.dp // Odstęp między "stronami"
        ) { pageIndex ->
            val item = items[pageIndex]

            // Tutaj wyświetlasz pełny widok elementu
            DetailContent(item = item)
        }
    }
}
