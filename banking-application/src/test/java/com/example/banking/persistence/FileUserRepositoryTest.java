package com.example.banking.persistence;

import com.example.banking.user.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FileUserRepository Tests")
class FileUserRepositoryTest {

    private FileUserRepository repository;
    private String testDataFile;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        testDataFile = tempDir.resolve("test_banking_data.ser").toString();
        repository = new FileUserRepository(testDataFile);
    }

    @AfterEach
    void tearDown() throws IOException {
        repository.close();
    }

    @Nested
    @DisplayName("Save User Tests")
    class SaveUserTests {

        @Test
        @DisplayName("Should save new user successfully")
        void shouldSaveNewUserSuccessfully() {
            // Given
            User user = new User("newuser", "password123");

            // When
            repository.saveUser(user);

            // Then
            User retrievedUser = repository.getUserByUsername("newuser");
            assertThat(retrievedUser).isNotNull();
            assertThat(retrievedUser.getUsername()).isEqualTo("newuser");
            assertThat(retrievedUser.authenticate("password123")).isTrue();
        }

        @Test
        @DisplayName("Should overwrite existing user when saving with same username")
        void shouldOverwriteExistingUserWhenSavingWithSameUsername() {
            // Given
            User originalUser = new User("user", "oldpassword");
            User updatedUser = new User("user", "newpassword");
            repository.saveUser(originalUser);

            // When
            repository.saveUser(updatedUser);

            // Then
            User retrievedUser = repository.getUserByUsername("user");
            assertThat(retrievedUser.authenticate("newpassword")).isTrue();
            assertThat(retrievedUser.authenticate("oldpassword")).isFalse();
        }

        @Test
        @DisplayName("Should persist data to file when saving user")
        void shouldPersistDataToFileWhenSavingUser() throws IOException {
            // Given
            User user = new User("persisttest", "password");

            // When
            repository.saveUser(user);

            // Then
            File dataFile = new File(testDataFile);
            assertThat(dataFile).exists();

            // Verify by creating new repository instance
            try (FileUserRepository newRepository = new FileUserRepository(testDataFile)) {
            User retrievedUser = newRepository.getUserByUsername("persisttest");
            assertThat(retrievedUser).isNotNull();
            assertThat(retrievedUser.getUsername()).isEqualTo("persisttest");
        }
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update existing user successfully")
        void shouldUpdateExistingUserSuccessfully() {
            // Given
            User user = new User("updatetest", "password");
            repository.saveUser(user);

            // Modify user (simulate account changes)
            user.getAccount().deposit(100.0);

            // When
            repository.updateUser(user);

            // Then
            User retrievedUser = repository.getUserByUsername("updatetest");
            assertThat(retrievedUser.getAccount().getBalance()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("Should not update non-existent user")
        void shouldNotUpdateNonExistentUser() {
            // Given
            User nonExistentUser = new User("nonexistent", "password");

            // When
            repository.updateUser(nonExistentUser);

            // Then
            assertThat(repository.getUserByUsername("nonexistent")).isNull();
        }

        @Test
        @DisplayName("Should handle null user gracefully")
        void shouldHandleNullUserGracefully() {
            // When/Then - Should not throw exception
            assertThatCode(() -> repository.updateUser(null))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Get User Tests")
    class GetUserTests {

        @Test
        @DisplayName("Should retrieve existing user by username")
        void shouldRetrieveExistingUserByUsername() {
            // Given
            User user = new User("gettest", "password");
            repository.saveUser(user);

            // When
            User retrievedUser = repository.getUserByUsername("gettest");

            // Then
            assertThat(retrievedUser).isNotNull();
            assertThat(retrievedUser.getUsername()).isEqualTo("gettest");
        }

        @Test
        @DisplayName("Should return null for non-existent user")
        void shouldReturnNullForNonExistentUser() {
            // When
            User retrievedUser = repository.getUserByUsername("nonexistent");

            // Then
            assertThat(retrievedUser).isNull();
        }

        @Test
        @DisplayName("Should handle null username gracefully")
        void shouldHandleNullUsernameGracefully() {
            // When
            User retrievedUser = repository.getUserByUsername(null);

            // Then
            assertThat(retrievedUser).isNull();
        }
    }

    @Nested
    @DisplayName("Get All Users Tests")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return empty list when no users exist")
        void shouldReturnEmptyListWhenNoUsersExist() {
            // When
            List<User> users = repository.getAllUsers();

            // Then
            assertThat(users).isEmpty();
        }

        @Test
        @DisplayName("Should return all saved users")
        void shouldReturnAllSavedUsers() {
            // Given
            User user1 = new User("user1", "password1");
            User user2 = new User("user2", "password2");
            User user3 = new User("user3", "password3");

            repository.saveUser(user1);
            repository.saveUser(user2);
            repository.saveUser(user3);

            // When
            List<User> users = repository.getAllUsers();

            // Then
            assertThat(users).hasSize(3);
            assertThat(users).extracting(User::getUsername)
                    .containsExactlyInAnyOrder("user1", "user2", "user3");
        }

        @Test
        @DisplayName("Should return independent copy of users list")
        void shouldReturnIndependentCopyOfUsersList() {
            // Given
            User user = new User("copytest", "password");
            repository.saveUser(user);

            // When
            List<User> users1 = repository.getAllUsers();
            List<User> users2 = repository.getAllUsers();

            // Then
            assertThat(users1).isNotSameAs(users2);
            users1.clear();
            assertThat(users2).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete existing user successfully")
        void shouldDeleteExistingUserSuccessfully() {
            // Given
            User user = new User("deletetest", "password");
            repository.saveUser(user);

            // When
            boolean result = repository.deleteUser("deletetest");

            // Then
            assertThat(result).isTrue();
            assertThat(repository.getUserByUsername("deletetest")).isNull();
        }

        @Test
        @DisplayName("Should return false when deleting non-existent user")
        void shouldReturnFalseWhenDeletingNonExistentUser() {
            // When
            boolean result = repository.deleteUser("nonexistent");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should persist deletion to file")
        void shouldPersistDeletionToFile() throws IOException {
            // Given
            User user = new User("persistdelete", "password");
            repository.saveUser(user);

            // When
            repository.deleteUser("persistdelete");

            // Then
            try (FileUserRepository newRepository = new FileUserRepository(testDataFile)) {
            assertThat(newRepository.getUserByUsername("persistdelete")).isNull();
        }
        }
    }

    @Nested
    @DisplayName("Save All Users Tests")
    class SaveAllUsersTests {

        @Test
        @DisplayName("Should save all users to file")
        void shouldSaveAllUsersToFile() throws IOException {
            // Given
            User user1 = new User("saveall1", "password1");
            User user2 = new User("saveall2", "password2");
            repository.saveUser(user1);
            repository.saveUser(user2);

            // When
            repository.saveAllUsers();

            // Then
            try (FileUserRepository newRepository = new FileUserRepository(testDataFile)) {
            List<User> users = newRepository.getAllUsers();
            assertThat(users).hasSize(2);
            assertThat(users).extracting(User::getUsername)
                    .containsExactlyInAnyOrder("saveall1", "saveall2");
        }
        }
    }
}
