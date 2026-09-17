package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.HangisDatabase
import com.example.data.local.toEntity
import com.example.data.model.Movie
import com.example.data.model.UserRole
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var database: HangisDatabase
  private lateinit var authRepository: AuthRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, HangisDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    authRepository = AuthRepository(database.userDao(), context)
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("HangisWatch", appName)
  }

  @Test
  fun `admin authentication lifecycle and role verification`() = runBlocking {
    authRepository.ensureAdminAccount()

    // 1. Verify admin account exists in database
    val adminEntity = database.userDao().getUserByEmail("admin@hangiswatch.com")
    assertNotNull("Admin entity must exist in users table", adminEntity)
    assertEquals("ADMIN", adminEntity?.role)
    assertTrue("Admin must be active", adminEntity?.isActive == true)

    // 2. Test incorrect password failure
    val failedResult = authRepository.login("admin@hangiswatch.com", "WrongPassword123")
    assertTrue(failedResult is AuthResult.Error)
    assertEquals("Invalid password", (failedResult as AuthResult.Error).message)

    // 3. Test correct admin authentication
    val successResult = authRepository.login("admin@hangiswatch.com", "AdminPass123!")
    assertTrue(successResult is AuthResult.Success)
    val adminUser = (successResult as AuthResult.Success).user
    assertEquals("admin@hangiswatch.com", adminUser.email)
    assertEquals(UserRole.ADMIN, adminUser.role)
    assertTrue("authRepository.isAdmin() must be true", authRepository.isAdmin())

    // 4. Test session persistence in StateFlow
    val current = authRepository.currentUser.value
    assertNotNull(current)
    assertEquals(UserRole.ADMIN, current?.role)

    // 5. Test password reset flow
    val resetResult = authRepository.resetPassword("admin@hangiswatch.com", "NewAdminSecure456!")
    assertTrue(resetResult is AuthResult.Success)

    // Old password now fails
    val oldLogin = authRepository.login("admin@hangiswatch.com", "AdminPass123!")
    assertTrue(oldLogin is AuthResult.Error)

    // New password succeeds
    val newLogin = authRepository.login("admin@hangiswatch.com", "NewAdminSecure456!")
    assertTrue(newLogin is AuthResult.Success)
    assertEquals(UserRole.ADMIN, (newLogin as AuthResult.Success).user.role)

    // 6. Test logout
    authRepository.logout()
    assertEquals(null, authRepository.currentUser.value)
    assertTrue(!authRepository.isAdmin())
  }

  @Test
  fun `insert and retrieve movie with real video source and subtitles`() = runBlocking {
    val testMovie = Movie(
      id = "test_movie_1",
      title = "Cybernetic Horizon",
      description = "A gripping sci-fi journey into the depths of a digital metropolis.",
      posterUrl = "https://example.com/poster.jpg",
      backdropUrl = "https://example.com/backdrop.jpg",
      trailerUrl = "https://example.com/trailer.mp4",
      videoUrl = "https://example.com/stream.m3u8",
      releaseYear = 2025,
      genre = "Sci-Fi",
      durationMinutes = 118,
      language = "English",
      subtitles = "https://example.com/subtitles_en.vtt",
      rating = 8.8,
      cast = "Alex Hunter, Sarah Connor",
      director = "Elena Vance",
      isFeatured = true,
      isTrending = true,
      isTvShow = false,
      isPublished = true,
      viewCount = 0
    )

    database.movieDao().insertMovie(testMovie.toEntity())

    val retrieved = database.movieDao().getMovieById("test_movie_1")
    assertNotNull(retrieved)
    assertEquals("https://example.com/stream.m3u8", retrieved?.videoUrl)
    assertEquals("https://example.com/subtitles_en.vtt", retrieved?.subtitles)
    assertEquals("English", retrieved?.language)
    assertEquals(8.8, retrieved?.rating ?: 0.0, 0.01)
  }
}

