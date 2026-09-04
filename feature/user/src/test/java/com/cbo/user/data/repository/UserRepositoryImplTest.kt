package com.cbo.user.data.repository

import com.cbo.core.data.mapper.UserDetailEntityMapper
import com.cbo.core.data.mapper.UserEntityMapper
import com.cbo.core.data.mapper.UserSettingsEntityMapper
import com.cbo.core.data.mapper.UserWithDetailEntityMapper
import com.cbo.core.database.dao.UserDao
import com.cbo.core.database.dao.UserDetailDao
import com.cbo.core.database.entity.UserDetailEntity
import com.cbo.core.database.entity.UserEntity
import com.cbo.core.database.entity.UserSettingsEntity
import com.cbo.core.database.entity.UserWithDetail as UserWithDetailEntity
import com.cbo.core.domain.model.UserDetail
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UserRepositoryImplTest {

    @Mock
    private lateinit var userDao: UserDao

    @Mock
    private lateinit var userDetailDao: UserDetailDao

    private lateinit var repository: UserRepositoryImpl

    private val userDetailMapper = UserDetailEntityMapper()
    private val userEntityMapper = UserEntityMapper()
    private val userSettingsMapper = UserSettingsEntityMapper()
    private val userWithDetailMapper = UserWithDetailEntityMapper(userEntityMapper, userDetailMapper, userSettingsMapper)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = UserRepositoryImpl(
            userDao = userDao,
            userDetailDao = userDetailDao,
            userWithDetailMapper = userWithDetailMapper,
            userDetailMapper = userDetailMapper
        )
    }

    @Test
    fun `getUserWithDetail should return mapped domain object when entity exists`() = runTest {
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
            id = 1,
            userId = userId,
            fullName = "Test User",
            avatarUrl = "avatar.jpg",
            bio = "Test bio",
            phoneNumber = "1234567890",
            address = "Test address",
            dateOfBirth = null,
            gender = null
        )
        val userSettingsEntity = UserSettingsEntity(
            userId = userId,
            isFirstLoginDone = true,
            isBiometricsEnabled = false,
            notesViewMode = com.cbo.core.domain.model.ViewMode.LIST,
            preferredLanguage = "tr"
        )
        val userWithDetail = UserWithDetailEntity(
            user = userEntity,
            userDetail = userDetailEntity,
            userSettingsEntity = userSettingsEntity
        )

        whenever(userDao.getUserWithDetailById(userId)).thenReturn(flowOf(userWithDetail))

        val result = repository.getUserWithDetail(userId).first()
        assertNotNull(result)
        assertEquals("testuser", result?.user?.username)
        assertEquals("Test User", result?.userDetail?.fullName)
        verify(userDao).getUserWithDetailById(userId)
    }

    @Test
    fun `getUserWithDetail should return null when entity is null`() = runTest {
        val userId = 1
        whenever(userDao.getUserWithDetailById(userId)).thenReturn(flowOf(null))

        val result = repository.getUserWithDetail(userId).first()
        assertNull(result)
        verify(userDao).getUserWithDetailById(userId)
    }

    @Test
    fun `updateUser should return success when dao succeeds`() = runTest {
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

        val result = repository.updateUser(userEntity)
        assertTrue(result.isSuccess)
        verify(userDao).update(userEntity)
    }

    @Test
    fun `upsertUserDetail should insert when detail does not exist`() = runTest {
        val userDetail = UserDetail(
            id = null,
            userId = 1,
            fullName = "Test User",
            avatarUrl = "avatar.jpg",
            bio = "Test bio",
            phoneNumber = "1234567890",
            address = "Test address",
            dateOfBirth = null,
            gender = null
        )

        whenever(userDetailDao.getUserDetailByUserId(1)).thenReturn(null)

        val result = repository.upsertUserDetail(userDetail)
        assertTrue(result.isSuccess)
        verify(userDetailDao).getUserDetailByUserId(1)
        verify(userDetailDao).insert(any())
    }

    @Test
    fun `upsertUserDetail should update when detail already exists`() = runTest {
        val existingDetail = UserDetailEntity(
            id = 1,
            userId = 1,
            fullName = "Old User",
            avatarUrl = "old.jpg",
            bio = "Old bio",
            phoneNumber = "0000000000",
            address = "Old address",
            dateOfBirth = null,
            gender = null
        )
        val userDetail = UserDetail(
            id = 1,
            userId = 1,
            fullName = "Updated User",
            avatarUrl = "new.jpg",
            bio = "New bio",
            phoneNumber = "1111111111",
            address = "New address",
            dateOfBirth = null,
            gender = null
        )

        whenever(userDetailDao.getUserDetailByUserId(1)).thenReturn(existingDetail)

        val result = repository.upsertUserDetail(userDetail)
        assertTrue(result.isSuccess)
        verify(userDetailDao).getUserDetailByUserId(1)
        verify(userDetailDao).update(any())
    }
}
