package com.ippo.taskflow.mvvm.view.main_view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ippo.taskflow.R

@Composable
fun TaskFlowBottomNavBar(
    onHomeClick: () -> Unit,
    onMainClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFACFFC1))
                .padding(horizontal = 40.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onHomeClick) {
                Image(
                    painter = painterResource(R.drawable.ic_home),
                    contentDescription = "Home",
                    modifier = Modifier.size(50.dp)
                )
            }

            IconButton(onClick = onMainClick) {
                Image(
                    painter = painterResource(R.drawable.ic_taskflow),
                    contentDescription = "TaskFlow",
                    modifier = Modifier.size(50.dp)
                )
            }

            IconButton(onClick = onProfileClick) {
                Image(
                    painter = painterResource(R.drawable.ic_profile),
                    contentDescription = "Profile",
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}