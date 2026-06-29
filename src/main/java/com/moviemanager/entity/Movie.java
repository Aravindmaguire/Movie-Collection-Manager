package com.moviemanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * WHAT IS THIS CLASS?
 * -------------------
 * This is our Entity class. It represents the "movies" table in the MySQL database.
 * Each INSTANCE of this class (each Movie object) represents ONE ROW in the table.
 * Each FIELD in this class represents ONE COLUMN in the table.
 * 
 * WHY DO WE NEED IT?
 * ------------------
 * Without this class, we'd have to write raw SQL to create tables and insert data.
 * With JPA, we just define this class and Hibernate does the rest:
 *   - Creates the "movies" table automatically
 *   - Maps each field to a column
 *   - Handles INSERT, UPDATE, DELETE operations through Java objects
 * 
 * HOW DOES IT COMMUNICATE WITH OTHER LAYERS?
 * -------------------------------------------
 * Entity is the FOUNDATION layer. It doesn't call any other layer.
 * Instead, other layers USE it:
 *   - Repository saves/retrieves Movie objects to/from the database
 *   - Service uses Movie objects for business logic
 *   - Controller sends/receives Movie objects as JSON to/from the browser
 */

// @Entity tells JPA: "This class should be mapped to a database table."
// Without this annotation, JPA would completely ignore this class.
@Entity

// @Table lets us customize the table name.
// Without it, JPA would name the table "Movie" (same as class name).
// We want "movies" (lowercase, plural) which is a common database convention.
@Table(name = "movies")
public class Movie {

    // ========================
    // FIELDS (= Table Columns)
    // ========================

    /**
     * @Id marks this field as the PRIMARY KEY of the table.
     * Every database table MUST have a primary key — it uniquely identifies each row.
     * No two movies can have the same id.
     * 
     * @GeneratedValue tells Hibernate to auto-generate this value.
     * GenerationType.IDENTITY means the DATABASE handles auto-increment (1, 2, 3, ...).
     * We never set the id manually — MySQL assigns it automatically when we insert a row.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @Column lets us customize this column's properties.
     * - nullable = false → This column CANNOT be empty (like NOT NULL in SQL)
     * - length = 200     → Maximum 200 characters (default is 255)
     * 
     * If we didn't use @Column, JPA would still create the column,
     * but with default settings (nullable, 255 chars).
     */
    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String director;

    /**
     * For genre, we allow it to be nullable (a movie might not have a clear genre).
     * But we still specify a reasonable length.
     */
    @Column(length = 50)
    private String genre;

    /**
     * We use 'Integer' (wrapper class) instead of 'int' (primitive) because:
     * - 'int' cannot be null (its default is 0)
     * - 'Integer' CAN be null, which makes sense if we don't know the release year
     * - In databases, a missing value is NULL, not 0
     */
    @Column(name = "release_year")
    private Integer releaseYear;

    /**
     * Rating from 1 to 10.
     * We use 'Integer' so it can be null (an unrated movie).
     * We'll enforce the 1-10 range in the Service layer (business logic),
     * not here in the Entity (which only defines structure).
     */
    @Column
    private Integer rating;

    /**
     * Has the user watched this movie?
     * We use 'boolean' (primitive) with a default of false.
     * A new movie is "unwatched" by default — this makes sense logically.
     */
    @Column(nullable = false)
    private boolean watched = false;

    // ========================
    // CONSTRUCTORS
    // ========================

    /**
     * DEFAULT CONSTRUCTOR (no arguments)
     * 
     * WHY IS THIS REQUIRED?
     * JPA/Hibernate MUST have a no-arg constructor to create Movie objects
     * when reading from the database. Here's what happens internally:
     *   1. Hibernate runs a SELECT query
     *   2. Gets the raw data from MySQL
     *   3. Creates an empty Movie object using this constructor
     *   4. Fills in the fields using setter methods
     * Without this constructor, Hibernate would crash with an error!
     */
    public Movie() {
    }

    /**
     * PARAMETERIZED CONSTRUCTOR (with arguments)
     * 
     * This is for OUR convenience when creating Movie objects in code.
     * Notice: we do NOT include 'id' because the database generates it automatically.
     */
    public Movie(String title, String director, String genre,
                 Integer releaseYear, Integer rating, boolean watched) {
        this.title = title;
        this.director = director;
        this.genre = genre;
        this.releaseYear = releaseYear;
        this.rating = rating;
        this.watched = watched;
    }

    // ========================
    // GETTERS AND SETTERS
    // ========================

    /**
     * WHY DO WE NEED GETTERS AND SETTERS?
     * 
     * 1. ENCAPSULATION: The fields are 'private' — no other class can access them directly.
     *    Getters and setters provide controlled access.
     * 
     * 2. JPA NEEDS THEM: Hibernate uses getters to READ field values (when saving to DB)
     *    and setters to WRITE field values (when reading from DB).
     * 
     * 3. JACKSON NEEDS THEM: When Spring converts a Movie object to JSON (to send to the browser),
     *    the Jackson library uses getters to read each field.
     *    When Spring receives JSON from the browser, Jackson uses setters to create a Movie object.
     * 
     *    Movie object ──(getters)──→ JSON   (sending to browser)
     *    JSON         ──(setters)──→ Movie  (receiving from browser)
     */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }

    // ========================
    // toString METHOD
    // ========================

    /**
     * toString() returns a human-readable string representation of a Movie object.
     * 
     * This is useful for:
     * - Debugging: System.out.println(movie) will print something meaningful
     * - Logging: You can see movie details in your console logs
     * 
     * Without this, printing a Movie object would show something like:
     *   "com.moviemanager.entity.Movie@4a574795" (not useful at all!)
     * 
     * With this, it shows:
     *   "Movie{id=1, title='Inception', director='Christopher Nolan', ...}"
     */
    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", director='" + director + '\'' +
                ", genre='" + genre + '\'' +
                ", releaseYear=" + releaseYear +
                ", rating=" + rating +
                ", watched=" + watched +
                '}';
    }
}
