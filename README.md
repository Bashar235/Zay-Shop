# Zay Shop Project

## Overview

Zay Shop is an e-commerce web application designed to provide a modern and user-friendly shopping experience. The project features reusable HTML fragments (e.g., top navigation, header, search modal, and footer) built with Thymeleaf, styled with custom CSS, and enhanced with JavaScript for interactivity. The design is minimalistic yet futuristic, incorporating gradients, glassmorphism effects, and smooth animations. It integrates with a backend (assumed to be Spring Boot) to handle dynamic content, authentication, and e-commerce functionalities.

This project is intended for developers looking to build or extend an e-commerce platform with a focus on reusable components and a responsive design.

## Features

- **Reusable Fragments**: Modular HTML components (top nav, header, search modal, footer) for easy integration into multiple pages.
- **Modern Design**: Utilizes gradients, glassmorphism, and animations for a futuristic aesthetic.
- **Responsive Layout**: Optimized for desktop, tablet, and mobile devices using Bootstrap 5.3 and custom media queries.
- **Interactivity**: Includes hover effects, AJAX form submissions (e.g., subscribe), and predictive search suggestions.
- **Authentication Integration**: Supports login/logout and user-specific actions (e.g., cart, dashboard) with Spring Security.
- **E-commerce Functionality**: Compatible with product listings, categories, cart management, and comments (assumes backend endpoints).
- **Custom Styling**: Tailored CSS with variables for consistent theming (e.g., green accents `#59ab6e`, dark footer `#212934`).

## Prerequisites

Before running the project, ensure you have the following installed:

- **Java Development Kit (JDK)**: Version 17 or later.
- **Apache Maven**: Version 3.6 or later (for dependency management).
- **Node.js and npm**: (Optional, for frontend asset management if expanded).
- **Git**: For cloning the repository.
- **IDE**: IntelliJ IDEA, Eclipse, or Visual Studio Code (recommended for Java/Spring development).
- **Database**: MySQL or H2 (configured in the backend, optional based on your setup).
- **Web Browser**: Latest version of Chrome, Firefox, or Safari for testing.

## Installation

Follow these steps to set up and run the Zay Shop project locally:

1. **Clone the Repository**
   ```bash
   git clone https://github.com/your-username/zay-shop.git
   cd zay-shop
2. Install Dependencies
Ensure Maven is configured to download dependencies.
Run: mvn clean install
This will download Bootstrap, Font Awesome, jQuery, and other webjars specified in the project.

3. Configure the Backend
If using a Spring Boot backend, update the application.properties or application.yml
Ensure the backend includes controllers for endpoints like /, /shop, /cart/add, /login, /logout, etc., as referenced in the fragments.

4.Run the Application
Start the Spring Boot application using Maven:
mvn spring-boot:run
Alternatively, use your IDE's run configuration for the main application class (e.g., ZayShopApplication.java)

5.Verify Setup
Open a web browser and navigate to http://localhost:8080.
You should see the Zay Shop homepage with the top nav, header, and footer rendered. Test navigation links and the search modal.

Usage
Navigation: Use the header to navigate between Home, About, Shop, and Contact pages.
Search: Click the search icon to open the modal and search for products (requires backend integration).
Authentication: Log in or out using the user icon or logout button (configured with Spring Security).
Footer Interaction: Click product categories, further info links, or the subscribe button (AJAX submission requires a /subscribe endpoint).
Error Handling: The design supports an error page (e.g., 500 status) with a back-to-home button.


Project Structure
src/main/resources/templates/fragments.html: Contains the reusable Thymeleaf fragments.
src/main/resources/static/css/: Custom CSS (if separated, otherwise inline in fragments.html).
src/main/java/: Backend Java code (e.g., controllers, services, repositories) if included.
src/main/resources/application.properties: Configuration file for the Spring Boot application.


Customization
Styling: Modify the CSS variables in the <style> tag of fragments.html to change colors or themes.
Endpoints: Update th:href attributes to match your backend API endpoints.
Assets: Replace placeholder images (e.g., /static/img/apple-icon.png) with your own.
Functionality: Extend JavaScript in fragments.html for additional interactivity (e.g., real search API integration).

Contributing
Fork the repository.
Create a new branch (git checkout -b feature-branch).
Make your changes and commit them (git commit -m "Add new feature").
Push to the branch (git push origin feature-branch).
Open a pull request.

Contact
For issues or questions, please open an issue on the GitHub repository or contact basharbidjere@gmail
