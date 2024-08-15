package com.example.selfnote.design

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.selfnote.R
import com.example.selfnote.ui.theme.Lobster_Font
import dataclass
import fetchNotes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun welcome(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val notes = remember { mutableStateListOf<dataclass>() }

    scope.launch {
        notes.clear()
        notes.addAll(fetchNotes())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 60.sp,
                            fontFamily = Lobster_Font,
                            fontWeight = FontWeight.Bold
                        )

//                        Image(
//                            painter = painterResource(id = R.drawable.search),
//                            contentDescription = "Search",
//                            modifier = Modifier
//                                .size(30.dp)
//                                .absoluteOffset(-20.dp, 0.dp)
//                                .clickable {
//                                    Toast
//                                        .makeText(context, "Search clicked", Toast.LENGTH_LONG)
//                                        .show()
//                                }
//                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Surface {


                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(paddingValues) // Apply the padding provided by Scaffold
                        .fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img),
                        contentDescription = "Note logo",
                        modifier = Modifier.size(150.dp)
                    )

                    Text(
                        text = "Add Your Notes",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 40.sp,
                        fontFamily = Lobster_Font,
                        fontWeight = FontWeight.Normal
                    )


                }
                Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.fillMaxSize()) {

                    FloatingActionButton(
                        onClick = { navController.navigate("addscreen") },
                        shape = RoundedCornerShape(5.dp),
                        modifier = Modifier
                            .size(70.dp)
                            .padding(bottom = 20.dp, end = 20.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_input_add),
                            contentDescription = "Add Note",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    )
}
