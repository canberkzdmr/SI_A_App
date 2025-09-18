package com.cbo.user.data.repository

import com.example.core.database.dao.UserDao
import com.example.core.database.dao.UserDetailDao
import com.example.core.database.entity.UserDetailEntity
import com.example.core.database.entity.UserEntity
import com.example.core.database.entity.UserWithDetail
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UserRepositoryImplTest {

    @Mock
    private lateinit var userDao: UserDao

    @Mock
    private lateinit var userDetailDao: UserDetailDao

    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = UserRepositoryImpl(userDao, userDetailDao)
    }

    @Test
    fun `getUserWithDetail should return success when user exists`() = runTest {
        // Given
        val userId = 1
        val userEntity = UserEntity(
            id = userId,
            username = "testuser",
            passwordHash = "password".toByteArray(),
            salt = "salt".toByteArray(),
            email = "test@example.com",
            registrationDate = "2024-01-01",
            lastPasswordChangeDate = "2024-01-01",
            isActive = true
        )
        val userDetailEntity = UserDetailEntity(
            userId = userId,
            fullName = "Test User",
            avatarUrl = "avatar.jpg",
            bio = "Test bio",
            phoneNumber = "1234567890",
            address = "Test address",
            dateOfBirth = null,
            gender = null
        )
        val userWithDetail = UserWithDetail(
            user = userEntity,
            userDetail = userDetailEntity
        )
        whenever(userDao.getUserWithDetailById(userId)).thenReturn(userWithDetail)

        // When
        val result = repository.getUserWithDetail(userId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(userWithDetail, result.getOrNull())
        verify(userDao).getUserWithDetailById(userId)
    }

    @Test
    fun `getUserWithDetail should return failure when user does not exist`() = runTest {
        // Given
        val userId = 1
        whenever(userDao.getUserWithDetailById(userId)).thenReturn(null)

        // When
        val result = repository.getUserWithDetail(userId)

        // Then
        assertTrue(result.isFailure)
        assertEquals("User not found", result.exceptionOrNull()?.message)
        verify(userDao).getUserWithDetailById(userId)
    }

    @Test
    fun `getUserWithDetail should return failure when dao throws exception`() = runTest {
        // Given
        val userId = 1
        val exception = RuntimeException("Database error")
        whenever(userDao.getUserWithDetailById(userId)).thenThrow(exception)

        // When
        val result = repository.getUserWithDetail(userId)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception.message, result.exceptionOrNull()?.message)
        verify(userDao).getUserWithDetailById(userId)
    }

    @Test
    fun `updateUser should return success when update succeeds`() = runTest {
        // Given
        val userEntity = UserEntity(
            id = 1,
            username = "testuser",
            passwordHash = "password".toByteArray(),
            salt = "salt".toByteArray(),
            email = "test@example.com",
            registrationDate = "2024-01-01",
            lastPasswordChangeDate = "2024-01-01",
            isActive = true
        )
        // userDao.update returns Unit, so no need to mock the return value

        // When
        val result = repository.updateUser(userEntity)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
        verify(userDao).update(userEntity)
    }

    @Test
    fun `updateUser should return failure when dao throws exception`() = runTest {
        // Given
        val userEntity = UserEntity(
            id = 1,
            username = "testuser",
            passwordHash = "password".toByteArray(),
            salt = "salt".toByteArray(),
            email = "test@example.com",
            registrationDate = "2024-01-01",
            lastPasswordChangeDate = "2024-01-01",
            isActive = true
        )
        val exception = RuntimeException("Update failed")
        whenever(userDao.update(userEntity)).thenThrow(exception)

        // When
        val result = repository.updateUser(userEntity)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception.message, result.exceptionOrNull()?.message)
        verify(userDao).update(userEntity)
    }

    @Test
    fun `upsertUserDetail should insert when detail does not exist`() = runTest {
        // Given
        val userDetail = UserDetailEntity(
            userId = 1,
            fullName = "Test User",
            avatarUrl = "avatar.jpg",
            bio = "Test bio",
            phoneNumber = "1234567890",
            address = "Test address",
            dateOfBirth = null,
            gender = null
        )
        whenever(userDetailDao.getUserDetailByUserId(userDetail.userId)).thenReturn(null)

        // When
        val result = repository.upsertUserDetail(userDetail)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
        verify(userDetailDao).getUserDetailByUserId(userDetail.userId)
        verify(userDetailDao).insert(userDetail)
    }

    @Test
    fun `upsertUserDetail should update when detail exists`() = runTest {
        // Given
        val existingDetail = UserDetailEntity(
            userId = 1,
            fullName = "Existing User",
            avatarUrl = "old_avatar.jpg",
            bio = "Old bio",
            phoneNumber = "0987654321",
            address = "Old address",
            dateOfBirth = null,
            gender = null
        )
        val updatedDetail = UserDetailEntity(
            userId = 1,
            fullName = "Updated User",
            avatarUrl = "new_avatar.jpg",
            bio = "New bio",
            phoneNumber = "1234567890",
            address = "New address",
            dateOfBirth = null,
            gender = null
        )
        whenever(userDetailDao.getUserDetailByUserId(updatedDetail.userId)).thenReturn(existingDetail)

        // When
        val result = repository.upsertUserDetail(updatedDetail)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
        verify(userDetailDao).getUserDetailByUserId(updatedDetail.userId)
        verify(userDetailDao).update(updatedDetail)
    }

    @Test
    fun `upsertUserDetail should return failure when dao throws exception on getUserDetailByUserId`() = runTest {
        // Given
        val userDetail = UserDetailEntity(
            userId = 1,
            fullName = "Test User",
            avatarUrl = "avatar.jpg",
            bio = "Test bio",
            phoneNumber = "1234567890",
            address = "Test address",
            dateOfBirth = null,
            gender = null
        )
        val exception = RuntimeException("Database query failed")
        whenever(userDetailDao.getUserDetailByUserId(userDetail.userId)).thenThrow(exception)

        // When
        val result = repository.upsertUserDetail(userDetail)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception.message, result.exceptionOrNull()?.message)
        verify(userDetailDao).getUserDetailByUserId(userDetail.userId)
    }

    @Test
    fun `upsertUserDetail should return failure when dao throws exception on insert`() = runTest {
        // Given
        val userDetail = UserDetailEntity(
            userId = 1,
            fullName = "Test User",
            avatarUrl = "avatar.jpg",
            bio = "Test bio",
            phoneNumber = "1234567890",
            address = "Test address",
            dateOfBirth = null,
            gender = null
        )
        val exception = RuntimeException("Insert failed")
        whenever(userDetailDao.getUserDetailByUserId(userDetail.userId)).thenReturn(null)
        whenever(userDetailDao.insert(userDetail)).thenThrow(exception)

        // When
        val result = repository.upsertUserDetail(userDetail)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception.message, result.exceptionOrNull()?.message)
        verify(userDetailDao).getUserDetailByUserId(userDetail.userId)
        verify(userDetailDao).insert(userDetail)
    }

    @Test
    fun `upsertUserDetail should return failure when dao throws exception on update`() = runTest {
        // Given
        val existingDetail = UserDetailEntity(
            userId = 1,
            fullName = "Existing User",
            avatarUrl = "old_avatar.jpg",
            bio = "Old bio",
            phoneNumber = "0987654321",
            address = "Old address",
            dateOfBirth = null,
            gender = null
        )
        val updatedDetail = UserDetailEntity(
            userId = 1,
            fullName = "Updated User",
            avatarUrl = "new_avatar.jpg",
            bio = "New bio",
            phoneNumber = "1234567890",
            address = "New address",
            dateOfBirth = null,
            gender = null
        )
        val exception = RuntimeException("Update failed")
        whenever(userDetailDao.getUserDetailByUserId(updatedDetail.userId)).thenReturn(existingDetail)
        whenever(userDetailDao.update(updatedDetail)).thenThrow(exception)

        // When
        val result = repository.upsertUserDetail(updatedDetail)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception.message, result.exceptionOrNull()?.message)
        verify(userDetailDao).getUserDetailByUserId(updatedDetail.userId)
        verify(userDetailDao).update(updatedDetail)
    }
}
