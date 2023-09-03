package com.example.rocketreserver

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.apollographql.apollo3.api.Error
import com.apollographql.apollo3.exception.ApolloException

@Composable
private fun LaunchDetails(
    data: LaunchDetailsQuery.Data,
    navigateToLogin: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Mission patch
            AsyncImage(
                modifier = Modifier.size(160.dp, 160.dp),
                model = data?.launch?.mission?.missionPatch,
                placeholder = painterResource(id = R.drawable.ic_placeholder),
                error = painterResource(id = R.drawable.ic_placeholder),
                contentDescription = "Mission patch"
            )

            Spacer(modifier = Modifier.size(16.dp))

            Column {
                // Mission name
                Text(
                    style = MaterialTheme.typography.headlineMedium,
                    text = data?.launch?.mission?.name ?: ""
                )

                // Rocket name
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    text = data?.launch?.rocket?.name?.let { "🚀 $it" } ?: "",
                )

                // Site
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    text = data?.launch?.site ?: ""
                )
            }
        }

        // Book button
        Button(
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxWidth(),
            onClick = {
                onBookButtonClick(
                    launchId = data.launch?.id ?: "",
                    isBooked = data.launch?.isBooked == true,
                    navigateToLogin = navigateToLogin
                )
            }
        ) {
            Text(text = "Book now")
        }
    }
}

private fun onBookButtonClick(launchId: String, isBooked: Boolean, navigateToLogin: () -> Unit): Boolean {
    if (TokenRepository.getToken() == null) {
        navigateToLogin()
        return false
    }

    if (isBooked) {

    } else {

    }

    return false
}

@Composable
fun LaunchDetails(launchId: String, navigateToLogin: () -> Unit) {
    var state by remember {
        mutableStateOf<LaunchDetailsState>(LaunchDetailsState.Loading)
    }
    LaunchedEffect(Unit) {
        state = try {
            val response = apolloClient.query(LaunchDetailsQuery(launchId)).execute()
            if (response.hasErrors()) {
                LaunchDetailsState.ApplicationError(response.errors!!)
            } else {
                LaunchDetailsState.Success(response.data!!)
            }
        } catch (e: ApolloException) {
            LaunchDetailsState.ProtocolError(e)
        }
    }
    when (val s = state) {
        LaunchDetailsState.Loading -> Loading()
        is LaunchDetailsState.ProtocolError -> ErrorMessage(text = "Oh no... A protocol error happened: ${s.exception.message}")
        is LaunchDetailsState.ApplicationError -> ErrorMessage(s.errors[0].message)
        is LaunchDetailsState.Success -> LaunchDetails(s.data, navigateToLogin)
    }
}

@Composable
private fun ErrorMessage(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text)
    }
}

@Composable
private fun Loading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SmallLoading() {
    CircularProgressIndicator(
        modifier = Modifier.size(24.dp),
        color = LocalContentColor.current,
        strokeWidth = 2.dp,
    )
}

private sealed interface LaunchDetailsState {
    object Loading : LaunchDetailsState
    data class ProtocolError(val exception: ApolloException) : LaunchDetailsState
    data class ApplicationError(val errors: List<Error>) : LaunchDetailsState
    data class Success(val data: LaunchDetailsQuery.Data) : LaunchDetailsState
}

@Preview(showBackground = true)
@Composable
private fun LaunchDetailsPreview() {
    LaunchDetails(launchId = "42", navigateToLogin = {})
}
