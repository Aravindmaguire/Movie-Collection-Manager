/**
 * ==========================================================================
 * SCRIPT.JS - The Frontend Logic
 * ==========================================================================
 * This file connects our HTML interface to our Spring Boot backend.
 * It uses the built-in Fetch API to send HTTP requests to our REST endpoints.
 */

// ==========================================================================
// CONSTANTS & GLOBAL VARIABLES
// ==========================================================================
// Base URL for our Spring Boot backend API
const API_URL = 'http://localhost:8080/api/movies';

// DOM Elements - We grab all the HTML elements we need to interact with
const moviesGrid = document.getElementById('movies-grid');
const emptyState = document.getElementById('empty-state');
const loadingContainer = document.getElementById('loading-container');
const toast = document.getElementById('toast');
const toastMessage = document.getElementById('toast-message');
const toastIcon = document.getElementById('toast-icon');

// Modals
const addModal = document.getElementById('add-modal-overlay');
const editModal = document.getElementById('edit-modal-overlay');
const deleteModal = document.getElementById('delete-modal-overlay');

// Forms
const addForm = document.getElementById('add-movie-form');
const editForm = document.getElementById('edit-movie-form');

// Controls
const searchInput = document.getElementById('search-input');
const genreFilter = document.getElementById('genre-filter');
const watchedFilter = document.getElementById('watched-filter');
const sortSelect = document.getElementById('sort-select');

// ==========================================================================
// INITIALIZATION
// ==========================================================================
// This runs as soon as the page loads. It fetches the initial list of movies.
document.addEventListener('DOMContentLoaded', () => {
    fetchMovies(); // Load all movies initially
    setupEventListeners(); // Attach all click/submit listeners
});

// ==========================================================================
// FETCH API FUNCTIONS (Talking to Spring Boot)
// ==========================================================================

/**
 * FETCH ALL MOVIES (GET request)
 * Uses async/await to handle the asynchronous network request gracefully.
 */
async function fetchMovies() {
    showLoading();
    try {
        // fetch() sends a GET request by default
        const response = await fetch(API_URL);
        
        if (!response.ok) throw new Error('Failed to fetch movies');
        
        // Parse the JSON response body into a JavaScript array of objects
        const movies = await response.json();
        
        // Give it a tiny delay just so the loading spinner is visible (looks nice)
        setTimeout(() => {
            hideLoading();
            renderMovies(movies);
        }, 300);
        
    } catch (error) {
        console.error('Error fetching movies:', error);
        hideLoading();
        showToast('Failed to load movies. Is the backend running?', 'error');
    }
}

/**
 * ADD A MOVIE (POST request)
 */
async function addMovie(movieData) {
    try {
        const response = await fetch(API_URL, {
            method: 'POST', // We are creating data
            headers: {
                'Content-Type': 'application/json' // Tell backend we're sending JSON
            },
            body: JSON.stringify(movieData) // Convert JS object to JSON string
        });

        if (!response.ok) throw new Error('Failed to add movie');

        closeModal(addModal);
        addForm.reset(); // Clear the form fields
        showToast('Movie added successfully!', 'success');
        fetchMovies(); // Refresh the list

    } catch (error) {
        console.error('Error adding movie:', error);
        showToast('Failed to add movie', 'error');
    }
}

/**
 * UPDATE A MOVIE (PUT request)
 */
async function updateMovie(id, movieData) {
    try {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'PUT', // We are updating data
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(movieData)
        });

        if (!response.ok) throw new Error('Failed to update movie');

        closeModal(editModal);
        showToast('Movie updated successfully!', 'success');
        fetchMovies(); // Refresh the list

    } catch (error) {
        console.error('Error updating movie:', error);
        showToast('Failed to update movie', 'error');
    }
}

/**
 * DELETE A MOVIE (DELETE request)
 */
async function deleteMovie(id) {
    try {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE' // We are deleting data
        });

        if (!response.ok) throw new Error('Failed to delete movie');

        closeModal(deleteModal);
        showToast('Movie deleted successfully!', 'success');
        fetchMovies(); // Refresh the list

    } catch (error) {
        console.error('Error deleting movie:', error);
        showToast('Failed to delete movie', 'error');
    }
}

/**
 * SEARCH AND FILTER (GET requests with query parameters)
 */
