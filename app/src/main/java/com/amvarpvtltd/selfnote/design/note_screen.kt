package com.amvarpvtltd.selfnote.design

import AutoSyncManager
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.amvarpvtltd.selfnote.components.AnimatedFloatingActionButton
import com.amvarpvtltd.selfnote.components.EmptyStateCard
import com.amvarpvtltd.selfnote.components.IconActionButton
import com.amvarpvtltd.selfnote.components.LoadingCard
import com.amvarpvtltd.selfnote.components.NoteScreenBackground
import com.amvarpvtltd.selfnote.components.NotesDisplay
import com.amvarpvtltd.selfnote.components.OfflineBanner
import com.amvarpvtltd.selfnote.components.OfflineEmptyStateCard
import com.amvarpvtltd.selfnote.components.SearchBar
import com.amvarpvtltd.selfnote.components.SortOptionsSheet
import com.amvarpvtltd.selfnote.components.SyncStatusIndicator
import com.amvarpvtltd.selfnote.components.ThemeToggleButton
import com.amvarpvtltd.selfnote.components.ViewModeToggleButton
import com.amvarpvtltd.selfnote.components.rememberViewModeState
import com.amvarpvtltd.selfnote.repository.NoteRepository
import com.amvarpvtltd.selfnote.search.rememberSearchAndSortManager
import com.amvarpvtltd.selfnote.theme.rememberThemeState
import com.amvarpvtltd.selfnote.ui.theme.Lobster_Font
import com.amvarpvtltd.selfnote.utils.Constants
import com.amvarpvtltd.selfnote.utils.ShareUtils
import dataclass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(navController: NavHostController) {
    val notesState = remember { mutableStateOf<List<dataclass>>(emptyList()) }
    val isLoadingState = remember { mutableStateOf(true) }
    val isRefreshingState = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val noteRepository = remember { NoteRepository(context) }

    // Network and sync management
    val networkManager = remember { com.amvarpvtltd.selfnote.utils.NetworkManager.getInstance(context) }
    val autoSyncManager = remember { AutoSyncManager.getInstance(context, noteRepository) }

    val isOnline by networkManager.isOnline.collectAsState()
    val isSyncing by autoSyncManager.isSyncing.collectAsState()
    val syncStatus by autoSyncManager.lastSyncStatus.collectAsState()
    val hasPendingSync by autoSyncManager.hasPendingSync.collectAsState()

    // Search and Sort functionality - Fixed implementation
    val searchAndSortManager = rememberSearchAndSortManager()
    val searchAndSortState by searchAndSortManager.searchAndSortState.collectAsState()
    var showSortSheet by remember { mutableStateOf(false) }

    // Local search state for immediate UI updates
    var localSearchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Theme management
    val themeState = rememberThemeState()
    var currentTheme by themeState

    // View mode management
    val viewModeState = rememberViewModeState()
    var currentViewMode by viewModeState

    // Offline banner state
    var showOfflineBanner by remember { mutableStateOf(false) }

    // Monitor offline state to show banner
    LaunchedEffect(isOnline) {
        if (!isOnline) {
            showOfflineBanner = true
        } else {
            // Hide banner when back online after a delay
            delay(2000)
            showOfflineBanner = false
        }
    }

    // Update search manager when local search changes
    LaunchedEffect(localSearchQuery) {
        searchAndSortManager.updateSearchQuery(localSearchQuery)
    }

    // Update notes in search manager when notes change
    LaunchedEffect(notesState.value) {
        searchAndSortManager.updateNotes(notesState.value)
    }

    // Save view mode when it changes
    LaunchedEffect(currentViewMode) {
        com.amvarpvtltd.selfnote.components.ViewModeManager.setViewMode(context, currentViewMode)
    }

    // Refresh functionality
    fun refreshNotes() {
        scope.launch(Dispatchers.IO) {
            isRefreshingState.value = true
            try {
                delay(Constants.REFRESH_DELAY)
                val result = noteRepository.fetchNotes()
                if (result.isSuccess) {
                    val notes = result.getOrNull() ?: emptyList()
                    notesState.value = notes
                    searchAndSortManager.updateNotes(notes)

                    // Note: hasPendingSync will update automatically from StateFlow
                }
            } finally {
                isRefreshingState.value = false
                if (isLoadingState.value) isLoadingState.value = false
            }
        }
    }

    // Delete note function
    fun deleteNote(noteId: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val result = noteRepository.deleteNote(noteId, context)
                if (result.isSuccess) {
                    withContext(Dispatchers.Main) { refreshNotes() }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, Constants.ERROR_DELETING_MESSAGE, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Share note function
    fun shareNote(note: dataclass) {
        ShareUtils.shareNote(context, note)
    }

    // Sync function
    fun syncNotes() {
        scope.launch(Dispatchers.IO) {
            try {
                val result = noteRepository.syncOfflineNotes(context)
                if (result.isSuccess) {
                    withContext(Dispatchers.Main) {
                        refreshNotes()
                        Toast.makeText(context, "✅ Sync completed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Sync failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshNotes()
        // Start automatic sync monitoring
        autoSyncManager.startAutoSync()
    }

    // Cleanup when screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            autoSyncManager.stopAutoSync()
        }
    }

    // Handle sync status changes
    LaunchedEffect(syncStatus) {
        when (syncStatus) {
            is AutoSyncManager.SyncStatus.Success -> {
                withContext(Dispatchers.Main) {
                    refreshNotes()
                    Toast.makeText(context, Constants.SYNC_SUCCESS_MESSAGE, Toast.LENGTH_SHORT).show()
                }
            }
            is AutoSyncManager.SyncStatus.Failed -> {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, Constants.SYNC_FAILED_MESSAGE, Toast.LENGTH_SHORT).show()
                }
            }
            else -> { /* No action needed for None and InProgress */ }
        }
    }

    // Apply theme changes
    LaunchedEffect(currentTheme) {
        com.amvarpvtltd.selfnote.theme.ThemeManager.setThemeMode(context, currentTheme)
    }

    com.amvarpvtltd.selfnote.theme.ProvideNoteTheme(themeMode = currentTheme) {
        NoteScreenBackground {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    Column {

                        Spacer(modifier = Modifier.height(16.dp))

                        // Add offline banner at the top
                        OfflineBanner(
                            isVisible = showOfflineBanner && !isOnline,
                            message = "You're offline. Notes are cached locally and will sync when connected.",
                            onDismiss = { showOfflineBanner = false }
                        )

                        // Main top bar with only title and theme toggle
                        TopAppBar(
                            title = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "My Notes",
                                            fontWeight = FontWeight.Bold,
                                            color = NoteTheme.OnSurface,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontFamily = Lobster_Font,
                                            fontSize = 28.sp
                                        )

                                        if (isRefreshingState.value) {
                                            Spacer(modifier = Modifier.width(Constants.CORNER_RADIUS_SMALL.dp))
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(Constants.ICON_SIZE_MEDIUM.dp),
                                                strokeWidth = 2.dp,
                                                color = NoteTheme.Primary
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.offset(x = (-15).dp)) {
                                        // Only keep sync status and theme toggle in top bar
                                        SyncStatusIndicator(
                                            isOnline = isOnline,
                                            hasPendingSync = hasPendingSync,
                                            isSyncing = isSyncing,
                                            onSyncClick = { syncNotes() }
                                        )

                                        Spacer(modifier = Modifier.width(15.dp))

                                        // Theme toggle button
                                        ThemeToggleButton(
                                            currentTheme = currentTheme,
                                            onThemeChange = { newTheme -> currentTheme = newTheme }
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )

                        // Search bar
                        SearchBar(
                            searchQuery = searchAndSortState.searchQuery,
                            onSearchQueryChange = { localSearchQuery = it },
                            isSearchActive = isSearchActive,
                            onSearchActiveChange = { isSearchActive = it },
                            onClearSearch = {
                                searchAndSortManager.clearSearch()
                                isSearchActive = false
                            },
                            modifier = Modifier.padding(horizontal = Constants.PADDING_MEDIUM.dp)

                        )

                        // Move view mode and sort buttons below search bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Constants.PADDING_MEDIUM.dp)
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End, // Changed to Arrangement.End to move buttons to right
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Show search results count when active - moved to left side
                            if (searchAndSortState.isSearchActive) {
                                Text(
                                    text = "${searchAndSortState.filteredNotes.size} result${if (searchAndSortState.filteredNotes.size != 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NoteTheme.Warning,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.weight(1f)) // Push buttons to right
                            } else {
                                Spacer(modifier = Modifier.weight(1f)) // Take up space when no search results
                            }

                            // View mode toggle button
                            ViewModeToggleButton(
                                currentViewMode = currentViewMode,
                                onViewModeChange = { newViewMode ->
                                    currentViewMode = newViewMode
                                }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Sort button
                            IconActionButton(
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showSortSheet = true
                                },
                                icon = Icons.AutoMirrored.Outlined.Sort,
                                contentDescription = "Sort",
                                containerColor = NoteTheme.Secondary.copy(alpha = 0.1f),
                                contentColor = NoteTheme.Secondary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                },
                floatingActionButton = {
                    AnimatedFloatingActionButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate("addscreen")
                        }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when {
                        isLoadingState.value -> {
                            LoadingCard("Loading your notes...", "Please wait a moment")
                        }

                        searchAndSortState.filteredNotes.isEmpty() && searchAndSortState.isSearchActive -> {
                            EmptyStateCard(
                                icon = Icons.Outlined.SearchOff,
                                title = "No results found",
                                description = "Try adjusting your search terms\nor create a new note",
                                buttonText = "Create New Note",
                                onButtonClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    navController.navigate("addscreen")
                                }
                            )
                        }

                        searchAndSortState.filteredNotes.isEmpty() -> {
                            // Use the new OfflineEmptyStateCard for better offline handling
                            OfflineEmptyStateCard(
                                isOnline = isOnline,
                                hasPendingSync = hasPendingSync,
                                onCreateNoteClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    navController.navigate("addscreen")
                                }
                            )
                        }

                        else -> {
                            Column {
                                // Notes display with different view modes
                                NotesDisplay(
                                    notes = searchAndSortState.filteredNotes,
                                    viewMode = currentViewMode,
                                    onView = { note ->
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        navController.navigate("viewnote/${note.id}")
                                    },
                                    onEdit = { note ->
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        navController.navigate("addscreen/${note.id}")
                                    },
                                    onDelete = { note -> deleteNote(note.id) },
                                    onShare = { note -> shareNote(note) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Sort options sheet
    if (showSortSheet) {
        SortOptionsSheet(
            currentSort = searchAndSortState.sortOption,
            onSortChange = { sortOption ->
                searchAndSortManager.updateSortOption(sortOption)
            },
            onDismiss = { showSortSheet = false }
        )
    }
}
