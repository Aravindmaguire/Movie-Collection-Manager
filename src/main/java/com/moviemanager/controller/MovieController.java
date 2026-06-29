package com.moviemanager.controller;

import com.moviemanager.entity.Movie;
import com.moviemanager.service.MovieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * WHAT IS THIS CLASS?
 * -------------------
 * MovieController is the REST API LAYER — the entry point for all HTTP requests.
 * When a browser or Postman sends a request to http://localhost:8080/api/movies,
 * this class receives it, calls the Service layer, and returns a JSON response.
 * 
 * WHY DO WE NEED IT?
 * ------------------
 * Without a Controller, our Service and Repository exist but nobody can access them.
 * The Controller EXPOSES our business logic as HTTP endpoints that the outside world can call.
 * 
 * HOW DOES IT COMMUNICATE WITH OTHER LAYERS?
 * -------------------------------------------
 *   Browser/Postman → Controller (we are here) → Service → Repository → Database
 * 
 *   The Controller:
 *     - RECEIVES HTTP requests (GET, POST, PUT, DELETE)
 *     - CALLS Service methods (delegates all logic)
 *     - RETURNS HTTP responses (JSON + status code)
 *     - Contains ZERO business logic (that's the Service's job)
 * 
 * ANNOTATIONS EXPLAINED:
 * 
 * @RestController combines TWO annotations:
 *   @Controller    → Marks this as a Spring MVC controller (handles HTTP requests)
 *   @ResponseBody  → Tells Spring to convert return values to JSON automatically
 *   
 *   So instead of returning HTML pages, every method returns JSON data.
 *   Spring uses Jackson library to convert Java objects → JSON automatically.
 * 
 * @RequestMapping("/api/movies"):
 *   Sets the BASE URL for all endpoints in this class.
 *   Every @GetMapping, @PostMapping, etc. builds on top of this base.
 *   Example: @GetMapping("/{id}") → full URL is /api/movies/{id}
 * 
 * @CrossOrigin("*"):
 *   Allows requests from ANY origin (any website/port).
 *   Without this, if your frontend runs on port 5500 and backend on port 8080,
 *   the browser would BLOCK the request due to CORS (Cross-Origin Resource Sharing) policy.
 *   In production, you'd restrict this to your specific frontend domain.
 */
@RestController
@RequestMapping("/api/movies")
@CrossOrigin("*")
public class MovieController {

    // ========================
    // DEPENDENCY
    // ========================

    /**
     * The Service layer that contains all business logic.
     * Injected via Constructor Dependency Injection (same pattern as in MovieService).
     */
    private final MovieService movieService;

    // ========================
    // CONSTRUCTOR DEPENDENCY INJECTION
    // ========================

    /**
     * Spring injects the MovieService instance automatically.
     * 
     * The full injection chain at startup:
     *   1. Spring creates MovieRepository (auto-generated implementation)
     *   2. Spring creates MovieService(movieRepository) ← injects repo
     *   3. Spring creates MovieController(movieService) ← injects service (we are here)
     * 
     * All three layers are now wired together automatically!
     */
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    // ===================================================================
    // CRUD ENDPOINTS
    // ===================================================================

    // ---------- CREATE ----------

