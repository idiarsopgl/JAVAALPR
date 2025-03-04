// Wait for the DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function() {
    // Add smooth scrolling for navigation links
    document.querySelectorAll('nav a').forEach(link => {
        link.addEventListener('click', function(e) {
            const href = this.getAttribute('href');
            if (href.startsWith('/')) {
                // Allow normal navigation for internal links
                return;
            }
            e.preventDefault();
            const target = document.querySelector(href);
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth'
                });
            }
        });
    });

    // Add table row hover effect
    const tableRows = document.querySelectorAll('table tbody tr');
    tableRows.forEach(row => {
        row.addEventListener('mouseover', function() {
            this.style.backgroundColor = '#f8f9fa';
        });
        row.addEventListener('mouseout', function() {
            this.style.backgroundColor = '';
        });
    });

    // Add responsive navigation menu toggle
    const navToggle = document.createElement('button');
    navToggle.className = 'nav-toggle';
    navToggle.innerHTML = '☰';
    const nav = document.querySelector('nav');
    
    if (window.innerWidth <= 768) {
        nav.parentNode.insertBefore(navToggle, nav);
        navToggle.addEventListener('click', function() {
            nav.classList.toggle('show');
        });
    }

    // Auto-update timestamps if present
    function updateTimestamps() {
        document.querySelectorAll('[data-timestamp]').forEach(element => {
            const timestamp = element.getAttribute('data-timestamp');
            const date = new Date(timestamp);
            element.textContent = date.toLocaleString();
        });
    }
    
    // Update timestamps every minute
    updateTimestamps();
    setInterval(updateTimestamps, 60000);
});