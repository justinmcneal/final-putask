import com.example.puttask.data.EmailRequest
import com.example.puttask.data.EmailResponse
import com.example.puttask.data.ForgotPasswordRequest
import com.example.puttask.data.ForgotPasswordResponse
import com.example.puttask.data.LoginRequest
import com.example.puttask.data.LoginResponse
import com.example.puttask.data.OTPRequest
import com.example.puttask.data.OTPResponse
import com.example.puttask.data.RegistrationRequest
import com.example.puttask.data.RegistrationResponse
import com.example.puttask.data.ResetPasswordRequest
import com.example.puttask.data.ResetPasswordResponse
import com.example.puttask.data.UserInfo
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {
    // User Login
    @POST("api/loginPost")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse> // Changed to Response

    // User Registration
    @POST("api/registrationPost")
    suspend fun register(@Body registrationRequest: RegistrationRequest): Response<RegistrationResponse>

    @GET("api/user")
    suspend fun getUser(@Header("Authorization") token: String): Response<UserInfo> // Changed to Response

    // Forgot Password - Validate Email
    @POST("api/check-email")
    suspend fun checkEmail(@Body forgotPasswordRequest: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    // Send OTP
    @POST("api/send-otp")
    suspend fun sendOTP(@Body emailRequest: EmailRequest): Response<EmailResponse>

    // Verify the OTP
    @POST("api/verify-otp")
    suspend fun verifyOTP(@Body otpRequest: OTPRequest): Response<OTPResponse>

    @POST("api/password/reset")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ResetPasswordResponse>
}
