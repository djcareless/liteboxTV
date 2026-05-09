package tv.litebox.ui.home

import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import tv.litebox.LiteBoxApp
import tv.litebox.data.db.LiteBoxDatabase
import tv.litebox.data.db.dao.MediaItemDao
import tv.litebox.data.db.entity.MediaItemEntity
import tv.litebox.domain.model.MediaType

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    private lateinit var mockApp: LiteBoxApp
    private lateinit var mockDb: LiteBoxDatabase
    private lateinit var mockDao: MediaItemDao

    private val continueWatchingFlow = MutableStateFlow<List<MediaItemEntity>>(emptyList())
    private val moviesFlow = MutableStateFlow<List<MediaItemEntity>>(emptyList())
    private val tvShowsFlow = MutableStateFlow<List<MediaItemEntity>>(emptyList())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockDao = mockk(relaxed = true)
        mockDb = mockk(relaxed = true)
        mockApp = mockk(relaxed = true)

        every { mockDb.mediaItemDao() } returns mockDao
        every { mockApp.database } returns mockDb

        // Mock the static singleton
        mockkObject(LiteBoxApp.Companion)
        every { LiteBoxApp.instance } returns mockApp

        // Stub DAO flows
        every { mockDao.observeContinueWatching() } returns continueWatchingFlow
        every { mockDao.observeByType(MediaType.MOVIE.name) } returns moviesFlow
        every { mockDao.observeByType(MediaType.TV_SHOW.name) } returns tvShowsFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state has isLoading=true and empty lists`() {
        // The default HomeUiState constructor has isLoading=true and empty lists
        val defaultState = HomeUiState()
        assertTrue("Default isLoading should be true", defaultState.isLoading)
        assertTrue("Default continueWatching should be empty", defaultState.continueWatching.isEmpty())
        assertTrue("Default recentMovies should be empty", defaultState.recentMovies.isEmpty())
        assertTrue("Default recentTvShows should be empty", defaultState.recentTvShows.isEmpty())
    }

    @Test
    fun `combine flows produces correct HomeUiState with data`() = runTest(testDispatcher) {
        // Arrange: emit sample data
        val movieEntity = MediaItemEntity(
            id = "m1", title = "Test Movie", type = MediaType.MOVIE.name,
            uri = "file:///movie.mp4", thumbnailUrl = null, backdropUrl = null,
            description = null, year = 2024, duration = 7200000L, rating = 8.5f,
            genres = listOf("Action"), sourcePlugin = null, resumePosition = 0L,
            watched = false, addedAt = System.currentTimeMillis(),
        )
        val tvEntity = MediaItemEntity(
            id = "t1", title = "Test Show", type = MediaType.TV_SHOW.name,
            uri = "file:///show.mp4", thumbnailUrl = null, backdropUrl = null,
            description = null, year = 2023, duration = 2700000L, rating = 9.0f,
            genres = listOf("Drama"), sourcePlugin = null, resumePosition = 1500L,
            watched = false, addedAt = System.currentTimeMillis(),
        )
        val continueEntity = MediaItemEntity(
            id = "c1", title = "Continue Me", type = MediaType.MOVIE.name,
            uri = "file:///continue.mp4", thumbnailUrl = null, backdropUrl = null,
            description = null, year = 2022, duration = 5000000L, rating = 7.0f,
            genres = listOf("Comedy"), sourcePlugin = null, resumePosition = 3000L,
            watched = false, addedAt = System.currentTimeMillis(),
        )

        continueWatchingFlow.value = listOf(continueEntity)
        moviesFlow.value = listOf(movieEntity)
        tvShowsFlow.value = listOf(tvEntity)

        // Act: create the ViewModel (triggers init block)
        val vm = HomeViewModel()
        advanceUntilIdle()

        // Assert
        val state = vm.uiState.value
        assertFalse("isLoading should be false after combine", state.isLoading)
        assertEquals("Should have 1 continue watching item", 1, state.continueWatching.size)
        assertEquals("continue watching title", "Continue Me", state.continueWatching[0].title)
        assertEquals("Should have 1 recent movie", 1, state.recentMovies.size)
        assertEquals("movie title", "Test Movie", state.recentMovies[0].title)
        assertEquals("Should have 1 recent tv show", 1, state.recentTvShows.size)
        assertEquals("tv show title", "Test Show", state.recentTvShows[0].title)
    }

    @Test
    fun `combine flows with empty DAO emits isLoading=false and empty lists`() = runTest(testDispatcher) {
        // All flows emit empty lists
        continueWatchingFlow.value = emptyList()
        moviesFlow.value = emptyList()
        tvShowsFlow.value = emptyList()

        val vm = HomeViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse("isLoading should be false", state.isLoading)
        assertTrue("continueWatching should be empty", state.continueWatching.isEmpty())
        assertTrue("recentMovies should be empty", state.recentMovies.isEmpty())
        assertTrue("recentTvShows should be empty", state.recentTvShows.isEmpty())
    }

    @Test
    fun `recentMovies is limited to 20 items`() = runTest(testDispatcher) {
        // Create 25 movie entities
        val manyMovies = (1..25).map { i ->
            MediaItemEntity(
                id = "m$i", title = "Movie $i", type = MediaType.MOVIE.name,
                uri = "file:///movie$i.mp4", thumbnailUrl = null, backdropUrl = null,
                description = null, year = 2024, duration = 7200000L, rating = null,
                genres = emptyList(), sourcePlugin = null, resumePosition = 0L,
                watched = false, addedAt = System.currentTimeMillis() - i,
            )
        }
        moviesFlow.value = manyMovies

        val vm = HomeViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("Recent movies should be capped at 20", 20, state.recentMovies.size)
    }

    @Test
    fun `toDomain mapping preserves fields`() = runTest(testDispatcher) {
        val entity = MediaItemEntity(
            id = "x1", title = "Domain Test", type = MediaType.MOVIE.name,
            uri = "file:///test.mp4", thumbnailUrl = "http://thumb.jpg",
            backdropUrl = "http://bg.jpg", description = "A test movie",
            year = 2024, duration = 5000000L, rating = 9.5f,
            genres = listOf("Sci-Fi", "Thriller"), sourcePlugin = "plugin.test",
            resumePosition = 1200L, watched = false, addedAt = 1700000000000L,
        )
        moviesFlow.value = listOf(entity)

        val vm = HomeViewModel()
        advanceUntilIdle()

        val item = vm.uiState.value.recentMovies.first()
        assertEquals("x1", item.id)
        assertEquals("Domain Test", item.title)
        assertEquals(MediaType.MOVIE, item.type)
        assertEquals("file:///test.mp4", item.uri)
        assertEquals("http://thumb.jpg", item.thumbnailUrl)
        assertEquals("http://bg.jpg", item.backdropUrl)
        assertEquals("A test movie", item.description)
        assertEquals(2024, item.year)
        assertEquals(5000000L, item.duration)
        assertEquals(9.5f, item.rating!!, 0.01f)
        assertEquals(listOf("Sci-Fi", "Thriller"), item.genres)
        assertEquals("plugin.test", item.sourcePlugin)
        assertEquals(1200L, item.resumePosition)
        assertFalse(item.watched)
        assertEquals(1700000000000L, item.addedAt)
    }
}
