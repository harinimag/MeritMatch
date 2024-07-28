package com.example.meritmatch

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
import com.example.meritmatch.ui.theme.KPoints
import com.example.meritmatch.ui.theme.approvals
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
import retrofit2.http.POST
import retrofit2.http.Path

data class ApprovalData(
    val afrom: String,
    val ato: String,
    val taskname: String,
    val points: Int
)

interface RTaskUsername {
    @GET("/rtasks/{username}")
    suspend fun rtaskUsername(@Path("username") username: String): Response<List<Map<String, String>>>?
}
interface CreateApproval {
    @POST("/approve")
    fun createApproval(@Body approvalData: ApprovalData): Call<String>
}

interface RTaskDelete {
    @DELETE("/delete_rtasks/{rtask_id}")
    fun deleteRTask(@Path("rtask_id") rtaskId: Int): Call<Void>
}


class reserved : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reserved)
        val user = intent.getStringExtra("user")


        val tasks = findViewById<Button>(R.id.tasks)
        tasks.setOnClickListener {
            val intent = Intent(this,mainscreen::class.java)
            intent.putExtra("user",user)
            startActivity(intent)
        }
        val approvals1 = findViewById<Button>(R.id.approvals1)
        approvals1.setOnClickListener {
            val intent = Intent(this,approvals::class.java)
            intent.putExtra("user",user)
            startActivity(intent)
        }
        val homebutton = findViewById<Button>(R.id.homebutton)
        homebutton.setOnClickListener {
            val intent = Intent(this,front::class.java)
            startActivity(intent)
        }


        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val rtaskusername = retrofit.create(RTaskUsername::class.java)

        val retrofit1 = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val createapproval = retrofit1.create(CreateApproval::class.java)

        var rtaskdetails: List<Map<String,String>>?
        val rtasktable = findViewById<TableLayout>(R.id.rtasktable)

        val retrofit2 = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val deletertask = retrofit2.create(RTaskDelete::class.java)

        val mpoints = findViewById<TextView>(R.id.points)

        val retrofit5 = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val kpoints = retrofit5.create(KPoints::class.java)

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
                        Toast.makeText(this@reserved, "User not found", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.e("MYAPP", "Exception during API request", e)
                runOnUiThread {
                    Toast.makeText(
                        this@reserved,
                        "Exception during API request 3",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response =
                    rtaskusername.rtaskUsername(user!!) // Perform asynchronous network call
                withContext(Dispatchers.Main) {
                    if (response != null && response.isSuccessful) {
                        rtaskdetails =
                            response.body() // Assign taskdetails when response is successful
                        Log.d("MYAPP", "rtaskdetails created for $user") // Log taskdetails creation
                        val borderDrawable: Drawable? = ContextCompat.getDrawable(this@reserved, R.drawable.border)

                        if (rtaskdetails != null) {
                            // Create table row dynamically
                            for (l in rtaskdetails!!) {
                                val tableRow = TableRow(this@reserved)
                                tableRow.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this@reserved,
                                        R.color.white
                                    )
                                )
                                tableRow.background = borderDrawable


                                val textView = TextView(this@reserved)
                                val layoutparams6 = TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    1f
                                )
                                textView.background = borderDrawable
                                textView.gravity = Gravity.CENTER
                                textView.layoutParams = layoutparams6
                                textView.setTextColor(
                                    ContextCompat.getColor(
                                        this@reserved,
                                        R.color.black
                                    )
                                )
                                textView.setTextSize(25f)
                                textView.text = l["taskname"] ?: ""

                                val textView2 = TextView(this@reserved)
                                val layoutparams3 = TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    1f
                                )
                                textView2.background = borderDrawable
                                textView2.gravity = Gravity.CENTER
                                textView2.setTextColor(
                                    ContextCompat.getColor(
                                        this@reserved,
                                        R.color.black
                                    )
                                )
                                textView2.setTextSize(22f)
                                textView2.layoutParams = layoutparams3
                                textView2.text = "from "+l["taskby"]

                                val textView3 = TextView(this@reserved)
                                val layoutparams4 = TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    1f
                                )
                                textView3.background = borderDrawable
                                textView3.gravity = Gravity.CENTER
                                textView3.setTextColor(
                                    ContextCompat.getColor(
                                        this@reserved,
                                        R.color.black
                                    )
                                )
                                textView3.setTextSize(32f)
                                textView3.layoutParams = layoutparams4
                                textView3.text = l["points"]

                                val completebutton = Button(this@reserved)
                                val layoutparams5 = TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    1f
                                )
                                completebutton.setTextColor(
                                    ContextCompat.getColor(
                                        this@reserved,
                                        R.color.black
                                    )
                                )
                                completebutton.setTextSize(20f)
                                completebutton.layoutParams = layoutparams5
                                completebutton.text = "TO COMPLETE"
                                completebutton.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this@reserved,
                                        R.color.green
                                    )
                                )

                                tableRow.addView(textView2)
                                tableRow.addView(textView)
                                tableRow.addView(textView3)
                                tableRow.addView(completebutton)
                                rtasktable.addView(tableRow)

                                completebutton.setOnClickListener {
                                    val approvaldata = ApprovalData(user!!, l["taskby"]!!,textView.text.toString(), textView3.text.toString().toInt())
                                    val call = createapproval.createApproval(approvaldata)
                                    call.enqueue(object : Callback<String> {
                                        override fun onResponse(call: Call<String>, response: Response<String>) {
                                            if (response.isSuccessful) {
                                                completebutton.isEnabled = false
                                                completebutton.text = "COMPLETED"
                                                completebutton.setBackgroundColor(ContextCompat.getColor(this@reserved, R.color.red))
                                                val rtaskId = l["id"]?.toIntOrNull() // Safely convert ID
                                                if (rtaskId != null) {
                                                    try {
                                                        val deleteCall = deletertask.deleteRTask(rtaskId)
                                                        deleteCall.enqueue(object : Callback<Void> {
                                                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                                                if (response.isSuccessful) {
                                                                    Log.d("MYAPP", "RTask deleted successfully")
                                                                } else {
                                                                    Log.e("MYAPP", "Failed to delete rtask: ${response.code()}")
                                                                    Toast.makeText(this@reserved, "Error deleting reserved task", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }

                                                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                                                Log.e("MYAPP", "Exception during task deletion", t)
                                                                Toast.makeText(this@reserved, "Error deleting reserved task", Toast.LENGTH_SHORT).show()
                                                            }
                                                        })
                                                    } catch (e: Exception) {
                                                        Log.e("MYAPP", "Exception during task deletion", e)
                                                        Toast.makeText(this@reserved, "Error deleting reserved task: ${e.message}", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    Log.e("MYAPP", "Invalid task ID")
                                                    Toast.makeText(this@reserved, "Invalid task ID", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                Toast.makeText(this@reserved, "Error in receiving response 1", Toast.LENGTH_SHORT).show()
                                            }
                                        }

                                        override fun onFailure(call: Call<String>, t: Throwable) {
                                            Log.e("MYAPP", "Exception occurred", t)
                                            Toast.makeText(this@reserved, "Error in receiving response 2", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                                }
                            }
                        } else {
                            Log.d("MYAPP", "rtaskdetails is null for $user")
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
                        this@reserved,
                        "Exception during API request for $user",
                        Toast.LENGTH_SHORT
                    ).show() // Show toast message for exception
                }
            }





 catch (e: Exception) {
    Log.e("MYAPP", "Exception during API request", e) // Log exception during API request
    withContext(Dispatchers.Main) {
        Toast.makeText(this@reserved, "Exception during API request", Toast.LENGTH_SHORT).show() // Show toast message for exception
    }
}
}
}
}