async function applyFiltersAndSearch() {
    const searchTerm = searchInput.value.trim();
    const genre = genreFilter.value;
    const watched = watchedFilter.value;
    const sortBy = sortSelect.value;

    showLoading();
    try {
        let url = API_URL;

        // Determine which endpoint to call based on the controls
        // Note: For simplicity in this beginner project, we are filtering one at a time.
        // (A more advanced backend would handle all filters in a single query).
        if (searchTerm) {
            url = `${API_URL}/search?title=${encodeURIComponent(searchTerm)}`;
        } else if (genre) {
            url = `${API_URL}/genre?genre=${encodeURIComponent(genre)}`;
        } else if (watched !== "") {
            url = `${API_URL}/watched?watched=${watched}`;
        } else if (sortBy === "rating") {
            url = `${API_URL}/sort/rating`;
        } else if (sortBy === "year") {
            url = `${API_URL}/sort/year`;
        }

        const response = await fetch(url);
        if (!response.ok) throw new Error('Failed to fetch filtered movies');
        
        const movies = await response.json();
        hideLoading();
        renderMovies(movies);

    } catch (error) {
        console.error('Error applying filters:', error);
        hideLoading();
        showToast('Failed to apply filters', 'error');
    }
}

// ==========================================================================
// DOM MANIPULATION & RENDERING
// ==========================================================================

/**
 * Renders the array of movies as HTML cards in the grid.
 */
function renderMovies(movies) {
    // Clear current grid content
    moviesGrid.innerHTML = '';

    // Show empty state if no movies
    if (movies.length === 0) {
        moviesGrid.style.display = 'none';
        emptyState.style.display = 'block';
        return;
    }

    // Hide empty state and show grid
    emptyState.style.display = 'none';
    moviesGrid.style.display = 'grid';

    // Loop through each movie and create its HTML structure dynamically
    movies.forEach(movie => {
        const card = document.createElement('div');
        card.className = 'movie-card';

        // Generate star rating string (e.g., ⭐⭐⭐⭐)
        const stars = movie.rating ? '⭐'.repeat(movie.rating) : 'No rating yet';
        
        // Format the watched badge
        const badgeClass = movie.watched ? 'badge-watched' : 'badge-unwatched';
        const badgeText = movie.watched ? 'Watched ✅' : 'Unwatched ⬜';

        card.innerHTML = `
            <div class="movie-card-header">
                <span class="badge ${badgeClass}">${badgeText}</span>
            </div>
            <h3 class="movie-title" title="${movie.title}">${movie.title}</h3>
            <p class="movie-director">${movie.director}</p>
            
            <div class="movie-details">
                <span class="movie-genre">${movie.genre || 'Unknown Genre'}</span>
                <span class="movie-year">${movie.releaseYear || 'Unknown Year'}</span>
            </div>
            
            <div class="movie-rating">${stars}</div>
            
            <div class="movie-actions">
                <button class="btn btn-secondary btn-small" onclick="openEditModal(${movie.id})">Edit</button>
                <button class="btn btn-danger btn-small" onclick="openDeleteModal(${movie.id}, '${movie.title.replace(/'/g, "\\'")}')">Delete</button>
            </div>
        `;

        moviesGrid.appendChild(card);
    });
}

// ==========================================================================
// MODAL MANAGEMENT
// ==========================================================================

function openModal(modalOverlay) {
    modalOverlay.classList.add('active');
}

function closeModal(modalOverlay) {
    modalOverlay.classList.remove('active');
}

// Specific open functions for Edit/Delete because they need to load data first

async function openEditModal(id) {
    try {
        // Fetch the specific movie details to populate the form
        const response = await fetch(`${API_URL}/${id}`);
        if (!response.ok) throw new Error('Failed to fetch movie details');
        const movie = await response.json();

        // Populate the edit form fields
        document.getElementById('edit-movie-id').value = movie.id;
        document.getElementById('edit-title').value = movie.title;
        document.getElementById('edit-director').value = movie.director;
        document.getElementById('edit-genre').value = movie.genre || '';
        document.getElementById('edit-year').value = movie.releaseYear || '';
        document.getElementById('edit-rating').value = movie.rating || '';
        document.getElementById('edit-watched').checked = movie.watched;

        openModal(editModal);
    } catch (error) {
        console.error('Error opening edit modal:', error);
        showToast('Failed to load movie details', 'error');
    }
}

