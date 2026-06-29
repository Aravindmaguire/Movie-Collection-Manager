package com.moviemanager.repository;

import com.moviemanager.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * WHAT IS THIS INTERFACE?
 * -----------------------
 * MovieRepository is the DATA ACCESS LAYER of our application.
 * It handles ALL communication with the MySQL database.
 * 
 * WHY IS IT AN INTERFACE (not a class)?
 * --------------------------------------
 * Spring Data JPA uses a technique called "proxy pattern."
 * At startup, Spring sees this interface and AUTOMATICALLY generates
 * a full implementation class with real code behind the scenes.
 * You never see this generated class, but it works perfectly.
 * 
 * WHY DO WE EXTEND JpaRepository?
 * --------------------------------
 * JpaRepository<Movie, Long> gives us:
 *   - Movie: The entity type this repository manages
 *   - Long:  The data type of the primary key (id field in Movie.java)
 * 
 * By extending it, we INHERIT all built-in CRUD methods:
 *   save(), findById(), findAll(), deleteById(), count(), existsById()
 * 
 * We don't write ANY implementation for these — they just work!
 * 
 * HOW DOES IT COMMUNICATE WITH OTHER LAYERS?
 * -------------------------------------------
 *   - Repository ←→ Database  (reads/writes data)
 *   - Service → Repository    (Service calls Repository methods)
 *   - Repository does NOT call Controller or Service (it's the bottom layer)
 * 
 * @Repository annotation:
 *   - Marks this as a Spring-managed component (Spring creates an instance for us)
 *   - Tells Spring: "This is a data access component"
 *   - Enables automatic translation of database exceptions into Spring's exception types
 *   - Note: @Repository is technically optional when extending JpaRepository
 *     (Spring auto-detects it), but we include it for clarity and best practice.
 */
@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    // =====================================================
    // CUSTOM QUERY METHODS (Derived from method names)
    // =====================================================

    // Spring reads method names and AUTOMATICALLY generates the SQL query.
    // This is called "Derived Query Methods" or "Query Derivation."
    // 
    // The naming pattern is:
    //   findBy + FieldName + Condition
    //
    // Examples of how Spring parses method names:
    //   findByGenre         → find... By... Genre         → WHERE genre = ?
    //   findByWatched       → find... By... Watched       → WHERE watched = ?
    //   findByTitleContaining → find... By... Title... Containing → WHERE title LIKE '%?%'

    /**
     * Find all movies that belong to a specific genre.
     * 
     * How Spring generates the query:
     *   Method name: findByGenre
     *   Spring reads: find → SELECT all | By → WHERE | Genre → genre column
     *   Generated SQL: SELECT * FROM movies WHERE genre = ?
     * 
     * Usage: movieRepository.findByGenre("Action")
     *   → Returns all Action movies
     * 
     * @param genre the genre to search for (e.g., "Action", "Comedy")
     * @return list of movies matching the genre
     */
    List<Movie> findByGenre(String genre);

    /**
     * Find all movies by their watched status.
     * 
     * How Spring generates the query:
     *   Method name: findByWatched
     *   Generated SQL: SELECT * FROM movies WHERE watched = ?
     * 
     * Usage: 
     *   movieRepository.findByWatched(true)  → Returns all watched movies
     *   movieRepository.findByWatched(false) → Returns all unwatched movies
     * 
     * @param watched true for watched movies, false for unwatched
     * @return list of movies matching the watched status
     */
    List<Movie> findByWatched(boolean watched);

    /**
     * Search movies by title (partial match, case-insensitive).
     * 
     * How Spring generates the query:
     *   Method name: findByTitleContainingIgnoreCase
     *   Spring reads: find → SELECT | By → WHERE | Title → title column
     *                 Containing → LIKE '%...%' | IgnoreCase → case-insensitive
     *   Generated SQL: SELECT * FROM movies WHERE LOWER(title) LIKE LOWER('%?%')
     * 
     * Usage: movieRepository.findByTitleContainingIgnoreCase("dark")
     *   → Returns "The Dark Knight", "Dark Waters", "IN THE DARK", etc.
     * 
     * Why "Containing"? → Matches if the search text appears ANYWHERE in the title
     * Why "IgnoreCase"? → "dark" matches "Dark", "DARK", "dArK", etc.
     * 
     * @param title the search keyword
     * @return list of movies whose titles contain the keyword
     */
    List<Movie> findByTitleContainingIgnoreCase(String title);

    /**
     * Get all movies sorted by rating in DESCENDING order (highest rated first).
     * 
     * How Spring generates the query:
     *   Method name: findAllByOrderByRatingDesc
     *   Spring reads: findAll → SELECT all | By → (no WHERE) | OrderBy → ORDER BY
     *                 Rating → rating column | Desc → DESCENDING
     *   Generated SQL: SELECT * FROM movies ORDER BY rating DESC
     * 
     * Usage: movieRepository.findAllByOrderByRatingDesc()
     *   → Returns [10-rated movie, 9-rated movie, 8-rated movie, ...]
     * 
     * @return all movies sorted by rating (highest first)
     */
    List<Movie> findAllByOrderByRatingDesc();

    /**
     * Get all movies sorted by release year in DESCENDING order (newest first).
     * 
     * How Spring generates the query:
     *   Method name: findAllByOrderByReleaseYearDesc
     *   Generated SQL: SELECT * FROM movies ORDER BY release_year DESC
     * 
     * Usage: movieRepository.findAllByOrderByReleaseYearDesc()
     *   → Returns [2024 movie, 2023 movie, 2020 movie, ...]
     * 
     * @return all movies sorted by release year (newest first)
     */
    List<Movie> findAllByOrderByReleaseYearDesc();
}
