package com.jotadev.aiapaec.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(
    navController: NavController,
    items: List<BottomNavItem>,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp)
                        .clickable(
                            indication = null, // ❌ sin ripple ni efecto visual
                            interactionSource = MutableInteractionSource()
                        ) {
                            if (!selected) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (selected) {
                            Icon(
                                imageVector = item.iconFilled,
                                contentDescription = item.label,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = item.label,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1
                            )
                        } else {
                            Icon(
                                imageVector = item.iconOutlined,
                                contentDescription = item.label,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}