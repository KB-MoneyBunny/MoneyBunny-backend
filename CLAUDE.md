# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Development Commands

### Build Commands
```bash
# Clean and build the project
./gradlew clean build

# Build WAR file for deployment
./gradlew war

# Run tests
./gradlew test

# Run specific test class
./gradlew test --tests "org.scoula.config.RootConfigTest"

# Run with detailed output
./gradlew test --info
```

### Running the Application
```bash
# Using Docker
docker-compose up

# Deploy WAR to Tomcat (after building)
# Copy build/libs/*.war to Tomcat webapps directory
```

### Windows-specific Commands
```bash
# On Windows, use gradlew.bat instead of ./gradlew
gradlew.bat clean build
gradlew.bat test
gradlew.bat war
```

## High-Level Architecture

### Technology Stack
- **Framework**: Spring MVC 5.3.39 (Traditional layered architecture)
- **Java Version**: 17
- **Build Tool**: Gradle with WAR plugin
- **Database**: MySQL (AWS RDS) with HikariCP connection pooling
- **ORM**: MyBatis with XML mappers
- **Caching**: Redis (Upstash) for session and temporary data
- **Security**: Spring Security 5.8.13 with JWT authentication
- **API Documentation**: Swagger 2.9.2 (access at `/swagger-ui.html`)
- **Push Notifications**: Firebase Cloud Messaging
- **External APIs**: CODEF (financial data), Youth Policy API (government data)
- **Logging**: SLF4J with Log4j2

### Core Architecture Pattern
This is a Spring MVC application with a traditional layered architecture:
- **Controller Layer**: REST API endpoints with Swagger documentation
- **Service Layer**: Business logic implementation
- **Mapper Layer**: MyBatis-based data access layer
- **Domain/VO Layer**: Entity objects mapped to database tables
- **DTO Layer**: Data transfer objects for API communication

### Key Modules and Their Responsibilities

1. **Security Module** (`org.scoula.security`)
   - JWT-based authentication using custom filters
   - Integrates with Spring Security for authorization
   - Custom UserDetails implementation with member authorities

2. **Policy Module** (`org.scoula.policy`)
   - Manages youth policies from external government APIs
   - Complex domain model with multiple relationship tables (regions, keywords, conditions)
   - Scheduled updates via PolicyScheduler

3. **UserPolicy Module** (`org.scoula.userPolicy`)
   - Personalizes policy recommendations based on user conditions
   - Complex matching algorithm in MyBatis queries
   - Calculates policy scores for user-policy matches

4. **Codef Module** (`org.scoula.codef`)
   - Financial data integration (bank accounts, card transactions)
   - RSA/AES encryption for sensitive data
   - Token-based authentication with external CODEF API

5. **Push Module** (`org.scoula.push`)
   - Firebase-based web push notifications
   - Subscription management and notification history
   - Scheduled notification system

6. **PolicyInteraction Module** (`org.scoula.policyInteraction`)
   - Manages user interactions with policies (bookmarks, applications)
   - Tracks user engagement with policy content

### Database Architecture
- **Primary Database**: MySQL on AWS RDS
- **Caching Layer**: Redis (Upstash) for session and temporary data
- **Transaction Management**: Declarative transactions via Spring `@Transactional`
- **Data Access**: MyBatis with XML mappers in `src/main/resources/org/scoula/*/mapper/`
- **Connection Pool**: HikariCP with 10 max connections, 5 min idle

### External Service Integration Pattern
All external APIs follow a similar pattern:
1. API Client class in `external` package (e.g., `YouthPolicyApiClient`, `GptApiClient`)
2. Service class handles business logic and error handling
3. Scheduled tasks for periodic updates (where applicable) using `@Scheduled`
4. Configuration in `application-dev.properties`
5. RSA/AES encryption for sensitive data (Codef module)

### Configuration Management
- Environment-specific properties: `application-{profile}.properties`
- Active profile set in `application.properties` (currently: `dev`)
- Sensitive data (API keys, passwords) stored in environment-specific files
- Spring @Configuration classes in `config` package:
  - `RootConfig`: Database, MyBatis, transactions
  - `ServletConfig`: Web MVC configuration
  - `SecurityConfig`: Spring Security and JWT
  - `SwaggerConfig`: API documentation
  - `RedisConfig`: Cache configuration
  - `MailConfig`: Email service

### Security Considerations
- JWT tokens for stateless authentication
- AES encryption for financial data (Codef module)
- Spring Security for method-level authorization
- CORS configuration in SecurityConfig

### Common Development Patterns
1. **DTO-VO Separation**: DTOs for API requests/responses, VOs for database entities
2. **Builder Pattern**: Used extensively with Lombok @Builder
3. **Mapper XML Naming**: Matches Java mapper interface names
4. **Error Handling**: Global exception handlers in `exception` package
5. **Logging**: SLF4J with Log4j2 implementation

### Testing Approach
- Unit tests for critical components (security, configuration)
- Integration tests for database connectivity
- Test files in `src/test/java/org/scoula/`:
  - `config/RootConfigTest`: Configuration testing
  - `security/PasswordEncoderTest`: Password encoding
  - `security/util/JwtProcessorTest`: JWT token testing
- Swagger UI for API testing at `/swagger-ui.html`

### Key Scheduled Tasks
- **PolicyScheduler**: Fetches and updates youth policies from external API
- **NotificationScheduler**: Sends scheduled push notifications
- **UserVectorUpdateScheduler**: Updates user recommendation vectors
- All schedulers use `@EnableScheduling` in `RootConfig`

### RESTful API Design Guidelines

1. **Request/Response Patterns**
   - Use `@RequestParam` for simple parameters instead of DTOs when possible
   - POST/PUT/DELETE operations should return `ResponseEntity<Void>` with appropriate HTTP status codes
   - Let HTTP status codes convey success/failure (200 OK, 400 Bad Request, 404 Not Found)
   - Avoid redundant message bodies when status codes are sufficient

2. **Parameter Handling**
   - Authentication: Use `@AuthenticationPrincipal CustomUser` to get current user
   - Simple operations: Prefer `@RequestParam` over `@RequestBody` with DTOs
   - Complex data: Use DTOs only when multiple fields are required

3. **Common Response Patterns**
   java
   // Success
   return ResponseEntity.ok().build();           // 200 OK
   return ResponseEntity.created(uri).build();   // 201 Created
   
   // Client errors
   return ResponseEntity.badRequest().build();   // 400 Bad Request
   return ResponseEntity.notFound().build();     // 404 Not Found
   
   // Server errors handled by global exception handlers

4. **API Documentation**
   - Use Swagger annotations (`@ApiOperation`, `@Api`) for all endpoints
   - Access Swagger UI at `/swagger-ui.html`
   - Keep API descriptions concise but informative

## Git Commit Convention

**Commit Message Format:**
- ⚡️feat: 새로운 기능추가 내용 #이슈번호
- 🚨bug: 버그 수정 내용 #이슈번호
- 📝docs: 문서 관련 변경 #이슈번호
- 🎨design: 디자인 내용 #이슈번호
- ♻️refactor: 리펙토링 내용 #이슈번호
- 🙈test: 테스트 코드 내용 #이슈번호
- 🩹fix: 고친 내용 #이슈번호

**Rules:**
- 한 줄로 작성
- 부가 설명 없이 간결하게
- 논리적 묶음 단위로 커밋