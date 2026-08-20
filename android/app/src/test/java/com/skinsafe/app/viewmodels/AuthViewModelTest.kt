package com.skinsafe.app.viewmodels

import com.skinsafe.app.data.api.NetworkResult
import com.skinsafe.app.data.local.TokenManager
import com.skinsafe.app.data.models.TokenResponse
import com.skinsafe.app.data.models.UserProfile
import com.skinsafe.app.data.repository.AuthRepository
import com.skinsafe.app.ui.viewmodels.AuthUiState
import com.skinsafe.app.ui.viewmodels.AuthViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val tokenManager: TokenManager = mockk(relaxed = true)

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(authRepository, tokenManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_withEmptyCredentials_setsErrorState() = runTest {
        viewModel.login("", "")
        val state = viewModel.uiState.first()
        assertTrue(state is AuthUiState.Error)
        assertEquals("Please enter your email and password.", (state as AuthUiState.Error).message)
    }

    @Test
    fun login_withValidCredentials_callsRepositoryAndSucceeds() = runTest {
        val fakeToken = TokenResponse("jwt_token_123", "bearer", 1, "Alice", "alice@example.com", "Sensitive")
        coEvery { authRepository.login("alice@example.com", "password123") } returns NetworkResult.Success(fakeToken)

        viewModel.login("alice@example.com", "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state is AuthUiState.Success)
    }

    @Test
    fun register_withMismatchedPasswords_setsErrorState() = runTest {
        viewModel.register("Bob", "bob@example.com", "secret123", "different123", "Sensitive")
        val state = viewModel.uiState.first()
        assertTrue(state is AuthUiState.Error)
        assertEquals("Passwords do not match.", (state as AuthUiState.Error).message)
    }
}
