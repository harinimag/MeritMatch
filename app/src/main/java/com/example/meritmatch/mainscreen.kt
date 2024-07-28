package com.example.meritmatch

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.Toast
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


data class TaskData(
    val taskname: String,
    val content: String,
    val kpoints: Int,
    val username: String
)

data class RTaskData(
    val taskby: String,
    val reservedby: String,
    val taskname: String,
    val points: Int
)

interface CreateTask {
    @POST("/post_tasks")
    fun createTask(@Body taskData: TaskData): Call<String>
}

interface CreateRTask {
    @POST("/post_rtasks")
    fun createRTask(@Body rtaskData: RTaskData): Call<String>
}

interface GetUsername {
    @GET("/get_users")
    suspend fun getUsername(): Response<List<String>>
}

interface TaskUsername {
    @GET("/tasks/{username}")
    suspend fun taskUsername(@Path("username") username: String): Response<List<Map<String,String>>>?
}

interface TaskDelete {
    @DELETE("/delete_tasks/{task_id}")
    fun deleteTask(@Path("task_id") taskId: Int): Call<Void>
}

interface KPoints {
    @GET("/users/{username}")
    suspend fun kPoints(@Path("username") username: String): Response<Map<String, String>>
}

class mainscreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mainscreen)
        val user = intent.getStringExtra("user")
        val userpoints = intent.getIntExtra("points",100)

        val homebutton = findViewById<Button>(R.id.homebutton)
        homebutton.setOnClickListener {
            val intent = Intent(this,front::class.java)
            startActivity(intent)
        }
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val createt = retrofit.create(CreateTask::class.java)
        var taskdata = TaskData("do", "supersecret",100,"John Doe")
        var rtaskdata = RTaskData("string","string2","do",100)

        val retrofit1 = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val usernameget = retrofit1.create(GetUsername::class.java)

        val retrofit2 = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val taskusername = retrofit2.create(TaskUsername::class.java)

        val retrofit3 = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val creatertask = retrofit3.create(CreateRTask::class.java)

        val retrofit4 = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val deletetask = retrofit4.create(TaskDelete::class.java)


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
                        Toast.makeText(this@mainscreen, "User not found", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.e("MYAPP", "Exception during API request", e)
                runOnUiThread {
                    Toast.makeText(
                        this@mainscreen,
                        "Exception during API request 3",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        val rtasks = findViewById<Button>(R.id.rtasks)
        rtasks.setOnClickListener {
            val intent = Intent(this,reserved::class.java)
            intent.putExtra("user",user)
            startActivity(intent)
        }
        val approvals1 = findViewById<Button>(R.id.approvals1)
        approvals1.setOnClickListener {
            val intent = Intent(this,approvals::class.java)
            Log.d("MYAPP","what")
            intent.putExtra("user",user)
            startActivity(intent)
        }
        val addtask = findViewById<ImageButton>(R.id.addtask)
        addtask.setOnClickListener {
            val inflater = LayoutInflater.from(this)
            val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom).create()
            val view = inflater.inflate(R.layout.createtask, null)
            builder.setView(view)
            builder.setCanceledOnTouchOutside(false)
            builder.show()
            val okb = view.findViewById<Button>(R.id.okb)
            val titlet = view.findViewById<EditText>(R.id.titlet)
            val descript = view.findViewById<EditText>(R.id.descript)
            val kpt = view.findViewById<EditText>(R.id.kpt)
            okb.setOnClickListener {
                if (titlet.text.toString() == "" || descript.text.toString() == "" || kpt.text.toString() == "") {
                    Toast.makeText(this@mainscreen, "Fill all fields", Toast.LENGTH_SHORT).show()
                }
                else{
                taskdata = TaskData(
                    titlet.text.toString(),
                    descript.text.toString(),
                    kpt.text.toString().toInt(),
                    user!!
                )
                val call = createt.createTask(taskdata)
                call.enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@mainscreen, "Task created", Toast.LENGTH_SHORT)
                                .show()
                            builder.cancel()
                        } else {
                            Toast.makeText(
                                this@mainscreen,
                                "error in receiving response",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Log.e("MYAPP", "Exception occurred", t)
                        Toast.makeText(
                            this@mainscreen,
                            "error in receiving response",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
            }

        }
        val tasktable = findViewById<TableLayout>(R.id.tasktable)
        Log.d("MYAPP", "table found!") // A clue!

        var usernames:List<String>? = listOf()
        var i:String
        var taskdetails: List<Map<String,String>>?
        val rlist = mutableListOf<String?>()



        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = usernameget.getUsername() // Perform asynchronous network call
                withContext(Dispatchers.Main) {
                    val usernames = response.body() // Assign usernames when response is received
                    Log.d("MYAPP", "usernames acquired!") // Log success message

                    // Check if usernames list is not null or empty
                    if (usernames.isNullOrEmpty()) {
                        Log.d("MYAPP", "Usernames list is empty or null!") // Handle empty or null case
                    } else {
                        Log.d("MYAPP", "Processing ${usernames.size} usernames") // Log number of usernames

                        // Iterate through each username
                        for (i in usernames) {
                            Log.d("MYAPP", "Processing username: $i")

                            // Check if username is not equal to user (assuming user is defined somewhere)
                            if (i != user) {
                                try {
                                    val response1 = taskusername.taskUsername(i) // Perform asynchronous task details request
                                    Log.d("MYAPP", "taskUsername function called for $i") // Log task details request

                                    // Update UI on main thread
                                    withContext(Dispatchers.Main) {
                                        if (response1 != null && response1.isSuccessful) {
                                            taskdetails = response1.body() // Assign taskdetails when response is successful
                                            Log.d("MYAPP", "taskdetails created for $i") // Log taskdetails creation

                                            // Ensure taskdetails is not null
                                            if (taskdetails != null) {
                                                // Create table row dynamically
                                                for (l in taskdetails!!) {
                                                    Log.d("MYAPP", "Creating row for task: ${l["taskname"]}")
                                                    val borderDrawable: Drawable? = ContextCompat.getDrawable(this@mainscreen, R.drawable.border)

                                                    val tableRow = TableRow(this@mainscreen)
                                                    tableRow.setBackgroundColor(ContextCompat.getColor(this@mainscreen, R.color.white))
                                                    tableRow.background = borderDrawable


                                                    val textView = TextView(this@mainscreen)
                                                    val layoutparams6 = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                                                    textView.gravity = Gravity.CENTER
                                                    textView.setTextColor(ContextCompat.getColor(this@mainscreen, R.color.black))
                                                    textView.setTextSize(25f)
                                                    textView.layoutParams = layoutparams6
                                                    textView.text = l["taskname"] ?: ""
                                                    textView.background = borderDrawable


                                                    val textView2 = TextView(this@mainscreen)
                                                    val layoutparams3 = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 2f)
                                                    textView2.gravity = Gravity.CENTER
                                                    textView2.setTextColor(ContextCompat.getColor(this@mainscreen, R.color.black))
                                                    textView2.setTextSize(22f)
                                                    textView2.layoutParams = layoutparams3
                                                    textView2.text = l["content"]
                                                    textView2.background = borderDrawable


                                                    val textView3 = TextView(this@mainscreen)
                                                    val layoutparams4 = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                                                    textView3.gravity = Gravity.CENTER
                                                    textView3.setTextColor(ContextCompat.getColor(this@mainscreen, R.color.black))
                                                    textView3.setTextSize(32f)
                                                    textView3.layoutParams = layoutparams4
                                                    textView3.text = l["kpoints"]
                                                    textView3.background = borderDrawable


                                                    val reservebutton = Button(this@mainscreen)
                                                    val layoutparams5 = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                                                    reservebutton.setTextColor(ContextCompat.getColor(this@mainscreen, R.color.black))
                                                    reservebutton.setTextSize(20f)
                                                    reservebutton.layoutParams = layoutparams5
                                                    reservebutton.text = "RESERVE"
                                                    reservebutton.setBackgroundColor(ContextCompat.getColor(this@mainscreen, R.color.green))


                                                    tableRow.addView(textView)
                                                    tableRow.addView(textView2)
                                                    tableRow.addView(textView3)
                                                    tableRow.addView(reservebutton)
                                                    tasktable.addView(tableRow)

                                                    reservebutton.setOnClickListener {
                                                        val rtaskdata = RTaskData(i, user!!, textView.text.toString(), textView3.text.toString().toInt())
                                                        val call = creatertask.createRTask(rtaskdata)
                                                        call.enqueue(object : Callback<String> {
                                                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                                                if (response.isSuccessful) {
                                                                    reservebutton.isEnabled = false
                                                                    reservebutton.text = "RESERVED"
                                                                    reservebutton.setBackgroundColor(ContextCompat.getColor(this@mainscreen, R.color.red))
                                                                    val taskId = l["id"]?.toIntOrNull() // Safely convert ID
                                                                    if (taskId != null) {
                                                                        try {
                                                                            val deleteCall = deletetask.deleteTask(taskId)
                                                                            deleteCall.enqueue(object : Callback<Void> {
                                                                                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                                                                    if (response.isSuccessful) {
                                                                                        Log.d("MYAPP", "Task deleted successfully")
                                                                                    } else {
                                                                                        Log.e("MYAPP", "Failed to delete task: ${response.code()}")
                                                                                        Toast.makeText(this@mainscreen, "Error deleting task", Toast.LENGTH_SHORT).show()
                                                                                    }
                                                                                }

                                                                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                                                                    Log.e("MYAPP", "Exception during task deletion", t)
                                                                                    Toast.makeText(this@mainscreen, "Error deleting task", Toast.LENGTH_SHORT).show()
                                                                                }
                                                                            })
                                                                        } catch (e: Exception) {
                                                                            Log.e("MYAPP", "Exception during task deletion", e)
                                                                            Toast.makeText(this@mainscreen, "Error deleting task: ${e.message}", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                    } else {
                                                                        Log.e("MYAPP", "Invalid task ID")
                                                                        Toast.makeText(this@mainscreen, "Invalid task ID", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                } else {
                                                                    Toast.makeText(this@mainscreen, "Error in receiving response", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }

                                                            override fun onFailure(call: Call<String>, t: Throwable) {
                                                                Log.e("MYAPP", "Exception occurred", t)
                                                                Toast.makeText(this@mainscreen, "Error in receiving response", Toast.LENGTH_SHORT).show()
                                                            }
                                                        })
                                                    }

                                                }
                                            } else {
                                                Log.d("MYAPP", "taskdetails is null for $i")
                                            }
                                        } else {
                                            Log.e("MYAPP", "API request failed with status code ${response1?.code()}") // Log API failure
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("MYAPP", "Exception during API request for $i", e) // Log exception during API request
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(this@mainscreen, "Exception during API request for $i", Toast.LENGTH_SHORT).show() // Show toast message for exception
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MYAPP", "Exception during API request", e) // Log exception during API request
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@mainscreen, "Exception during API request", Toast.LENGTH_SHORT).show() // Show toast message for exception
                }
            }
        }
    }
}