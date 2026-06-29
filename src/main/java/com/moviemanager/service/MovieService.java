package com.moviemanager.service;

import com.moviemanager.entity.Movie;
import com.moviemanager.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * WHAT IS THIS CLASS?
 * -------------------
 * MovieService is the BUSINESS LOGIC LAYER of our application.
 * It sits BETWEEN the Controller and the Repository:
 * 
 *   Controller → Service (we are here) → Repository → Database
 * 
 * WHY DO WE NEED IT?
 * ------------------
 * 1. VALIDATION: Ensures data is valid before saving to the database
 *    (e.g., rating must be 1-10, title cannot be empty)
 * 
 * 2. ERROR HANDLING: Handles cases like "movie not found" gracefully
 *    instead of letting the app crash
 * 
 * 3. SEPARATION OF CONCERNS: The Controller handles HTTP logic (requests/responses).
 *    The Service handles BUSINESS logic (rules, validation, decisions).
 *    The Repository handles DATABASE logic (queries, saves, deletes).
 * 
 * 4. REUSABILITY: If we later add a mobile app or another API,
 *    we can reuse the same Service without duplicating business logic.
 * 
 * HOW DOES IT COMMUNICATE WITH OTHER LAYERS?
 * -------------------------------------------
 *   Controller → calls Service methods (e.g., service.getAllMovies())
 *   Service → calls Repository methods (e.g., repository.findAll())
 *   Service does NOT know about HTTP, JSON, or the browser.
 *   Service does NOT write SQL or talk to the database directly.
 * 
 * @Service annotation:
 *   - Marks this class as a Spring-managed BEAN (Spring creates and manages this object)
 *   - Tells Spring: "This is a service component — find it during component scanning"
 *   - Functionally similar to @Component, but @Service makes the intent clearer
 */
@Service
public class MovieService {

    // ========================
    // DEPENDENCY
    // ========================

    /**
     * This is the Repository that we DEPEND on to access the database.
     * 
     * 'private final' means:
     *   - private: Only this class can access it (encapsulation)
     *   - final: Once assigned in the constructor, it can NEVER be changed
     *     This ensures the repository is always the same instance — no accidental replacement.
     */
    private final MovieRepository movieRepository;

    // ========================
    // CONSTRUCTOR DEPENDENCY INJECTION
    // ========================

    /**
     * CONSTRUCTOR DEPENDENCY INJECTION
     * 
     * Instead of creating MovieRepository ourselves (which is impossible for an interface anyway),
     * we let Spring INJECT it through the constructor.
     * 
     * HOW IT WORKS (step by step):
     *   1. When the app starts, Spring creates the MovieRepository implementation (auto-generated)
     *   2. Spring sees this MovieService class and wants to create it
     *   3. Spring looks at this constructor and sees it needs a MovieRepository
     *   4. Spring says: "I already have a MovieRepository! Let me pass it in."
     *   5. Spring creates: new MovieService(movieRepositoryInstance)
     *   6. Now our service has a working repository — ready to use!
     * 
     * WHY CONSTRUCTOR INJECTION (not field injection)?
     *   - @Autowired on fields works too, but constructor injection is the BEST PRACTICE because:
     *     a) Dependencies are clear — you see them in the constructor
     *     b) Fields can be 'final' — immutable and safe
     *     c) Easier to test — you can pass mock objects in tests
     *     d) Spring officially recommends constructor injection
     * 
     * NOTE: When a class has only ONE constructor, the @Autowired annotation is OPTIONAL.
     * Spring automatically uses it for injection. We skip @Autowired to keep the code clean.
     */
    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    // ========================
    // SERVICE METHODS
    // ========================
    // Each method below represents one BUSINESS OPERATION.
    // The Controller will call these methods.

    // ---------- CREATE ----------

    /**
     * Save a new movie to the database.
     * 
     * Business logic:
     *   - Validates that the rating is between 1 and 10 (if provided)
     *   - Delegates the actual saving to the Repository
     * 
     * @param movie the Movie object to save (received from Controller)
     * @return the saved Movie (now with an auto-generated id from the database)
     * 
     * How it works:
     *   1. Controller receives JSON from the browser → creates Movie object
     *   2. Controller calls this method → passes the Movie object
     *   3. This method validates the data
     *   4. This method calls repository.save(movie)
     *   5. Hibernate generates: INSERT INTO movies (...) VALUES (...)
     *   6. MySQL saves the movie and assigns an auto-incremented id
     *   7. The saved Movie (with id) is returned back through all layers to the browser
     */
    public Movie addMovie(Movie movie) {
        // Validate rating: must be between 1 and 10 (if provided)
        if (movie.getRating() != null && (movie.getRating() < 1 || movie.getRating() > 10)) {
            throw new RuntimeException("Rating must be between 1 and 10");
        }
        // repository.save() does an INSERT if the movie is new (no id),
        // or an UPDATE if the movie already has an id.
        return movieRepository.save(movie);
    }

    // ---------- READ (All) ----------

