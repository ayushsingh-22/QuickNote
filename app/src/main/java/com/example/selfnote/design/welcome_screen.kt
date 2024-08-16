package com.example.selfnote.design


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.selfnote.R
import com.example.selfnote.ui.theme.Lobster_Font
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun welcome(navController: NavHostController) {
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            delay(2000)
            isLoading = false
        }
    }

    if (isLoading) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,

        ) {
            CircularProgressIndicator(color = Color.Red,
                strokeWidth = 4.dp,
                strokeCap = StrokeCap.Square,
                trackColor = Color.White)
        }
    } else {

        LaunchedEffect(Unit) {
            delay(500)
        }

        Scaffold(
            topBar = {
                TopAppBar(modifier = Modifier.padding(top = 30.dp),
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
                            .padding(paddingValues)
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

                    Box(contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier
                            .fillMaxSize()) {

                        FloatingActionButton(
                            onClick = { navController.navigate("addscreen") },
                            shape = RoundedCornerShape(5.dp),
                            modifier = Modifier
                                .size(70.dp)
                                .padding(bottom = 30.dp, end = 30.dp)
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
}
