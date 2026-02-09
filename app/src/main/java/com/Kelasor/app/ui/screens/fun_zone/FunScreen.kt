package com.Kelasor.app.ui.screens.fun_zone

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.ui.components.AudioPlayer
import com.Kelasor.app.ui.components.VideoPlayer
import com.Kelasor.app.ui.viewmodel.EntertainmentViewModel
import com.Kelasor.app.util.Constants
import com.Kelasor.app.ui.theme.MessageAppTheme

@Composable
fun FunScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToAllMovies: () -> Unit = {},
    onNavigateToAllMusic: () -> Unit = {},
    onNavigateToAllRiddles: () -> Unit = {},
    onNavigateToRiddleDetail: (String) -> Unit = {},
    onPlayVideo: (String) -> Unit = {},
    viewModel: EntertainmentViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val extendedColors = MessageAppTheme.extendedColors
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Aesthetic Background Glow
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-100).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(extendedColors.accent.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                FunHeader(onNavigateBack)
            }
            
            if (state.isLoading) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(2.dp).padding(horizontal = 24.dp),
                        color = extendedColors.accent,
                        trackColor = Color.Transparent
                    )
                }
            }

            // Featured Movies Section
            item {
                EntertainmentSection(
                    title = "سینما کلاسور",
                    subtitle = "جدیدترین ویدیوهای انگیزشی",
                    icon = Icons.Default.Movie,
                    onViewAll = onNavigateToAllMovies
                ) {
                    FeaturedMoviesList(state.movies, onPlayVideo)
                }
            }

            // Featured Music Section
            item {
                EntertainmentSection(
                    title = "نواخانه",
                    subtitle = "موسیقی تمرکز و مطالعه",
                    icon = Icons.Default.MusicNote,
                    onViewAll = onNavigateToAllMusic
                ) {
                    FeaturedMusicList(state.music)
                }
            }

            // Challenges Section
            item {
                EntertainmentSection(
                    title = "باشگاه معما",
                    subtitle = "هوش خود را به چالش بکشید",
                    icon = Icons.Default.Gamepad,
                    onViewAll = onNavigateToAllRiddles
                ) {
                    FeaturedGamesList(state.challenges, onRiddleClick = { riddle ->
                        onNavigateToRiddleDetail(riddle.id)
                    })
                }
            }
        }

        if (state.error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ErrorMessage(message = state.error!!, onRetry = { viewModel.refresh() })
            }
        }
    }
}

@Composable
fun FunHeader(onBack: () -> Unit) {
    val extendedColors = MessageAppTheme.extendedColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .background(extendedColors.glass, CircleShape)
                    .border(1.dp, extendedColors.glassBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Spacer(Modifier.weight(1f))
            
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(extendedColors.accent.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, extendedColors.accent.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Stars, 
                    contentDescription = null, 
                    tint = extendedColors.accent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(Modifier.height(24.dp))

        Text(
            "دنیای سرگرمی",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "لحظاتی برای آرامش و یادگیری در کلاسور",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun EntertainmentSection(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onViewAll: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MessageAppTheme.extendedColors.accent.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = MessageAppTheme.extendedColors.accent, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            }
            TextButton(
                onClick = onViewAll,
                colors = ButtonDefaults.textButtonColors(contentColor = MessageAppTheme.extendedColors.accent)
            ) {
                Text("مشاهده همه", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(16.dp))
        content()
    }
}

@Composable
fun FeaturedMoviesList(
    movies: List<com.Kelasor.app.data.remote.dto.MovieDto>,
    onPlayVideo: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(movies) { movie ->
            val extendedColors = MessageAppTheme.extendedColors
            Surface(
                modifier = Modifier
                    .width(320.dp)
                    .clip(RoundedCornerShape(28.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, extendedColors.glassBorder)
            ) {
                Column {
                    Box {
                            VideoPlayer(
                                videoUrl = if (movie.videoUrl.startsWith("http")) movie.videoUrl else Constants.BASE_URL + movie.videoUrl.removePrefix("/"),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(190.dp),
                                onFullScreenClick = {
                                    val url = if (movie.videoUrl.startsWith("http")) movie.videoUrl else Constants.BASE_URL + movie.videoUrl.removePrefix("/")
                                    onPlayVideo(url)
                                }
                            )
                    }
                    Column(Modifier.padding(20.dp)) {
                        Text(movie.title, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            movie.description, 
                            style = MaterialTheme.typography.bodySmall, 
                            maxLines = 2,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturedMusicList(music: List<com.Kelasor.app.data.remote.dto.MusicDto>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        music.forEach { track ->
            val audioUrl = if (track.audioUrl.startsWith("http")) track.audioUrl else Constants.BASE_URL + track.audioUrl.removePrefix("/")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .border(1.dp, MessageAppTheme.extendedColors.glassBorder, RoundedCornerShape(24.dp))
            ) {
                AudioPlayer(
                    audioUrl = audioUrl,
                    title = track.title,
                    artist = track.artist,
                    modifier = Modifier.fillMaxWidth().padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun FeaturedGamesList(
    challenges: List<com.Kelasor.app.data.remote.dto.GameChallengeDto>,
    onRiddleClick: (com.Kelasor.app.data.remote.dto.GameChallengeDto) -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(challenges) { challenge ->
            Surface(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .clickable { onRiddleClick(challenge) },
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, extendedColors.glassBorder)
            ) {
                Box {
                    // Accent Glow Background
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 40.dp, y = (-40).dp)
                            .background(Brush.radialGradient(listOf(extendedColors.accent.copy(alpha = 0.25f), Color.Transparent)))
                    )

                    Column(Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = extendedColors.accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                challenge.type ?: "چالش هوش",
                                color = extendedColors.accent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        Text(challenge.title, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 28.sp)
                        
                        Spacer(Modifier.height(12.dp))
                        Text(
                            challenge.question, 
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            lineHeight = 22.sp
                        )
                        
                        Spacer(Modifier.height(20.dp))
                        
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(extendedColors.accent.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Stars, null, tint = extendedColors.accent, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "جایزه: ${challenge.reward ?: "بدون امتیاز"}", 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 13.sp,
                                color = extendedColors.accent
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorMessage(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
    ) {
        Column(
            Modifier.padding(24.dp), 
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text("خطایی رخ داده است", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("تلاش مجدد", color = Color.White)
            }
        }
    }
}
