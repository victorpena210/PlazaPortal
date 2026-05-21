# PlazaPortal

PlazaPortal is a full-stack tenant management portal built with Java and Spring Boot for managing office plaza operations. The application allows tenants to manage payments and maintenance requests while giving administrators tools to oversee tenants, offices, and plaza activity through a centralized dashboard.

## Features

### Tenant Features

* Secure user registration and login
* Tenant dashboard for managing account activity
* Submit maintenance requests
* Online rent/payment processing with Stripe integration
* View payment history and request status updates

### Admin Features

* Admin dashboard with management tools
* Review and manage maintenance requests
* Monitor tenant payments
* Office and tenant management
* Role-based access control for admins and tenants

## Tech Stack

### Backend

* Java 17
* Spring Boot 4
* Spring Security
* Spring Data JPA
* Flyway Database Migrations
* MySQL
* Stripe Java SDK

### Frontend

* Thymeleaf
* HTML/CSS

### Build Tools

* Maven

## Project Structure

```bash
src/main/java/com/victorpena/plaza
├── config/          # Security and application configuration
├── controller/      # MVC controllers
├── model/           # Entity models and enums
├── repository/      # JPA repositories
├── service/         # Business logic and services
└── web/             # Form and web-related classes

src/main/resources
├── templates/       # Thymeleaf templates
├── db/              # Flyway migration scripts
└── application.properties
```

## Main Pages

* Home Page
* Login & Registration
* Tenant Dashboard
* Admin Dashboard
* Payment Portal
* Maintenance Request Form

## Security

PlazaPortal uses Spring Security for:

* Authentication and authorization
* Role-based access control
* Protected admin routes
* Secure login handling

## Payment Integration

Stripe is integrated for handling tenant payments.

Features include:

* Secure checkout sessions
* Payment tracking
* Webhook handling for payment updates

## Database

The application uses MySQL with Flyway migrations for database version control.

## Getting Started

### Prerequisites

Before running the project, make sure you have:

* Java 17+
* Maven
* MySQL Server
* Stripe API Keys

### Clone the Repository

```bash
git clone https://github.com/yourusername/plazaportal.git
cd plazaportal
```

### Configure Environment

Update your `application.properties` file with your database and Stripe credentials.

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/plaza_portal
spring.datasource.username=your_username
spring.datasource.password=your_password

stripe.secret.key=your_stripe_secret_key
```

### Run the Application

Using Maven:

```bash
./mvnw spring-boot:run
```

Or on Windows:

```bash
mvnw.cmd spring-boot:run
```

The application will start on:

```text
http://localhost:8080
```

## Future Improvements

* Email notifications for tenants
* File uploads for maintenance requests
* Tenant lease management
* Analytics dashboard
* Mobile responsive UI improvements
* Multi-property support

## Author

Developed by Victor Pena.

## License

This project is for educational and portfolio purposes.
