// File: app/src/main/java/com/example/stride/ui/theme/MovementAnimationProvider.kt
package com.example.stride.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.stride.sensors.MovementMode

@Composable
fun MovementAnimation(mode: MovementMode) {
    when (mode) {
        MovementMode.WALKING -> WalkingAnimation()
        MovementMode.JOGGING -> JoggingAnimation()
        MovementMode.BICYCLE -> BicycleAnimation()
        MovementMode.CAR_SLOW -> CarSlowAnimation()
        MovementMode.CAR_FAST -> CarFastAnimation()
        MovementMode.TRAIN -> TrainAnimation()
        else -> "Stationary"
    }
}

@Composable
fun WalkingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "walking")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "walking_scale"
    )

    Image(
        painter = painterResource(id = R.drawable.ic_walking),
        contentDescription = "Walking",
        modifier = Modifier
            .size(80.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
    )
}

@Composable
fun JoggingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "jogging")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jogging_scale"
    )

    Image(
        painter = painterResource(id = R.drawable.ic_jogging),
        contentDescription = "Jogging",
        modifier = Modifier
            .size(80.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
    )
}

@Composable
fun BicycleAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "bicycle")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bicycle_rotation"
    )

    Image(
        painter = painterResource(id = R.drawable.ic_bicycle),
        contentDescription = "Bicycle",
        modifier = Modifier
            .size(80.dp)
            .graphicsLayer(rotationZ = rotation)
    )
}

@Composable
fun CarSlowAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "car_slow")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "car_slow_offset"
    )

    Image(
        painter = painterResource(id = R.drawable.ic_car),
        contentDescription = "Car Slow",
        modifier = Modifier
            .size(80.dp)
            .graphicsLayer(translationX = offsetX)
    )
}

@Composable
fun CarFastAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "car_fast")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "car_fast_offset"
    )

    Image(
        painter = painterResource(id = R.drawable.ic_car_fast),
        contentDescription = "Car Fast",
        modifier = Modifier
            .size(80.dp)
            .graphicsLayer(translationX = offsetX)
    )
}

@Composable
fun TrainAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "train")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "train_offset"
    )

    Image(
        painter = painterResource(id = R.drawable.ic_train),
        contentDescription = "Train",
        modifier = Modifier
            .size(80.dp)
            .graphicsLayer(translationX = offsetX)
    )
}