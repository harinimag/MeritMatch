package com.example.meritmatch

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class UserData(
    val username: String,
    val password: String,
    val karma: Int
)

interface UserService {
    @POST("/users")
    fun createUser(@Body userData: UserData): Call<String>
}

interface LoginCheck {
    @GET("/users/{username}")
    suspend fun getUser(@Path("username") username: String): Response<Map<String, String>>
}


class front : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_front)
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val userservice = retrofit.create(UserService::class.java)
        var userData = UserData("John Doe", "supersecret",100)

        /*val retrofit1 = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()*/

        val logincheck = retrofit.create(LoginCheck::class.java)

        val login = findViewById<Button>(R.id.login)
        login.setOnClickListener {
            val animationZoomIn = AnimationUtils.loadAnimation(this, R.anim.scale_up)
            login.startAnimation(animationZoomIn)
            val animationZoomOut = AnimationUtils.loadAnimation(this, R.anim.scale_down)
            login.startAnimation(animationZoomOut)
            val builder =
                AlertDialog.Builder(this, R.style.AlertDialogCustom).create()
            val view = LayoutInflater.from(this).inflate(R.layout.login, null)
            builder.setView(view)
            builder.setCanceledOnTouchOutside(false)
            builder.show()
            val usernamel = view.findViewById<EditText>(R.id.username)
            val passwordl = view.findViewById<EditText>(R.id.password)
            val okl = view.findViewById<Button>(R.id.ok)
            okl.setOnClickListener {
                if (usernamel.text.toString() == "" || (passwordl.text.toString() == "")) {
                    Toast.makeText(this@front, "Fill all fields", Toast.LENGTH_SHORT).show()
                }
                else{
                Log.d("MYAPP", "Button clicked!") // A clue!
                val username = usernamel.text.toString()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = logincheck.getUser(username)
                        runOnUiThread {
                            if (response.isSuccessful) {
                                val user = response.body()
                                if (user == null) {
                                    Toast.makeText(this@front, "User not found", Toast.LENGTH_SHORT)
                                        .show()
                                } else {
                                    val password = user["password"]
                                    if (password == passwordl.text.toString()) {
                                        Log.i("MYAPP", "Correct password!")
                                        val intent = Intent(this@front, mainscreen::class.java)
                                        intent.putExtra("user", username)
                                        intent.putExtra("points", user["points"]!!.toInt())
                                        startActivity(intent)
                                        builder.cancel()
                                    } else {
                                        Log.w("MYAPP", "Incorrect password")
                                        Toast.makeText(
                                            this@front,
                                            "Incorrect password",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } else {
                                Log.e(
                                    "MYAPP",
                                    "API request failed with status code ${response.code()}"
                                )
                                Toast.makeText(this@front, "User not found", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MYAPP", "Exception during API request", e)
                        runOnUiThread {
                            Toast.makeText(
                                this@front,
                                "Exception during API request 3",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            }
        }
        val signup = findViewById<Button>(R.id.signup)
        signup.setOnClickListener {
            val animationZoomIn = AnimationUtils.loadAnimation(this, R.anim.scale_up)
            signup.startAnimation(animationZoomIn)
            val animationZoomOut = AnimationUtils.loadAnimation(this, R.anim.scale_down)
            signup.startAnimation(animationZoomOut)
            val builder =
                AlertDialog.Builder(this, R.style.AlertDialogCustom).create()
            val view = LayoutInflater.from(this).inflate(R.layout.signup, null)
            builder.setView(view)
            builder.setCanceledOnTouchOutside(false)
            builder.show()
            val username = view.findViewById<EditText>(R.id.username)
            val pd1 = view.findViewById<EditText>(R.id.pd1)
            val pd2 = view.findViewById<EditText>(R.id.pd2)
            val ok = view.findViewById<Button>(R.id.ok)
            ok.setOnClickListener{
                if(username.text.toString() == "" || (pd1.text.toString() == "" ) || (pd2.text.toString() == "")){
                    Toast.makeText(this@front, "Fill all fields", Toast.LENGTH_SHORT).show()
                }
                else if(pd1.text.toString() == pd2.text.toString()){
                    userData = UserData(username.text.toString(),pd1.text.toString(),100)
                    val call = userservice.createUser(userData)
                    call.enqueue(object : Callback<String> {
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@front, "Record saved", Toast.LENGTH_SHORT).show()
                                builder.cancel()
                            } else {
                                Toast.makeText(this@front, "Username already exists", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Log.e("MYAPP", "Exception occurred", t)
                            Toast.makeText(this@front, "error in receiving response fail", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                else{
                    Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}