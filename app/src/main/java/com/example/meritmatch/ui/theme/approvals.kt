package com.example.meritmatch.ui.theme

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.meritmatch.LoginCheck
import com.example.meritmatch.R
import com.example.meritmatch.RTaskDelete
import com.example.meritmatch.RTaskUsername
import com.example.meritmatch.front
import com.example.meritmatch.mainscreen
import com.example.meritmatch.reserved
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

data class PointsUpdate(val amount: Int)
data class UserData(
    val username: String,
    val password: String,
    val karma: Int
)

interface IncreasePointsService {
    @PUT("/increasep_users/{username}/increase_points")
    fun increasePoints(@Path("username") username: String, @Body pointsUpdate: PointsUpdate): Call<Void>
}

interface DecreasePointsService {
    @PUT("/decreasep_users/{username}/decrease_points")
    fun decreasePoints(@Path("username") username: String, @Body pointsUpdate: PointsUpdate): Call<Void>
}

interface ApprovalUsername {
    @GET("/approval/{username}")
    suspend fun approvalUsername(@Path("username") username: String): Response<List<Map<String, String>>>?
}


interface ApprovalDelete {
    @DELETE("/delete_approval/{approval_id}")
    fun approvalDelete(@Path("approval_id") approvalId: Int): Call<Void>
}

interface KPoints {
    @GET("/users/{username}")
    suspend fun kPoints(@Path("username") username: String): Response<Map<String, String>>
}

