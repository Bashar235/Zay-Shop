$(document).ready(function() {
    const usernameInput = $('#username');
    const emailInput = $('#email');
    const passwordInput = $('#password');
    const roleSelect = $('#role');
    const adminCodeGroup = $('#adminCodeGroup');
    const adminCodeInput = $('#adminCode');
    const form = $('#registerForm');
    const errorMessage = $('#errorMessage');
    const csrfHeader = $('meta[name="_csrf_header"]').attr('content') || 'X-CSRF-TOKEN';
    const csrfToken = $('meta[name="_csrf"]').attr('content') || '';

    console.log('register.js initialized');

    // Show/hide admin code input
    roleSelect.on('change', function() {
        if ($(this).val() === 'ROLE_ADMIN') {
            adminCodeGroup.show();
            adminCodeInput.prop('required', true);
        } else {
            adminCodeGroup.hide();
            adminCodeInput.prop('required', false).val('');
        }
    }).trigger('change');

    // Username validation
    usernameInput.on('input', debounce(function() {
        const username = $(this).val();
        if (username.length < 3) {
            usernameInput.addClass('is-invalid').removeClass('is-valid');
            $('#usernameFeedback').text('Username must be at least 3 characters');
            return;
        }
        $.ajax({
            url: '/user/check-username',
            data: { username: username },
            headers: { [csrfHeader]: csrfToken },
            success: function(isAvailable) {
                if (isAvailable) {
                    usernameInput.addClass('is-valid').removeClass('is-invalid');
                    $('#usernameFeedback').text('');
                } else {
                    usernameInput.addClass('is-invalid').removeClass('is-valid');
                    $('#usernameFeedback').text('Username is already taken');
                }
            },
            error: function(xhr) {
                console.error('Username check failed:', xhr);
                errorMessage.text('Error checking username availability');
            }
        });
    }, 500));

    // Email validation
    emailInput.on('input', debounce(function() {
        const email = $(this).val();
        if (!email.match(/^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/)) {
            emailInput.addClass('is-invalid').removeClass('is-valid');
            $('#emailFeedback').text('Invalid email format');
            return;
        }
        $.ajax({
            url: '/user/check-email',
            data: { email: email },
            headers: { [csrfHeader]: csrfToken },
            success: function(isAvailable) {
                if (isAvailable) {
                    emailInput.addClass('is-valid').removeClass('is-invalid');
                    $('#emailFeedback').text('');
                } else {
                    emailInput.addClass('is-invalid').removeClass('is-valid');
                    $('#emailFeedback').text('Email is already registered');
                }
            },
            error: function(xhr) {
                console.error('Email check failed:', xhr);
                errorMessage.text('Error checking email availability');
            }
        });
    }, 500));

    // Password strength
    passwordInput.on('input', function() {
        const password = $(this).val();
        let strength = 0;
        if (password.length >= 8) strength += 25;
        if (password.match(/[A-Z]/)) strength += 25;
        if (password.match(/[0-9]/)) strength += 25;
        if (password.match(/[@$!%*?&]/)) strength += 25;

        const strengthBar = $('#passwordStrength');
        strengthBar.css('width', strength + '%');
        if (strength < 50) {
            strengthBar.removeClass('bg-success bg-warning').addClass('bg-danger');
            $('#passwordFeedback').text('Password is weak');
            passwordInput.addClass('is-invalid');
        } else if (strength < 75) {
            strengthBar.removeClass('bg-danger bg-success').addClass('bg-warning');
            $('#passwordFeedback').text('Password is moderate');
            passwordInput.removeClass('is-invalid').addClass('is-valid');
        } else {
            strengthBar.removeClass('bg-danger bg-warning').addClass('bg-success');
            $('#passwordFeedback').text('Password is strong');
            passwordInput.removeClass('is-invalid').addClass('is-valid');
        }
    });

    // Form submission
    form.on('submit', function(e) {
        console.log('Form submission triggered');
        if (!form[0].checkValidity()) {
            e.preventDefault();
            form.addClass('was-validated');
            errorMessage.text('Please fill in all required fields correctly');
            return;
        }

        if (roleSelect.val() === 'ROLE_ADMIN' && !adminCodeInput.val()) {
            e.preventDefault();
            adminCodeInput.addClass('is-invalid');
            errorMessage.text('Admin registration code is required');
            return;
        }

        // Allow form to submit naturally (no AJAX)
        // The RegisterController will handle the submission
    });

    // Debounce function
    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }
});