function openDeleteModal(id, title) {
    // Set the hidden input and the title in the message
    document.getElementById('delete-movie-id').value = id;
    document.getElementById('delete-movie-title').textContent = title;
    openModal(deleteModal);
}

// ==========================================================================
// EVENT LISTENERS SETUP
// ==========================================================================

function setupEventListeners() {
    
    // --- Modals (Opening) ---
    document.getElementById('add-movie-btn').addEventListener('click', () => openModal(addModal));
    document.getElementById('empty-state-add-btn').addEventListener('click', () => openModal(addModal));

    // --- Modals (Closing) ---
    // Close buttons inside modals
    document.getElementById('add-modal-close').addEventListener('click', () => closeModal(addModal));
    document.getElementById('edit-modal-close').addEventListener('click', () => closeModal(editModal));
    document.getElementById('delete-modal-close').addEventListener('click', () => closeModal(deleteModal));
    document.getElementById('delete-cancel-btn').addEventListener('click', () => closeModal(deleteModal));

    // Close when clicking the dark overlay outside the modal box
    [addModal, editModal, deleteModal].forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) closeModal(modal); // Only close if clicking exactly on the overlay
        });
    });

    // --- Form Submissions ---
    addForm.addEventListener('submit', (e) => {
        e.preventDefault(); // Prevents the default page reload
        
        // Gather data from the form
        const movieData = {
            title: document.getElementById('add-title').value,
            director: document.getElementById('add-director').value,
            genre: document.getElementById('add-genre').value || null,
            releaseYear: document.getElementById('add-year').value ? parseInt(document.getElementById('add-year').value) : null,
            rating: document.getElementById('add-rating').value ? parseInt(document.getElementById('add-rating').value) : null,
            watched: document.getElementById('add-watched').checked
        };
        
        addMovie(movieData);
    });

    editForm.addEventListener('submit', (e) => {
        e.preventDefault();
        
        const id = document.getElementById('edit-movie-id').value;
        const movieData = {
            title: document.getElementById('edit-title').value,
            director: document.getElementById('edit-director').value,
            genre: document.getElementById('edit-genre').value || null,
            releaseYear: document.getElementById('edit-year').value ? parseInt(document.getElementById('edit-year').value) : null,
            rating: document.getElementById('edit-rating').value ? parseInt(document.getElementById('edit-rating').value) : null,
            watched: document.getElementById('edit-watched').checked
        };
        
        updateMovie(id, movieData);
    });

    document.getElementById('delete-confirm-btn').addEventListener('click', () => {
        const id = document.getElementById('delete-movie-id').value;
        deleteMovie(id);
    });

    // --- Search & Filters ---
    // We add listeners so that when a user types or selects a dropdown, it updates instantly
    searchInput.addEventListener('input', () => {
        // Debouncing logic (optional advanced concept):
        // Only search after they stop typing for 300ms to avoid spamming the backend
        clearTimeout(window.searchTimeout);
        window.searchTimeout = setTimeout(applyFiltersAndSearch, 300);
    });

    genreFilter.addEventListener('change', applyFiltersAndSearch);
    watchedFilter.addEventListener('change', applyFiltersAndSearch);
    sortSelect.addEventListener('change', applyFiltersAndSearch);
}

// ==========================================================================
// UTILITY FUNCTIONS (UI helpers)
// ==========================================================================

function showLoading() {
    loadingContainer.style.display = 'flex';
    moviesGrid.style.display = 'none';
    emptyState.style.display = 'none';
}

function hideLoading() {
    loadingContainer.style.display = 'none';
}

/**
 * Displays a non-intrusive popup message at the top right of the screen
 * @param {string} message - The text to display
 * @param {string} type - 'success' or 'error' (changes the color and icon)
 */
function showToast(message, type = 'success') {
    toastMessage.textContent = message;
    
    if (type === 'error') {
        toast.classList.add('error');
        toastIcon.textContent = '❌';
    } else {
        toast.classList.remove('error');
        toastIcon.textContent = '✅';
    }
    
    // Slide it in
    toast.classList.add('show');
    
    // Slide it out after 3 seconds
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}