class approvals : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_approvals)
        val user = intent.getStringExtra("user")

        val mpoints = findViewById<TextView>(R.id.points)

        val homebutton = findViewById<Button>(R.id.homebutton)
        homebutton.setOnClickListener {
            val intent = Intent(this, front::class.java)
            startActivity(intent)
        }

        val retrofit1 = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val kpoints = retrofit1.create(KPoints::class.java)

        val retrofit3 = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val increasepoints = retrofit3.create(IncreasePointsService::class.java)

        val retrofit5 = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val decreasepoints = retrofit5.create(DecreasePointsService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = kpoints.kPoints(user!!)
                runOnUiThread {
                    if (response.isSuccessful) {
                        val user = response.body()
                        if (user != null) {
                            mpoints.gravity = Gravity.CENTER
                            mpoints.textSize = 30f
                            mpoints.text = user["points"]
                        }
                    } else {
                        Log.e(
                            "MYAPP",
                            "API request failed with status code ${response.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("MYAPP", "Exception during API request", e)
                runOnUiThread {
                    Toast.makeText(
                        this@approvals,
                        "Exception during API request 3",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val approvalusername = retrofit.create(ApprovalUsername::class.java)

        val retrofit2 = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val deleteapproval = retrofit2.create(ApprovalDelete::class.java)

        var approvaldetails: List<Map<String,String>>?
        val approvaltable = findViewById<TableLayout>(R.id.approvaltable)

        val tasks = findViewById<Button>(R.id.tasks)
        tasks.setOnClickListener {
            val intent = Intent(this, mainscreen::class.java)
            intent.putExtra("user",user)
            startActivity(intent)
        }
        val rtasks = findViewById<Button>(R.id.rtasks)
        rtasks.setOnClickListener {
            val intent = Intent(this, reserved::class.java)
            intent.putExtra("user",user)
            startActivity(intent)
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response =
                    approvalusername.approvalUsername(user!!) // Perform asynchronous network call
                withContext(Dispatchers.Main) {
                    if (response != null && response.isSuccessful) {
                        approvaldetails =
                            response.body() // Assign taskdetails when response is successful
                        Log.d("MYAPP", "rtaskdetails created for $user") // Log taskdetails creation

                        if (approvaldetails != null) {
                            // Create table row dynamically
                            for (l in approvaldetails!!) {
                                val borderDrawable: Drawable? = ContextCompat.getDrawable(this@approvals, R.drawable.border)

                                val tableRow = TableRow(this@approvals)
                                tableRow.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this@approvals,
                                        R.color.white
                                    )
                                )
                                tableRow.background = borderDrawable


                                val textView = TextView(this@approvals)
                                val layoutparams6 = TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    1f
                                )
                                textView.background = borderDrawable

                                textView.layoutParams = layoutparams6
                                textView.gravity = Gravity.CENTER
                                textView.setTextColor(
                                    ContextCompat.getColor(
                                        this@approvals,
                                        R.color.black
                                    )
                                )
                                textView.setTextSize(25f)
                                textView.text = "by " + l["afrom"] ?: ""

                                val textView2 = TextView(this@approvals)
                                val layoutparams3 = TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    1f
                                )
                                textView2.background = borderDrawable
                                textView2.gravity = Gravity.CENTER
                                textView2.setTextColor(
                                    ContextCompat.getColor(
                                        this@approvals,
                                        R.color.black
                                    )
                                )
                                textView2.setTextSize(22f)
                                textView2.layoutParams = layoutparams3
                                textView2.text = l["taskname"]

                                val textView3 = TextView(this@approvals)
                                val layoutparams4 = TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    1f
                                )
                                textView3.background = borderDrawable
                                textView3.gravity = Gravity.CENTER
                                textView3.setTextColor(
                                    ContextCompat.getColor(
                                        this@approvals,
                                        R.color.black
                                    )
                                )
                                textView3.setTextSize(32f)
                                textView3.layoutParams = layoutparams4
                                textView3.text = l["points"]

                                val approveb = Button(this@approvals)
                                val layoutparams5 = TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    1f
                                )
                                approveb.setTextColor(
                                    ContextCompat.getColor(
                                        this@approvals,
                                        R.color.black
                                    )
                                )
                                approveb.setTextSize(20f)
                                approveb.layoutParams = layoutparams5
                                approveb.text = "APPROVE"
                                approveb.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this@approvals,
                                        R.color.green
                                    )
                                )

                                tableRow.addView(textView)
                                tableRow.addView(textView2)
                                tableRow.addView(textView3)
                                tableRow.addView(approveb)
                                approvaltable.addView(tableRow)

                                approveb.setOnClickListener {
                                    // Create PointsUpdate object
                                    val pointsUpdate = PointsUpdate(amount = l["points"]!!.toInt())

                                    // Log button click
                                    Log.d("MYAPP", "Approve button clicked")

                                    // Increase points
                                    val increaseCall = increasepoints.increasePoints(username = l["afrom"]!!, pointsUpdate)
                                    increaseCall.enqueue(object : Callback<Void> {
                                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                            if (response.isSuccessful) {
                                                Log.d("MYAPP", "User points increased successfully")
                                                // Decrease points
                                                val decreaseCall = decreasepoints.decreasePoints(username = user!!, pointsUpdate)
                                                decreaseCall.enqueue(object : Callback<Void> {
                                                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                                        if (response.isSuccessful) {
                                                            Log.d("MYAPP", "User points decreased successfully")
                                                            // Delete approval
                                                            val approveId = l["id"]?.toIntOrNull() // Safely convert ID
                                                            if (approveId != null) {
                                                                val deleteCall = deleteapproval.approvalDelete(approveId)
                                                                deleteCall.enqueue(object : Callback<Void> {
                                                                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                                                        if (response.isSuccessful) {
                                                                            Log.d("MYAPP", "Approval deleted successfully")
                                                                        } else {
                                                                            Log.e("MYAPP", "Failed to delete approval: ${response.code()}")
                                                                            Toast.makeText(this@approvals, "Error deleting approval", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                    }

                                                                    override fun onFailure(call: Call<Void>, t: Throwable) {
                                                                        Log.e("MYAPP", "Exception during approval deletion", t)
                                                                        Toast.makeText(this@approvals, "Error deleting approval", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                })
                                                            } else {
                                                                Log.e("MYAPP", "Invalid approval ID")
                                                                Toast.makeText(this@approvals, "Invalid approval ID", Toast.LENGTH_SHORT).show()
                                                            }
                                                        } else {
                                                            Log.e("MYAPP", "Failed to decrease user points: ${response.code()}")
                                                            Toast.makeText(this@approvals, "Error decreasing user points", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }

                                                    override fun onFailure(call: Call<Void>, t: Throwable) {
                                                        Log.e("MYAPP", "Exception occurred", t)
                                                        Toast.makeText(this@approvals, "Error decreasing user points", Toast.LENGTH_SHORT).show()
                                                    }
                                                })
                                            } else {
                                                Log.e("MYAPP", "Failed to increase user points: ${response.code()}")
                                                Toast.makeText(this@approvals, "Error increasing user points", Toast.LENGTH_SHORT).show()
                                            }
                                        }

                                        override fun onFailure(call: Call<Void>, t: Throwable) {
                                            Log.e("MYAPP", "Exception occurred", t)
                                            Toast.makeText(this@approvals, "Error increasing user points", Toast.LENGTH_SHORT).show()
                                        }
                                    })

                                    // Disable the button to prevent multiple clicks
                                    approveb.isEnabled = false
                                    approveb.text = "APPROVED"
                                    approveb.setBackgroundColor(ContextCompat.getColor(this@approvals, R.color.red))
                                }


                            }
                        } else {
                            Log.d("MYAPP", "approvaldetails is null for $user")
                        }
                    } else {
                        Log.e(
                            "MYAPP",
                            "API request failed with status code ${response?.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    "MYAPP",
                    "Exception during API request for $user",
                    e
                ) // Log exception during API request
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@approvals,
                        "Exception during API request for $user",
                        Toast.LENGTH_SHORT
                    ).show() // Show toast message for exception
                }
            }





            catch (e: Exception) {
                Log.e("MYAPP", "Exception during API request", e) // Log exception during API request
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@approvals, "Exception during API request", Toast.LENGTH_SHORT).show() // Show toast message for exception
                }
            }
        }
    }
}