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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun BottomNavigationBar(
    navController: NavController,
    items: List<BottomNavItem>,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp <= 360

    Surface(
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 12.dp,
        shadowElevation = 16.dp,
        color = MaterialTheme.colorScheme.primary.copy(0.98f),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (isSmallScreen) 12.dp else 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isSmallScreen) 60.dp else 72.dp)
                .padding(vertical = if (isSmallScreen) 4.dp else 6.dp),
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
                                painter = painterResource(id = item.iconFilled),
                                contentDescription = item.label,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(if (isSmallScreen) 24.dp else 30.dp)
                            )
                            Text(
                                text = item.label,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = if (isSmallScreen) 11.sp else 13.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = item.iconOutlined),
                                contentDescription = item.label,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(if (isSmallScreen) 20.dp else 24.dp)
                            )
                            Text(
                                text = item.label,
                                fontWeight = FontWeight.ExtraLight,
                                fontSize = if (isSmallScreen) 10.sp else 12.sp,
                                color = MaterialTheme.colorScheme.onPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