    /**
     * ADD A NEW MOVIE
     * 
     * HTTP Method: POST
     * URL:         http://localhost:8080/api/movies
     * Request Body: JSON representing the movie
     * 
     * Example request (from browser/Postman):
     *   POST /api/movies
     *   Content-Type: application/json
     *   {
     *     "title": "Inception",
     *     "director": "Christopher Nolan",
     *     "genre": "Sci-Fi",
     *     "releaseYear": 2010,
     *     "rating": 9,
     *     "watched": true
     *   }
     * 
     * @PostMapping tells Spring: "When someone sends a POST request to /api/movies, call this method"
     * 
     * @RequestBody tells Spring:
     *   "Take the JSON from the request body and convert it into a Movie Java object"
     *   This is done automatically by the Jackson library:
     *     JSON {"title": "Inception"} → movie.setTitle("Inception")
     *     JSON {"rating": 9}          → movie.setRating(9)
     * 
     * ResponseEntity<Movie>:
     *   A wrapper that lets us return BOTH the data AND a custom HTTP status code.
     *   ResponseEntity.status(201).body(movie) means:
     *     - Status: 201 CREATED (tells the client "a new resource was created")
     *     - Body: The saved movie object as JSON (now includes the auto-generated id)
     * 
     * @param movie the Movie object created from the request JSON
     * @return the saved Movie with HTTP 201 CREATED status
     */
    @PostMapping
    public ResponseEntity<Movie> addMovie(@RequestBody Movie movie) {
        Movie savedMovie = movieService.addMovie(movie);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMovie);
    }

    // ---------- READ (All) ----------

    /**
     * GET ALL MOVIES
     * 
     * HTTP Method: GET
     * URL:         http://localhost:8080/api/movies
     * 
     * @GetMapping tells Spring: "When someone sends a GET request to /api/movies, call this method"
     * 
     * Since there's no path in @GetMapping, it uses the base path /api/movies.
     * 
     * ResponseEntity.ok(movies) is a shortcut for:
     *   ResponseEntity.status(HttpStatus.OK).body(movies)
     *   Status 200 OK = "Here's your data, everything went well"
     * 
     * Spring automatically converts List<Movie> → JSON array:
     *   [
     *     {"id": 1, "title": "Inception", ...},
     *     {"id": 2, "title": "The Matrix", ...}
     *   ]
     * 
     * @return list of all movies with HTTP 200 OK status
     */
    @GetMapping
    public ResponseEntity<List<Movie>> getAllMovies() {
        List<Movie> movies = movieService.getAllMovies();
        return ResponseEntity.ok(movies);
    }

    // ---------- READ (By ID) ----------

    /**
     * GET A SINGLE MOVIE BY ID
     * 
     * HTTP Method: GET
     * URL:         http://localhost:8080/api/movies/5
     * 
     * @GetMapping("/{id}"):
     *   {id} is a PATH VARIABLE — a placeholder in the URL.
     *   When someone requests /api/movies/5, Spring extracts "5" from the URL.
     * 
     * @PathVariable Long id:
     *   Tells Spring: "Take the {id} from the URL and assign it to this parameter"
     *   /api/movies/5   → id = 5
     *   /api/movies/42  → id = 42
     * 
     * @param id the movie ID extracted from the URL path
     * @return the Movie object with HTTP 200 OK status
     */
    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable Long id) {
        Movie movie = movieService.getMovieById(id);
        return ResponseEntity.ok(movie);
    }

    // ---------- UPDATE ----------

    /**
     * UPDATE AN EXISTING MOVIE
     * 
     * HTTP Method: PUT
     * URL:         http://localhost:8080/api/movies/5
     * Request Body: JSON with updated movie data
     * 
     * This combines TWO annotations:
     *   @PathVariable → Gets the movie ID from the URL (/api/movies/5 → id = 5)
     *   @RequestBody  → Gets the updated movie data from the JSON body
     * 
     * Example request:
     *   PUT /api/movies/5
     *   Content-Type: application/json
     *   {
     *     "title": "Inception (Director's Cut)",
     *     "director": "Christopher Nolan",
     *     "genre": "Sci-Fi",
     *     "releaseYear": 2010,
     *     "rating": 10,
     *     "watched": true
     *   }
     * 
     * @param id the ID of the movie to update (from URL)
     * @param movie the updated movie data (from request body JSON)
     * @return the updated Movie with HTTP 200 OK status
     */
    @PutMapping("/{id}")
    public ResponseEntity<Movie> updateMovie(@PathVariable Long id, @RequestBody Movie movie) {
        Movie updatedMovie = movieService.updateMovie(id, movie);
        return ResponseEntity.ok(updatedMovie);
    }

    // ---------- DELETE ----------

    /**
     * DELETE A MOVIE
     * 
     * HTTP Method: DELETE
     * URL:         http://localhost:8080/api/movies/5
     * 
     * ResponseEntity.noContent().build():
     *   Returns HTTP 204 NO CONTENT — meaning:
     *   "The operation was successful, but there's nothing to send back"
     *   (The movie is deleted, so there's no data to return)
     * 
     * .build() creates the ResponseEntity without a body (since we return Void/nothing).
     * 
     * @param id the ID of the movie to delete (from URL)
     * @return empty response with HTTP 204 NO CONTENT status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    // ===================================================================
    // SEARCH, FILTER & SORT ENDPOINTS
    // ===================================================================

    /**
     * SEARCH MOVIES BY TITLE
     * 
     * HTTP Method: GET
     * URL:         http://localhost:8080/api/movies/search?title=dark
     * 
     * @RequestParam:
     *   Reads a QUERY PARAMETER from the URL.
     *   Query parameters come after the "?" in a URL.
     *   
     *   URL: /api/movies/search?title=dark
     *                           ↑          ↑
     *                      param name   param value
     *   
     *   Spring extracts: title = "dark"
     * 
     * The difference between @PathVariable and @RequestParam:
     *   @PathVariable: /api/movies/5        → part of the URL path
     *   @RequestParam: /api/movies?title=dark → a query parameter (after ?)
     * 
     * @param title the search keyword from the query parameter
     * @return list of movies whose titles contain the keyword
     */
    @GetMapping("/search")
    public ResponseEntity<List<Movie>> searchByTitle(@RequestParam String title) {
        List<Movie> movies = movieService.searchByTitle(title);
        return ResponseEntity.ok(movies);
    }

    /**
     * FILTER MOVIES BY GENRE
     * 
     * HTTP Method: GET
     * URL:         http://localhost:8080/api/movies/genre?genre=Action
     * 
     * @param genre the genre to filter by (from query parameter)
     * @return list of movies in the specified genre
     */
    @GetMapping("/genre")
    public ResponseEntity<List<Movie>> getMoviesByGenre(@RequestParam String genre) {
        List<Movie> movies = movieService.getMoviesByGenre(genre);
        return ResponseEntity.ok(movies);
    }

    /**
     * FILTER MOVIES BY WATCHED STATUS
     * 
     * HTTP Method: GET
     * URLs:        http://localhost:8080/api/movies/watched?watched=true
     *              http://localhost:8080/api/movies/watched?watched=false
     * 
     * Spring automatically converts the String "true"/"false" to boolean.
     * 
     * @param watched true for watched movies, false for unwatched
     * @return list of movies matching the watched status
     */
    @GetMapping("/watched")
    public ResponseEntity<List<Movie>> getMoviesByWatchedStatus(@RequestParam boolean watched) {
        List<Movie> movies = movieService.getMoviesByWatchedStatus(watched);
        return ResponseEntity.ok(movies);
    }

    /**
     * SORT MOVIES BY RATING (highest first)
     * 
     * HTTP Method: GET
     * URL:         http://localhost:8080/api/movies/sort/rating
     * 
     * @return all movies sorted by rating in descending order
     */
    @GetMapping("/sort/rating")
    public ResponseEntity<List<Movie>> getMoviesSortedByRating() {
        List<Movie> movies = movieService.getMoviesSortedByRating();
        return ResponseEntity.ok(movies);
    }

    /**
     * SORT MOVIES BY RELEASE YEAR (newest first)
     * 
     * HTTP Method: GET
     * URL:         http://localhost:8080/api/movies/sort/year
     * 
     * @return all movies sorted by release year in descending order
     */
    @GetMapping("/sort/year")
    public ResponseEntity<List<Movie>> getMoviesSortedByReleaseYear() {
        List<Movie> movies = movieService.getMoviesSortedByReleaseYear();
        return ResponseEntity.ok(movies);
    }
}
