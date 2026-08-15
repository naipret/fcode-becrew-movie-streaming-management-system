# Contributing Guidelines

Thank you for contributing to the **Movie Streaming Management System**! To maintain clean code and high engineering standards, please adhere to the following workflow guidelines.

---

## 1. Branching Strategy

We follow a simplified **Gitflow** branching strategy:
- `main`: Production-ready, stable releases.
- `feature/<feature-name>`: Dedicated branches for new features (e.g., `feature/movie-crud`, `feature/undo-redo-watchlist`).
- `fix/<bug-name>`: Bug fixes (e.g., `fix/csv-delimiter-escaping`).
- `refactor/<target>`: Code refactoring without changing behavior.

---

## 2. Commit Message Guidelines (Conventional Commits)

All commit messages MUST follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>(<scope>): <short description in lowercase imperative mood>

[optional body explaining motivation and non-obvious details]

[optional footer(s)]
```

### Allowed Types:
- `feat`: A new feature (correlates with MINOR in Semantic Versioning).
- `fix`: A bug fix (correlates with PATCH in Semantic Versioning).
- `refactor`: A code change that neither fixes a bug nor adds a feature.
- `test`: Adding missing tests or correcting existing tests.
- `docs`: Documentation-only changes.
- `chore`: Changes to the build process, tooling, or helper libraries.
- `style`: Changes that do not affect the meaning of code (formatting, whitespace, etc.).
- `ci`: Changes to CI configuration files and scripts.

### Atomic Commits Rule:
- Each commit MUST be **atomic** (one single cohesive logical change).
- Never bundle unrelated tasks (e.g., a bug fix + new feature + doc change) into a single commit.

---

## 3. Code Quality & Standards

- **Java Version**: Strict Java 8 source and target compatibility.
- **Framework Constraint**: No third-party runtime frameworks (Spring, Hibernate, etc.). Pure Java Standard Library only.
- **Formatting**: Adhere to `checkstyle.xml` (Google Java Style with 4-space indentation).
- **Testing**: Write JUnit 5 unit tests for all domain models, validators, repositories, and services. Verify with `mvn clean verify`.