    /**
     * Get ALL movies from the database.
     * 
     * @return a List of all Movie objects
     * 
     * Calls repository.findAll() which generates:
     *   SELECT * FROM movies
     */
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    // ---------- READ (By ID) ----------

    /**
     * Get a single movie by its ID.
     * 
     * This demonstrates OPTIONAL — one of the most important Java concepts for avoiding null errors.
     * 
     * repository.findById() returns Optional<Movie>, not Movie.
     * Why? Because the movie might NOT exist. Optional forces us to handle that case.
     * 
     * .orElseThrow() means:
     *   - If the movie exists → unwrap it and return the Movie object
     *   - If the movie doesn't exist → throw the exception we specify
     * 
     * Without Optional, we'd risk NullPointerException crashes.
     * With Optional, we handle the "not found" case explicitly.
     * 
     * @param id the movie's unique identifier
     * @return the Movie object if found
     * @throws RuntimeException if no movie exists with the given id
     */
    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
        // The lambda () -> new RuntimeException(...) creates the exception ONLY if needed.
        // If the movie is found, the exception is never created — efficient!
    }

    // ---------- UPDATE ----------

    /**
     * Update an existing movie's details.
     * 
     * IMPORTANT: We can't just save the new data — we must first CHECK if the movie exists.
     * Then we update EACH field individually to preserve the existing data.
     * 
     * Steps:
     *   1. Find the existing movie by ID (throws exception if not found)
     *   2. Update each field with the new values
     *   3. Save the updated movie (repository.save() does UPDATE because the movie has an id)
     *   4. Return the updated movie
     * 
     * @param id the ID of the movie to update
     * @param updatedMovie the new data to apply
     * @return the updated Movie object
     * @throws RuntimeException if no movie exists with the given id
     */
    public Movie updateMovie(Long id, Movie updatedMovie) {
        // Step 1: Find the existing movie (reusing our getMovieById method)
        Movie existingMovie = getMovieById(id);

        // Step 2: Validate the new rating (if provided)
        if (updatedMovie.getRating() != null && (updatedMovie.getRating() < 1 || updatedMovie.getRating() > 10)) {
            throw new RuntimeException("Rating must be between 1 and 10");
        }

        // Step 3: Update each field of the existing movie with new values
        // We update the EXISTING object (which has the correct id) rather than
        // saving the updatedMovie directly (which might not have an id)
        existingMovie.setTitle(updatedMovie.getTitle());
        existingMovie.setDirector(updatedMovie.getDirector());
        existingMovie.setGenre(updatedMovie.getGenre());
        existingMovie.setReleaseYear(updatedMovie.getReleaseYear());
        existingMovie.setRating(updatedMovie.getRating());
        existingMovie.setWatched(updatedMovie.isWatched());

        // Step 4: Save the updated movie
        // Since existingMovie already has an id, repository.save() generates:
        //   UPDATE movies SET title=?, director=?, ... WHERE id=?
        return movieRepository.save(existingMovie);
    }

    // ---------- DELETE ----------

    /**
     * Delete a movie by its ID.
     * 
     * We first check if the movie exists before deleting.
     * If we called deleteById() directly on a non-existent id,
     * it would either silently do nothing or throw an unclear error.
     * By checking first, we give a clear error message.
     * 
     * @param id the ID of the movie to delete
     * @throws RuntimeException if no movie exists with the given id
     */
    public void deleteMovie(Long id) {
        // Check if movie exists first (throws exception if not found)
        getMovieById(id);

        // If we reach this line, the movie exists — safe to delete
        // Generates: DELETE FROM movies WHERE id = ?
        movieRepository.deleteById(id);
    }

    // ---------- SEARCH & FILTER ----------

    /**
     * Search movies by title (partial match, case-insensitive).
     * 
     * Example: searchByTitle("dark") finds "The Dark Knight", "Dark Waters", etc.
     * 
     * @param title the search keyword
     * @return list of movies matching the keyword
     */
    public List<Movie> searchByTitle(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * Filter movies by genre.
     * 
     * @param genre the genre to filter by (e.g., "Action", "Comedy")
     * @return list of movies in the specified genre
     */
    public List<Movie> getMoviesByGenre(String genre) {
        return movieRepository.findByGenre(genre);
    }

    /**
     * Filter movies by watched status.
     * 
     * @param watched true → return watched movies, false → return unwatched movies
     * @return list of movies matching the watched status
     */
    public List<Movie> getMoviesByWatchedStatus(boolean watched) {
        return movieRepository.findByWatched(watched);
    }

    // ---------- SORT ----------

    /**
     * Get all movies sorted by rating (highest rated first).
     * 
     * @return list of movies ordered by rating descending
     */
    public List<Movie> getMoviesSortedByRating() {
        return movieRepository.findAllByOrderByRatingDesc();
    }

    /**
     * Get all movies sorted by release year (newest first).
     * 
     * @return list of movies ordered by release year descending
     */
    public List<Movie> getMoviesSortedByReleaseYear() {
        return movieRepository.findAllByOrderByReleaseYearDesc();
    }
}
