package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.util.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        MovieEntity::class,
        TvEpisodeEntity::class,
        WatchlistEntity::class,
        WatchHistoryEntity::class,
        PaymentEntity::class,
        NotificationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class HangisDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun movieDao(): MovieDao
    abstract fun tvEpisodeDao(): TvEpisodeDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun paymentDao(): PaymentDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: HangisDatabase? = null

        fun getDatabase(context: Context): HangisDatabase {
            return INSTANCE ?: synchronized(this) {
                var instance: HangisDatabase? = null
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    HangisDatabase::class.java,
                    "hangiswatch_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(DatabaseCallback { instance ?: INSTANCE })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val getDb: () -> HangisDatabase?) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        getDb()?.let { populateDatabase(it) }
                    } catch (_: Exception) {}
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                try {
                    db.execSQL(
                        "UPDATE movies SET video_url = 'https://archive.org/download/Tears-of-Steel/tears_of_steel_720p.mp4', trailer_url = 'https://archive.org/download/Tears-of-Steel/tears_of_steel_720p.mp4' WHERE video_url LIKE '%gtv-videos-bucket%' OR video_url LIKE '%commondatastorage%'"
                    )
                    db.execSQL(
                        "UPDATE tv_episodes SET video_url = 'https://archive.org/download/Tears-of-Steel/tears_of_steel_720p.mp4' WHERE video_url LIKE '%gtv-videos-bucket%' OR video_url LIKE '%commondatastorage%'"
                    )
                } catch (_: Exception) {}
            }
        }

        suspend fun populateDatabase(db: HangisDatabase) {
            // Seed Default Real Administrator
            val adminSalt = "hangis_admin_salt_883"
            val adminHash = SecurityUtils.hashPassword("Adminpass123!", adminSalt)
            val adminUser = UserEntity(
                id = "user_admin_001",
                email = "admin@hangiswatch.com",
                passwordHash = adminHash,
                salt = adminSalt,
                name = "Hangis Administrator",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                role = "ADMIN",
                subscriptionTier = "PREMIUM",
                subscriptionExpiresAt = System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000,
                isActive = true,
                isEmailVerified = true,
                createdAt = System.currentTimeMillis()
            )
            db.userDao().insertUser(adminUser)

            // Seed Movies (Licensed Open-Content & Blender Foundation Public Movies with active video streams)
            val movies = listOf(
                MovieEntity(
                    id = "movie_001",
                    title = "Cosmic Odyssey: Tears of Steel",
                    description = "In a dystopian future set in Amsterdam, a group of scientists and soldiers struggle to protect human existence against rogue biomechanical cyborgs in this high-octane sci-fi thriller.",
                    posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600",
                    backdropUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1200",
                    trailerUrl = "https://archive.org/download/Tears-of-Steel/tears_of_steel_720p.mp4",
                    videoUrl = "https://archive.org/download/Tears-of-Steel/tears_of_steel_720p.mp4",
                    releaseYear = 2024,
                    genre = "Sci-Fi",
                    durationMinutes = 124,
                    language = "English",
                    subtitles = "English, Spanish, French, German",
                    rating = 9.2,
                    cast = "Thom Hoffman, Denise Rebergen, Vanja Rukavina",
                    director = "Ian Hubert",
                    isFeatured = true,
                    isTrending = true,
                    isTvShow = false,
                    isPublished = true,
                    viewCount = 0,
                    createdAt = System.currentTimeMillis() - 5L * 24 * 60 * 60 * 1000
                ),
                MovieEntity(
                    id = "movie_002",
                    title = "Neon Velocity: Charge",
                    description = "An adrenaline-fueled cyber heist where an operative must infiltrate an automated energy generator guarded by lethal surveillance units in a rain-soaked neon metropolis.",
                    posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600",
                    backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200",
                    trailerUrl = "https://archive.org/download/ElephantsDream/ed_1024_512kb.mp4",
                    videoUrl = "https://archive.org/download/ElephantsDream/ed_1024_512kb.mp4",
                    releaseYear = 2025,
                    genre = "Action",
                    durationMinutes = 98,
                    language = "English",
                    subtitles = "English, German, Japanese",
                    rating = 8.8,
                    cast = "Hjalti Hjalmarsson, Sarah Lawson, Kenji Sato",
                    director = "Hjalti Hjalmarsson",
                    isFeatured = true,
                    isTrending = true,
                    isTvShow = false,
                    isPublished = true,
                    viewCount = 0,
                    createdAt = System.currentTimeMillis() - 3L * 24 * 60 * 60 * 1000
                ),
                MovieEntity(
                    id = "movie_003",
                    title = "Big Buck Bunny: The Meadow",
                    description = "A gentle giant rabbit encounters mischievous forest bullies and devises a clever, hilarious series of traps to protect his peaceful woodland paradise.",
                    posterUrl = "https://images.unsplash.com/photo-1579202673506-ca3ce28943ef?w=600",
                    backdropUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=1200",
                    trailerUrl = "https://archive.org/download/BigBuckBunny_124/Content/big_buck_bunny_720p_surround.mp4",
                    videoUrl = "https://archive.org/download/BigBuckBunny_124/Content/big_buck_bunny_720p_surround.mp4",
                    releaseYear = 2024,
                    genre = "Animation",
                    durationMinutes = 75,
                    language = "English",
                    subtitles = "English, Spanish, Portuguese, Italian",
                    rating = 8.7,
                    cast = "Sacha Goedegebure, Jan Morgenstern",
                    director = "Sacha Goedegebure",
                    isFeatured = false,
                    isTrending = true,
                    isTvShow = false,
                    isPublished = true,
                    viewCount = 0,
                    createdAt = System.currentTimeMillis() - 8L * 24 * 60 * 60 * 1000
                ),
                MovieEntity(
                    id = "movie_004",
                    title = "Sintel: The Dragon's Oath",
                    description = "A lone warrior girl discovers a wounded baby dragon and embarks on a dangerous pilgrimage across snow-capped peaks and desolate deserts to save him.",
                    posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600",
                    backdropUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200",
                    trailerUrl = "https://archive.org/download/Sintel/sintel-2048-surround.mp4",
                    videoUrl = "https://archive.org/download/Sintel/sintel-2048-surround.mp4",
                    releaseYear = 2023,
                    genre = "Drama",
                    durationMinutes = 110,
                    language = "English",
                    subtitles = "English, Dutch, Spanish",
                    rating = 8.9,
                    cast = "Halina Reijn, Thom Hoffman",
                    director = "Colin Levy",
                    isFeatured = true,
                    isTrending = false,
                    isTvShow = false,
                    isPublished = true,
                    viewCount = 0,
                    createdAt = System.currentTimeMillis() - 15L * 24 * 60 * 60 * 1000
                ),
                MovieEntity(
                    id = "movie_005",
                    title = "Cosmos Laundromat: First Cycle",
                    description = "On a desolate windswept island, a melancholy sheep meets an eccentric salesman who offers him a mysterious portal into infinite lives and dimensions.",
                    posterUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600",
                    backdropUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1200",
                    trailerUrl = "https://archive.org/download/CosmosLaundromatFirstCycle/Cosmos%20Laundromat%20-%20First%20Cycle%20%281080p%29.mp4",
                    videoUrl = "https://archive.org/download/CosmosLaundromatFirstCycle/Cosmos%20Laundromat%20-%20First%20Cycle%20%281080p%29.mp4",
                    releaseYear = 2024,
                    genre = "Comedy",
                    durationMinutes = 88,
                    language = "English",
                    subtitles = "English, French",
                    rating = 8.4,
                    cast = "Pierre Bokma, Reinout Scholten van Aschat",
                    director = "Mathieu Auvray",
                    isFeatured = false,
                    isTrending = true,
                    isTvShow = false,
                    isPublished = true,
                    viewCount = 0,
                    createdAt = System.currentTimeMillis() - 4L * 24 * 60 * 60 * 1000
                ),
                MovieEntity(
                    id = "movie_006",
                    title = "Midnight Shadows",
                    description = "An antique mirror unearthed in an abandoned Victorian estate harbors an ancient whisper that turns darkness into a waking nightmare for anyone trapped inside.",
                    posterUrl = "https://images.unsplash.com/photo-1509281373149-e957c6296406?w=600",
                    backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200",
                    trailerUrl = "https://archive.org/download/Tears-of-Steel/tears_of_steel_720p.mp4",
                    videoUrl = "https://archive.org/download/Tears-of-Steel/tears_of_steel_720p.mp4",
                    releaseYear = 2025,
                    genre = "Horror",
                    durationMinutes = 104,
                    language = "English",
                    subtitles = "English, Spanish",
                    rating = 8.1,
                    cast = "Elena Rostova, Marcus Thorne",
                    director = "Guillermo Vance",
                    isFeatured = false,
                    isTrending = false,
                    isTvShow = false,
                    isPublished = true,
                    viewCount = 0,
                    createdAt = System.currentTimeMillis() - 2L * 24 * 60 * 60 * 1000
                ),
                MovieEntity(
                    id = "movie_007",
                    title = "Autumn in Paris",
                    description = "Two musicians cross paths under the golden foliage of the Seine and compose an unforgettable love letter across seasons, time zones, and heartbreaks.",
                    posterUrl = "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=600",
                    backdropUrl = "https://images.unsplash.com/photo-1499856871958-5b9627545d1a?w=1200",
                    trailerUrl = "https://archive.org/download/ElephantsDream/ed_1024_512kb.mp4",
                    videoUrl = "https://archive.org/download/ElephantsDream/ed_1024_512kb.mp4",
                    releaseYear = 2024,
                    genre = "Romance",
                    durationMinutes = 115,
                    language = "English",
                    subtitles = "English, French, Italian",
                    rating = 8.6,
                    cast = "Camille Laurent, Julian Croft",
                    director = "Sophie Moreau",
                    isFeatured = false,
                    isTrending = true,
                    isTvShow = false,
                    isPublished = true,
                    viewCount = 0,
                    createdAt = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
                ),
                MovieEntity(
                    id = "movie_008",
                    title = "Chronicles of Aethelgard",
                    description = "An epic fantasy TV series detailing five rival kingdoms competing for control of the Celestial Keystone as ancient winter leviathans awaken from the northern depths.",
                    posterUrl = "https://images.unsplash.com/photo-1514533450685-4493e01d1fdc?w=600",
                    backdropUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=1200",
                    trailerUrl = "https://archive.org/download/BigBuckBunny_124/Content/big_buck_bunny_720p_surround.mp4",
                    videoUrl = "https://archive.org/download/BigBuckBunny_124/Content/big_buck_bunny_720p_surround.mp4",
                    releaseYear = 2025,
                    genre = "TV Shows",
                    durationMinutes = 52,
                    language = "English",
                    subtitles = "English, Spanish, German, Japanese",
                    rating = 9.4,
                    cast = "Alistair Finch, Rowan Drake, Lyra Sterling",
                    director = "Catherine Vance",
                    isFeatured = true,
                    isTrending = true,
                    isTvShow = true,
                    isPublished = true,
                    viewCount = 0,
                    createdAt = System.currentTimeMillis() - 1L * 24 * 60 * 60 * 1000
                )
            )

            db.movieDao().insertMovies(movies)

            // Seed TV Episodes for Chronicles of Aethelgard
            val episodes = listOf(
                TvEpisodeEntity(
                    id = "ep_001",
                    tvShowId = "movie_008",
                    seasonNumber = 1,
                    episodeNumber = 1,
                    title = "Winter's Awakening",
                    description = "Lord Sterling receives word of an unexplainable celestial disturbance across the northern frontier, while Princess Lyra uncovers a forgotten cipher.",
                    videoUrl = "https://archive.org/download/Tears-of-Steel/tears_of_steel_720p.mp4",
                    durationMinutes = 54,
                    isPublished = true
                ),
                TvEpisodeEntity(
                    id = "ep_002",
                    tvShowId = "movie_008",
                    seasonNumber = 1,
                    episodeNumber = 2,
                    title = "The Obsidian Citadel",
                    description = "The rival kingdoms convene in the iron fortress of Kar-Dun. Tensions boil when an assassin strikes within the council chambers.",
                    videoUrl = "https://archive.org/download/ElephantsDream/ed_1024_512kb.mp4",
                    durationMinutes = 49,
                    isPublished = true
                ),
                TvEpisodeEntity(
                    id = "ep_003",
                    tvShowId = "movie_008",
                    seasonNumber = 1,
                    episodeNumber = 3,
                    title = "Flames of the Vanguard",
                    description = "The dragon vanguard rallies at the Sunken Pass as Rowan Drake risks everything to rescue the captive rangers.",
                    videoUrl = "https://archive.org/download/Sintel/sintel-2048-surround.mp4",
                    durationMinutes = 56,
                    isPublished = true
                ),
                TvEpisodeEntity(
                    id = "ep_004",
                    tvShowId = "movie_008",
                    seasonNumber = 2,
                    episodeNumber = 1,
                    title = "Reckoning of Kings",
                    description = "Season 2 Premiere: A year after the siege, a new emperor ascends the throne with a terrifying technological secret.",
                    videoUrl = "https://archive.org/download/BigBuckBunny_124/Content/big_buck_bunny_720p_surround.mp4",
                    durationMinutes = 58,
                    isPublished = true
                )
            )

            db.tvEpisodeDao().insertEpisodes(episodes)

            // Seed System Notification
            db.notificationDao().insertNotification(
                NotificationEntity(
                    id = "notif_001",
                    userId = "ALL",
                    title = "Welcome to HangisWatch!",
                    message = "Enjoy cinema and original TV series streaming on demand.",
                    type = "SYSTEM",
                    isRead = false,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }
}
