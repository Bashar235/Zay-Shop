$(document).ready(function() {
    $('.btn-danger').click(function(e) {
        e.preventDefault();
        if (confirm('Are you sure you want to delete this product?')) {
            // Add delete functionality here
            alert('Delete functionality to be implemented');
        }
    });
});